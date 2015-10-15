package com.github.ybr.csv

import java.util.{ Date, UUID }
import java.text.SimpleDateFormat

import scala.util.{ Try, Success, Failure }
import scala.util.matching.Regex

import org.joda.time.DateTime

object DefaultCsvColumnReaders {
  /**
   * CsvColumnReader for an Enumeration type.
   *
   * @param enum an Enumeration.
   */
  def enum[E <: Enumeration](enum: E): CsvColumnReader[E#Value] = new CsvColumnReader[E#Value] {
    def read(columnContent: String): CsvResult[E#Value] = enum.values.find(_.toString == columnContent).map(CsvSuccess.apply).getOrElse(CsvError("error.expected.validenumvalue"))
  }

  /**
   * CsvColumnReader for the `java.util.Date` type.
   *
   * @param pattern a date pattern, as specified in `java.text.SimpleDateFormat`.
   */
  def dateReads(pattern: String) = new CsvColumnReader[Date] {
    val df = new SimpleDateFormat(pattern)

    def read(columnContent: String): CsvResult[Date] = Try(df.parse(columnContent)) match {
      case Success(date) => CsvSuccess(date)
      case Failure(t) => CsvError("error.expected.date", ("content" -> columnContent), ("cause" -> t.getMessage))
    }
  }

  /**
   * CsvColumnReader for the `org.joda.time.DateTime` type.
   *
   * @param pattern a date pattern, as specified in `java.text.SimpleDateFormat`.
   */
  def jodaDateReads(pattern: String) = new CsvColumnReader[DateTime] {
    val df = org.joda.time.format.DateTimeFormat.forPattern(pattern)

    def read(columnContent: String): CsvResult[DateTime] = Try(DateTime.parse(columnContent, df)) match {
      case Success(date) => CsvSuccess(date)
      case Failure(t) => CsvError("error.expected.jodadate", ("content" -> columnContent), ("cause" -> t.getMessage))
    }
  }
}

/**
 * Default CSV column readers type classe instances.
 */
trait DefaultCsvColumnReadersImplicits {
  /**
   * CsvColumnReader for the `scala.Int` type.
   */
  implicit val IntCsvReader: CsvColumnReader[Int] = new CsvColumnReader[Int] {
    def read(columnContent: String): CsvResult[Int] = Try(columnContent.trim.toInt) match {
      case Success(int) => CsvSuccess(int)
      case Failure(_) => CsvError("error.expected.int")
    }
  }

  /**
   * CsvColumnReader for the `scala.Long` type.
   */
  implicit val LongCsvReader: CsvColumnReader[Long] = new CsvColumnReader[Long] {
    def read(columnContent: String): CsvResult[Long] = Try(columnContent.trim.toLong) match {
      case Success(int) => CsvSuccess(int)
      case Failure(_) => CsvError("error.expected.long")
    }
  }

  /**
   * CsvColumnReader for the `scala.Double` type.
   */
  implicit val DoubleCsvReader: CsvColumnReader[Double] = new CsvColumnReader[Double] {
    def read(columnContent: String): CsvResult[Double] = Try(columnContent.trim.toDouble) match {
      case Success(double) => CsvSuccess(double)
      case Failure(_) => CsvError("error.expected.double")
    }
  }

  /**
   * CsvColumnReader for the `scala.Float` type.
   */
  implicit val FloatCsvReader: CsvColumnReader[Float] = new CsvColumnReader[Float] {
    def read(columnContent: String): CsvResult[Float] = Try(columnContent.trim.toFloat) match {
      case Success(int) => CsvSuccess(int)
      case Failure(_) => CsvError("error.expected.float")
    }
  }

  /**
   * CsvColumnReader for the `java.lang.String` type.
   */
  implicit val StringCsvReader: CsvColumnReader[String] = new CsvColumnReader[String] {
    def read(columnContent: String): CsvResult[String] = CsvSuccess(columnContent)
  }

  /**
   * CsvColumnReader for the `java.util.UUID` type.
   */
  implicit val UUIDCsvReader: CsvColumnReader[UUID] = new CsvColumnReader[UUID] {
    def read(columnContent: String): CsvResult[UUID] = Try(UUID.fromString(columnContent.trim)) match {
      case Success(uuid) => CsvSuccess(uuid)
      case Failure(_) => CsvError("error.expected.uuid")
    }
  }

  /**
   * Default CsvColumnReader for the `java.util.Date` type with the default pattern yyyy-MM-dd.
   */
  implicit val DefaultDateReader: CsvColumnReader[Date] = DefaultCsvColumnReaders.dateReads("yyyy-MM-dd")

  /**
   * Default CsvColumnReader for the `org.joda.time.DateTime` type with the default pattern yyyy-MM-dd.
   */
  implicit val DefaultJodaDateTimeColumnReader: CsvColumnReader[DateTime] = DefaultCsvColumnReaders.jodaDateReads("yyyy-MM-dd")

  /**
   * CsvColumnReader for an Option of any type A, provided we have a CsvColumnReader that type A.
   */
  implicit def optionCsvColumnReader[A](implicit reader: CsvColumnReader[A]): CsvColumnReader[Option[A]] = new CsvColumnReader[Option[A]] {
    def read(columnContent: String): CsvResult[Option[A]] = reader.read(columnContent) match {
      case CsvSuccess(a) => CsvSuccess(Some(a))
      case CsvError(_) => CsvSuccess(None)
    }
  }
}