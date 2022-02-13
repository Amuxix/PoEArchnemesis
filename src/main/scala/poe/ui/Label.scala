package poe.ui

import com.sun.java.swing.plaf.motif.MotifBorders.FocusBorder
import poe.nemesis.Archnemesis
import poe.{Configuration, Main}

import java.awt.event.{MouseEvent, MouseListener}
import java.awt.{Color, Font}
import javax.swing.border.Border
import javax.swing.{BorderFactory, JLabel, JToolTip}
import Label.opaque

case class Label(
  repaint: () => Unit,
  originalLabel: String,
  originalColor: Color = Color.white,
  tooltip: Option[String] = None,
  onClick: (Label, MouseEvent) => Boolean = (_, _) => false,
) extends MouseListener:
  val label = new JLabel(originalLabel) {
    override def createToolTip(): JToolTip =
      val tooltip: JToolTip = super.createToolTip
      tooltip.setBorder(BorderFactory.createLineBorder(Color.white))
      tooltip.setBackground(Main.config.window.backgroundColor.opaque)
      tooltip
  }
  label.setBackground(new Color(0, 0, 0, 0))
  label.setForeground(originalColor)
  tooltip.foreach(label.setToolTipText)
  label.createToolTip()
  label.setFont(Main.config.window.font)
  label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3))
  label.addMouseListener(this)

  private var _clicked: Boolean = false

  def clicked: Boolean = _clicked

  private def font = label.getFont

  def bold(): Unit =
    label.setFont(font.deriveFont(font.getStyle | Font.BOLD))

  def unbold(): Unit =
    label.setFont(font.deriveFont(font.getStyle & ~Font.BOLD))

  def color(color: Color): Unit =
    label.setForeground(color)
    repaint()

  def resetColor: Unit = color(originalColor)

  override def mouseClicked(e: MouseEvent): Unit =
    _clicked = onClick(this, e)
    repaint()

  def text: String = label.getText
  def text(text: String): Unit = label.setText(text)
  def resetText: Unit = label.setText(originalLabel)

  override def mousePressed(e: MouseEvent): Unit = ()

  override def mouseReleased(e: MouseEvent): Unit = ()

  override def mouseEntered(e: MouseEvent): Unit =
    bold()
    repaint()

  override def mouseExited(e: MouseEvent): Unit =
    if !_clicked then
      unbold()
      repaint()

object Label:
  val noCraftOrConsumeColor = Color.lightGray
  val canCraftColor = Color.orange
  val canConsumeColor = Color.white
  val canExtractMapping = Color.green

  extension (string: String)
    def padLeftTo(len: Int, elem: Char): String =
      val sLen = string.length
      if sLen >= len then
        string
      else
        val sb = new StringBuilder(len)
        sb.append(String.valueOf(elem).repeat(len - sLen))
        sb.append(string)
        sb.toString

  extension (color: Color)
    def hexCode: String = "#" + color.getRed.toHexString.padLeftTo(2, '0') + color.getGreen.toHexString.padLeftTo(2, '0') + color.getBlue.toHexString.padLeftTo(2, '0')
    def opaque: Color = new Color(color.getRed, color.getGreen, color.getBlue)

  private def nemesisWithColorAndAmount(nemesis: Archnemesis, extracted: Map[Archnemesis, Int]): String =
    val amount = extracted.getOrElse(nemesis, 0)
    val amountText = if amount == 0 then "" else s"(Got $amount)"
    s"""<tr style="color:${getColor(nemesis, extracted).hexCode};margin-left: 15px;margin-bottom: 3px"><td>${nemesis.name}</td><td>$amountText</td></tr>"""

  private def createTooltip(nemesis: Archnemesis, extracted: Map[Archnemesis, Int]): String =
    val ingredients =
      if nemesis.ingredients.isEmpty then
        ""
      else //${nemesis.ingredients.map(_.name).mkString("<p>", "<br>", "</p>")}
        s"""
        <h3 style="color:${Color.white.hexCode};margin-bottom: 3px">Ingredients</h3>
        <table>
        ${nemesis.ingredients.map(nemesisWithColorAndAmount(_, extracted)).mkString}
        </table>
          """
    val usedToCraft =
      if nemesis.ingredientForBest.isEmpty then
        ""
      else
        s"""
        <h3 style="color:${Color.white.hexCode};margin-bottom: 3px">Used for</h3>
        <table>
        ${nemesis.ingredientForBest.toList.sortBy(_.reward)(Ordering[Double].reverse).map(nemesisWithColorAndAmount(_, extracted)).mkString}
        </table>
        """
    s"""<html>
      <body style="padding: 0 5px 5px 5px;border: none">
      $ingredients
      $usedToCraft
      </body>
      </html>"""

  private def createName(nemesis: Archnemesis, extracted: Map[Archnemesis, Int], existingMappings: Set[Archnemesis]): String =
    val mappingMissing = if existingMappings.contains(nemesis) then "" else " (M)"
    val amount = extracted.getOrElse(nemesis, 0)
    val amountText = if amount == 0 then "" else s" (Got $amount)"
    val ingredients = if nemesis.ingredients.isEmpty then "" else s" [${nemesis.ingredients.size}]"
    s"${nemesis.name.padTo(18, ' ')}$ingredients$amountText$mappingMissing"

  private def getColor(nemesis: Archnemesis, extracted: Map[Archnemesis, Int]): Color =
    val canCraft = nemesis.tier > 0 && nemesis.ingredients.forall(extracted.contains)
    val gotten = extracted.contains(nemesis)
    if !canCraft && !gotten then
      noCraftOrConsumeColor
    else if canCraft && Main.missingMappings.contains(nemesis) then
      canExtractMapping
    else if canCraft then
      canCraftColor
    else
      canConsumeColor

  def apply(
    window: Window,
    nemesis: Archnemesis,
    extracted: Map[Archnemesis, Int],
    existingMappings: Set[Archnemesis],
    onClick: (Label, MouseEvent) => Boolean,
  ): Label =
    Label(
      () => window.repaint(),
      createName(nemesis, extracted, existingMappings),
      getColor(nemesis, extracted),
      Some(createTooltip(nemesis, extracted)),
      onClick
    )

