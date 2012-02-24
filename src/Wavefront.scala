import scala.util._
import iarray._

/**
 * calculates a wavefront using IArrays
 */
object Wavefront extends App {

  // Dimensions
  val m = 5
  val n = 5
  
  // Initialize array
  val test = IArray.ofDim[Int](m,n)
  
  // Declare the actual calculation first to show dataflow
  for (i <- 1 to m-1)
    for (j <- 1 to n-1)
      test(i)(j) = for ( x <- test(i-1)(j) ;
                         y <- test(i-1)(j-1) ;
                         z <- test(i)(j-1) ) yield x+y+z
  
  // Declare initial values                        
  for (i <- 0 to m-1)
    test(i)(0) = 1
    
  for (j <- 1 to n-1)
    test(0)(j) = 1

  // Output some value
  test(m-1)(n-1) onComplete { 
    case Success(s) => println(s); sys.exit(0)
    case Failure(e) => println("fail: " + e); sys.exit(1)
  }
  
}