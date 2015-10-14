package ybr.csv

import scala.annotation.implicitNotFound

/**
 * Csv column reader: deserialize one column to the type A.
 *
 * Write an implicit to define a reader for one column of a CSV line for any type.
 */
@implicitNotFound(
  "No CSV column reader found for type ${A}. Try to implement an implicit CsvColumnReader for this type."
)
trait CsvColumnReader[A] {
  /**
   * Convert a column of CSV into an A
   */
  def read(columnContent: String): CsvResult[A]
}