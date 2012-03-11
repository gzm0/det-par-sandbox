package iarray

import scala.concurrent._
import scala.util._

/**
 * proxies an Future[IArrayLike]
 */
class IArrayProxy[A](int: Future[IArrayLike[A]], dim: Int)
 extends IArrayLike[A] {

  def update(i: Int, x: =>A) = int onComplete {
    case Success(l) => l.update(i,x)
    case Failure(e) => fail(e)
  }
  def update(i: Int, x: Future[A]) = int onComplete {
    case Success(l) => l.update(i,x)
    case Failure(e) => fail(e)
  }
  
  /**
   * read i-th element
   */
  override def apply(i: Int) = int.flatMap(x => x(i))
  
  /**
   * late read i-th element 
   */
  override def apply(i: Future[Int]) = int.flatMap(x => x(i))
  
  /**
   * get length 
   */
  override def length = dim
  
}