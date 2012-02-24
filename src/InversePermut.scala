import scala.util._

object InversePermut extends App {
  
  val n = 5
  
  val b = new IArray[Int](n)
  val a = new IArray[Int](n)
  
  for (i <- 0 to n-1) a(b(i)) = i
  for (i <- 0 to n-1) b(i) = i
  
  a(2) onComplete { 
    case Success(s) => println(s); sys.exit(0)
    case Failure(e) => println("fail: " + e); sys.exit(1)
  }
  
}