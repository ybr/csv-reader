package com.github.ybr.csv

import com.github.ybr.csv._
import com.github.ybr.csv.CsvReaderMonadImplicit._

import org.scalacheck.{ Arbitrary, Gen, Properties }
import org.scalacheck.Prop.forAll

import scalaz._
import scalaz.Scalaz._
import scalaz.scalacheck.ScalazProperties._
import scalaz.scalacheck.ScalazArbitrary._
import scalaz.scalacheck.ScalaCheckBinding._

object CsvReaderLaws extends Properties("CsvReaderLaws") {
  property("functor laws") = forAll(csvLineGen) { (csvLine: String) =>
    implicit val equalCsvReaderWithData = csvReaderEqual[Int](csvLine.split(','))
    functor.laws[CsvReader]
  }

  property("applicative laws") = forAll(csvLineGen) { (csvLine: String) =>
    implicit val equalCsvReaderWithData = csvReaderEqual[Int](csvLine.split(','))
    applicative.laws[CsvReader]
  }

  property("monad laws") = forAll(csvLineGen) { (csvLine: String) =>
    implicit val equalCsvReaderWithData = csvReaderEqual[Int](csvLine.split(','))
    monad.laws[CsvReader]
  }

  def csvReaderEqual[A](data: Seq[String])(implicit eqA: Equal[A]): Equal[CsvReader[A]] = new Equal[CsvReader[A]] {
    def equal(reader1: CsvReader[A], reader2: CsvReader[A]): Boolean = (reader1.read(data), reader2.read(data)) match {
      case (CsvSuccess(a1), CsvSuccess(a2)) => eqA.equal(a1, a2)
      case (CsvError(errors1), CsvError(errors2)) => errors1 == errors2
      case _ => false
    }
  }

  implicit def csvReaderIntArbitrary(implicit arb: Arbitrary[Int]): Arbitrary[CsvReader[Int]] = arb.map(_ => col(0).as[Int])
  implicit def csvReaderInt2IntArbitrary(implicit arb: Arbitrary[Int => Int]): Arbitrary[CsvReader[Int => Int]] = arb.map(CsvReader.apply)

  lazy val csvLineGen: Gen[String] = for {
    size <- Gen.choose(0, 30)
    columns <- Gen.listOfN(size, Gen.numStr)
  } yield columns.mkString(",")
}