
import org.scalatest.tools.Runner

object runem extends App {
  for (i <- 1 to 100)
    Runner.run(
      Array("-p", ".", "-o", "-s", "UnitedStates")
    )
}

