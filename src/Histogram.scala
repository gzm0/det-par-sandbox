import scala.util._
import iarray._

/**
 * calculates a histogram with IArrays
 */
object Histogram extends App {

  val nr = 100  // number of results
  val nc = 10   // number of classes (in hist)
  
  val values = new IArray[Int](nr)  // results
  val hist = new IArray[Int](nc)    // histogramm
  
  // Calculate some results
  for (i <- 0 to nr-1)
    values(i) = (i + 3) * i % nc
  
  // Populate histogram (note that the RHS is passed by-name)
  for (i <- 0 to nc-1)
    hist(i) = values.compact.map(_.filter(_ == i).length)
    
  // Display hist once it is finished
  hist.compact onComplete { 
    case Success(l) => {
      for ((el,i) <- l.zipWithIndex) println("i: " + el)
      sys.exit(0)
    }
    case Failure(l) => println("hist failure"); sys.exit(1)
  } 
  
}