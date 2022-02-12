package poe

import cats.effect.IO

import java.awt.{Adjustable, Color, Dimension, FlowLayout, GraphicsConfiguration, GraphicsDevice, GraphicsEnvironment}
import javax.swing.{BorderFactory, BoxLayout, ImageIcon, JFrame, JLabel, JOptionPane, JPanel, JScrollBar, JScrollPane, ScrollPaneConstants, WindowConstants}
import java.awt.event.{MouseEvent, MouseListener, WindowEvent, WindowFocusListener}
import java.awt.geom.RoundRectangle2D
import org.bytedeco.librealsense.frame

import java.awt.event.MouseAdapter
import javax.swing.event.{ChangeEvent, ChangeListener}

case class Window(position: (Int, Int), dimensions: (Int, Int), onHide: Window => Unit):
  window =>
  private val focusListener = new WindowFocusListener {
    override def windowGainedFocus(e: WindowEvent): Unit = ()

    override def windowLostFocus(e: WindowEvent): Unit = hide()
  }

  private val mouseAdapter = new MouseAdapter() {
    override def mouseExited(e: MouseEvent): Unit =
      if e.getX >= frame.getContentPane.getWidth || e.getX <= 0 || e.getY >= frame.getContentPane.getHeight || e.getY < 0 then
        hide()
  }

  private val frame = new JFrame("PoE Archnemesis Calculator")
  frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  frame.setUndecorated(true)
  frame.setBackground(new Color(0, 0, 0, 0))
  frame.setPreferredSize(new Dimension(dimensions._1, dimensions._2))
  frame.setLocation(position._1, position._2)
  frame.setShape(new RoundRectangle2D.Double(0, 0, dimensions._1, dimensions._2, 15, 15))
  frame.addWindowFocusListener(focusListener)
  frame.setAlwaysOnTop(true)
  private val panel = new JPanel()
  //panel.addMouseListener(mouseListener)
  //panel.setPreferredSize(new Dimension(dimensions._1, dimensions._2 + 200))
  val scrollPane: JScrollPane = new JScrollPane(panel)
  scrollPane.setBackground(new Color(0, 0, 0, 0))
  //scrollFrame.setPreferredSize(new Dimension(dimensions._1, dimensions._2))
  val scrollBar: JScrollBar = new JScrollBar(Adjustable.VERTICAL) {
    override def isVisible: Boolean = true
  }
  //scrollFrame.setVerticalScrollBar(scrollBar)
  scrollPane.getVerticalScrollBar.setPreferredSize(new Dimension(0, 0 ))
  scrollPane.getVerticalScrollBar.setUnitIncrement(6)
  scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED)
  scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
  scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0))
  scrollPane.getViewport.addChangeListener((e: ChangeEvent) => repaint())
  panel.setAutoscrolls(true)
  frame.add(scrollPane)
  panel.addMouseListener(mouseAdapter)
  panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))
  panel.setBackground(new Color(0, 34, 92, 120))
  panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15))
  //frame.add(panel)
  frame.pack()

  def add(label: Label): Unit = panel.add(label.label)

  def clear(): Unit =
    panel.removeAll()
    frame.revalidate()
    frame.repaint()

  def hide(): Unit =
    onHide(window)
    frame.setVisible(false)

  def show(): Unit = frame.setVisible(true)

  def repaint(): Unit = frame.repaint()
