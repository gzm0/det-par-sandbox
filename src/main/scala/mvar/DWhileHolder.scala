package mvar

class DWhileHolder[T](c: DWhileCond[T]) {
  def using(x: MFuture[T]) = new DUsingHolder(x,c)
}