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

object WavefrontTest extends App {

  val m = 5
  val n = 5
  
  val test = new IMatrix[Int](m,n)
  
  for (i <- 1 to m-1)
    for (j <- 1 to n-1)
      test(i,j) = for ( x <- test(i-1,j) ;
                        y <- test(i-1,j-1) ;
                        z <- test(i,j-1) ) yield x+y+z
  
  for (i <- 0 to m-1)
    test(i,0) = 1
    
  for (j <- 1 to n-1)
    test(0,j) = 1
      
  test(m-1,n-1) onComplete { 
    case Success(s) => println(s); sys.exit(0)
    case Failure(e) => println("fail: " + e); sys.exit(1)
  }
  
}