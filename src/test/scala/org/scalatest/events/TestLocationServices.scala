package org.scalatest.events

import org.scalatest.Assertions._

trait TestLocationServices {
  private[events] case class TopOfClassPair(className: String, var checked: Boolean = false)
  private[events] case class SeeStackDepthExceptionPair(name: String, var checked: Boolean = false)
  private[events] case class LineInFilePair(message: String, fileName: String, lineNumber: Int, var checked: Boolean = false)
  
  val suiteTypeName: String
  val expectedSuiteStartingList: List[TopOfClassPair]
  val expectedSuiteCompletedList: List[TopOfClassPair]
  val expectedSuiteAbortedList: List[SeeStackDepthExceptionPair]
  val expectedTestFailedList: List[SeeStackDepthExceptionPair]
  val expectedInfoProvidedList: List[LineInFilePair]
  
  private def checkLineInFile(expectedList: List[LineInFilePair], message: String, event: Event) {
    val expectedPairOpt: Option[LineInFilePair] = expectedList.find { pair => pair.message == message }
    expectedPairOpt match {
      case Some(expectedPair) =>
        event.location match {
          case Some(location) => 
            if(location.isInstanceOf[LineInFile]) {
              val lineInFile = location.asInstanceOf[LineInFile]
              assert(expectedPair.fileName == lineInFile.fileName, "Suite " + suiteTypeName + " - Event " + event.getClass.getName + " expected " + expectedPair.fileName +", got " + lineInFile.fileName)
              assert(expectedPair.lineNumber == lineInFile.lineNumber, "Suite " + suiteTypeName + " - Event " + event.getClass.getName + " expected " + expectedPair.lineNumber + ", got " + lineInFile.lineNumber)
              expectedPair.checked = true
            }
            else
              fail("Suite " + suiteTypeName + "'s " + event.getClass.getName + " event expect to have LineInFile location, but got " + location.getClass.getName)
          case None => fail("Suite " + suiteTypeName + "'s '" + event.getClass.getName + " does not have location (None)")
        }
      case None => fail("Suite " + suiteTypeName + " got unexpected info message '" + message + "' for event " + event.getClass.getName)
    }
  }
  
  private def checkTopOfClass(expectedList: List[TopOfClassPair], suiteId: String, event: Event) {
    val expectedPairOpt: Option[TopOfClassPair] = expectedList.find { pair => pair.className == suiteId }
    expectedPairOpt match {
      case Some(expectedPair) => 
        event.location match {
          case Some(location) => 
            location match {
            case topOfClass: TopOfClass => 
              assert(suiteId == topOfClass.className, "Suite " + suiteTypeName + "'s " + event.getClass.getName + " event's TopOfClass.className expected to be " + suiteId + ", but got " + topOfClass.className)
              expectedPair.checked = true
            case _ => fail("Suite " + suiteTypeName + "'s " + event.getClass.getName + " event expect to have TopOfClass location, but got " + location.getClass.getName)
          }
          case None => fail("Suite " + suiteTypeName + "'s " + event.getClass.getName + " does not have location (None)")
        }
      case None => fail("Suite " + suiteTypeName + " got unexpected " + suiteId + " for event " + event.getClass.getName)
    }
  }
  
  private def checkSeeStackDepthExceptionPair(expectedList: List[SeeStackDepthExceptionPair], expectedName: String, event: Event) {
    val expectedPairOpt: Option[SeeStackDepthExceptionPair] = expectedList.find { pair => pair.name == expectedName }
    expectedPairOpt match {
      case Some(expectedPair) =>
        event.location match {
          case Some(location) =>
            assert(location == SeeStackDepthException, "Suite " + suiteTypeName + "'s " + event.getClass.getName + " event expect to have SeeStackDepthException location, but got " + location.getClass.getName)
            expectedPair.checked = true
          case None => fail("Suite " + suiteTypeName + "'s " + event.getClass.getName + " does not have location (None)")
        }
      case None => fail("Suite " + suiteTypeName + " got unexpected " + expectedName + " for event " + event.getClass.getName)
    }
  }
  
  def checkFun(event: Event) {
    event match {
      case suiteStarting: SuiteStarting => checkTopOfClass(expectedSuiteStartingList, suiteStarting.suiteID, event)
      case suiteCompleted: SuiteCompleted => checkTopOfClass(expectedSuiteCompletedList, suiteCompleted.suiteID, event)
      case suiteAborted: SuiteAborted => checkSeeStackDepthExceptionPair(expectedSuiteAbortedList, suiteAborted.suiteID, event)
      case testFailed: TestFailed => checkSeeStackDepthExceptionPair(expectedTestFailedList, testFailed.testName, event)
      case infoProvided: InfoProvided => checkLineInFile(expectedInfoProvidedList, infoProvided.message, event)
      case _ => // Tested in LocationMethodSuiteProp or LocationFunctionSuiteProp
    }
  }
  
  def allChecked = {
    expectedSuiteStartingList.foreach { pair => assert(pair.checked, suiteTypeName + ": SuiteStarting for " + pair.className + " not fired.") }
    expectedSuiteCompletedList.foreach { pair => assert(pair.checked, suiteTypeName + ": SuiteCompleted for " + pair.className + " not fired.") }
    expectedSuiteAbortedList.foreach { pair => assert(pair.checked, suiteTypeName + ": SuiteAborted for " + pair.name + " not fired.") }
    expectedTestFailedList.foreach { pair => assert(pair.checked, suiteTypeName + ": TestFailed for " + pair.name + " not fired.") }
    expectedInfoProvidedList.foreach { pair => assert(pair.checked, suiteTypeName + ": InfoProvided for " + pair.message + " not fired.") }
  }
}