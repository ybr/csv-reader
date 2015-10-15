package com.github.ybr.csv

import language.experimental.macros

object CSV {
  /**
   * Provided a CsvReader implicit for that type is available, convert a Seq[String] to any type.
   *
   * @param csv Seq[String] value to transform as an instance of A.
   */
  def read[A](csv: Seq[String])(implicit reader: CsvReader[A]): CsvResult[A] = reader.read(csv)

  /**
   * Creates a CsvReader[T] by resolving case class fields & required implicits at COMPILE-time.
   *
   * If any missing implicit is discovered, compiler will break with corresponding error.
   * {{{
   *   import ybr.csv.CSV
   *
   *   case class User(name: String, age: Int)
   *
   *   implicit val userCsvReader = CSV.reader[User]
   *   // macro-compiler replaces CSV.reader[User] by injecting into compile chain
   *   // the exact code you would write yourself. This is strictly equivalent to:
   *   implicit val userCsvReader = {
   *    import ybr.csv._
   *    CsvReader(User.apply _ curried) <*>
   *      col(0).name("name").as[String] <*>
   *      col(1).name("name").as[Int]
   *   }
   * }}}
   */
  def reader[A]: CsvReader[A] = macro CsvMacroImpl.reader[A]
}