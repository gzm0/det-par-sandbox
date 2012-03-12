import mvar.{ MPromise, MFuture }
import scala.util.{ Try, Success, Failure }

object MVarTest extends App {

  val x = new MPromise[Int]()
  
  val y = x map (_ + 2)
  
  y onComplete { (i,x) => println(i + " " + x) }
  
  x.tryComplete(0 :: Nil,Success(1))
  x.tryComplete(1 :: Nil,Success(4))
  x.tryComplete(2 :: Nil,Success(9))
  x.tryComplete(3 :: Nil,Success(2))
  
}