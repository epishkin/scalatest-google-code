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
      val temp1 = shouldLine.replaceAll("<code>must</code>", "<code>I_WAS_must_ORIGINALLY</code>")
      val temp2 = temp1.replaceAll(
        "<a href=\"MustMatchers.html\"><code>MustMatchers</code></a>",
        "<a href=\"I_WAS_Must_ORIGINALLYMatchers.html\"><code>I_WAS_Must_ORIGINALLYMatchers</code></a>"
      )
      val temp3 = temp2.replaceAll("should", "must")
      val temp4 = temp3.replaceAll("Should", "Must")
      val temp5 = temp4.replaceAll("I_WAS_must_ORIGINALLY", "should")
      val mustLine = temp5.replaceAll("I_WAS_Must_ORIGINALLY", "Should")
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

