import scala.concurrent._

package object iarray {

  implicit def valToFut[A](x: A): Future[A] = {
    val p = promise[A]
    p.success(x)
    p.future
  }
  
  def ftry[A](v: Future[A]) = new FTry(v)
  
}