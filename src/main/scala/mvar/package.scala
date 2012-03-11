import scala.concurrent._

package object mvar {

  implicit def toMArray[A](s: IndexedSeq[MFuture[A]]) = new MArray[A](s)
  implicit def toStaticMArray[A](s: IndexedSeq[A]) =
    s map { x => toMFuture(future(x)) }
  implicit def toMFuture[A](x: Future[A]) = new FutureMFuture(x)
  
}