package mvar

class DUsingHolder[T](x: MFuture[T], c: DWhileCond[T]) {
  def over(u: DWhileUpdate[T]) = iterate(x)(c)(u)
}
