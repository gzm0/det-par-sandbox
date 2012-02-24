import scala.util._

object SharedComp extends App {
  
  def f(x: Double) = x*x
  def g(x: Double) = x-1
  def h(x: Double) = x/2
  
  val n = 5
  
  val tmp = new IArray[Double](n)
  val a = new IArray[Double](n)
  val b = new IArray[Double](n)
  
  for (i <- 0 to n-1) {
    a(i) = tmp(i) map f
    b(i) = tmp(i) map g
    tmp(i) = h(i)
  }
  
  a(2) onComplete { 
    case Success(s) => println(s); sys.exit(0)
    case Failure(e) => println("fail: " + e); sys.exit(1)
  }
  
}