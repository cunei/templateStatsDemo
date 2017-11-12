import scala.reflect.internal.util.{ BatchSourceFile, Position } 
import scala.reflect.io.VirtualDirectory
import scala.reflect.internal.Positions
import scala.tools.nsc.{ CompilerCommand, Global }
import scala.tools.nsc.reporters.{ ConsoleReporter, Reporter, StoreReporter }
import scala.collection.JavaConverters._
import java.io.File

//////////////////////////
//
// Shows how templateStats() may not be thread-safe
//

object Boom extends App {

  val test1 =
"""name := "alaMaKota"
libraryDependencies := Seq("org.scala-sbt" %% "sbinary" % "0.4.1")

lazy val checkPom = taskKey[Unit]("check pom to ensure no <type> sections are generated"); checkPom := {
        val pomFile = makePom.value
        val pom = xml.XML.loadFile(pomFile)
        val tpe = pom \\ "type"
        if(!tpe.isEmpty)
                sys.error("Expected no <type> sections, got: " + tpe + " in \n\n" + pom)
};scalacOptions := Seq("-deprecation")

val b = ( <b/>)
val a = <aaa>

</aaa>
/*

*/


organization := "jozwikr" // OK

scalaVersion := "2.9.2"

organization := "ololol"


"""
  val parsedTest1 = """Apply(Select(Ident(name), $colon$eq), List(Literal(Constant(alaMaKota))))
Apply(Select(Ident(libraryDependencies), $colon$eq), List(Apply(Ident(Seq), List(Apply(Select(Apply(Select(Literal(Constant(org.scala-sbt)), $percent$percent), List(Literal(Constant(sbinary)))), $percent), List(Literal(Constant(0.4.1))))))))
ValDef(Modifiers(2147483648, , List()), checkPom, TypeTree(), Apply(TypeApply(Ident(taskKey), List(Ident(Unit))), List(Literal(Constant(check pom to ensure no <type> sections are generated)))))
Apply(Select(Ident(checkPom), $colon$eq), List(Block(List(ValDef(Modifiers(0, , List()), pomFile, TypeTree(), Select(Ident(makePom), value)), ValDef(Modifiers(0, , List()), pom, TypeTree(), Apply(Select(Select(Ident(xml), XML), loadFile), List(Ident(pomFile)))), ValDef(Modifiers(0, , List()), tpe, TypeTree(), Apply(Select(Ident(pom), $bslash$bslash), List(Literal(Constant(type)))))), If(Select(Select(Ident(tpe), isEmpty), unary_$bang), Apply(Select(Ident(sys), error), List(Apply(Select(Apply(Select(Apply(Select(Literal(Constant(Expected no <type> sections, got: )), $plus), List(Ident(tpe))), $plus), List(Literal(Constant( in 

)))), $plus), List(Ident(pom))))), Literal(Constant(()))))))
Apply(Select(Ident(scalacOptions), $colon$eq), List(Apply(Ident(Seq), List(Literal(Constant(-deprecation))))))
ValDef(Modifiers(0, , List()), b, TypeTree(), Block(List(), Block(List(), Apply(Select(New(Select(Select(Select(Ident(_root_), scala), xml), Elem)), <init>), List(Literal(Constant(null)), Literal(Constant(b)), Select(Select(Select(Ident(_root_), scala), xml), Null), Ident($scope), Literal(Constant(true)))))))
ValDef(Modifiers(0, , List()), a, TypeTree(), Block(List(), Block(List(), Apply(Select(New(Select(Select(Select(Ident(_root_), scala), xml), Elem)), <init>), List(Literal(Constant(null)), Literal(Constant(aaa)), Select(Select(Select(Ident(_root_), scala), xml), Null), Ident($scope), Literal(Constant(false)), Typed(Block(List(ValDef(Modifiers(0, , List()), $buf, TypeTree(), Apply(Select(New(Select(Select(Select(Ident(_root_), scala), xml), NodeBuffer)), <init>), List())), Apply(Select(Ident($buf), $amp$plus), List(Apply(Select(New(Select(Select(Select(Ident(_root_), scala), xml), Text)), <init>), List(Literal(Constant(

))))))), Ident($buf)), Ident(_*)))))))
Apply(Select(Ident(organization), $colon$eq), List(Literal(Constant(jozwikr))))
Apply(Select(Ident(scalaVersion), $colon$eq), List(Literal(Constant(2.9.2))))
Apply(Select(Ident(organization), $colon$eq), List(Literal(Constant(ololol))))"""

  val test2 =
"""name := "newName"
libraryDependencies := Seq("org.scala-sbt" %% "sbinary" % "0.4.1")

lazy val checkPom = taskKey[Unit]("check pom to ensure no <type> sections are generated"); checkPom := {
        val pomFile = makePom.value
        val pom = xml.XML.loadFile(pomFile)
        val tpe = pom \\ "type"
        if(!tpe.isEmpty)
                sys.error("Expected no <type> sections, got: " + tpe + " in \n\n" + pom)
};scalacOptions := Seq("-deprecation")

val b = ( <b/>)
val a = <aaa>

</aaa>
/*

*/


organization := "jozwikr" // OK

scalaVersion := "2.9.2"

organization := "ololol"


"""

  val parsedTest2 = """Apply(Select(Ident(name), $colon$eq), List(Literal(Constant(newName))))
Apply(Select(Ident(libraryDependencies), $colon$eq), List(Apply(Ident(Seq), List(Apply(Select(Apply(Select(Literal(Constant(org.scala-sbt)), $percent$percent), List(Literal(Constant(sbinary)))), $percent), List(Literal(Constant(0.4.1))))))))
ValDef(Modifiers(2147483648, , List()), checkPom, TypeTree(), Apply(TypeApply(Ident(taskKey), List(Ident(Unit))), List(Literal(Constant(check pom to ensure no <type> sections are generated)))))
Apply(Select(Ident(checkPom), $colon$eq), List(Block(List(ValDef(Modifiers(0, , List()), pomFile, TypeTree(), Select(Ident(makePom), value)), ValDef(Modifiers(0, , List()), pom, TypeTree(), Apply(Select(Select(Ident(xml), XML), loadFile), List(Ident(pomFile)))), ValDef(Modifiers(0, , List()), tpe, TypeTree(), Apply(Select(Ident(pom), $bslash$bslash), List(Literal(Constant(type)))))), If(Select(Select(Ident(tpe), isEmpty), unary_$bang), Apply(Select(Ident(sys), error), List(Apply(Select(Apply(Select(Apply(Select(Literal(Constant(Expected no <type> sections, got: )), $plus), List(Ident(tpe))), $plus), List(Literal(Constant( in 

)))), $plus), List(Ident(pom))))), Literal(Constant(()))))))
Apply(Select(Ident(scalacOptions), $colon$eq), List(Apply(Ident(Seq), List(Literal(Constant(-deprecation))))))
ValDef(Modifiers(0, , List()), b, TypeTree(), Block(List(), Block(List(), Apply(Select(New(Select(Select(Select(Ident(_root_), scala), xml), Elem)), <init>), List(Literal(Constant(null)), Literal(Constant(b)), Select(Select(Select(Ident(_root_), scala), xml), Null), Ident($scope), Literal(Constant(true)))))))
ValDef(Modifiers(0, , List()), a, TypeTree(), Block(List(), Block(List(), Apply(Select(New(Select(Select(Select(Ident(_root_), scala), xml), Elem)), <init>), List(Literal(Constant(null)), Literal(Constant(aaa)), Select(Select(Select(Ident(_root_), scala), xml), Null), Ident($scope), Literal(Constant(false)), Typed(Block(List(ValDef(Modifiers(0, , List()), $buf, TypeTree(), Apply(Select(New(Select(Select(Select(Ident(_root_), scala), xml), NodeBuffer)), <init>), List())), Apply(Select(Ident($buf), $amp$plus), List(Apply(Select(New(Select(Select(Select(Ident(_root_), scala), xml), Text)), <init>), List(Literal(Constant(

))))))), Ident($buf)), Ident(_*)))))))
Apply(Select(Ident(organization), $colon$eq), List(Literal(Constant(jozwikr))))
Apply(Select(Ident(scalaVersion), $colon$eq), List(Literal(Constant(2.9.2))))
Apply(Select(Ident(organization), $colon$eq), List(Literal(Constant(ololol))))"""

  val lib = (getClass.getClassLoader match {
    case url: java.net.URLClassLoader =>
      url.getURLs.find {u => new File(u.getFile).getName == "scala-library.jar"}.map{_.getPath}
    case _ => None
  }).get
  val options = "-cp" :: lib :: "-Yrangepos" :: Nil
  val reportError = (msg: String) => System.err.println(msg)
  val globalReporter = scala.tools.nsc.reporters.NoReporter

  for (i <- 1 to 10000) {
  
    val defaultGlobalForParser = {
      val command = new CompilerCommand(options, reportError)
      val settings = command.settings
      settings.outputDirs.setSingleOutput(new VirtualDirectory("(memory)", None))
  
      // Mix Positions, otherwise global ignores -Yrangepos
      val global = new Global(settings, globalReporter) with Positions
      val run = new global.Run
      // Add required dummy unit for initialization...
      val initFile = new BatchSourceFile("<wrapper-init>", "")
      val _ = new global.CompilationUnit(initFile)
      global.phase = run.parserPhase
      global
    }
    import defaultGlobalForParser._
  
    def parse(code:String) = {
      val wrapperFile = new BatchSourceFile("test-${Random.nextInt}", code)
      val unit = new CompilationUnit(wrapperFile)
      val parser = new syntaxAnalyzer.UnitParser(unit)
      val parsedTrees = parser.templateStats()
      parsedTrees.map { t => scala.reflect.runtime.universe.showRaw(t: scala.tools.nsc.Global#Tree) }.mkString("\n")    
    }
  
    def go(in: String, expected:String) = {
      val thread = new Thread {
          override def run {
              val out = parse(in)
              if (out != expected) {
              val deltas = difflib.DiffUtils.diff(out.split("\n").toList.asJava, expected.split("\n").toList.asJava).getDeltas()
              println(deltas.asScala.mkString("\n")+"\nBoom! in run "+i)
  
            }
          }
      }
      thread.start
      thread
    }  
  
    val thr1 = go(test1, parsedTest1)
    val thr2 = go(test2, parsedTest2)
    val thr3 = go(test1, parsedTest1)
    val thr4 = go(test2, parsedTest2)
    val thr5 = go(test1, parsedTest1)
    val thr6 = go(test2, parsedTest2)
    val thr7 = go(test1, parsedTest1)
    val thr8 = go(test2, parsedTest2)
    thr1.join
    thr2.join
    thr3.join
    thr4.join
    thr5.join
    thr6.join
    thr7.join
    thr8.join
  }

}
