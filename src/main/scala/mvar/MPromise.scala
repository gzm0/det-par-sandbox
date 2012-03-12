package mvar

import scala.util.{ Try, Success, Failure }
import scala.collection.mutable._

// TODO this is not threadsafe
class MPromise[T] extends MFuture[T] {

  val listeners: Set[(MFuture.Index,Try[T]) => Any] = Set.empty
  val vals: Map[MFuture.Index,Try[T]] = Map.empty
  var aflush = false
  
  def tryComplete(v: MFuture.Index, result: Try[T]) = {
    val ok = !vals.contains(v)
    if (ok && !aflush) vals(v) = result
    listeners foreach { f => f(v,result) }
    ok
  }
  
  def mfuture = this
  
  def onComplete[U](func: (MFuture.Index,Try[T]) => U) = {
    listeners += func
    vals.foreach(Function.tupled(func))
    this
  }
  
  def autoFlush_=(v: Boolean) = {
    aflush = v
    if (v) vals.empty
  }
  
  // What follows is stolen
  
  def success(i: MFuture.Index, v: T): this.type = if (trySuccess(i,v)) this else throwCompleted(i)

  def trySuccess(i: MFuture.Index, value: T): Boolean = tryComplete(i,Success(value))

  def failure(i: MFuture.Index, t: Throwable): this.type = if (tryFailure(i,t)) this else throwCompleted(i)

  def tryFailure(i: MFuture.Index, t: Throwable): Boolean = tryComplete(i,Failure(t))
  
  def complete(i: MFuture.Index, result:Try[T]): this.type = if (tryComplete(i,result)) this else throwCompleted(i)
  
  private def throwCompleted(i: MFuture.Index) =
    throw new IllegalStateException("Promise already completed at index %s.".format(i))
  
}