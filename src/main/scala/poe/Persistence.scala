package poe

import cats.effect.IO
import cats.syntax.foldable.*
import cats.syntax.traverse.*
import fs2.io.file.{Files, Path}
import fs2.{Stream, text}
import io.circe.{Codec, Decoder, Encoder, Error}
import io.circe.syntax.*
import io.circe.parser.decode

import java.awt.Color
import java.io.File

object Persistence:
  private def saveStream[A: Encoder](path: Path, thing: A): Stream[IO, Unit] =
    for
      exists <- Stream.eval(Files[IO].exists(path))
      _ <- if exists then Stream.eval(IO.unit) else Stream.eval(Files[IO].createFile(path))
      _ <- Stream.emit(thing.asJson.spaces2).through(text.utf8.encode).through(Files[IO].writeAll(path))
    yield ()
  def saveFile[A: Encoder](path: Path, thing: A): IO[Unit] =
    saveStream(path, thing).compile.drain

  private def loadFileStream[A: Decoder](path: Path): Stream[IO, A] =
    for
      exists <- Stream.eval(Files[IO].exists(path))
      _ <- if !exists then Stream.empty else Stream.eval(IO.unit)
      contents <- Files[IO].readAll(path).through(text.utf8.decode)
      thing <- Stream.emit(decode[A](contents).toOption).collect {
        case Some(thing) => thing
      }
    yield thing

  def loadFile[A: Decoder](path: Path): IO[Option[A]] =
    loadFileStream[A](path).compile.last

  def loadAndMerge[A: Decoder](paths: List[Path]): IO[List[A]] =
    (for
      path <- Stream.emits(paths)
      thing <- loadFileStream[List[A]](path)
    yield thing).compile.foldMonoid