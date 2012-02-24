import scala.util._

/**
 * shared computation example with IArrays
 */
object SharedComp extends App {
  
  // the computations
  def f(x: Double) = x*x
  def g(x: Double) = x-1
  def h(x: Double) = x/2
  
  // dimension
  val n = 5
  
  // arrays
  val tmp = new IArray[Double](n)
  val a = new IArray[Double](n)
  val b = new IArray[Double](n)
  
  // assign data
  for (i <- 0 to n-1) {
    a(i) = tmp(i) map f
    b(i) = tmp(i) map g
    tmp(i) = h(i)
  }
  
  // display some data
  a(2) onComplete { 
    case Success(s) => println(s); sys.exit(0)
    case Failure(e) => println("fail: " + e); sys.exit(1)
  }
  
}