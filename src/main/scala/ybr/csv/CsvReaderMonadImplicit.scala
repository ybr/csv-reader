package ybr.csv

import scalaz.Monad

/**
 * CsvReader instance for the `scalaz.Monad` type class.
 */
package object CsvReaderMonadImplicit {
  implicit val CsvReaderMonad = new Monad[CsvReader] {
    def point[A](a: => A) = CsvReader(a)

    def bind[A, B](fa: CsvReader[A])(f: A => CsvReader[B]): CsvReader[B] = fa.flatMap(f)

    override def map[A, B](fa: CsvReader[A])(f: A => B): CsvReader[B] = fa.map(f)

    /*
     * Provides expected Applicative behaviour to this Monad, appends CsvErrors.
     */
    override def ap[A, B](fa: => CsvReader[A])(f: => CsvReader[A => B]): CsvReader[B] = fa.ap(f)
  }
}