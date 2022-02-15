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

  private val (startX, startY) = Main.config.reader.firstSquarePosition
  @inline def squareAt(x: Int, y: Int, size: Int): IO[ColorSquare] =
    //Bot.colorSquare(startX + lineMap(x), startY + columnMap(y), size)
    Bot.color(startX + lineMap(x) - 1, startY + columnMap(y) - 1).map(c => List(List(c)))

  def extract(n: Int, size: Int): IO[List[ColorSquare]] =
    val half = (-size / 2D).round.toInt
    val range = List.range(0, size)
    Bot.screenCapture(startX + half, startY + half, lineMap(7) + size + 1, columnMap(7) + size + 1).map { image =>
      List.tabulate(n) { i =>
        range.map { ox =>
          range.map { oy =>
            new Color(image.getRGB(lineMap(i % 8) + ox, columnMap(i / 8) + oy))
          }
        }
      }
    }

  def extractMappings(nemesis: List[Archnemesis]): IO[List[(Archnemesis, ColorSquare)]] =
    extract(nemesis.size, Main.config.reader.extractSize).map(nemesis.zip)

  val extractAll: IO[List[Option[Archnemesis]]] = extract(8*8, Main.config.reader.parseSize)
    .map { colorSquares =>
      val nemesis = colorSquares.map(findNemesis)
      Main.latestFullExtract = nemesis
      nemesis
    }

  private def simpleMapping: Map[Int, Archnemesis] = Main.mappings.map {
    case (nemesis, colorSquare) => colorSquare(1)(1).getRGB -> nemesis
  }
  /*val extractAll: IO[List[Option[Archnemesis]]] =
    Bot.screenCapture(startX - 1, startY - 1, lineMap(7) + 1, columnMap(7) + 1) .map { image =>
      List.tabulate(8*8) { i =>
        simpleMapping.get(image.getRGB(lineMap(i % 8), columnMap(i / 8)))
      }
    }*/

  def printGrid(extracted: List[Option[Archnemesis]]): IO[Unit] =
    val parsed = extracted.map(_.fold("")(_.toString))
    IO.println(parsed.grouped(8).toList.map(_.map(_.padTo(18, ' ')).mkString(" ")).mkString("\n"))
