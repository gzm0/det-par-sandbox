import scala.util._

/**
 * Inverse permutation example
 */
object InversePermut extends App {
  
  // Dimension
  val n = 5
  
  // Permutation
  val b = new IArray[Int](n)
  b(0) = 1; b(1) = 2; b(3) = 0; b(4) = 4
  
  // Inverse Permutation
  val a = new IArray[Int](n)

  // Calculate
  for (i <- 0 to n-1) a(b(i)) = i
  
  // Show some value
  a(2) onComplete { 
    case Success(s) => println(s); sys.exit(0)
    case Failure(e) => println("fail: " + e); sys.exit(1)
  }
  
}