//From a Gist courtesy Eugene Burmako: https://gist.github.com/5845539.git
import scala.reflect.runtime.{universe => ru}
import scala.tools.reflect.ToolBox
import scala.language.reflectiveCalls

import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._

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
  import scala.reflect.runtime._
 
  def cdef() = q"case class C(v: String)"
  def newc(csym: Symbol) = q"""${csym.companionSymbol}"""
  val tb = cm.mkToolBox()
  val csym = define(tb, cdef(), "pkg")
  //val csym = tb.define(cdef()) // for 2.11
  val obj = tb.eval(newc(csym))


// Attempting to load a second class in the same package:
  def xcdef() = q"case class D(u: String)"
  def xnewc(xcsym: Symbol) = q"""${xcsym.companionSymbol}"""
  val xtb = cm.mkToolBox()
  val xcsym = define(xtb, xcdef(), "pkg")
  //val csym = tb.define(cdef()) // for 2.11
  val xobj = xtb.eval(newc(xcsym))

/* 
Results in an error when using either the same or a different toolbox:

[error] (run-main-0) scala.tools.reflect.ToolBoxError: reflective compilation has failed: 
[error] 
[error] not found: value <none>
scala.tools.reflect.ToolBoxError: reflective compilation has failed: 

not found: value <none>
	at scala.tools.reflect.ToolBoxFactory$ToolBoxImpl$ToolBoxGlobal.throwIfErrors(ToolBoxFactory.scala:314)
	at scala.tools.reflect.ToolBoxFactory$ToolBoxImpl$ToolBoxGlobal.compile(ToolBoxFactory.scala:248)
	at scala.tools.reflect.ToolBoxFactory$ToolBoxImpl.compile(ToolBoxFactory.scala:411)
	at scala.tools.reflect.ToolBoxFactory$ToolBoxImpl.eval(ToolBoxFactory.scala:414)
	at Test$delayedInit$body.apply(Main.scala:58)
	at scala.Function0$class.apply$mcV$sp(Function0.scala:40)
	at scala.runtime.AbstractFunction0.apply$mcV$sp(AbstractFunction0.scala:12)
	at scala.App$$anonfun$main$1.apply(App.scala:71)
	at scala.App$$anonfun$main$1.apply(App.scala:71)
	at scala.collection.immutable.List.foreach(List.scala:318)
	at scala.collection.generic.TraversableForwarder$class.foreach(TraversableForwarder.scala:32)
	at scala.App$class.main(App.scala:71)
	at Test$.main(Main.scala:13)
	at Test.main(Main.scala)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:606)
*/


}


