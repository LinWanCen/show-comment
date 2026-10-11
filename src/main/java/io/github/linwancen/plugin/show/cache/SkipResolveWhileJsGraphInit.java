package io.github.linwancen.plugin.show.cache;

import com.intellij.lang.Language;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Skip the resolve while the JS/TS config graph is being initialized, and retry after it is done.
 * <p>
 * There is no public API to ask whether that graph is already built, and asking would trigger the
 * build: JSConfigGraphCache.ensureGraphInitialized blocks in runBlockingCancellable right inside
 * the read action, which is what froze the EDT. So the graph state is detected by consequence:
 * a resolve in {@code BaseLangDoc.refDoc} took far longer than a normal resolve, i.e. it was stuck
 * in the graph init. Then skip the JS/TS resolves for a while instead of queueing the same wait
 * again. The graph build itself is not restarted by the skip, so once it completes the resolves
 * become fast again and the skip expires on its own.
 */
public class SkipResolveWhileJsGraphInit {

    private SkipResolveWhileJsGraphInit() {}

    /**
     * A normal resolve is a few milliseconds. Anything near this threshold means the resolve was
     * blocked waiting for the JS/TS config graph build, not doing real work.
     */
    private static final long SLOW_RESOLVE_MS = 1_000L;

    /**
     * How long JS/TS resolves are skipped after one was slow. Long enough to cover a typical
     * config graph build, short enough to not hide the doc for long if the slowness had
     * another cause.
     */
    private static final long SKIP_MS = 30_000L;

    private static final Key<Long> SKIP_UNTIL_KEY =
            Key.create(SkipResolveWhileJsGraphInit.class.getName() + "-SKIP-UNTIL");

    /**
     * Only a JS/TS resolve can block on the config graph build, so only those are skipped:
     * a slow resolve elsewhere must not hide the doc of other languages.
     */
    private static boolean jsFamily(@NotNull PsiElement ref) {
        // TypeScript getBaseLanguage() is JavaScript, TS/TSX land on "JavaScript" here too
        @Nullable Language language = ref.getLanguage();
        while (language != null) {
            if (language.getID().startsWith("JavaScript")) {
                return true;
            }
            language = language.getBaseLanguage();
        }
        return false;
    }

    /**
     * Called from {@code BaseLangDoc.refDoc} around every resolve, both when it returned and when
     * it threw (a cancelled resolve throws instead of returning). If it took far longer than a
     * normal resolve, treat it as the config graph init and stop queueing the same wait.
     */
    public static void markIfSlow(@NotNull PsiElement ref, long startNano) {
        if ((System.nanoTime() - startNano) / 1_000_000 <= SLOW_RESOLVE_MS) {
            return;
        }
        if (!ref.isValid() || !jsFamily(ref)) {
            return;
        }
        ref.getProject().putUserData(SKIP_UNTIL_KEY, System.currentTimeMillis() + SKIP_MS);
    }

    /** @return true to skip the resolve this time and retry after the skip window */
    public static boolean resolveCoolingDown(@NotNull PsiElement ref) {
        if (!jsFamily(ref)) {
            return false;
        }
        @Nullable Long until = ref.getProject().getUserData(SKIP_UNTIL_KEY);
        return until != null && System.currentTimeMillis() < until;
    }
}
