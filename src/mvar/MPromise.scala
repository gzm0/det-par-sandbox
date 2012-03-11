package mvar

import scala.util.Try
import scala.collection.mutable._

// TODO this is not threadsafe
class MPromise[T] extends MFuture[T] {

  val listeners: Set[(Int,Try[T]) => Any] = Set.empty
  val vals: Map[Int,Try[T]] = Map.empty
  var aflush = false
  
  def tryComplete(v: Int, result: Try[T]) = {
    val ok = !vals.contains(v)
    if (ok && !aflush) vals(v) = result
    listeners foreach { f => f(v,result) }
    ok
  }
  
  def mfuture = this
  
  def onComplete[U](func: (Int,Try[T]) => U) = {
    listeners += func
    vals.foreach(Function.tupled(func))
    this
  }
  
  def autoFlush_=(v: Boolean) = {
    aflush = v
    if (v) vals.empty
  }
  
}