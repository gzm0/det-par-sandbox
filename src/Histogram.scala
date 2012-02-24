import scala.util._

object Histogram extends App {

  val nr = 100
  val nc = 10
  val values = new IArray[Int](nr)
  val hist = new IArray[Int](nc)
  
  for (i <- 0 to nr-1)
    values(i) = (i + 3) * i % nc
  
  for (i <- 0 to nc-1)
    hist(i) = values.compact.map(_.filter(_ == i).length)
    
  hist.compact onSuccess { 
    case l: IndexedSeq[Int] => {
      for ((el,i) <- l.zipWithIndex) println("i: " + el)
      sys.exit(0)
    }
  } 
  
}