import scala.concurrent._
import scala.util._

package object mvar {

  implicit def toMArray[A](s: IndexedSeq[MFuture[A]]) = new MArray[A](s)
  implicit def toStaticMArray[A](s: IndexedSeq[A]) =
    s map { x => toMFuture(future(x)) }
  implicit def toMFuture[A](x: Future[A]) = new FutureMFuture(x)
  
  // TODO change definition of while to have Future[Boolean] as result
  // TODO this should actually be on top of a Future...
  def iterate[T]
    (x: MFuture[T])
    (`while`: (Int,Try[T]) => Boolean)
    (update: (MFuture[T]) => MFuture[T]) = {
    
    val p_iter_start = new MPromise[T]()
    val p_iter_end   = update(p_iter_start.mfuture)
    val p_end  = new MPromise[T]()
    
    
    def fiter(x: MFuture[T]) = {
      x onComplete {
        case (i,tr) if `while`(i+1,tr) => p_iter_start.complete(i+1, tr)
        case (i,tr) => p_end.complete(i+1,tr)
      }
    }

    fiter(x)
    fiter(p_iter_end)
    
    p_end.mfuture
    
  }
  
}