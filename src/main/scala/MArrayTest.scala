import mvar._

object MArrayTest extends App {

  val n = 10
  val x = MArray.ofDim[Int](n)
  val y = MArray.ofDim[Int](n)
  
  implicit val arith = new Arithmetic[Int](_ * _, _ + _)
  
  val z = x * y
  
  z onSuccess shdebug("z")
  
  for (i <- 0 to 5) x foreach { _ success (i :: Nil,i+1) }
  for (i <- 0 to 5) y foreach { _ success (i :: Nil,i-1) }
  
}