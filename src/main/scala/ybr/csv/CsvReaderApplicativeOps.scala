package ybr.csv

/**
 * Syntax for applicative
 */
trait CsvReaderApplicativeSyntax {
  implicit class ApplicativeCsvReader[A, B](readerF: CsvReader[A => B]) {
    def <*>(readerA: CsvReader[A]): CsvReader[B] = readerA ap readerF
  }
}