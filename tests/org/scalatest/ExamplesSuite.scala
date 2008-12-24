package org.scalatest

class ExamplesSuite extends FunSuite {

  test("that duplicate specTexts result in a thrown exception at construction time") {

    class MySpec extends Spec {

      def myOtherExamples() {
        it("should lead the whole game") {}
        it("should lead just part of the game") {}
      }

      myOtherExamples()

      def myExamples() {
        it("should lead the whole game") {}
        it("should lead the whole game") {}
      }

      intercept[IllegalArgumentException] {
        myExamples()
      }
    }

    new MySpec
  }

  test("duplicate testNames should result in an exception when one is in the Examples and the other in the Spec") {
    class MySpec extends Spec {
      def myOtherExamples() {
        it("should lead the whole game") {}
        it("should lead just part of the game") {}
      }
      myOtherExamples()
      it("should lead the whole game") {}
    }
    intercept[IllegalArgumentException] {
      new MySpec  
    }
    class MyOtherSpec extends Spec {
      def myOtherExamples() {
        it("should lead the whole game") {}
        it("should lead just part of the game") {}
      }
      it("should lead the whole game") {}
      myOtherExamples()
    }
    intercept[IllegalArgumentException] {
      new MyOtherSpec  
    }
  }

  test("that a null specText results in a thrown NPE at construction time") {

    class MySpec extends Spec {

      def examples() {
        it(null) {}
      }
      intercept[NullPointerException] {
        examples()
      }
    }
    new MySpec
  }

  test("groups work correctly in Examples") {

    val a = new Spec {
      def aExamples() {
        it("test this", mygroups.SlowAsMolasses) {}
        ignore("test that", mygroups.SlowAsMolasses) {}
      }
      aExamples()
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses"), "test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
      a.groups
    }

    val b = new Spec {
      def bExamples() {}
      bExamples()
    }
    expect(Map()) {
      b.groups
    }

    val c = new Spec {
      def cExamples() {
        it("test this", mygroups.SlowAsMolasses, mygroups.WeakAsAKitten) {}
        it("test that", mygroups.SlowAsMolasses) {}
      }
      cExamples()
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
      c.groups
    }
  }
}