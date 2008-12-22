package org.scalatest

class ExamplesSuite extends FunSuite {

  class MyOtherExamples extends Examples {
    it("should lead the whole game") {}
    it("should lead just part of the game") {}
  }

  test("that duplicate specTexts result in a thrown exception at construction time") {

    new MyOtherExamples

    class MyExamples extends Examples {
      it("should lead the whole game") {}
      it("should lead the whole game") {}
    }
    intercept[IllegalArgumentException] {
      new MyExamples  
    }
  }
  test("duplicate testNames should result in an exception when one is in the Examples and the other in the Spec") {
    class MySpec extends Spec {
      includeExamples(new MyOtherExamples)
      it("should lead the whole game") {}
    }
    intercept[IllegalArgumentException] {
      new MySpec  
    }
    class MyOtherSpec extends Spec {
      it("should lead the whole game") {}
      includeExamples(new MyOtherExamples)
    }
    intercept[IllegalArgumentException] {
      new MyOtherSpec  
    }
  }

  test("that a null specText results in a thrown NPE at construction time") {
    intercept[NullPointerException] {
      new Examples {
        it(null) {}
      }
    }
  }

  test("groups work correctly in Examples") {

    val aEx = new Examples {
      it("test this", mygroups.SlowAsMolasses) {}
      ignore("test that", mygroups.SlowAsMolasses) {}
    }
    val a = new Spec {
      includeExamples(aEx)
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses"), "test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
      a.groups
    }

    val bEx = new Examples {}
    val b = new Spec {
      includeExamples(bEx)
    }
    expect(Map()) {
      b.groups
    }

    val cEx = new Examples {
      it("test this", mygroups.SlowAsMolasses, mygroups.WeakAsAKitten) {}
      it("test that", mygroups.SlowAsMolasses) {}
    }
    val c = new Spec {
      includeExamples(cEx)
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
      c.groups
    }
  }
}