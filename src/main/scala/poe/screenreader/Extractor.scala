package poe.screenreader

import cats.effect.IO
import cats.syntax.foldable.*
import cats.syntax.traverse.*
import poe.Main
import poe.screenreader.Bot
import poe.nemesis.Archnemesis
import poe.nemesis.Archnemesis.*
import poe.screenreader.ColorSquare.*

import java.awt.Color

object Extractor:
  def findNemesis(square: ColorSquare): Option[Archnemesis] = Main.mappings.collectFirst {
    case (nemesis, nemesisSquare) if nemesisSquare.matches(square) => nemesis
  }

  private lazy val lineMap = List.range(0, 8).map(i => i -> Main.config.reader.lineSizes.take(i).sum).toMap
  private lazy val columnMap = List.range(0, 8).map(i => i -> Main.config.reader.columnSizes.take(i).sum).toMap

  @inline def squareAt(x: Int, y: Int, size: Int): IO[ColorSquare] =
    val (startX, startY) = Main.config.reader.firstSquarePosition
    Bot.colorSquare(startX + lineMap(x), startY + columnMap(y), size)

  def extract(n: Int, size: Int): IO[List[ColorSquare]] =
    List.tabulate(n)(i => squareAt(i % 8, i / 8, size)).sequence

  def extractMappings(nemesis: List[Archnemesis]): IO[List[(Archnemesis, ColorSquare)]] =
    extract(nemesis.size, Main.config.reader.extractSize).map(nemesis.zip)

  val extractAll: IO[List[Option[Archnemesis]]] = extract(8*8, Main.config.reader.parseSize)
    .map { colorSquares =>
      val nemesis = colorSquares.map(findNemesis)
      Main.latestFullExtract = nemesis
      nemesis
    }

  def printGrid(extracted: List[Option[Archnemesis]]): IO[Unit] =
    val parsed = extracted.map(_.fold("")(_.toString))
    IO.println(parsed.grouped(8).toList.map(_.map(_.padTo(18, ' ')).mkString(" ")).mkString("\n"))
