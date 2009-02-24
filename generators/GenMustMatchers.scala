import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter
import scala.io.Source

object GenMustMatchers extends Application {
  val dir = new File("build/generated/src/org/scalatest")
  dir.mkdirs()
  val writer = new BufferedWriter(new FileWriter("build/generated/src/org/scalatest/MustMatchers.scala"))
  try {
    val shouldLines = Source.fromFile("src/org/scalatest/ShouldMatchers.scala").getLines.toList
    for (shouldLine <- shouldLines) {
      val mustLine = shouldLine.replaceAll("should", "must").replaceAll("Should", "Must")
      writer.write(mustLine.toString)
    }
  }
  finally {
    writer.close()
  }
}

object GenMustMatchersTests extends Application {

  val dir = new File("build/generated/tests/org/scalatest")
  dir.mkdirs()
  val shouldFileNames =
    List(
      "ShouldBehaveLikeSpec.scala",
      "ShouldContainElementSpec.scala",
      "ShouldContainKeySpec.scala",
      "ShouldContainValueSpec.scala",
      "ShouldEqualSpec.scala",
      "ShouldHavePropertiesSpec.scala",
      "ShouldLengthSpec.scala",
      "ShouldOrderedSpec.scala",
      "ShouldSizeSpec.scala",
      "ShouldStackSpec.scala",
      "ShouldBeASymbolSpec.scala",
      "ShouldBeAnSymbolSpec.scala",
      "ShouldBeMatcherSpec.scala",
      "ShouldBePropertyMatcherSpec.scala",
      "ShouldBeSymbolSpec.scala",
      "ShouldEndWithRegexSpec.scala",
      "ShouldEndWithSubstringSpec.scala",
      "ShouldFullyMatchSpec.scala",
      "ShouldIncludeRegexSpec.scala",
      "ShouldIncludeSubstringSpec.scala",
      "ShouldLogicalMatcherExprSpec.scala",
      "ShouldMatcherSpec.scala",
      "ShouldPlusOrMinusSpec.scala",
      "ShouldSameInstanceAsSpec.scala",
      "ShouldStartWithRegexSpec.scala",
      "ShouldStartWithSubstringSpec.scala"
    )

  for (shouldFileName <- shouldFileNames) {

    val mustFileName = shouldFileName.replace("Should", "Must")
    val writer = new BufferedWriter(new FileWriter("build/generated/tests/org/scalatest/" + mustFileName))
    try {
      val shouldLines = Source.fromFile("tests/org/scalatest/" + shouldFileName).getLines.toList
      for (shouldLine <- shouldLines) {
        val mustLine = shouldLine.replaceAll("should", "must").replaceAll("Should", "Must")
        writer.write(mustLine.toString)
      }
    }
    finally {
      writer.close()
    }
  }
}

