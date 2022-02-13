package poe

import cats.effect.IO
import fs2.io.file.{Files, Path}
import fs2.{Stream, text}
import io.circe.{Codec, Decoder, Encoder, Error}
import io.circe.syntax.*
import io.circe.parser.decode

import java.awt.Color
import java.io.File

object Persistence:
  def saveFile[A: Encoder](path: Path, thing: A): IO[Unit] =
    (for {
      exists <- Stream.eval(Files[IO].exists(path))
      _ <- if exists then Stream.eval(IO.unit) else Stream.eval(Files[IO].createFile(path))
      _ <- Stream.emit(thing.asJson.spaces2).through(text.utf8.encode).through(Files[IO].writeAll(path))
    } yield ()).compile.drain

  def loadFile[A: Decoder](path: Path, errorHandler: (String, Error) => IO[Unit]): IO[Option[A]] =
    Files[IO].exists(path).flatMap {
      case false => IO.pure(None)
      case true =>
        (for {
          contents <- Files[IO].readAll(path).through(text.utf8.decode)
          thing <- decode[A](contents).fold(
            error => Stream.eval(errorHandler(path.fileName.toString, error).as(None)),
            thing => Stream.emit(Some(thing))
          ).collect {
            case Some(thing) => thing
          }
        } yield thing).compile.last
    }
