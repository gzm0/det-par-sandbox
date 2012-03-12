import mvar._

object MMatrixTest extends App {

  implicit val arith = new Arithmetic[Int](_ * _, _ + _)
  
  val x = MMatrix.ofDim[Int](2,4)
  val y = MMatrix.ofDim[Int](4,3)
  
  val z = x * y
  
  z(0)(0) onSuccess shdebug("z")
  
  x.foreach { _.zipWithIndex.foreach({ (f: MPromise[Int],i: Int) => f.success(Nil,i+5) }.tupled)}
  y.foreach { _.zipWithIndex.foreach({ (f: MPromise[Int],i: Int) => f.success(Nil,i+5) }.tupled)}
  
}