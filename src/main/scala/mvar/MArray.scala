package mvar

class MArray[A](val x: IndexedSeq[MFuture[A]])
 extends IndexedSeq[MFuture[A]]{

  override def apply(idx: Int) = x.apply(idx)
  override def length = x.length
  
  def dot[B,C](mul: (A,B) => C, add: (C,C) => C)(y: MArray[B]) = {
    def fmul = (x: MFuture[A], y: MFuture[B]) => (x zip y) map mul.tupled
    def fadd = (x: MFuture[C], y: MFuture[C]) => (x zip y) map add.tupled
    
    val prods = (x zip y) map fmul.tupled
    prods reduce fadd
  }
  
}
