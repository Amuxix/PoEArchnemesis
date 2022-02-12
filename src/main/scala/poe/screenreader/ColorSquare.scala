package poe.screenreader

import poe.Main

import java.awt.Color

object ColorSquare:

  type ColorSquare = List[List[Color]]

  extension (color: Color)
    def isSimilar(other: Color) =
      color == other ||
        math.abs(color.getRed - other.getRed) <= Main.config.reader.minSimilarity &&
        math.abs(color.getBlue - other.getBlue) <= Main.config.reader.minSimilarity &&
        math.abs(color.getGreen - other.getGreen) <= Main.config.reader.minSimilarity

  extension (first: ColorSquare)
    def matches(second: ColorSquare): Boolean =
      if Main.config.reader.useSimilar then
        first.isSimilar(second)
      else
        first.measuresUpTo(second)

    def measuresUpTo(second: ColorSquare): Boolean =
      first.exists(_.containsSlice(second.head))

    def isSimilar(second: ColorSquare): Boolean =
      val secondSize = second.head.size
      val sizeDifference = first.head.size - secondSize
      (0 to sizeDifference).exists { i =>
        first.exists(_.slice(i, i + secondSize).zip(second.head).forall((c1, c2) => c1.isSimilar(c2)))
      }
