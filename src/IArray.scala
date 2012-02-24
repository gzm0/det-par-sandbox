import scala.concurrent._
import scala.util._
import scala.collection.mutable.ArrayLike

class IArray[A](n1: Int) extends IndexedSeq[Future[A]] {

  private val proms = Array.fill(n1)(promise[A])
  
  lazy val compact = {
    val init = promise[List[A]].success(Nil)
    val xs = proms.foldRight(init.future) {
      (el,acc) => for (l <- acc ; x <- el.future) yield x :: l
    }
    xs.map(_.toIndexedSeq)
  }

  def update(i: Future[Int], x: Future[A]): Unit = i onComplete {
    case Success(i) => update(i,x)
    case Failure(e) => throw e 
  }
  def update(i: Future[Int], x: A): Unit = i onComplete {
    case Success(i) => update(i,x)
    case Failure(e) => throw e 
  }
  def update(i: Int, x: => A): Unit = proms(i).success(x)
  def update(i: Int, x: Future[A]): Unit = proms(i).completeWith(x)

  // Stuff in IndexedSeq[Future[A]]
  override def apply(i: Int) = proms(i).future
  override def length = n1
  
}
