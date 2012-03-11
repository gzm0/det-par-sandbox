import scala.util._
import scala.concurrent._
import iarray._

object ExceptionHandling extends App {

  val p = promise[Int]
  val fut = p.future
  
  val m = ftry { fut } fcatch { case _ => 1 }
  
  p.failure(new Exception())
  
  m.onSuccess { 
    case v => println(v)
  }
  
}