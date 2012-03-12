package mvar

class MMatrix[A](val x: IndexedSeq[IndexedSeq[MFuture[A]]]) 
  extends IndexedSeq[MArray[MFuture[A]]] {

  override def length = x.length
  override def apply(idx: Int) = toMArray(x(idx))
  
  def *(y: MMatrix[A])(implicit a: Arithmetic[A]) = {
    transpose map { col =>
      y.x map { row => col * row }
    }
  }
  
  def transpose = x.transpose
  
}

object MMatrix {
  def ofDim[A](n1: Int, n2: Int) = IndexedSeq.fill(n2,n1)(new MPromise[A])
}