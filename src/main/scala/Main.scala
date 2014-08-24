//From a Gist courtesy Eugene Burmako: https://gist.github.com/5845539.git
import scala.reflect.runtime.{universe => ru}
import scala.tools.reflect.ToolBox
import scala.language.reflectiveCalls

import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._


case class T(s: String)

object Test extends App {

  def define(tb: ToolBox[ru.type], tree: ru.ImplDef, namespace: String): ru.Symbol = {
    val compiler = tb.asInstanceOf[{ def compiler: scala.tools.nsc.Global }].compiler
    val importer = compiler.mkImporter(ru)
    val exporter = importer.reverse
    val ctree: compiler.ImplDef = importer.importTree(tree).asInstanceOf[compiler.ImplDef]
    def defineInternal(ctree: compiler.ImplDef): compiler.Symbol = {
      import compiler._
 
    //  val packageName = newTermName("__wrapper$" + java.util.UUID.randomUUID.toString.replace("-", ""))
      val packageName = newTermName(namespace)
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
 
  def cdef() = q"case class C(v: String)"
  def newc(csym: Symbol) = q"""${csym.companionSymbol}"""
  val tb = cm.mkToolBox()
  val csym = define(tb, cdef(), "pkg")
  //val csym = tb.define(cdef()) // for 2.11
  val obj = tb.eval(newc(csym))

  import scala.reflect.runtime._

  val method_apply$ = obj.getClass.getMethod("apply", "".getClass)
  val instantiated$ = method_apply$.invoke(obj,  "hello")
  type Type = instantiated$.type

  val loader = (tb.asInstanceOf[scala.tools.reflect.ToolBoxFactory$ToolBoxImpl].classLoader)
  ctx.registerClassLoader(loader)
  val dbo = grater[Type].asDBObject(instantiated$)
  println("back: " + grater[Type].asObject(dbo))
  //println("back: " + grater[Type].asObject(dbo).v)//of course compiler chokes on v since it is not (yet?) a member of the type underlying the alias `Test.Type`. Perhaps use `Dynamic` 


}


