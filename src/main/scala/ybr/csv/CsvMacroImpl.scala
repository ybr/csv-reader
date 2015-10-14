package ybr.csv

import scala.reflect.macros.whitebox.Context
import scala.annotation.StaticAnnotation
import scala.reflect.runtime.universe._

/**
 * Macro definitions
 */
object CsvMacroImpl {
  /**
   * Macro definition to create a CsvReader from a case class.
   */
  def reader[A: c.WeakTypeTag](c: Context): c.Expr[CsvReader[A]] = {
    import c.universe._
    val tpe = weakTypeOf[A]

    if(!tpe.typeSymbol.isClass || !tpe.typeSymbol.asClass.isCaseClass) c.abort(c.enclosingPosition, s"${tpe.typeSymbol} is not a case class")

    val csvReader = tpe.decls
                  .filter(field => field.isMethod && field.asMethod.isCaseAccessor)
                  .zipWithIndex
                  .map { case (field, index) =>
                    q"""col(${index}).name(${field.name.toTermName.toString}).as[${field.typeSignature.resultType}]"""
                  }
                  .foldLeft(q"CsvReader(${tpe.typeSymbol.companion}.apply _ curried)") {
                    case (acc, elem) => q"${acc} <*> ${elem}"              
                  }

    c.Expr[CsvReader[A]](q"""
      import ybr.csv._
      ${csvReader}
    """)
  }
}