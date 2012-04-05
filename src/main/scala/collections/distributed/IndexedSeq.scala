package collections.distributed

import scala.concurrent.Future
import scala.collection.GenSeq

/**
 * a lot of functions that use other FutureIndexedSeqs
 * (N.B. ++, ++:, flatMap, flatten) 
 * require a complex referential inner data structure, if we do not implement
 * a FutureIndexedSeq[A] as a IndexedSeq[Future[A]]. How can we handle this?
 * --> Have a look at scala.collection.mutable.IndexedSeqView
 *
 * TBD: how to handle indices? Do we allow for "lazy" indices
 * 
 * TBD: How do we handle sizes? Do we allow for structures where the size
 * is not yet known? (look at dropWhile, partition, takeWhile)
 *
 * TBD: how to handle builders and multi dimensionality. Look at
 * transpose. Raises also issue of size.
 *
 * TBD: FutureTuple? Difference between Future[(A,B)],(Future[A],Future[B]). Especially w.r.t. zip/zip3
 *
 * TBD: Numeric: is this associative/commutative?
 * 
 * TBD: how to express Futured functions? (see aggregate)
 *
 * TBD: side effects?
 *
 * TBD: iterator?
 *
 * TBD: FutureOrdering?
 *
 * TBD: equals
 * 
 * TODO: combinations?
 * TODO: diff?
 * TODO: scan?
 * TODO: intersect?
 * TODO: put all the CanBuildFrom again
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
   * prepend
   * TODO create methdos for general kind of collections
   * use case only
   */
  def +:[B >: A](elem: B): FutureIndexedSeq[B]
  
  /**
   * late prepend (use rarely, future injection)
   * use case only
   */
  def +:[B >: A](felem: Future[B]): FutureIndexedSeq[B]
  
  // Missing: /:
  // Missing: /:\
  
  /**
   * append use case only
   */
  def :+[B >: A](elem: B): FutureIndexedSeq[B]
  /**
   * late append use case only
   */
  def :+[B >: A](felem: Future[B]): FutureIndexedSeq[B]

  // Missing: :\
  
  // Missing: addString // TODO do we need this?
  
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
  def collectF[B](pf: PartialFunction[A,Future[B]]): FutureIndexedSeq[B]
  
  /**
   * waits for head of list to evaluate pf condition,
   */
  def collectFirst[B](pf: PartialFunction[A,B]): Future[Option[B]]
  def collectAny[B](pf: PartialFunction[A,B]): Future[Option[B]]
  
  /**
   * TODO: do we need that, can we implement it efficiently?
   * Certain: do not return iterator, as we cannot work parallel on it
   */
  def combinations(n: Int): Unit
  
  // Missing: companion // TODO what is this?
  
  // -> compose (from Function1)
  
  /**
   * checks if this fiseq contains a given element
   */
  def contains(elem: Any): Future[Boolean]
  
  def containsSlice[B](that: GenSeq[B]): Future[Boolean]
  def containsSlice[B](that: FutureIndexedSeq[B]): Future[Boolean]
  
  // Missing: Copy to Array/Buffer stuff // Need to define other types first
  
  def corresponds[B](that: GenSeq[B])(p: (A, B) => Boolean): Future[Boolean] 
  def corresponds[B](that: FutureIndexedSeq[B])
                (p: (Future[A], Future[B]) => Future[Boolean]): Future[Boolean]
  
  /**
   * counts elements
   * note: does not need lazy fct, as it is sufficient to
   * execute it once we know the element since there is nothing more to know.
   */
  def count(p: A => Boolean): Future[Int]
  
  // Missing: diff // TODO what is the exact semantics of that w.r.t. ordering?
  
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
   * drops all first true
   * TODO: unknown length type!
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
   * TODO: all those have the size issue!!
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
  def foldF[A1 >: A](z: Future[A1])
                   (op: (Future[A1], Future[A1]) => Future[A1]): Future[A1]
  
  def foldLeft[B](z: Future[B])(op: (B, A) ⇒ B): Future[B]
  def foldLeftF[B](z: Future[B])(op:(Future[B],Future[A])=>Future[B]):Future[B]
  def foldRight[B](z: Future[B])(op: (A, B) ⇒ B): Future[B]
  def foldRightF[B](z: Future[B])(op:(Future[A],Future[B])=>Future[B]):Future[B]
  
  def forall(p: A => Boolean): Future[Boolean]
  
  // Missing: foreach // side effects are dangerous! Do not allow them! (TBD)
  // Missing: GenericBuilder
  // Missing: groupBy // TODO have to think about this w.r.t. types
  
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
   * trivial
   */
  def indices = new Range(0,length,1)
  
  def init: FutureIndexedSeq[A]
  def inits: Iterator[FutureIndexedSeq[A]]
  
  // Missing: intersect // TODO semantics?
  
  /**
   * needed for PartialFunction
   */
  def isDefinedAt(x: Int): Boolean = x >= 0 && length > x
  
  def isEmpty = length == 0
  def isTraversableAgain: Boolean
  
  // Missing: iterator // does not make sense TBD
  
  def last = apply(length-1)

  /**
   * indexOf Fcts
   */
  def lastIndexOf[B >: A](elem: B, from: Int): Future[Int]
  def lastIndexOfSlice[B >: A](that: GenSeq[B], from: Int): Future[Int]
  def lastIndexOfSlice[B >: A](that: FutureIndexedSeq[B],from: Int):Future[Int]
  def lastIndexWhere(p: A => Boolean, from: Int): Future[Int]

  def lastOption = if (length > 0) Some(last) else None

  def lengthCompare(len: Int): Int

  // -> lift (from PartialFunction)  

  /**
   * the famouse map (use case only)
   */
  def map[B](f: A => B): FutureIndexedSeq[B]

  def max[B >: A](implicit cmp: Ordering[B]): Future[A]
  def min[B >: A](implicit cmp: Ordering[B]): Future[A]
  def maxBy[B](f: A => B)(implicit cmp: Ordering[B]): Future[A]
  def minBy[B](f: A => B)(implicit cmp: Ordering[B]): Future[A]

  def mkString: Future[String]
  def mkString (sep: String): Future[String]
  def mkString (start: String, sep: String, end: String): Future[String]

  def nonEmpty = length > 0

  def padTo[B >: A](len: Int, elem: B): FutureIndexedSeq[B]
  def padTo[B >: A](len: Int, elem: Future[B]): FutureIndexedSeq[B]
  
  // Missing: par // Does not make sense
  
  /**
   * partitions this FutureIndexedSeq according to a predicate
   * TODO we need another future-type for the result, as the lengths are not
   * known
   */
  def partition(p: A => Boolean): (Seq[A],Seq[A])

  // Missing: patch // TODO, see diff

  def permutations: Iterator[FutureIndexedSeq[A]]

  def prefixLength(p: A => Boolean): Future[Int]

  /**
   * TODO Numeric[B] does not define the associativity property of the
   * underlying type. (can we assume it as we are probably working
   * with a field?)
   * does reduce guarantee order preservation? otherwise we'll have to
   * look at commutativity, too
   */
  def product[B >: A](implicit num: Numeric[B]): B //= fold(num.one)(num.times(_,_))

  def reduce[A1 >: A](op: (A1, A1) => A1): Future[A1]
  def reduceF[A1 >: A](op: (Future[A1], Future[A1]) => Future[A1]): Future[A1]
  
  def reduceLeft[B >: A](op: (B, A) => B): Future[B]
  def reduceLeftF[B >: A](op: (Future[B], Future[A]) => Future[B]): Future[B]

  /**
   * note: as the length is known, we can return Option[Future[B]] and
   * not Future[Option[B]]
   */
  def reduceLeftOption[B >: A](op: (B, A) => B): Option[Future[B]] =
    if (length > 0) Some(reduceLeft(op))
    else None
  def reduceLeftOptionF[B >: A](op: (Future[B], Future[A]) => Future[B]): Option[Future[B]] =
    if (length > 0) Some(reduceLeftF(op))
    else None

  def reduceOption[A1 >: A](op: (A1, A1) => A1): Option[Future[A1]] =
    if (length > 0) Some(reduce(op))
    else None
  def reduceOptionF[A1 >: A](op: (Future[A1], Future[A1]) => Future[A1]): Option[Future[A1]] = 
    if (length > 0) Some(reduceF(op))
    else None

  def reduceRight[B >: A](op: (A, B) => B): Future[B]
  def reduceRightF[B >: A](op: (Future[A], Future[B]) => Future[B]): Future[B]

  def reduceRightOption[B >: A](op: (A, B) => B): Option[Future[B]]
  def reduceRightOptionF[B >: A](op: (Future[A], Future[B]) => Future[B]): Option[Future[B]]
  
  /**
   * TODO what is this needed for, why should we want to change this?
   */
  def repr = this

  def reverse: FutureIndexedSeq[A]

  // Missing: reverseIterator // see iterator

  /**
   * reverse map (use case only)
   */
  def reverseMap[B](f: A => B): FutureIndexedSeq[B]

  /**
   * checks for same elements (non-lazy)
   */
  def sameElements[B >: A](that: IndexedSeq[B]): Future[Boolean]

  /**
   * lazy version
   */
  def sameElements[B >: A](that: FutureIndexedSeq[B]): Future[Boolean]

  /**
   * scan. TODO what is the semantics of that?
   * TODO, handle CBFs
   * TODO scanLeft, scanRight
   */
  def scan[B >: A, That](z: B)(op: (B, B) ⇒ B): FutureIndexedSeq[B] 

  def segmentLength(p: A => Boolean, start: Int): Future[Int]

  // Missing: seq // Does not make sense

  def size = length

  def slice(from: Int, until: Int): FutureIndexedSeq[A]
  
  def sliding(size: Int, step: Int): Iterator[FutureIndexedSeq[A]]
  def sliding(size: Int): Iterator[FutureIndexedSeq[A]]

  /**
   * note that sort REQUIRES packing
   * except if we define a FutureOrdering[_]
   * TBD
   */
  def sortBy[B](f: A => B): Future[IndexedSeq[A]]
  def sortWith(lt: (A,A) => Boolean): Future[IndexedSeq[A]]
  def sortWith(lt: (Future[A],Future[A]) => Future[Boolean]): FutureIndexedSeq[A]
  def sorted: IndexedSeq[A]

  /**
   * TODO find other datatype for return (see partition)
   */
  def span(p: A => Boolean): (Seq[A],Seq[A])

  def splitAt(n: Int): (FutureIndexedSeq[A],FutureIndexedSeq[A])

  def startsWith[B](that: Seq[B], offset: Int): Future[Boolean]
  def startsWith[B](that: FutureIndexedSeq[B], offset: Int): Future[Boolean]

  // Missing: String Prefix // does not make sense (different toString)

  def sum[B >: A](implicit num: Numeric[B]): B //= fold(num.zero)(num.add(_,_))

  def tail: FutureIndexedSeq[A]
  def tails: Iterator[FutureIndexedSeq[A]]

  def take(n: Int): FutureIndexedSeq[A]
  def takeRight (n: Int): FutureIndexedSeq[A]
  /**
   * TODO: unknown length type
   */
  def takeWhile (p: A => Boolean): FutureIndexedSeq[A]
  
  // Missing: to_ Stuff

  /**
   * TODO this can be done, but we need a builder since we need to
   * swap types
   */
  def transpose: Traversable[FutureIndexedSeq[A]]

  def union[B >: A](that: IndexedSeq[B]): FutureIndexedSeq[B]
  def union[B >: A](that: FutureIndexedSeq[B]): FutureIndexedSeq[B]

  /**
   * TODO how to handle pairs of futures and futures of pairs, etc.
   */
  def unzip[A1, A2](implicit asPair: (A) => (A1, A2)): (FutureIndexedSeq[A1], FutureIndexedSeq[A2])
  def unzip3[A1, A2, A3](implicit asTriple: (A) => (A1, A2, A3)): (FutureIndexedSeq[A1], FutureIndexedSeq[A2],  FutureIndexedSeq[A2])

  def updated[B >: A](index: Int, elem: B): FutureIndexedSeq[B]
  def updated[B >: A](index: Int, elem: Future[B]): FutureIndexedSeq[B]
  
  // Missing: view // same thing as slice!

  // Missing: withFilter // same thing as filter!

  /**
   * handle Builder and Tuple issues!
   */
  def zip: Unit

  /**
   * zipWithIndex has the typical tuple issue:
   * we want FutureIndexedSeq[(Int,A)], but actually the index is
   * available from the start so more sth like
   * IndexedSeq[(Int,Future[A])], but we do not want this because of
   * the futures
   */
  def zipWithIndex: Unit

}
