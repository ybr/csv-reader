package com.github.ybr.csv

object DefaultCsvReaders {
  def tuple2[A, B](implicit readerA: CsvColumnReader[A], readerB: CsvColumnReader[B]): CsvReader[(A, B)] = {
    CsvReader(Tuple2.apply[A, B] _ curried) <*> col(0).as[A] <*> col(1).as[B]
  }

  def tuple3[A, B, C](implicit readerA: CsvColumnReader[A],
                                readerB: CsvColumnReader[B],
                                readerC: CsvColumnReader[C]): CsvReader[(A, B, C)] = {
    CsvReader(Tuple3.apply[A, B, C] _ curried) <*> col(0).as[A] <*> col(1).as[B] <*> col(2).as[C]
  }
}

trait DefaultCsvReadersImplicits {
  implicit def tuple2CsvReader[A, B](implicit readerA: CsvColumnReader[A], readerB: CsvColumnReader[B]): CsvReader[(A, B)] = DefaultCsvReaders.tuple2[A, B]

  implicit def tuple3CsvReader[A, B, C](implicit readerA: CsvColumnReader[A],
                                                  readerB: CsvColumnReader[B],
                                                  readerC: CsvColumnReader[C]): CsvReader[(A, B, C)] = DefaultCsvReaders.tuple3[A, B, C]
}