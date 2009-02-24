import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter
import scala.io.Source

object GenMustMatchers  extends Application {
  val dir = new File("build/generated/org/scalatest")
  dir.mkdirs()
  val writer = new BufferedWriter(new FileWriter("build/generated/org/scalatest/MustMatchers.scala"))
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
