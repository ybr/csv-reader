package ybr.csv

import scala.annotation.implicitNotFound
import scala.collection.immutable

/**
 * Csv reader: deserialize one line of CSV to the type A.
 *
 * Compared to CsvColumnReader which focus only on column content, one column in the line,
 * CsvReader intent is to be some kind of router between a line to columns for CsvColumnReaders.
 *
 * Write an implicit to define a CSV reader for a line of CSV for any type.
 */
@implicitNotFound(
  "No CSV reader found for type ${A}. Try to implement an implicit CsvReader for this type."
)
trait CsvReader[A] { self =>
  /**
   * Convert a line of CSV into an A
   */
  def read(columns: immutable.Seq[String]): CsvResult[A]

  def flatMap[B](f: A => CsvReader[B]): CsvReader[B] = new CsvReader[B] {
    def read(columns: immutable.Seq[String]): CsvResult[B] = self.read(columns) match {
      case CsvSuccess(a) => f(a).read(columns)
      case error: CsvError => error
    }
  }

  def map[B](f: A => B): CsvReader[B] = new CsvReader[B] {
    def read(columns: immutable.Seq[String]): CsvResult[B] = self.read(columns) match {
      case CsvSuccess(a) => CsvSuccess(f(a))
      case error: CsvError => error
    }
  }

  def ap[B](f: => CsvReader[A => B]): CsvReader[B] = new CsvReader[B] {
    def read(columns: immutable.Seq[String]): CsvResult[B] = (f.read(columns), self.read(columns)) match {
      case (CsvSuccess(f), CsvSuccess(a)) => CsvSuccess(f(a))
      case (CsvError(errors1), CsvError(errors2)) => CsvError(errors1 ++ errors2)
      case (_, error: CsvError) => error
      case (error: CsvError, _) => error
    }
  }

  /*
   * Returns this CsvReader if it is in success and applying the predicate p to this CsvReader's value returns true. Otherwise, return a CsvReader in error.
   */
  def filter(p: A => Boolean) = new CsvReader[A] {
    def read(columns: immutable.Seq[String]): CsvResult[A] = self.read(columns) match {
      case success@CsvSuccess(a) =>
        if(p(a)) success
        else CsvError("error.filtered", "message" -> s"The result has been filtered, it was previously ${a}")
      case error => error
    }
  }
}

object CsvReader {
  /*
   * Creates a CsvReader that always succeeds with the given value.
   */
  def apply[A](a: A) = new CsvReader[A] {
    def read(columns: immutable.Seq[String]): CsvResult[A] = CsvSuccess(a)
  }

  /*
   * Creates a CsvReader that always fails with the given error message and arguments
   */
  def failed(msg: String, args: (String, Any)*) = new CsvReader[Nothing] {
    def read(columns: immutable.Seq[String]): CsvResult[Nothing] = CsvError(msg, args: _*)
  }
}