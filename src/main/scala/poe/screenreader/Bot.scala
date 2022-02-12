package poe.screenreader

import cats.effect.{IO, Ref}
import cats.syntax.traverse.*
import poe.Clipboard
import poe.screenreader.ColorSquare.ColorSquare

import java.awt.event.{InputEvent, KeyEvent}
import java.awt.{Color, Robot}
import scala.concurrent.duration.*
import scala.util.Random

object Bot:
  private val robot: Robot = new Robot()
  private val mousePosition: Ref[IO, (Int, Int)] = Ref.unsafe[IO, (Int, Int)]((0, 0))

  def click: IO[Unit] =
    for
      _ <- IO(robot.mousePress(InputEvent.BUTTON1_DOWN_MASK))
      _ <- IO(robot.delay(50 + Random.nextInt(30)))
      _ <- IO(robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK))
      _ <- IO(robot.waitForIdle())
    yield ()

  def copy: IO[Unit] =
    for
      _ <- IO(robot.keyPress(KeyEvent.VK_CONTROL))
      _ <- IO(robot.delay(35 + Random.nextInt(30)))
      _ <- IO(robot.keyPress(KeyEvent.VK_C))
      _ <- IO(robot.delay(50 + Random.nextInt(30)))
      _ <- IO(robot.keyRelease(KeyEvent.VK_C))
      _ <- IO(robot.delay(35 + Random.nextInt(30)))
      _ <- IO(robot.keyRelease(KeyEvent.VK_CONTROL))
      _ <- IO(robot.waitForIdle())
    yield ()

  def search(text: String): IO[Unit] =
    for
      _ <- IO(robot.keyPress(KeyEvent.VK_CONTROL))
      _ <- IO(robot.delay(35 + Random.nextInt(30)))
      _ <- IO(robot.keyPress(KeyEvent.VK_F))
      _ <- IO(robot.delay(50 + Random.nextInt(30)))
      _ <- IO(robot.keyRelease(KeyEvent.VK_F))
      _ <- IO(robot.delay(35 + Random.nextInt(30)))
      _ <- IO(robot.keyRelease(KeyEvent.VK_CONTROL))
      _ <- Clipboard.set(text)
      _ <- IO(robot.keyPress(KeyEvent.VK_CONTROL))
      _ <- IO(robot.delay(35 + Random.nextInt(30)))
      _ <- IO(robot.keyPress(KeyEvent.VK_V))
      _ <- IO(robot.delay(50 + Random.nextInt(30)))
      _ <- IO(robot.keyRelease(KeyEvent.VK_V))
      _ <- IO(robot.delay(35 + Random.nextInt(30)))
      _ <- IO(robot.keyRelease(KeyEvent.VK_CONTROL))
    yield ()

  def go(pos: (Int, Int)): IO[Unit] =
    for
      _ <- mousePosition.set(pos)
      (x, y) = pos
      _ <- IO(robot.mouseMove(x, y))
      _ <- IO(robot.waitForIdle())
    yield ()

  def move(x: Int, y: Int): IO[Unit] =
    for
      (oldX, oldY) <- mousePosition.get
      _ <- go((oldX + x, oldY + y))
    yield ()

  def color(x: Int, y: Int): IO[Color] = IO(robot.getPixelColor(x, y))

  def colorSquare(x: Int, y: Int, size: Int): IO[ColorSquare] =
    val half = size / 2D
    val range = List.range((-half).round.toInt, half.round.toInt)
    (for
      ox <- range
      oy <- range
    yield color(x + ox, y + oy)).sequence.map(_.grouped(size).toList)
