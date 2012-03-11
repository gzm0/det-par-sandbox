package mvar

import scala.util.{ Try, Success, Failure }
import scala.concurrent.resolver

trait MFuture[T] {

  def onComplete[U](func: (Int,Try[T]) => U): this.type
 
  def autoFlush_=(v: Boolean): Unit
  
  // What follows is stolen :D
  
  def map[S](f: T => S): MFuture[S] = {
    val p = new MPromise[S]()
    
    onComplete {
      case (i,Failure(t)) => p.tryComplete(i,Failure(t))
      case (i,Success(v)) =>
        try p.tryComplete(i,Success(f(v)))
        catch {
          case t => p.tryComplete(i,Failure(t))
        }
    }
    
    p.mfuture
  }
  
}