package collections.distributed

import scala.concurrent.Future
import scala.collection.GenSeq

/**
 * a lot of functions that use other FutureIndexedSeqs
 * (N.B. ++, ++:, flatMap, flatten) 
 * require a complex referential inner data structure, if we do not implement
 * a FutureIndexedSeq[A] as a IndexedSeq[Future[A]]. How can we handle this?
 * 
 * Have a look at scala.collection.mutable.IndexedSeqView 
 */
trait FutureIndexedSeq[+A] extends PartialFunction[Int,Future[A]] {

  /**
   * explicitly accesses an element of the collection.
   * to be used rarely as it spawns a new future 
   */
  def apply(idx: Int): Future[A]
  
  /**
   * length of this IndexedSeq
   * this is not a Future[Int], as we should always be aware of the length at 
   * object construction (through concatenation)
   */
  def length: Int  
  
  /**
   * concatenation (use case only)
   * TODO create methods for general kind of collections 
   */
  def ++[B >: A](that: TraversableOnce[B]): FutureIndexedSeq[B]
  def ++:[B >: A](that: TraversableOnce[B]): FutureIndexedSeq[B]
  
  /**
   * prepend & append
   * TODO create methdos for general kind of collections
   */
  def +:(elem: A): FutureIndexedSeq[A]
  
  /**
   * late prepend/append (use rarely, future injection)
   */
  def +:(felem: Future[A]): FutureIndexedSeq[A]
  
  def /:  = foldLeft _
  def /:\ = fold _ 
  
  def :+(elem: A): FutureIndexedSeq[A]
  def :+(felem: Future[A]): FutureIndexedSeq[A]

  def :\ = foldRight _		
  
  // Missing: addString
  
  /**
   * non-lazy aggregate 
   */
  def aggregate[B](z: B)(seqop: (B,A) => B, combop: (B,B) => B): Future[B]
  
  /**
   * lazy aggregate (use if seqop,combop vary a lot in time)
   * TBD: how to express such fcts?
   */
  def aggregate[B](z: Future[B])(seqop: (Future[B],Future[A]) => Future[B],
      				             combop: (Future[B],Future[B]) => Future[B]): Future[B]
  
  // -> andThen (from Function1)
      				             
  def canEqual(that: Any) = true
  
  /**
   * use case only
   */
  def collect[B](pf: PartialFunction[A,B]): FutureIndexedSeq[B]
  
  /**
   * use case only
   * note that the PF needs to be able to determine whether an element
   * belongs to its domain immediately to allow for memalloc
   */
  def collect[B](pf: PartialFunction[A,Future[B]]): FutureIndexedSeq[B]
  
  /**
   * waits for head of list to evaluate pf condition,
   * TBD: do we need lazy PFs?
   */
  def collectFirst[B](pf: PartialFunction[A,B]): Future[Option[B]]
  def collectFirst[B](pf: PartialFunction[A,Future[B]]): Future[Option[B]] 
  
  /**
   * TODO: do we need that, can we implement it efficiently?
   * Certain: do not return iterator, as we cannot work parallel on it
   */
  def combinations(n: Int): Unit
  
  // Missing: companion
  
  // -> compose (from Function1)
  
  /**
   * checks if this fiseq contains a given element
   */
  def contains(elem: Any): Future[Boolean]
  
  def containsSlice[B](that: GenSeq[B]): Future[Boolean]
  def containsSlice[B](that: FutureIndexedSeq[B]): Future[Boolean]
  
  // Missing: Copy to Array/Buffer stuff
  
  def corresponds[B](that: GenSeq[B])(p: (A, B) => Boolean): Future[Boolean] 
  def corresponds[B](that: FutureIndexedSeq[B])
                (p: (Future[A], Future[B]) => Future[Boolean]): Future[Boolean]
  
  /**
   * counts elements
   * note: does not need lazy fct, as it is sufficient to
   * execute it once we know the element since there is nothing more to know.
   */
  def count(p: A => Boolean): Future[Int]
  
  // Missing: diff // what is the exact semantics of that w.r.t. ordering?
  
  /**
   * note: we need not to know the whole sequence for that.
   * TODO how to implement efficiently
   */
  def distinct: FutureIndexedSeq[A]

  /**
   * drops first n (straightforward)
   */
  def drop(n: Int): FutureIndexedSeq[A]
  
  /**
   * drops last n (straightforward)
   */
  def dropRight(n: Int): FutureIndexedSeq[A]
  
  /**
   * drops all first true (straightforward)
   */
  def dropWhile(p: A => Boolean): FutureIndexedSeq[A]
  
  /**
   * checks for suffix
   */
  def endsWith[B](that: GenSeq[B]): Future[Boolean]
  
  /**
   * checks for suffix (w/ quick reject)
   */
  def endsWith[B](that: FutureIndexedSeq[B]): Future[Boolean]
  
  /**
   * TODO what should the semantics of this be?
   */
  def equals(that: Any): Boolean
  
  /**
   * tests for predicate
   */
  def exists(p: A => Boolean): Future[Boolean]
  
  /**
   * filters
   */
  def filter(p: A => Boolean): FutureIndexedSeq[A]
  
  /**
   * filters
   */
  def filterNot(p: A => Boolean): FutureIndexedSeq[A]
  
  /**
   * find (first element)
   */
  def findFirst(p: A => Boolean): Future[Option[A]]
  
  /**
   * find (any element)
   */
  def findAny(p: A => Boolean): Future[Option[A]]
  
  /**
   * flatMap (use case only)
   */
  def flatMap [B] (f: (A) => FutureIndexedSeq[B]): FutureIndexedSeq[B]
  
  /**
   * (use case only)
   */
  def flatten[B]: FutureIndexedSeq[B]
  
  /**
   * _out of order_ fold, non-lazy
   */
  def fold[A1 >: A](z: Future[A1])(op: (A1, A1) ⇒ A1): Future[A1]
  
  /**
   * _out of order_ fold, lazy
   * use if execution time of op varies a lot / is big
   */
  def fold[A1 >: A](z: Future[A1])
                   (op: (Future[A1], Future[A1]) => Future[A1]): Future[A1]
  
  def foldLeft[B](z: Future[B])(op: (B, A) ⇒ B): Future[B]
  def foldLeft[B](z: Future[B])(op:(Future[B],Future[A])=>Future[B]):Future[B]
  def foldRight[B](z: Future[B])(op: (A, B) ⇒ B): Future[B]
  def foldRight[B](z: Future[B])(op:(Future[A],Future[B])=>Future[B]):Future[B]
  
  def forall(p: A => Boolean): Future[Boolean]
  
  // Missing: foreach // side effects are dangerous! Do not allow them! (TBD)
  // Missing: GenericBuilder
  // Missing: groupBy // TODO have to thing about this
  
  /**
   * since the size is known, this is easy
   */
  def grouped(size: Int): Iterator[FutureIndexedSeq[A]]
  
  def hasDefiniteSize = true
  
  /**
   * TODO semantics? (same as for equals)
   */
  def hashCode(): Int
  
  def head = apply(0)
  def headOption = if (length > 0) Some(apply(0)) else None
  
  /**
   * indexOf Fcts
   */
  def firstIndexOf[B >: A](elem: B, from: Int): Future[Int]
  def anyIndexOf[B >: A](elem: B, from: Int): Future[Int]
  
  def firstIndexOfSlice[B >: A](that: GenSeq[B], from: Int): Future[Int]
  def anyIndexOfSlice[B >: A](that: GenSeq[B], from: Int): Future[Int]
  
  def firstIndexOfSlice[B >: A](that: FutureIndexedSeq[B],from: Int):Future[Int]
  def anyIndexOfSlice[B >: A](that: FutureIndexedSeq[B], from: Int):Future[Int]
  
  def firstIndexWhere(p: A => Boolean, from: Int): Future[Int]
  def anyIndexWhere(p: A => Boolean, from: Int): Future[Int]
  
  /**
   * trivial, woot!
   */
  def indices = new Range(0,length,1)
  
  def init: FutureIndexedSeq[A]
  def inits: Iterator[FutureIndexedSeq[A]]
  
  /**
   * needed for PartialFunction
   */
  def isDefinedAt(x: Int): Boolean =
    x >= 0 && length > x
  
  
}
