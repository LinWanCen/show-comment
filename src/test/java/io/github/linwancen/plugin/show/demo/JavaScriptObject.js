obj.isA(obj.getB())
obj.setA(obj.getB())
obj.setB(obj.a)

/** A */
let a = obj.isA(obj.getA())
/** B */
let b = obj
    .setA(obj.getA())
/** B */
let b = obj
    .setA(
        obj.getB())

/** obj1 */
let obj = {
  /** a */
  "a": "A",
  /** b */
  "b": "B",
  /** sub */
  "sub": {
    /** subKey */
    "subKey": "subVal"
  },
  /** isA */
  isA: function (t) {
    return t
  },
  /** setA */
  setA: function (t) {
    return t
  },
  /** getB */
  getB: function (t) {
    return t
  },
  /** setB */
  setB: function (t) {
    return t
  },
}