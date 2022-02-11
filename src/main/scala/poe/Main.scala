package poe

import cats.effect.{IO, IOApp, Ref}
import cats.syntax.traverse.*
import cats.syntax.foldable.*
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import cats.effect.unsafe.implicits.global
import javax.swing.ToolTipManager

import java.awt.Color

object Main extends IOApp.Simple:
  val FKeys = List(
    NativeKeyEvent.VC_F1,
    NativeKeyEvent.VC_F2,
    NativeKeyEvent.VC_F3,
    NativeKeyEvent.VC_F4,
    NativeKeyEvent.VC_F5,
    NativeKeyEvent.VC_F6,
    NativeKeyEvent.VC_F7,
    NativeKeyEvent.VC_F8,
    NativeKeyEvent.VC_F9,
    NativeKeyEvent.VC_F10,
    NativeKeyEvent.VC_F11,
    NativeKeyEvent.VC_F12,
  )
  val unregisterAllFs: IO[Unit] = KeyListener.keyMap.update(_ -- FKeys)

  def set(label: Label, nemesis: Archnemesis, ingredients: Set[Archnemesis]): Unit =
    val clickedIngredients = clicked.flatMap(_._2.ingredients)
    clicked += (label -> nemesis)
    if (clickedIngredients ++ ingredients).size > 4 || (clickedIngredients & ingredients).nonEmpty then
      clicked.map(_._1).foreach(_.color(Color.red))
    label.text("> " + label.text)
    label.bold()

  def unset(label: Label, nemesis: Archnemesis, ingredients: Set[Archnemesis]): Unit =
    val clickedIngredients = clicked.toList.collect {
      case (_, labelNemesis) if labelNemesis != nemesis => labelNemesis.ingredients
    }.flatten
    if clickedIngredients == clickedIngredients.distinct then
      clicked.map(_._1).foreach(_.color(Color.white))
    clicked -= (label -> nemesis)
    label.text(label.text.drop(2))
    label.unbold()

  def displayRecipes(window: Window, recipes: List[(Archnemesis, Int, Set[Archnemesis])]): Unit =
    recipes.foreach { case (nemesis, amount, ingredients) =>
      window.add(Label(window, nemesis, amount, set(_, nemesis, ingredients), unset(_, nemesis, ingredients)))
    }
    window.show()
    window.repaint()

  def findRecipesAndDisplay(window: Window): IO[Unit] =
    for
      _ <- IO(window.clear())
      _ <- IO(window.show())
      extracted <- RecipeFinder.extractAll
      baseIngredients = extracted.flatten.distinct.collect {
        case nemesis if nemesis.tier == 0 =>
        (nemesis, 0, Set(nemesis))
      }.sortBy(_._1.reward)(Ordering[Double].reverse)
      recipes = RecipeFinder.findRecipes(extracted)
      _ <- IO(displayRecipes(window, recipes ++ baseIngredients))
    yield ()

  val mappingSet: Set[Archnemesis] = RecipeFinder.mappings.values.toSet
  val missingMappings: List[Archnemesis] = Archnemesis.values.toList.filter(n => !mappingSet.contains(n))

  var clicked: Set[(Label, Archnemesis)] = Set.empty

  def handleClose(window: Window): Unit =
    if clicked.nonEmpty then
      val search = clicked.flatMap(_._2.ingredients).map(_.regex).mkString("^(", "|", ")")
      Bot.search(search).unsafeRunAndForget()
      clicked = Set.empty

  override def run: IO[Unit] =
    for
      _ <- KeyListener.register
      window = Window((900, 200), (450, 700), window => handleClose(window))
      _ <- IO(ToolTipManager.sharedInstance.setDismissDelay(Integer.MAX_VALUE))
      _ <- KeyListener.keyMap.update(_ + (NativeKeyEvent.VC_J -> RecipeFinder.extractAll.flatMap(RecipeFinder.printGrid)))
      _ <- KeyListener.keyMap.update(_ + (NativeKeyEvent.VC_L -> RecipeFinder.extractMappings))
      _ <- KeyListener.keyMap.update(_ + (NativeKeyEvent.VC_B -> findRecipesAndDisplay(window)))
      _ <- KeyListener.keyMap.update(_ + (NativeKeyEvent.VC_F3 -> findRecipesAndDisplay(window)))
      _ <- KeyListener.keyMap.update(_ + (NativeKeyEvent.VC_F2 -> IO(window.show())))
      _ <- KeyListener.keyMap.update(_ + (NativeKeyEvent.VC_ESCAPE -> IO(window.hide())))
      _ <- IO.println(s"Missing mapping for $missingMappings")
      _ <- IO.println(missingMappings.map(_.regex).grouped(10).map(_.mkString("^(", "|", ")")).mkString("\n"))
      _ <- IO.never
    yield ()