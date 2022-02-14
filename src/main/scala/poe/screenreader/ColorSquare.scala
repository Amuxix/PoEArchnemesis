package poe.screenreader

import poe.Main

import java.awt.Color

object ColorSquare:

  type ColorSquare = List[List[Color]]

  extension (first: ColorSquare)
    def matches(second: ColorSquare): Boolean =
      first.exists(_.containsSlice(second.head))
