package poe

import com.typesafe.config.{Config, ConfigFactory}
import pureconfig.*
import pureconfig.generic.derivation.default.derived

import java.awt.{Color, Font}

case class Window(
  position: (Int, Int),
  dimensions: (Int, Int),
  scrollSpeed: Int,
  fontName: String,
  fontSize: Int,
  backgroundColorRgba: (Int, Int, Int, Int),
) derives ConfigReader:
  val backgroundColor: Color =
    val (r, g, b, a) = backgroundColorRgba
    new Color(r, g, b, a)
  val font: Font = new Font(fontName, Font.PLAIN, fontSize)

case class Reader(
  firstSquarePosition: (Int, Int),
  extractSize: Int,
  parseSize: Int,
  columnSizes: List[Int],
  lineSizes: List[Int],
  useSimilar: Boolean,
  minSimilarity: Int,
) derives ConfigReader

case class Mappings(
  mappings: List[List[Int]],
) derives ConfigReader

case class Configuration(
  window: Window,
  reader: Reader,
) derives ConfigReader

object Configuration:
  def fromConfig(config: Config = ConfigFactory.load()): Configuration = ConfigSource.fromConfig(config).loadOrThrow[Configuration]