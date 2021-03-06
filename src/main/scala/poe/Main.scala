package poe

import cats.effect.{IO, IOApp, Ref, Resource}
import cats.syntax.traverse.*
import cats.syntax.foldable.*
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import cats.effect.unsafe.implicits.global
import fs2.io.file.Path
import io.circe.{Codec, Encoder}
import poe.nemesis.Archnemesis
import poe.screenreader.ColorSquare.ColorSquare
import poe.screenreader.{Bot, Extractor}
import poe.ui.{Label, Window}

import javax.swing.{JLabel, ToolTipManager}
import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.event.{InputEvent, MouseEvent}
import java.io.File

object Main extends IOApp.Simple:
  lazy val config: Configuration = Configuration.fromConfig()
  val mappingsPath: Path = Path("../conf/mappings.json")
  var toCraft: Set[(Label, Archnemesis)] = Set.empty
  var toConsume: Set[(Label, Archnemesis)] = Set.empty
  var toExtractMapping: List[(Label, Archnemesis)] = List.empty
  var mappings: Map[Archnemesis, ColorSquare] = Map.empty
  def missingMappings: List[Archnemesis] = Archnemesis.values.toList.filter(n => !mappings.keySet.contains(n))
  var latestFullExtract: List[Option[Archnemesis]] = List.empty
  def failedMatchesAtStart: Int = latestFullExtract.takeWhile(_.isEmpty).size

  def impossibleExtraction: Boolean = toExtractMapping.size > failedMatchesAtStart
  
  def updateIfImpossibleExtract(label: Label): Unit =
    label.resetColor
    if impossibleExtraction then
      toExtractMapping.foreach(_._1.color(Color.red))
    else
      toExtractMapping.foreach(_._1.resetColor)

  def setToExtract(label: Label, nemesis: Archnemesis): Boolean =
    toExtractMapping :+= (label -> nemesis)
    label.text("e " + label.originalLabel)
    label.bold()
    //updateIfImpossibleExtract(label)
    if !missingMappings.contains(nemesis) then
      label.color(Color.yellow)
    true

  def unsetExtract(label: Label, nemesis: Archnemesis): Boolean =
    toExtractMapping = toExtractMapping.filter(_ != (label, nemesis))
    label.resetText
    label.unbold()
    //updateIfImpossibleExtract(label)
    false

  def impossibleSelection(toCraft: Set[(Label, Archnemesis)] = toCraft, toConsume: Set[(Label, Archnemesis)] = toConsume): Boolean =
    val tooManyStuff = (toCraft.map(_._2.ingredients.size).sum + toConsume.size) > 4
    lazy val multipleIngredients = (toCraft.toList.flatMap(_._2.ingredients) ++ toConsume.toList.map(_._2)).groupBy(identity).exists(_._2.size > 1)
    tooManyStuff || multipleIngredients

  def updateIfImpossible(label: Label): Unit =
    label.resetColor
    if impossibleSelection() then
      toCraft.foreach(_._1.color(Color.red))
      toConsume.foreach(_._1.color(Color.red))
    else
      toCraft.foreach(_._1.resetColor)
      toConsume.foreach(_._1.resetColor)

  def setToCraft(label: Label, nemesis: Archnemesis): Boolean =
    toConsume -= (label -> nemesis)
    toCraft += (label -> nemesis)
    label.text("c " + label.originalLabel)
    label.bold()
    updateIfImpossible(label)
    true

  def setToConsume(label: Label, nemesis: Archnemesis): Boolean =
    toCraft -= (label -> nemesis)
    toConsume += (label -> nemesis)
    label.text("u " + label.originalLabel)
    label.bold()
    updateIfImpossible(label)
    true

  def unset(label: Label, nemesis: Archnemesis): Boolean =
    toCraft -= (label -> nemesis)
    toConsume -= (label -> nemesis)
    label.resetText
    label.unbold()
    updateIfImpossible(label)
    false

  def handleClick(label: Label, event: MouseEvent, nemesis: Archnemesis, gotten: Boolean, canCraft: Boolean): Boolean =
    if event.getButton == MouseEvent.BUTTON1 then
      val isSetToCraft = toCraft.contains((label, nemesis))
      val isSetToConsume = toConsume.contains((label, nemesis))
      (gotten, isSetToConsume, canCraft, isSetToCraft) match {
        case (false, _, false, _) => false
        case (false, _, _, true) => unset(label, nemesis)
        case (_, true, _, _) => unset(label, nemesis)
        case (true, false, true, _) if impossibleSelection(toCraft + (label -> nemesis)) => setToConsume(label, nemesis)
        case (_, _, true, false) => setToCraft(label, nemesis)
        case _ => setToConsume(label, nemesis)
      }
    else if event.getButton == MouseEvent.BUTTON3 && event.isControlDown then
      if toExtractMapping.contains((label, nemesis)) then
        unsetExtract(label, nemesis)
      else
        setToExtract(label, nemesis)
    else
      false

  def handleClose(window: Window): Unit =
    if toCraft.nonEmpty || toConsume.nonEmpty then
      val search = (toCraft.flatMap(_._2.ingredients) ++ toConsume.map(_._2)).map(_.regex).mkString("^(", "|", ")")
      if config.window.searchOnClose then Bot.search(search).unsafeRunAndForget()
    if toExtractMapping.nonEmpty && !impossibleExtraction then
      (for
        newMappings <- Extractor.extractMappings(toExtractMapping.map(_._2))
        _ = mappings ++= newMappings
        _ <- Persistence.saveFile(mappingsPath, mappings.toList)
      yield ()).unsafeRunSync()

  private def createHTML(text: String): String = s"<html>&nbsp $text</html>"

  def helpLabels(window: Window): List[Label] = List(
    Label(window.repaint, "Quick Help:"),
    Label(window.repaint, createHTML("Grey   - Can't craft or use"), Label.noCraftOrConsumeColor),
    Label(window.repaint, createHTML("Orange - Can be crafted"), Label.canCraftColor),
    Label(window.repaint, createHTML("Green  - Can craft and extract mapping"), Label.canExtractMapping),
    Label(window.repaint, createHTML("White  - Can't craft, only use"), Label.canConsumeColor),
    Label(window.repaint, createHTML("Red    - Can't use that combo or extract"), Color.red),
    Label(window.repaint, createHTML("[x] - Uses x ingredients")),
    Label(window.repaint, createHTML("(M) - Mapping is missing")),
    Label(window.repaint, createHTML("<u>u</u> Name - Marked to be used")),
    Label(window.repaint, createHTML("<u>c</u> Name - Marked to be crafted")),
    Label(window.repaint, createHTML("<u>e</u> Name - Marked to extract mapping")),
    Label(window.repaint, " "),
    Label(window.repaint, "Keys:"),
    Label(window.repaint, createHTML(s"${config.keys.closeWindow} - Close window")),
    Label(window.repaint, createHTML(s"${config.keys.showHelp}  - Show this help")),
    Label(window.repaint, createHTML(s"${config.keys.reopen}  - Reopen window")),
    Label(window.repaint, createHTML(s"${config.keys.openAndParse}  - Show available recipes")),
  )

  def showHelp(window: Window): IO[Unit] =
    for
      _ <- IO(window.clear())
      _ <- helpLabels(window).traverse_(label => IO(window.add(label)))
      _ <- IO(window.show())
      _ <- IO(window.repaint())
    yield ()

  private val sortedNemesis = Archnemesis.values.toList.sortBy(_.reward)(Ordering[Double].reverse)
  def showArchNemesis(window: Window): IO[Unit] =
    for
      extracted <- Extractor.extractAll.map(_.flatten)
      _ = toConsume = Set.empty
      _ = toCraft = Set.empty
      _ = toExtractMapping = List.empty
      extractedSet = extracted.toSet
      extractedMap = extracted.groupBy(identity).view.mapValues(_.size).toMap
      (craftable, nonCraftable) = sortedNemesis.map { nemesis =>
        val ingredients = nemesis.ingredients.toSet
        val canCraft = ingredients.nonEmpty && ingredients.subsetOf(extractedSet)
        val onClick = handleClick(_, _, nemesis, extractedMap.contains(nemesis), canCraft)
        canCraft -> IO(window.add(Label(window, nemesis, extractedMap, mappings.keySet, onClick)))
      }
        .partition(_._1)
      _ <- IO(window.clear())
      _ <- (craftable ++ nonCraftable).traverse_(_._2)
      _ <- IO(window.show())
    yield ()

  given Codec[Color] = Codec.from(_.as[Int].map(new Color(_)), Encoder[Int].contramap[Color](_.getRGB))

  override def run: IO[Unit] =
    for
      mappings <- Persistence.loadFile[List[(Archnemesis, ColorSquare)]](mappingsPath)
      _ = mappings.foreach(mappings => Main.mappings = mappings.toMap)
      _ <- KeyListener.register
      window = Window(config.window.position, config.window.dimensions, config.window.scrollSpeed, window => handleClose(window))
      _ <- IO(ToolTipManager.sharedInstance.setDismissDelay(Integer.MAX_VALUE))
      _ <- KeyListener.keyMap.update(_ ++ Map(
        config.keys.closeWindowKey -> IO(window.hide()),
        config.keys.showHelpKey -> showHelp(window),
        config.keys.reopenKey -> IO(window.show()),
        config.keys.openAndParseKey -> showArchNemesis(window),
        NativeKeyEvent.VC_F5 -> Extractor.extractAll.flatMap(Extractor.printGrid),
      ))
      _ <- IO.println(s"Missing mapping for $missingMappings")
      _ <- IO.println(missingMappings.map(_.regex).grouped(10).map(_.mkString("^(", "|", ")")).mkString("\n"))
      _ <- IO.never
    yield ()

  /*override def run: IO[Unit] =
    for
      mappings <- Persistence.loadAndMerge[(Archnemesis, ColorSquare)](List("mappings.json", "merge.json").map(Path(_)))
      _ <- Persistence.saveFile(Path("mappings.json"), mappings.distinctBy(_._1))
    yield ()*/
