package mvar

import scala.util.{ Try, Success, Failure }
import scala.concurrent._

trait MFuture[T] {

  def onComplete[U](func: (Int,Try[T]) => U): this.type
 
  def autoFlush_=(v: Boolean): Unit
  
  // TODO change definition of while to have Future[Boolean] as result
  // TODO this should actually be on top of a Future...
  def iter(`while`: (Int,Try[T]) => Boolean)
          (update: MFuture[T] => MFuture[T]) = {
    val p_iter_start = new MPromise[T]()
    val p_iter_end   = update(p_iter_start.mfuture)
    val p_end  = new MPromise[T]()
    
    def fiter(x: MFuture[T]) = {
      x onComplete {
        case (i,tr) if `while`(i+1,tr) => p_iter_start.complete(i+1, tr)
        case (i,tr) => p_end.complete(i+1,tr)
      }
    }

    fiter(this)
    fiter(p_iter_end)
    
    p_end.mfuture
    
  }
  
  // What follows is stolen :D
  
  def map[S](f: T => S): MFuture[S] = {
    val p = new MPromise[S]()
    
    onComplete {
      case (i,Failure(t)) => p.failure(i,t)
      case (i,Success(v)) =>
        try p.success(i,f(v))
        catch {
          case t => p.failure(i,t)
        }
    }
    
    p.mfuture
  }
  
  def zip[U](that: MFuture[U]): MFuture[(T, U)] = {
    val p = new MPromise[(T, U)]
    
    this onComplete {
      case (i,Failure(t))  => p.failure(i,t)
      case (i,Success(r)) => that onSuccess {
        case (i2,r2) if i == i2 => p.success(i2,(r,r2))
      }
    }
    
    that onFailure {
      case (i,f) => p.failure(i,f)
    }
    
    p.mfuture
  }
  
  def onSuccess[U](pf: PartialFunction[(Int,T), U]): this.type = onComplete {
    case (i,Failure(t)) => // do nothing
    case (i,Success(v)) => if (pf isDefinedAt (i,v)) pf(i,v) else { /*do nothing*/ }
  }

  def onFailure[U](callback: PartialFunction[(Int,Throwable), U]): this.type = onComplete {
    case (i,Failure(t)) => if (callback.isDefinedAt(i,t)) callback(i,t) else { /*do nothing*/ }
    case (i,Success(v)) => // do nothing
  }
  
}

private[mvar] class FutureMFuture[T](f: Future[T]) extends MFuture[T] {
  def onComplete[U](func: (Int,Try[T]) => U) = {
    f onComplete (func.curried(0))
    this
  }
  def autoFlush_=(v: Boolean) = {}
}