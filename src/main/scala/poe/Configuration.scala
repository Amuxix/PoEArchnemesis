package poe

import com.typesafe.config.{Config, ConfigFactory}
import fs2.io.file.Path
import pureconfig.*
import pureconfig.generic.derivation.default.derived

import java.awt.{Color, Font}
import java.io.File

case class Window(
  position: (Int, Int),
  dimensions: (Int, Int),
  scrollSpeed: Int,
  fontName: String,
  fontSize: Int,
  backgroundColorRgba: (Int, Int, Int, Int),
  searchOnClose: Boolean,
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
) derives ConfigReader

case class Keys(
  closeWindow: String,
  showHelp: String,
  reopen: String,
  openAndParse: String,
) derives ConfigReader:
  lazy val closeWindowKey: Int = KeyConverter.findKey(closeWindow)
  lazy val showHelpKey: Int = KeyConverter.findKey(showHelp)
  lazy val openAndParseKey: Int = KeyConverter.findKey(openAndParse)
  lazy val reopenKey: Int = KeyConverter.findKey(reopen)

case class Configuration(
  window: Window,
  reader: Reader,
  keys: Keys,
) derives ConfigReader

object Configuration:
  def fromConfig(config: Config = ConfigFactory.load()): Configuration = ConfigSource.fromConfig(config).loadOrThrow[Configuration]
