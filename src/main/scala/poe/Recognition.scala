package poe

/*import org.bytedeco.javacpp.Loader
import org.opencv.core.*
import org.opencv.highgui.HighGui
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc*/

import org.bytedeco.javacpp.*
import org.opencv.core.Core
import org.bytedeco.opencv.opencv_core.*
import org.bytedeco.opencv.opencv_imgproc.*
import org.bytedeco.opencv.global.opencv_core.*
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.global.opencv_imgcodecs.*
import org.bytedeco.opencv.global.opencv_highgui.*

import scala.util.Try


object Recognition extends App:

  @main def recognition =
    val image = imread("Screenshot.png")
    //val imageGrey = new Mat(image.size, CV_8UC1)
    //cvtColor(image, imageGrey, COLOR_BGR2GRAY)

    val overcharged = imread("Overcharged.png")
    //val overchargedGrey = new Mat(overcharged.size, CV_8UC1)
    //cvtColor(overcharged, overchargedGrey, COLOR_BGR2GRAY)
    val rect = new Rect(19, 4, 10, 13)
    val cropped = new Mat(overcharged, rect)
    val resized = cropped
    //val resized = new Mat()
    //resize(overcharged, resized, new Size(72, 72))

    val size = new Size(image.cols - resized.cols + 1, image.rows - resized.rows + 1)
    val result = new Mat(size, CV_32FC1)
    matchTemplate(image, resized, result, TM_CCOEFF_NORMED) //TM_SQDIFF_NORMED
    val minVal = new DoublePointer(1)
    val maxVal = new DoublePointer(1)
    val min = new Point()
    val max = new Point()
    minMaxLoc(result, minVal, maxVal, min, max, null)
    normalize(result, result, 0.9, 0, NORM_INF, -1, null)
    rectangle(image, new Rect(max.x, max.y, resized.cols, resized.rows), new Scalar(255, 0, 0, 0), 2, 0, 0)
    rectangle(image, new Rect(min.x, min.y, resized.cols, resized.rows), new Scalar(0, 255, 0, 0), 2, 0, 0)
    imshow("Original marked", image)
    imshow("Template", resized)
    imshow("Results matrix", result)
    println(Try(maxVal.get))
    println(Try(minVal.get))
    waitKey(0)
    destroyAllWindows()