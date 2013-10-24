//From a Gist courtesy Eugene Burmako: https://gist.github.com/5845539.git
import scala.reflect.runtime.{universe => ru}
import scala.tools.reflect.ToolBox
import scala.language.reflectiveCalls



import scala.tools.scalap.scalax.rules.scalasig._

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations.util._

import scala.reflect.ScalaSignature
import reflect.internal.pickling.ByteCodecs
import reflect.internal.pickling.PickleFormat
import reflect.internal.pickling.PickleBuffer
import java.io._
import com.gensler.scalavro.types.AvroType
import com.gensler.scalavro.io.AvroTypeIO
import scala.util.{Try, Success, Failure}



object Test extends App {
  def define(tb: ToolBox[ru.type], tree: ru.ImplDef): ru.Symbol = {
    val compiler = tb.asInstanceOf[{ def compiler: scala.tools.nsc.Global }].compiler
    val importer = compiler.mkImporter(ru)
    val exporter = importer.reverse
    val ctree: compiler.ImplDef = importer.importTree(tree).asInstanceOf[compiler.ImplDef]
    def defineInternal(ctree: compiler.ImplDef): compiler.Symbol = {
      import compiler._
 
      val packageName = newTermName("__wrapper$" + java.util.UUID.randomUUID.toString.replace("-", ""))
      val pdef = PackageDef(Ident(packageName), List(ctree))
      val unit = new CompilationUnit(scala.reflect.internal.util.NoSourceFile)
      unit.body = pdef
 
      val run = new Run
      reporter.reset()
      run.compileUnits(List(unit), run.namerPhase)
      compiler.asInstanceOf[{ def throwIfErrors(): Unit }].throwIfErrors()
 
      ctree.symbol
    }
    val csym: compiler.Symbol = defineInternal(ctree)
    val usym = exporter.importSymbol(csym)
    usym
  }
 
  import scala.reflect.runtime.universe._
  import Flag._
  import scala.reflect.runtime.{currentMirror => cm}
 
  def pendingSuperCall() = Apply(Select(Super(This(tpnme.EMPTY), tpnme.EMPTY), nme.CONSTRUCTOR), Nil)
  // equivalent to q"class C"
  def cdef() = ClassDef(
    NoMods, newTypeName("C"), Nil,
    Template(
      List(Select(Ident(newTermName("scala")), newTypeName("AnyRef"))),
      emptyValDef,
      List(
        DefDef(NoMods, nme.CONSTRUCTOR, Nil, List(Nil), TypeTree(), Block(List(pendingSuperCall()), Literal(Constant(())))),
        DefDef(Modifiers(OVERRIDE), newTermName("toString"), Nil, Nil, TypeTree(), Literal(Constant("C")))
    )))
  def newc(csym: Symbol) = Apply(Select(New(Ident(csym)), nme.CONSTRUCTOR), List())
 
  val tb = cm.mkToolBox()
  val csym = define(tb, cdef())
  val obj = tb.eval(newc(csym))
//End Gist

  println(obj)

  val cls = obj.getClass()
    println(cls)

  type MyRecord = cls.type
////println(typeOf[cl].typeSymbol) //type mismatch, found: Test.obj.type (with underlying type Any), req: AnyRef, can't cast


//No Scala Sig is available
val scalaSig = ScalaSigParser.parse(cls)
  println(scalaSig)//none
  println(cls.annotation[ScalaSignature])


//Can't use it as a type parameter in neither Salat nor Scalavro
//val dbo = grater[MyRecord].asDBObject(obj)//dynamic types still have incorrect underlying type of Any

 // val myRecordType = AvroType[MyRecord]//java.lang.NoClassDefFoundError: no Java class corresponding to Test.cls.type found
// println("schema: " + myRecordType.schema)



//Maybe if Salat changes to parse with typeOf, we'll have a way
//println(typeOf[Record].member(nme.CONSTRUCTOR)) //Constructor available for normal case class, but not my dumb parsed. how about eugene's using the toolbox??

}

