package poe

import cats.effect.IO

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object Clipboard:
  def set(text: String): IO[Unit] =
    IO {
      Toolkit.getDefaultToolkit
        .getSystemClipboard
        .setContents(new StringSelection(text), null)
    }
