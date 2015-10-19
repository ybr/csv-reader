package com.github.ybr.csv

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object CsvProperties extends Properties("CSV") {
  case class User(id: Long, name: String, age: Int)

  property("Yield a user with CSV.reader[User]") = forAll { (userId: Long, name: String, age: Int) =>
    implicit val userCsvReader = CSV.reader[User]

    val line = Seq(userId.toString, name, age.toString)
    CSV.read[User](line) match {
      case CsvSuccess(user) => user.id == userId && user.name == name
      case error => false
    }
  }

  property("Yield one error over id with CSV.reader[User] on id") = forAll { (userId: Long, name: String, age: Int) =>
    implicit val userCsvReader = CSV.reader[User]

    val line = Seq(userId.toString + "a", name, age.toString)
    CSV.read[User](line) match {
      case CsvSuccess(user) => false
      case CsvError(Seq(ColumnError("error.expected.long", arguments))) => arguments("index") == 0 && arguments("name") == "id"
    }
  }

  property("Yield two errors over id and age with CSV.reader[User] on id") = forAll { (userId: Long, name: String, age: Int) =>
    implicit val userCsvReader = CSV.reader[User]

    val line = Seq(userId.toString + "a", name, age.toString + "years")
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

    val line = Seq(userId.toString + "a", name, age.toString)
    CSV.read[User](line) match {
      case CsvSuccess(user) => false
      case CsvError(Seq(ColumnError("error.expected.long", arguments))) => arguments("index") == 0 && arguments("name") == "id"
    }
  }

  property("Yield one error over age with applicative <*>") = forAll { (userId: Long, name: String, age: Int) =>
    implicit val userCsvReader = CsvReader(User.apply _ curried) <*>
                                  col(0).as[Long]   <*>
                                  col(1).as[String] <*>
                                  col(2).as[Int]

    val line = Seq(userId.toString, name, age.toString + "years")
    CSV.read[User](line) match {
      case CsvSuccess(_) => false
      case CsvError(Seq(ColumnError("error.expected.int", arguments))) => arguments("index") == 2
    }
  }

  property("Yield two errors over id and age with applicative <*>") = forAll { (userId: Long, name: String, age: Int) =>
    implicit val userCsvReader = CsvReader(User.apply _ curried) <*>
                                  col(0).as[Long]   <*>
                                  col(1).as[String] <*>
                                  col(2).as[Int]

    val line = Seq(userId.toString + "a", name, age.toString + "years")
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
    import com.github.ybr.csv.CsvReaderMonadImplicit._

    implicit val userCsvReader = (
      col(0).as[Long]   |@|
      col(1).as[String] |@|
      col(2).as[Int]
    )(User.apply)

    val line = Seq(userId.toString, name, age.toString)
    CSV.read[User](line) match {
      case CsvSuccess(user) => user.id == userId && user.name == name
      case error => false
    }
  }

  property("Yield one error over age with applicative |@|") = forAll { (userId: Long, name: String, age: Int) =>
    import scalaz.Scalaz._
    import com.github.ybr.csv.CsvReaderMonadImplicit._

    implicit val userCsvReader = (
      col(0).as[Long]   |@|
      col(1).as[String] |@|
      col(2).name("age").as[Int]
    )(User.apply)

    val line = Seq(userId.toString, name, age.toString + "years")
    CSV.read[User](line) match {
      case CsvSuccess(_) => false
      case CsvError(Seq(ColumnError("error.expected.int", arguments))) => arguments("index") == 2 && arguments("name") == "age"
    }
  }

  property("Yield two errors over id and age with applicative |@|") = forAll { (userId: Long, name: String, age: Int) =>
    import scalaz.Scalaz._
    import com.github.ybr.csv.CsvReaderMonadImplicit._

    implicit val userCsvReader = (
      col(0).name("id").as[Long]   |@|
      col(1).as[String] |@|
      col(2).as[Int]
    )(User.apply)

    val line = Seq(userId.toString + "a", name, age.toString + "years")
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

  property("Yield a tuple2 with CSV.read[(A, B)]") = forAll { (a: Int, b: Long) =>
    val line = Seq(a.toString, b.toString)
    CSV.read[(Int, Long)](line) match {
      case CsvSuccess(t2) => t2._1 == a && t2._2 == b
      case error => false
    }
  }

  property("Yield one error with CSV.read[(A, B)]") = forAll { (a: Int, b: Long) =>
    val line = Seq(a.toString + "years", b.toString)
    CSV.read[(Int, Long)](line) match {
      case CsvSuccess(_) => false
      case CsvError(errors) =>
        errors.length == 1 &&
        errors(0).message == "error.expected.int" && errors(0).args("index") == 0
    }
  }

  property("Yield two errors with CSV.read[(A, B)]") = forAll { (a: Int, b: Long) =>
    val line = Seq(a.toString + "years", b.toString + "m")
    CSV.read[(Int, Long)](line) match {
      case CsvSuccess(_) => false
      case CsvError(errors) =>
        val errorA = errors(0)
        val errorB = errors(1)
        errors.length == 2 &&
        errorA.message == "error.expected.int" && errorA.args("index") == 0 &&
        errorB.message == "error.expected.long" && errorB.args("index") == 1
    }
  }

  property("Collect successes from a list of CSV results") = forAll { (as: Seq[(Int, Double)]) =>
    val lines: Seq[Seq[String]] = as.flatMap { case (a, b) => Seq(Seq(a.toString, b.toString), Seq("failed", "0")) }
    val results = lines.map(CSV.read[(Int, Double)])

    val successes = results.collect(CsvSuccess.partial)

    results.length == 2 * as.length &&
    successes.length == as.length
  }

  property("Collected successes shall always be less or equal than the count of results") = forAll { (as: Seq[(String, String)]) =>
    val lines: Seq[Seq[String]] = as.map { case (a, b) => Seq(a, b) }
    val results = lines.map(CSV.read[(Int, Double)])

    val successes = results.collect(CsvSuccess.partial)

    successes.length <= results.length
  }
}