import mvar._

object MArrayTest extends App {

  val n = 10
  val x = MArray.ofDim[Int](n)
  val y = MArray.ofDim[Int](n)
  
  val z = x.dot[Int,Int](_ * _, _ + _)(y)
  
  z onSuccess {
    case (i,s) => println("%d: %s".format(i,s))
  }
  
  for (i <- 0 to 5) x foreach { _ success (i :: Nil,i+1) }
  for (i <- 0 to 5) y foreach { _ success (i :: Nil,i-1) }
  
}