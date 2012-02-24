import scala.concurrent._

// TODO should extend Future[Array[Array[A]]]
class IMatrix[A](n1: Int, n2: Int) {

  val proms = Array.fill(n1,n2)(promise[A])
  
  def update(i: Int, j: Int, x: A) = proms(i)(j).success(x)
    
  def update(i: Int, j: Int, x: Future[A]) = proms(i)(j).completeWith(x)

  def apply(i: Int, j: Int) = proms(i)(j).future
  
}
