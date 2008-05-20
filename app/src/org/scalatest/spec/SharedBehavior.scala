package org.scalatest.spec

trait SharedBehavior {

  /*private*/ case class Example(exampleName: String, f: () => Unit)

  /*private*/ var examples: List[Example] = Nil

  class Inifier(exampleName: String) {
    def in(f: => Unit) {
      examples ::= Example(exampleName, f _)
    }
  }
  
  class Itifier {
    def should(exampleName: String) = new Inifier(exampleName)
  }

  protected def it = new Itifier
  
  def execute(reporter: Reporter, stopper: Stopper) {
    examples.reverse.foreach { _.f() }
  }
}
