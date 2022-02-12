package poe.ui

import com.sun.java.swing.plaf.motif.MotifBorders.FocusBorder
import poe.nemesis.Archnemesis
import poe.{Configuration, Main}

import java.awt.event.{MouseEvent, MouseListener}
import java.awt.{Color, Font}
import javax.swing.border.Border
import javax.swing.{BorderFactory, JLabel}

case class Label(window: Window, nemesis: Archnemesis, amount: Int, set: Label => Unit, unset: Label => Unit) extends MouseListener:
  private val missing = if Main.mappingSet.contains(nemesis) then "" else " (Missing)"
  private val amountText = if amount == 0 then "" else s" (Got $amount)"
  private val ingredients = if nemesis.ingredients.isEmpty then "" else s" (${nemesis.ingredients.size})"
  val label = new JLabel(s"${nemesis.name}$ingredients$missing$amountText")
  label.setBackground(new Color(0, 0, 0, 0))
  label.setForeground(Color.white)
  label.setToolTipText(Label.createTooltip(nemesis))
  label.setFont(Main.config.window.font)
  label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3))
  label.addMouseListener(this)

  private var clicked: Boolean = false

  private def font = label.getFont

  def bold(): Unit =
    label.setFont(font.deriveFont(font.getStyle | Font.BOLD))

  def unbold(): Unit =
    label.setFont(font.deriveFont(font.getStyle & ~Font.BOLD))

  def color(color: Color): Unit =
    label.setForeground(color)
    window.repaint()

  override def mouseClicked(e: MouseEvent): Unit =
    if !clicked then set(this) else unset(this)
    clicked = !clicked
    window.repaint()

  def text: String = label.getText
  def text(text: String): Unit = label.setText(text)

  override def mousePressed(e: MouseEvent): Unit = ()

  override def mouseReleased(e: MouseEvent): Unit = ()

  override def mouseEntered(e: MouseEvent): Unit =
    bold()
    window.repaint()

  override def mouseExited(e: MouseEvent): Unit =
    if !clicked then
      unbold()
      window.repaint()

object Label:
  def createTooltip(nemesis: Archnemesis): String =
    val ingredients =
      if nemesis.ingredients.isEmpty then
        ""
      else
        s"""
        <h3>Ingredients</h3>
        ${nemesis.ingredients.map(_.name).mkString("<p>", "<br>", "</p>")}
        """
    val usedToCraft =
      if nemesis.ingredientForBest.isEmpty then
        ""
      else
        s"""
        <h3>Used for</h3>
        ${nemesis.ingredientForBest.toList.sortBy(_.reward)(Ordering[Double].reverse).map(_.name).mkString("<p>", "<br>", "</p>")}
        """
    s"""<html>
      $ingredients
      $usedToCraft
      </html>"""

