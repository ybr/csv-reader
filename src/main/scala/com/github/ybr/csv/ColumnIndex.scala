package com.github.ybr.csv

/**
 * Column with an index and possibly a name.
 */
case class ColumnIndex(index: Int) extends Column { self =>
  def as[A](implicit reader: CsvColumnReader[A]) = new CsvReader[A] {
    def read(columns: Seq[String]): CsvResult[A] = {
      // out of bounds
      if(columns.size <= index || index < 0) CsvError("error.column.missing", ("index" -> index))
      else {
        val columnContent = columns(index)
        reader.read(columnContent) match {
          case CsvError(errors) => CsvError(errors.map(error => error.copy(args = error.args + ("index" -> index) + ("content" -> columnContent))))
          case success => success
        }
      }
    }
  }

  /**
   * Supplement this column with a name
   */
  def name(columnName: String): Column = new Column {
    def as[A](implicit reader: CsvColumnReader[A]) = new CsvReader[A] {
      def read(columns: Seq[String]): CsvResult[A] = self.as(reader).read(columns) match {
        case CsvError(errors) => CsvError(errors.map(error => error.copy(args = error.args + ("name" -> columnName))))
        case success => success
      }
    }
  }
}

object ColumnIndex {
  def col(index: Int) = ColumnIndex(index)
}