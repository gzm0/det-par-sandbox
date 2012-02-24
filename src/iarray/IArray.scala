package iarray

import scala.concurrent._
import scala.util._
import scala.collection.mutable.ArrayLike
import scala.collection.immutable.Vector

/**
 * IArray structure for distributed calculations
 */
class IArray[A](n1: Int) extends IArrayLike[A] {

  /**
   * promise for each element
   */
  private val proms = Array.fill(n1)(promise[A])
  
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
   * late read i-th element 
   */
  override def apply(i: Future[Int]) = i.flatMap(x => proms(x).future)
  
  /**
   * get length 
   */
  override def length = n1
  
}

object IArray {
  /**
   * Creates an -dimensional IArray
   */
  def ofDim [A] (n1: Int) =
    new IArray[A](n1)
  /**
   * Creates an 2-dimensional IArray
   */
  def ofDim [A] (n1: Int, n2: Int) =
    new IMatrix(Vector.fill(n1)(new IArray[A](n2)),n2)
}