package io.github.linwancen.plugin.show.tree.find;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiElement;
import com.intellij.usages.UsageGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Reads the {@link PsiElement} a Find view group node exposes to actions
 * (a PsiPackage for "group by package", a PsiMethod for "group by method", a PsiFile for
 * "group by file", ...).
 * <p>
 * The group classes are private and expose no getter, so the same data method the platform
 * calls for actions is invoked directly, with a capturing DataSink proxy.
 * <p>
 * Everything is resolved by name because the API changed between IDE versions:
 * <ul>
 *   <li>2020.x: {@code TypeSafeDataProvider.calcData(DataKey, DataSink)} then
 *       {@code sink.put(key, value)};</li>
 *   <li>newer: {@code UiDataProvider.uiDataSnapshot(DataSink)} then
 *       {@code sink.lazy(key, () -> value)} - TypeSafeDataProvider and DataSink.put are gone,
 *       so neither may be referenced at compile time.</li>
 * </ul>
 * Cost per call: a usage group guard, a hash lookup, one proxy instance (the JDK caches the
 * proxy class itself) and a Method.invoke - the method scan runs only on the first call for
 * each group class. All of that is negligible against resolving a doc comment, and the caller
 * (RenderDocCache) only reaches it on a cache miss, a few times per second at most.
 * <p>
 * Threading: called from the pooled read action of RenderDocCache, so PSI reads are allowed.
 */
public final class FindGroupElementUtils {

    /**
     * Resolved through a helper method instead of a {@code static {}} block: an initializer
     * that throws would poison this class for the whole IDE session
     * (ExceptionInInitializerError, then NoClassDefFoundError on every later call).
     * null simply means "this IDE has no DataSink", every call returns null then.
     */
    @Nullable
    private static final Class<?> DATA_SINK = findDataSink();

    /**
     * Marker for "this group class has no data method": a map cannot store null, and without
     * a marker every paint of such a node would repeat the whole method scan.
     */
    @NotNull
    private static final Object NOT_AVAILABLE = new Object();

    /**
     * Group class -&gt; data method (a {@link Method}) or {@link #NOT_AVAILABLE}.
     * Groups come from a handful of classes, so the scan is done once per class and the
     * per render cost drops to a hash lookup.
     */
    @NotNull
    private static final ConcurrentMap<Class<?>, Object> DATA_METHODS = new ConcurrentHashMap<>();

    /**
     * Hard cap: group classes of unloadable plugins are pinned by these keys, so a full map
     * is dropped rather than grown. Losing entries only means one rescan later.
     */
    private static final int MAX_DATA_METHODS = 64;

    private FindGroupElementUtils() {
    }

    /**
     * @param descriptor the user object of a tree node (a UsageGroup for group rows)
     * @return the element the group would give to actions, or null: not a group, group without
     * data, group already invalid, or an IDE whose API this code does not know
     */
    @Nullable
    public static PsiElement of(@Nullable Object descriptor) {
        try {
            // leaf usage rows and other user objects: no reflection at all
            if (!(descriptor instanceof UsageGroup)) {
                return null;
            }
            @Nullable Class<?> sinkClass = DATA_SINK;
            @Nullable Method method = dataMethod(descriptor.getClass());
            if (sinkClass == null || method == null) {
                return null;
            }
            @NotNull PsiElement[] captured = new PsiElement[1];
            @NotNull Object sink = Proxy.newProxyInstance(sinkClass.getClassLoader(),
                    new Class<?>[]{sinkClass}, (proxy, invoked, args) -> {
                        @NotNull String name = invoked.getName();
                        if (invoked.getDeclaringClass() == Object.class) {
                            if ("equals".equals(name)) {
                                return args != null && args.length == 1 && proxy == args[0];
                            }
                            return "hashCode".equals(name) ? System.identityHashCode(proxy) : "DataSink";
                        }
                        // put(key, value) / set(key, value) / lazy(key, () -> value)
                        if (args != null && args.length > 1 && CommonDataKeys.PSI_ELEMENT == args[0]) {
                            captured[0] = psiElementOf(args[1]);
                        }
                        return null;
                    });
            if (!Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
                try {
                    method.setAccessible(true);
                } catch (Throwable ignore) {
                    // public interface methods do not need it, and some IDEs forbid it
                }
            }
            @NotNull Class<?>[] params = method.getParameterTypes();
            method.invoke(descriptor, params.length == 2
                    ? new Object[]{CommonDataKeys.PSI_ELEMENT, sink}
                    : new Object[]{sink});
            return captured[0];
        } catch (Throwable ignore) {
            // removed API, restricted reflection, broken or already invalid group: no element,
            // the caller falls back to the file found below the group node
            return null;
        }
    }

    /**
     * calcData(DataKey, DataSink) or uiDataSnapshot(DataSink), preferring the declaration of a
     * public interface so that Method.invoke works without setAccessible even when the group
     * class itself is private. Cached per class, including the "none" result.
     */
    @Nullable
    private static Method dataMethod(@NotNull Class<?> clazz) {
        @Nullable Object cached = DATA_METHODS.get(clazz);
        if (cached == null) {
            if (DATA_METHODS.size() >= MAX_DATA_METHODS) {
                DATA_METHODS.clear();
            }
            cached = DATA_METHODS.computeIfAbsent(clazz, FindGroupElementUtils::findDataMethod);
        }
        return cached == NOT_AVAILABLE ? null : (Method) cached;
    }

    @NotNull
    private static Object findDataMethod(@NotNull Class<?> clazz) {
        @NotNull Set<Class<?>> types = new HashSet<>();
        @NotNull ArrayDeque<Class<?>> queue = new ArrayDeque<>();
        queue.add(clazz);
        while (!queue.isEmpty()) {
            @NotNull Class<?> type = queue.removeFirst();
            if (!types.add(type)) {
                continue;
            }
            for (@NotNull Class<?> itf : type.getInterfaces()) {
                queue.add(itf);
            }
            @Nullable Class<?> superClass = type.getSuperclass();
            if (superClass != null) {
                queue.add(superClass);
            }
        }
        @Nullable Method hidden = null;
        for (@NotNull Class<?> type : types) {
            for (@NotNull Method method : type.getMethods()) {
                if (!isGroupDataMethod(method)) {
                    continue;
                }
                if (Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
                    return method;
                }
                if (hidden == null) {
                    hidden = method;
                }
            }
        }
        return hidden == null ? NOT_AVAILABLE : hidden;
    }

    private static boolean isGroupDataMethod(@NotNull Method method) {
        @NotNull Class<?>[] params = method.getParameterTypes();
        if (params.length == 2) {
            return "calcData".equals(method.getName())
                    && params[0].getName().endsWith("DataKey")
                    && "com.intellij.openapi.actionSystem.DataSink".equals(params[1].getName());
        }
        return params.length == 1
                && "uiDataSnapshot".equals(method.getName())
                && "com.intellij.openapi.actionSystem.DataSink".equals(params[0].getName());
    }

    /**
     * sink.lazy(key, ...) passes a supplier instead of the value itself, and older IDEs pass
     * the value directly - both end up here.
     */
    @Nullable
    private static PsiElement psiElementOf(@Nullable Object value) {
        if (value instanceof PsiElement) {
            return (PsiElement) value;
        }
        if (value == null) {
            return null;
        }
        for (@NotNull Class<?> itf : value.getClass().getInterfaces()) {
            @NotNull String name = itf.getName();
            @Nullable String methodName;
            if ("kotlin.jvm.functions.Function0".equals(name)) {
                methodName = "invoke";
            } else if ("java.util.function.Supplier".equals(name)) {
                methodName = "get";
            } else {
                continue;
            }
            try {
                @Nullable Object element = itf.getMethod(methodName).invoke(value);
                return element instanceof PsiElement ? (PsiElement) element : null;
            } catch (Throwable ignore) {
                return null;
            }
        }
        return null;
    }

    @Nullable
    private static Class<?> findDataSink() {
        try {
            return Class.forName("com.intellij.openapi.actionSystem.DataSink");
        } catch (Throwable ignore) {
            // never throw from a static initializer: it would break this class for the session
            return null;
        }
    }
}
