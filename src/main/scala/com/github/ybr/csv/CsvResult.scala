package com.github.ybr.csv

/**
 * CsvResult is the type returned fro a CSV reading.
 * It can be of two flavors, either a succes or possibly many errors.
 */
sealed trait CsvResult[+A]

case class CsvSuccess[A](a: A) extends CsvResult[A]
case class CsvError(errors: Seq[ColumnError]) extends CsvResult[Nothing]

object CsvError {
  def apply(message: String, args: (String, Any)*): CsvError = CsvError(Seq(ColumnError(message, Map(args: _*))))
}

case class ColumnError(message: String, args: Map[String, Any])