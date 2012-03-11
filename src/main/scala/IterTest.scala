import mvar._

object IterTest extends App {

  val n = 100
  val its = 1000
  
  // This is my huge data array and which I want to iterate on
  val x = new MPromise[Int]()
  
  // Now define our iteration
  val result = x.iter((i,ign) => i < 1000) { x =>
    x map (_ + 1)
  }
  
  // Now initialize x
  x.success(0,1)
  
  result onSuccess {
    case (i,s) => println("%d: %s".format(i,s))
  }
  
}