package com.github.ybr.csv

import scalaz.Monad

/**
 * CsvColumnReader instance for the `scalaz.Monad` type class.
 */
package object CsvColumnReaderMonadImplicit {
  implicit val CsvColumnReaderMonad = new Monad[CsvColumnReader] {
    def point[A](a: => A) = CsvColumnReader(a)

    def bind[A, B](fa: CsvColumnReader[A])(f: A => CsvColumnReader[B]): CsvColumnReader[B] = fa.flatMap(f)
  }
}