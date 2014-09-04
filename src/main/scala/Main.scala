//From a Gist courtesy Eugene Burmako: https://gist.github.com/5845539.git
import scala.reflect.runtime.{universe => ru}
import scala.tools.reflect.ToolBox
import scala.language.reflectiveCalls

import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._

import com.gensler.scalavro.types.AvroType



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

/*
  //Salat
  val loader = (tb.asInstanceOf[scala.tools.reflect.ToolBoxFactory$ToolBoxImpl].classLoader)
  ctx.registerClassLoader(loader)
  val dbo = grater[Type].asDBObject(instantiated$)
  val objj = grater[Type].asObject(dbo)
  println(objj == instantiated$) //true

  //println("back: " + grater[Type].asObject(dbo).v)//of course compiler chokes on v since it is not (yet?) a member of the type underlying the alias `Test.Type`. Perhaps use `Dynamic`? 
*/

  //Scalavro

println(ru.typeOf[Type]) // returns: Test.Type
println(AvroType[Type].schema) //returns: error
/*
[error] (run-main) java.lang.NoClassDefFoundError: no Java class corresponding to Test.instantiated$.type found
java.lang.NoClassDefFoundError: no Java class corresponding to Test.instantiated$.type found
	at scala.reflect.runtime.JavaMirrors$JavaMirror.typeToJavaClass(JavaMirrors.scala:1258)
	at scala.reflect.runtime.JavaMirrors$JavaMirror.typeToJavaClass(JavaMirrors.scala:1256)
	at scala.reflect.runtime.JavaMirrors$JavaMirror.runtimeClass(JavaMirrors.scala:202)
	at scala.reflect.runtime.JavaMirrors$JavaMirror.runtimeClass(JavaMirrors.scala:65)
	at com.gensler.scalavro.util.ReflectionHelpers$class.typeableSubTypesOf(ReflectionHelpers.scala:120)
	at com.gensler.scalavro.util.ReflectionHelpers$.typeableSubTypesOf(ReflectionHelpers.scala:11)
	at com.gensler.scalavro.types.AvroType$$anonfun$fromTypeHelper$1.apply(AvroType.scala:461)
	at com.gensler.scalavro.types.AvroType$$anonfun$fromTypeHelper$1.apply(AvroType.scala:227)
	at scala.util.Try$.apply(Try.scala:161)
	at com.gensler.scalavro.types.AvroType$.fromTypeHelper(AvroType.scala:227)
	at com.gensler.scalavro.types.AvroType$.fromType(AvroType.scala:223)
	at com.gensler.scalavro.types.AvroType$.apply(AvroType.scala:217)
	at Test$delayedInit$body.apply(Main.scala:71)
	at scala.Function0$class.apply$mcV$sp(Function0.scala:40)
	at scala.runtime.AbstractFunction0.apply$mcV$sp(AbstractFunction0.scala:12)
	at scala.App$$anonfun$main$1.apply(App.scala:71)
	at scala.App$$anonfun$main$1.apply(App.scala:71)
	at scala.collection.immutable.List.foreach(List.scala:318)
	at scala.collection.generic.TraversableForwarder$class.foreach(TraversableForwarder.scala:32)
	at scala.App$class.main(App.scala:71)
	at Test$.main(Main.scala:16)
	at Test.main(Main.scala)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:606)
[trace] Stack trace suppressed: run last compile:run for the full output.
*/
  
}


