package mvar

import scala.util.{ Try, Success, Failure }
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
  
  // What follows is stolen
  
  def success(i: Int, v: T): this.type = if (trySuccess(i,v)) this else throwCompleted

  def trySuccess(i: Int, value: T): Boolean = tryComplete(i,Success(value))

  def failure(i: Int, t: Throwable): this.type = if (tryFailure(i,t)) this else throwCompleted

  def tryFailure(i: Int, t: Throwable): Boolean = tryComplete(i,Failure(t))
  
  private def throwCompleted =
    throw new IllegalStateException("Promise already completed.")
  
}