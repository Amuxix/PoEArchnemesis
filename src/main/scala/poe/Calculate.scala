package poe

import poe.nemesis.Archnemesis.*
import poe.nemesis.Reward.*

object Calculate:
  extension[T] (xs: Iterable[T])
    def sumBy[U](f: T => U)(implicit N: Numeric[U]): U = xs.foldLeft(N.zero)((acc, x) => N.plus(acc, f(x)))

  extension (number: Double)
    def round(precision: Int): Double =
      val d = Math.pow(10, precision)
      Math.round(number * d) / d
