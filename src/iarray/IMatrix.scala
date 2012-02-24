package iarray

import scala.concurrent._
import scala.util._

/**
 * represents a 2-dimensional IArray
 * TODO how can we raise this to higher dimensions, w/o increasing
 * class hierarchy?
 */
class IMatrix[A](vals: IndexedSeq[IArrayLike[A]], innerDim: Int)
  extends IndexedSeq[IArrayLike[A]] {
  
  override def apply(i: Int) = vals(i)
  override def length = vals.length
  def apply(i: Future[Int]) =
    new IArrayProxy(i map {x => vals(x)}, innerDim)

}
