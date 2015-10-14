package ybr.csv

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import scala.collection.immutable

object CsvSpec extends Properties("CSV") {
  case class User(id: Long, name: String, age: Int)

  property("Yield a user with CSV.reader[User]") = forAll { (userId: Long, name: String, age: Int) =>
    implicit val userCsvReader = CSV.reader[User]

    val line = immutable.Seq(userId.toString, name, age.toString)
    CSV.read[User](line) match {
      case CsvSuccess(user) => user.id == userId && user.name == name
      case error => false
    }
  }

  property("Yield one error over id with CSV.reader[User] on id") = forAll { (userId: Long, name: String, age: Int) =>
    implicit val userCsvReader = CSV.reader[User]

    val line = immutable.Seq(userId.toString + "a", name, age.toString)
    CSV.read[User](line) match {
      case CsvSuccess(user) => false
      case CsvError(immutable.Seq(ColumnError("error.expected.long", arguments))) => arguments("index") == 0 && arguments("name") == "id"
    }
  }

  property("Yield two errors over id and age with CSV.reader[User] on id") = forAll { (userId: Long, name: String, age: Int) =>
    implicit val userCsvReader = CSV.reader[User]

    val line = immutable.Seq(userId.toString + "a", name, age.toString + "years")
    CSV.read[User](line) match {
      case CsvSuccess(user) => false
      case CsvError(errors) =>
        val idError = errors(0)
        val ageError = errors(1)
        errors.length == 2 &&
        idError.message == "error.expected.long" && idError.args("index") == 0 && idError.args("name") == "id" 
        ageError.message == "error.expected.int" && ageError.args("index") == 2 && ageError.args("name") == "age"
    }
  }

  property("Yield a user with CSV.reader[User] on id") = forAll { (userId: Long, name: String, age: Int) =>
    implicit val userCsvReader = CSV.reader[User]

    val line = immutable.Seq(userId.toString + "a", name, age.toString)
    CSV.read[User](line) match {
      case CsvSuccess(user) => false
      case CsvError(immutable.Seq(ColumnError("error.expected.long", arguments))) => arguments("index") == 0 && arguments("name") == "id"
    }
  }

  property("Yield one error over age with applicative <*>") = forAll { (userId: Long, name: String, age: Int) =>
    implicit val userCsvReader = CsvReader(User.apply _ curried) <*>
                                  col(0).as[Long]   <*>
                                  col(1).as[String] <*>
                                  col(2).as[Int]

    val line = immutable.Seq(userId.toString, name, age.toString + "years")
    CSV.read[User](line) match {
      case CsvSuccess(_) => false
      case CsvError(immutable.Seq(ColumnError("error.expected.int", arguments))) => arguments("index") == 2
    }
  }

  property("Yield two errors over id and age with applicative <*>") = forAll { (userId: Long, name: String, age: Int) =>
    implicit val userCsvReader = CsvReader(User.apply _ curried) <*>
                                  col(0).as[Long]   <*>
                                  col(1).as[String] <*>
                                  col(2).as[Int]

    val line = immutable.Seq(userId.toString + "a", name, age.toString + "years")
    CSV.read[User](line) match {
      case CsvSuccess(_) => false
      case CsvError(errors) =>
        val idError = errors(0)
        val ageError = errors(1)
        errors.length == 2 &&
        idError.message == "error.expected.long" && idError.args("index") == 0
        ageError.message == "error.expected.int" && ageError.args("index") == 2
    }
  }

  property("Yield a user with applicative |@|") = forAll { (userId: Long, name: String, age: Int) =>
    import scalaz.Scalaz._
    import ybr.csv.CsvReaderMonadImplicit._

    implicit val userCsvReader = (
      col(0).as[Long]   |@|
      col(1).as[String] |@|
      col(2).as[Int]
    )(User.apply)

    val line = immutable.Seq(userId.toString, name, age.toString)
    CSV.read[User](line) match {
      case CsvSuccess(user) => user.id == userId && user.name == name
      case error => false
    }
  }

  property("Yield one error over age with applicative |@|") = forAll { (userId: Long, name: String, age: Int) =>
    import scalaz.Scalaz._
    import ybr.csv.CsvReaderMonadImplicit._

    implicit val userCsvReader = (
      col(0).as[Long]   |@|
      col(1).as[String] |@|
      col(2).name("age").as[Int]
    )(User.apply)

    val line = immutable.Seq(userId.toString, name, age.toString + "years")
    CSV.read[User](line) match {
      case CsvSuccess(_) => false
      case CsvError(immutable.Seq(ColumnError("error.expected.int", arguments))) => arguments("index") == 2 && arguments("name") == "age"
    }
  }

  property("Yield two errors over id and age with applicative |@|") = forAll { (userId: Long, name: String, age: Int) =>
    import scalaz.Scalaz._
    import ybr.csv.CsvReaderMonadImplicit._

    implicit val userCsvReader = (
      col(0).name("id").as[Long]   |@|
      col(1).as[String] |@|
      col(2).as[Int]
    )(User.apply)

    val line = immutable.Seq(userId.toString + "a", name, age.toString + "years")
    CSV.read[User](line) match {
      case CsvSuccess(_) => false
      case CsvError(errors) =>
        val idError = errors(0)
        val ageError = errors(1)
        errors.length == 2 &&
        idError.message == "error.expected.long" && idError.args("index") == 0 && idError.args("name") == "id"
        ageError.message == "error.expected.int" && ageError.args("index") == 2
    }
  }
}