package poe

import cats.effect.{IO, Ref}
import com.github.kwhat.jnativehook.keyboard.{NativeKeyEvent, NativeKeyListener}
import com.github.kwhat.jnativehook.GlobalScreen
import cats.effect.unsafe.implicits.global

object KeyListener extends NativeKeyListener:

  val keyMap: Ref[IO, Map[Int, IO[Unit]]] = Ref.unsafe(Map.empty)

  override def nativeKeyReleased(nativeEvent: NativeKeyEvent): Unit =
    keyMap.get.flatMap(_.getOrElse(nativeEvent.getKeyCode, IO.unit)).unsafeRunSync()


  def register: IO[Unit] =
    for
      _ <- IO(GlobalScreen.registerNativeHook())
      _ <- IO(GlobalScreen.addNativeKeyListener(this))
    yield ()


