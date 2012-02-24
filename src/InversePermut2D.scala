import scala.util._
import iarray._

object InversePermut2D extends App {

  // Dimensions
  val n = 3
  val m = 3
  
  // Permutation
  val b1 = IArray.ofDim[Int](n)
  b1(0) = 1; b1(1) = 2; b1(2) = 0;
  
  val b2 = IArray.ofDim[Int](m)
  b2(0) = 1; b2(1) = 0; b2(2) = 2;
  
  // Inverse Permutation
  val a = IArray.ofDim[(Int,Int)](m,n)

  // Calculate
  for (i <- 0 to m-1)
    for (j <- 0 to n-1)
    	a(b1(i))(b2(j)) = (i,j)
  
  // Show some value
  a(1)(0) onComplete { 
    case Success(s) => println(s); sys.exit(0)
    case Failure(e) => println("fail: " + e); sys.exit(1)
  }
  
  
}