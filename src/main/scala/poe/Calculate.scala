package poe

import poe.Archnemesis.*
import poe.Reward.*

object Calculate extends App:
  extension[T] (xs: Iterable[T])
    def sumBy[U](f: T => U)(implicit N: Numeric[U]): U = xs.foldLeft(N.zero)((acc, x) => N.plus(acc, f(x)))

  extension (number: Double)
    def round(precision: Int): Double =
      val d = Math.pow(10, precision)
      Math.round(number * d) / d

  @main def run: Unit =
    Archnemesis.values.foreach { nemesis =>
      //println(s"$nemesis -> ${nemesis.reward}")
      //println(s"${nemesis.tier}\t${nemesis.toCraft}\t${nemesis.reward}")
      println(s"${nemesis.roundedReward}\t${nemesis.rewardReason}")
      //println(s"${nemesis.allIngredientAmounts}\t${nemesis.allIngredientAmountsByTier}")
      //println(nemesis.roundedReward)
      //println(nemesis.rewardReason)
    }
