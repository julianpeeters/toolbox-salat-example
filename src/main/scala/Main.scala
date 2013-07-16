//From a Gist courtesy Eugene Burmako: https://gist.github.com/5845539.git
import scala.reflect.runtime.{universe => ru}
import scala.tools.reflect.ToolBox
import scala.language.reflectiveCalls

import scala.tools.scalap.scalax.rules.scalasig._


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
  val cls = obj.getClass()
println(obj)

//type Obj = obj.type
//println(typeOf[Obj].typeSymbol) //type mismatch, found: Test.obj.type (with underlying type Any), req: AnyRef, can't cast

val scalaSig = ScalaSigParser.parse(cls)
  println(scalaSig)


}

