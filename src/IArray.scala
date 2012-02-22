import scala.concurrent._
import scala.util._

// TODO should extend Future[Array[A]]
class IArray[A](n1: Int) {

  val proms = Array.fill(n1)(promise[A])

  def update(i: Future[Int], x: Future[A]): Unit = i onComplete {
    case Success(i) => update(i,x)
    case Failure(e) => throw e 
  }
  def update(i: Future[Int], x: => A): Unit = i onComplete {
    case Success(i) => update(i,x)
    case Failure(e) => throw e 
  }
  def update(i: Int, x: => A): Unit = proms(i).success(x)
  def update(i: Int, x: Future[A]): Unit = proms(i).completeWith(x)

  def apply(i: Int) = proms(i).future
  
}
