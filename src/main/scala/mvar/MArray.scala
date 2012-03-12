package mvar

class MArray[A](val x: IndexedSeq[MFuture[A]])
 extends IndexedSeq[MFuture[A]] {

  override def apply(idx: Int) = x.apply(idx)
  override def length = x.length
  
  def *(y: MArray[A])(implicit a: Arithmetic[A]) = {
    def fmul = (x: MFuture[A], y: MFuture[A]) => (x zip y) map a.mul.tupled
    def fadd = (x: MFuture[A], y: MFuture[A]) => (x zip y) map a.sum.tupled
    
    assert (x.length == y.length)
    
    val prods = (x zip y) map fmul.tupled
    prods reduce fadd
  }
  
  def +(y: MArray[A])(implicit a: Arithmetic[A]) = {
    def fadd = (x: MFuture[A], y: MFuture[A]) => (x zip y) map a.sum.tupled
    
    assert (x.length == y.length)
    (x zip y) map fadd.tupled
  }

}

class Arithmetic[A](val mul: (A,A) => A, val sum: (A,A) => A) 

object MArray {
  def ofDim[A](n: Int) = IndexedSeq.fill(n)(new MPromise[A]())
}