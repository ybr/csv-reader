# csv-reader

CSV reader library for Scala.

It provides a purely functional and type safe mean of reading CSV. Write CSV recipe by focusing on composing readers not the boilerplate of dealing with errors at every step.

This is still an alpha version.

[![Build Status](https://travis-ci.org/ybr/csv-reader.svg?branch=develop)](https://travis-ci.org/ybr/csv-reader)

## Getting csv-reader

If you are using SBT, add the following line to your build file (Build.scala):

```scala
lazy val csvreader = RootProject(uri("git@github.com:ybr/csv-reader.git#develop"))
```

and add a dependsOn(csvreader) to the project depending on csv-reader.

## Documentation

[CSV reader scaladoc](http://csvreader.ybr.s3-website-eu-west-1.amazonaws.com/api/nightly/#com.github.ybr.csv.package)

## Quick start

In the Scala REPL:

```scala
import com.github.ybr.csv._

val line = Seq("ybr", "35")

scala> val CsvSuccess((name, age)) = CSV.read[(String, Int)](line)
name: String = ybr
age: Int = 35
```

We get the name and age with the expected types and values.

```scala
import com.github.ybr.csv._

val line = Seq("ybr", "35years")

scala> val CsvError(errors) = CSV.read[(String, Int)](line)
errors: Seq[com.github.ybr.csv.ColumnError] = List(ColumnError(error.expected.int,Map(index -> 1, content -> 35years)))
```

This time the result is a CsvError with the error explaining why this line is not a tuple (String, Int)

The result of reading a line of CSV can be either a CsvSuccess of the expected type
or a CsvError with all the reasons why the line does not conform to the recipe.

## Read case class

```scala
import com.github.ybr.csv._

case class User(name: String, age: Int, height: Long)

implicit val userCsvReader = CSV.reader[User]

val line = Seq("ybr", "35", "1.75")

scala> CSV.read[User](line)
com.github.ybr.csv.CsvResult[User] = CsvError(List(ColumnError(error.expected.long,Map(index -> 2, content -> 1.75, name -> height))))
```

Oops my height is right but the User.height shall be a Long, let's rewrite that case class.

```scala
import com.github.ybr.csv._

case class User(name: String, age: Int, height: Float)

implicit val userCsvReader = CSV.reader[User]

val line = Seq("ybr", "35", "1.75")

scala> CSV.read[User](line)
com.github.ybr.csv.CsvResult[User] = CsvSuccess(User(ybr,35,1.75))
```

Good, the user has been created correctly. We just changed the type in the case class and this was seamlessly propagated to CSV.read.
CSV.reader[A] allows us to automatically create a CsvReader[A] without being cluttered by details.

## Custom reader

In case the provided tuple and case class readers are not enough.
We can create our own CSV readers, by specifying the index and name of a column and arranging the order of readers, ignoring some columns and combining others.

CsvReaders can be transformed with map and combined with one another thanks to flatMap, CsvReader is a Monad.
CsvReader is an Applicative too which let us state that we want errors to be accumulated whereas the Monad would
stop on the first error it encounters.

### Applicative builder

```scala
import com.github.ybr.csv._
import com.github.ybr.csv.CsvReaderMonadImplicit._
import scalaz.Scalaz._

case class TV(model: String, dimension: String)

implicit val tvCsvReader = (
  col(2).as[String] |@|
  (col(0).name("height").as[Int] |@| col(1).name("width").as[Int])(_ + "x" + _)
)(TV)

scala> CSV.read[TV](Seq("160", "140", "Samsung TV"))
com.github.ybr.csv.CsvResult[TV] = CsvSuccess(TV(Samsung TV,160x140))
```

Applicative builder |@| from scalaz is helpful to express a CSV reader for the TV type.
We specify that we want to combine column 0 and 1 into a single value so if one fails then the overall fails and we are interested by errors from both column 2 and 0,1.

### Applicative syntax

Otherwise we can use the applicative operator `<*>`, it is like the applicative builder without suffering the tuple 22 limit.

```scala
import com.github.ybr.csv._

case class TV(model: String, dimension: String)

implicit val tvCsvReader = {
  CsvReader(TV.apply _ curried) <*>
    col(2).as[String] <*>
    col(0).as[Int].flatMap(height => col(1).as[Int].map(width => s"${height}x${width}"))
}

scala> CSV.read[TV](Seq("160", "140", "Samsung TV"))
com.github.ybr.csv.CsvResult[TV] = CsvSuccess(TV(Samsung TV,160x140))
```
