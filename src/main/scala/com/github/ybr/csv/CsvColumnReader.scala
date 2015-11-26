package com.github.ybr.csv

import scala.annotation.implicitNotFound

/**
 * Csv column reader: deserialize one column to the type A.
 *
 * Write an implicit to define a reader for one column of a CSV line for any type.
 */
@implicitNotFound("No CSV column reader found for type ${A}. Try to implement an implicit CsvColumnReader[${A}] for this type.")
trait CsvColumnReader[A] { self =>
  /**
   * Convert a column of CSV into an A
   */
  def read(columnContent: String): CsvResult[A]

  def flatMap[B](f: A => CsvColumnReader[B]): CsvColumnReader[B] = new CsvColumnReader[B] {
    def read(columnContent: String): CsvResult[B] = self.read(columnContent) match {
      case CsvSuccess(a) => f(a).read(columnContent)
      case error: CsvError => error
    }
  }

  def map[B](f: A => B): CsvColumnReader[B] = new CsvColumnReader[B] {
    def read(columnContent: String): CsvResult[B] = self.read(columnContent) match {
      case CsvSuccess(a) => CsvSuccess(f(a))
      case error: CsvError => error
    }
  }

  /**
   * Returns this CsvReader if it is in success and applying the predicate p to this CsvReader's value returns true. Otherwise, return a CsvReader in error.
   */
  def filter(p: A => Boolean) = new CsvColumnReader[A] {
    def read(columnContent: String): CsvResult[A] = self.read(columnContent) match {
      case success@CsvSuccess(a) =>
        if(p(a)) success
        else CsvError("error.filtered", "message" -> s"The result has been filtered, it was previously ${a}")
      case error => error
    }
  }
}

object CsvColumnReader {
  /**
   * Creates a CsvColumnReader that always result in a success of the provided A.
   */
	def apply[A](a: A): CsvColumnReader[A] = new CsvColumnReader[A] {
		def read(columnContent: String): CsvResult[A] = CsvSuccess(a)
	}

  /**
   * Creates a CsvColumnReader that always result in a success of the function f applied to the column content.
   */
	def apply[A](f: String => A): CsvColumnReader[A] = new CsvColumnReader[A] {
		def read(columnContent: String): CsvResult[A] = CsvSuccess(f(columnContent))
	}

  /**
   * Finds the implicits CsvColumnReader in context for type A.
   */
	def of[A](implicit reader: CsvColumnReader[A]): CsvColumnReader[A] = reader
}