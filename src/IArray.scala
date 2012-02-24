import scala.concurrent._
import scala.util._
import scala.collection.mutable.ArrayLike
import scala.collection.immutable.Vector

/**
 * IArray structure for distributed calculations
 * TODO how to implement nice out-of-order folding
 */
class IArray[A](n1: Int) extends IndexedSeq[Future[A]] {

  /**
   * promise for each element
   */
  private val proms = Array.fill(n1)(promise[A])
  
  /**
   * compacted array, i.e. future of an array. completes when all elements are
   * assigned
   * TODO best would be to have IArray extends Future[IndexedSeq[A]]. This does
   * not work because of clashes in the combinators. e.g.:
   * map[B](f: (IndexedSeq[A]) => B) from Future[IndexedSeq[A]]
   * map[B](f: (Future[A]) => B)     from IndexedSeq[Future[A]]
   */
  lazy val compact = {
    val init = promise[List[A]].success(Nil)
    val xs = proms.foldRight(init.future) {
      (el,acc) => for (l <- acc ; x <- el.future) yield x :: l
    }
    xs.map(_.toIndexedSeq)
  }
  
  /**
   * late-updates an element
   */
  def update(i: Future[Int], x: Future[A]): Unit = i onComplete {
    case Success(i) => update(i,x)
    case Failure(e) => throw e // TODO this should fail this.compact (or this)
  }
  /**
   * late-updates an element
   */
  def update(i: Future[Int], x: =>A): Unit = i onComplete {
    case Success(i) => update(i,x)
    case Failure(e) => throw e // TODO this should fail this.compact (or this)
  }
  
  /**
   * updates an element
   */
  def update(i: Int, x: =>A): Unit = proms(i).success(x)
  
  /**
   * updates an element
   */
  def update(i: Int, x: Future[A]): Unit = proms(i).completeWith(x)

  /**
   * read i-th element
   */
  override def apply(i: Int) = proms(i).future
  
  /**
   * get length 
   */
  override def length = n1
  
}

object IArray {
  /**
   * Creates an 2-dimensional IArray
   * TODO this does not allow late assignments where the first index is a future 
   */
  def ofDim [A] (n1: Int, n2: Int) =
    Vector.fill(n1)(new IArray[A](n2))
}