package iarray

import scala.concurrent._
import scala.util._

/**
 * IArrayLike structure
 * TODO how to implement nice out-of-order folding
 */
trait IArrayLike[A] extends IndexedSeq[Future[A]] {

  /**
   *  Promise to handle failure of late-updates
   */
  private val ffail = promise[Nothing] 
  
  protected def fail(e: Throwable) = ffail.tryFailure(e)
  
  /**
   * late-updates an element
   */
  def update(i: Future[Int], x: Future[A]): Unit = i onComplete {
    case Success(i) => update(i,x)
    case Failure(e) => fail(e)
  }
  /**
   * late-updates an element
   */
  def update(i: Future[Int], x: =>A): Unit = i onComplete {
    case Success(i) => update(i,x)
    case Failure(e) => fail(e)
  }
  
  def update(i: Int, x: =>A): Unit
  def update(i: Int, x: Future[A]): Unit
  
    /**
   * read i-th element
   */
  override def apply(i: Int): Future[A]
  
  /**
   * late read i-th element 
   */
  def apply(i: Future[Int]): Future[A]
  
  /**
   * get length 
   */
  override def length: Int
  
  /**
   * compacted array, i.e. future of an array. completes when all elements are
   * assigned
   * TODO best would be to have IArrayLike extends Future[IndexedSeq[A]].
   * This does not work because of clashes in the combinators. e.g.:
   * map[B](f: (IndexedSeq[A]) => B) from Future[IndexedSeq[A]]
   * map[B](f: (Future[A]) => B)     from IndexedSeq[Future[A]]
   */
  lazy val compact: Future[IndexedSeq[A]] = {
    val compProm = promise[IndexedSeq[A]]
    ffail.future onFailure { case e => compProm.failure(e) }
    intCompact   onSuccess { case l => compProm.success(l) }
    compProm.future
  }
  
  /**
   * internal compacted array. same as compact but no failure checking.
   * i.e. this future will not fail if there is a failure, but compact will.
   */
  private lazy val intCompact = {
    val init = promise[List[A]].success(Nil)
    val xs = this.foldRight(init.future) {
      (el,acc) => for (l <- acc ; x <- el) yield x :: l
    }
    xs.map(_.toIndexedSeq)
  }
  
}