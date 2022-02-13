package poe

import cats.effect.{IO, IOApp, Ref}
import cats.syntax.traverse.*
import cats.syntax.foldable.*
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import cats.effect.unsafe.implicits.global
import poe.nemesis.Archnemesis
import poe.screenreader.{Bot, Extractor}
import poe.ui.{Label, Window}

import javax.swing.{JLabel, ToolTipManager}
import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.io.File

object Main extends IOApp.Simple:
  val config: Configuration = Configuration.fromConfig()
  var toCraft: Set[(Label, Archnemesis)] = Set.empty
  var toConsume: Set[(Label, Archnemesis)] = Set.empty
  val mappingSet: Set[Archnemesis] = Extractor.mappings.values.toSet
  val missingMappings: List[Archnemesis] = Archnemesis.values.toList.filter(n => !mappingSet.contains(n))

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
    label.text(label.originalLabel)
    label.unbold()
    updateIfImpossible(label)
    false


  def handleClick(label: Label, nemesis: Archnemesis, gotten: Boolean, canCraft: Boolean): Boolean =
    val isSetToCraft = toCraft.contains((label, nemesis))
    val isSetToConsume = toConsume.contains((label, nemesis))
    //println((gotten, isSetToConsume, canCraft, isSetToCraft, impossibleSelection(toCraft + (label -> nemesis))))
    (gotten, isSetToConsume, canCraft, isSetToCraft) match {
      case (false, _, false, _) => false
      case (false, _, _, true) => unset(label, nemesis)
      case (_, true, _, _) => unset(label, nemesis)
      case (true, false, true, _) if impossibleSelection(toCraft + (label -> nemesis)) => setToConsume(label, nemesis)
      case (_, _, true, false) => setToCraft(label, nemesis)
      case _ => setToConsume(label, nemesis)
    }

  def handleClose(window: Window): Unit =
    println("Closing")
    if toCraft.nonEmpty || toConsume.nonEmpty then
      val search = (toCraft.flatMap(_._2.ingredients) ++ toConsume.map(_._2)).map(_.regex).mkString("^(", "|", ")")
      Bot.search(search).unsafeRunAndForget()

  private def createHTML(text: String): String = s"<html>&nbsp&nbsp $text</html>"

  def helpLabels(window: Window): List[Label] = List(
    Label(window.repaint, "Extracting Archnemesis..."),
    Label(window.repaint, " "),
    Label(window.repaint, "Quick Help:"),
    Label(window.repaint, createHTML("Grey   - Can't craft or use"), Label.noCraftOrConsumeColor),
    Label(window.repaint, createHTML("Orange - Can be crafted"), Label.canCraftColor),
    Label(window.repaint, createHTML("White  - Can't craft, only use"), Label.canConsumeColor),
    Label(window.repaint, createHTML("Red    - Can't use that combo"), Color.red),
    Label(window.repaint, createHTML("[x] - Uses x ingredients")),
    Label(window.repaint, createHTML("(M) - Mapping is missing")),
    Label(window.repaint, createHTML("<u>u</u> Name - Marked to be used")),
    Label(window.repaint, createHTML("<u>c</u> Name - Marked to be crafted")),
    Label(window.repaint, " "),
    Label(window.repaint, "Keys:"),
    Label(window.repaint, createHTML("Esc - Close window")),
    Label(window.repaint, createHTML("F2  - Reopen window")),
    Label(window.repaint, createHTML("F3  - Show available recipes")),
  )

  private val sortedNemesis = Archnemesis.values.toList.sortBy(_.reward)(Ordering[Double].reverse)
  private def addNemesisWithoutExtraction(window: Window) = sortedNemesis.traverse_ { nemesis =>
    IO(window.add(Label(window, nemesis, Map.empty, mappingSet, _ => false)))
  }
  def showArchNemesis(window: Window): IO[Unit] =
    for
      _ <- IO(window.clear())
      _ = toConsume = Set.empty
      _ = toCraft = Set.empty
      //_ <- addNemesisWithoutExtraction(window)
      _ <- helpLabels(window).traverse_(label => IO(window.add(label)))
      _ <- IO(window.show())
      _ <- IO(window.repaint())
      extracted <- Extractor.extractAll.map(_.flatten)
      extractedSet = extracted.toSet
      extractedMap = extracted.groupBy(identity).view.mapValues(_.size).toMap
      addToWindow = sortedNemesis.traverse_ { nemesis =>
        val ingredients = nemesis.ingredients.toSet
        val canCraft = ingredients.nonEmpty && ingredients.subsetOf(extractedSet)
        val onClick = handleClick(_, nemesis, extractedMap.contains(nemesis), canCraft)
        IO(window.add(Label(window, nemesis, extractedMap, mappingSet, onClick)))
      }
      _ <- IO(window.clear())
      _ <- addToWindow
      //_ <- IO(window.show())
      _ <- IO(window.repaint())
    yield ()

  def loadFont: IO[Unit] =
    val ge: GraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment
    IO(ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(s"C:/Windows/Fonts/${config.window.fontName}.ttf"))))

  override def run: IO[Unit] =
    for
      _ <- KeyListener.register
      window = Window(config.window.position, config.window.dimensions, config.window.scrollSpeed, window => handleClose(window))
      _ <- IO(ToolTipManager.sharedInstance.setDismissDelay(Integer.MAX_VALUE))
      _ <- KeyListener.keyMap.update(_ ++ Map(
        NativeKeyEvent.VC_ESCAPE -> IO(window.hide()),
        NativeKeyEvent.VC_F2 -> IO(window.show()),
        NativeKeyEvent.VC_F3 -> showArchNemesis(window),
        //NativeKeyEvent.VC_F3 -> findRecipesAndDisplay(window),
        NativeKeyEvent.VC_F4 -> Extractor.extractMappings,
        NativeKeyEvent.VC_F5 -> Extractor.extractAll.flatMap(Extractor.printGrid),
        //NativeKeyEvent.VC_B -> findRecipesAndDisplay(window),
      ))
      _ <- IO.println(s"Missing mapping for $missingMappings")
      _ <- IO.println(missingMappings.map(_.regex).grouped(10).map(_.mkString("^(", "|", ")")).mkString("\n"))
      _ <- IO.never
    yield ()