package com.github.ybr

/**
 * CSV API
 * For example:
 * {{{
 * import scalaz.Scalaz._
 * import com.github.ybr.csv._
 * import com.github.ybr.csv.CsvReaderMonadImplicit._
 * 
 * case class User(id: Long, name: String)
 * object User {
 * 
 *  implicit val userCsvReader: CsvReader[User] = (
 *    col(0).name("id").as[Long] |@|
 *    col(1).name("name").as[String]
 *  )(User.apply)
 * }
 * 
 * //then to use it:
 * 
 * import User._
 * object MyApplication {
 *   def parseUser(line: String): CsvResult[User] = CSV.read[User](line.split(','))
 * }
 * }}}
 */
package object csv extends CsvReaderApplicativeSyntax
                      with DefaultCsvColumnReadersImplicits
                      with DefaultCsvReadersImplicits {

  val col = ColumnIndex.col _
}