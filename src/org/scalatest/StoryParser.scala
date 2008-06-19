abstract class StoryParser {

  type F
  def createFixture: F

  implicit def stringToZerosie(first: String) = new Zerosie(first)

  class Zerosie(val first: String) {
    def ___ (second: String): Onesie = new Onesie(first: String, second: String)
  }

  class Onesie(val first: String, val second: String) {
    def ___ (third: String): Twosie = new Twosie(first: String, second: String, third: String)
  }

  class Twosie (val first: String, val second: String, val third: String)

  def given(twosie: Twosie)(f: (F, String, String) => Unit) { /* register the function */ }
}

