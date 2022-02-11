package poe

import java.awt.Color

object ColorSquare:

  type ColorSquare = List[List[Color]]

  extension (first: ColorSquare)
    def matches(second: ColorSquare, maxOffset: Int): Boolean =
      first.exists(_.containsSlice(second.head))
      /*val secondT = second.transpose
      LazyList.range(0, maxOffset + 1)
        .flatMap(i => LazyList(second.drop(i)/*, second.dropRight(i)*/, secondT.drop(i).transpose, secondT.dropRight(i).transpose))
        .exists(slice => first.exists(_.containsSlice(slice.head)))*/
