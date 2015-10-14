package ybr.csv

/**
 * Column bridges a column to a line of CSV.
 * 
 */
trait Column {
  /**
   * Returns a CsvReader from a CsvColumnReader of any type A.
   *
   * Specifying how to read a column you can read a line by combining many CsvReaders.
   */
  def as[A: CsvColumnReader]: CsvReader[A]
}