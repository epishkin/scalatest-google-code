
import org.scalatest.tools.Runner

object runem extends App {
  for (i <- 1 to 200) {
    Runner.run(
      Array("-p", ".", "-oD", "-F", "dashboard", "-s", "UnitedStates")
    )
    Thread.sleep(1000)
  }
}

