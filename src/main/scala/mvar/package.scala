import scala.concurrent._
import scala.util._
import scala.PartialFunction

package object mvar {

  type DWhileCond[T]   = MFuture[T] => MFuture[Boolean]
  type DWhileUpdate[T] = MFuture[T] => MFuture[T]
  
  implicit def toMArray[A](s: IndexedSeq[MFuture[A]]) = new MArray[A](s)
  implicit def toMMatrix[A](s: IndexedSeq[IndexedSeq[MFuture[A]]]) = new MMatrix[A](s)
  implicit def toStaticMArray[A](s: IndexedSeq[A]) =
    s map { x => toMFuture(future(x)) }
  implicit def toMFuture[A](x: Future[A]) = new FutureMFuture(x)
  
  def dwhile[T](cond: DWhileCond[T]) = new DWhileHolder(cond)
  
  def iterate[T]
    (x: MFuture[T])
    (cond: DWhileCond[T])
    (update: DWhileUpdate[T]) = {
    
    val iter_in   = new MPromise[T]
    val iter_cond = cond(iter_in)
    val iter_go   = new MPromise[T]
    val iter_end  = update(iter_go)
    val end       = new MPromise[T]
    
    
    x.onComplete((is,v) => iter_in.complete(0 :: is, v))
    iter_end.onComplete(iter_in.complete _)
    
    // TODO need to handle failures differently to recover gracefully in loop
    // if possible
    (iter_in zip iter_cond) onComplete {
      case (i :: is, Failure(t))          => end.failure(is,t)
      case (i :: is, Success((v, true ))) => iter_go.success(i+1 :: is, v)
      case (i :: is, Success((v, false))) => end.success(is, v)
      case (Nil,_) =>
        throw new IllegalStateException("Index cannot be Nil inside iteration")
    }
    
    iter_in   onSuccess shdebug("in")
    iter_cond onSuccess shdebug("cond")
    iter_go   onSuccess shdebug("go")
    iter_end  onSuccess shdebug("end")
    
    end
    
  }
  
  def iterate[A,B,C]
    (x1: MFuture[A], x2: MFuture[B], x3: MFuture[C])
    (cond: (MFuture[A],MFuture[B],MFuture[C]) => MFuture[Boolean])
    (update: (MFuture[A],MFuture[B],MFuture[C]) =>
             (MFuture[A],MFuture[B],MFuture[C])) = {
    
    val iter_in1   = new MPromise[A]
    val iter_in2   = new MPromise[B]
    val iter_in3   = new MPromise[C]
    
    val iter_cond = cond(iter_in1,iter_in2,iter_in3)
    
    val iter_go1   = new MPromise[A]
    val iter_go2   = new MPromise[B]
    val iter_go3   = new MPromise[C]
    
    val iter_end1  = update(iter_go)
    val iter_end2  = update(iter_go)
    val iter_end3  = update(iter_go)
    
    val end1 = new MPromise[T]
    val end2 = new MPromise[T]
    val end3 = new MPromise[T]
    
    val end       = new MPromise[T]
    
    
    x1.onComplete((is,v) => iter_in1.complete(0 :: is, v))
    iter_end1.onComplete(iter_in1.complete _)
    
    x2.onComplete((is,v) => iter_in2.complete(0 :: is, v))
    iter_end2.onComplete(iter_in2.complete _)
    
    x3.onComplete((is,v) => iter_in3.complete(0 :: is, v))
    iter_end3.onComplete(iter_in3.complete _)
    
    // TODO need to handle failures differently to recover gracefully in loop
    // if possible
    (iter_in zip iter_cond) onComplete {
      case (i :: is, Failure(t))          => end.failure(is,t)
      case (i :: is, Success((v, true ))) => iter_go.success(i+1 :: is, v)
      case (i :: is, Success((v, false))) => end.success(is, v)
      case (Nil,_) =>
        throw new IllegalStateException("Index cannot be Nil inside iteration")
    }
    
    iter_in   onSuccess shdebug("in")
    iter_cond onSuccess shdebug("cond")
    iter_go   onSuccess shdebug("go")
    iter_end  onSuccess shdebug("end")
    
    end
    
  }
  
  def max_iter[T](i: Int): DWhileCond[T] =
    (v: MFuture[T]) => v.mapi((is,v) => is.head < i)    
  
  def shdebug[T](varn: String): PartialFunction[(MFuture.Index,T),Unit] = {
    case (is,v) => println("%-4s @ %s : %s".format(varn,is.toString,v.toString))
  }
  
  /*
  // TODO support multiple iterations
  def iterate[T]
    (x: MFuture[T])
    (cond: MFuture[T] => MFuture[Boolean])
    (update: (MFuture[T]) => MFuture[T]) = {
    
    val p_iter_start = new MPromise[T]
    val p_iter_end   = update(p_iter_start.mfuture)
    val p_end  = new MPromise[T]
    
    (p_iter_end zip cond(p_iter_end)) onComplete {
      case (i,Success((xv,true)) ) => p_iter_start.success(i+1,xv)
      case (i,Success((xv,false))) => p_end.success(0,xv)
      case (i,Failure(t)       )   => p_end.failure(0,t)
    }
    
    // TODO cond has wrong iteration number
    (x zip cond(x)) onComplete {
      case (i,Success((xi,true ))) => p_iter_start.success(0,xi)
      case (i,Success((xi,false))) => p_end.success(0,xi)
      case (i,Failure(t)       )   => p_end.failure(0,t)
    }
    
    p_end.mfuture
    
  }*/
  
  /*
  def iterate[T,U]
    (x: MFuture[T], y: MFuture[U])
    (cond: (Int,MFuture[T],MFuture[U]) => MFuture[Boolean])
    (update: (MFuture[T],MFuture[U]) => (MFuture[T],MFuture[U])) = {
    
    val p_iter_start = new MPromise[(T,U)]()
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
  */
  
}