package iarray

import scala.util._
import scala.concurrent._

class FTry[A](v: Future[A]) {
  
  def fcatch(catcher: PartialFunction[Throwable, A]): Future[A] = {
    val p = v.newPromise[A]
    v.onComplete { res => p.complete(completeHandler(catcher,res)) }
    p.future
  }
  
  private def completeHandler[U](
      catcher: PartialFunction[Throwable, A],
      res: Try[A]): Try[A] =
    res match {
      case Failure(t) if catcher.isDefinedAt(t) =>
        try { Success(catcher(t)) }
        catch { case ex => Failure(ex) }
      case _ => res
    }
  
}
