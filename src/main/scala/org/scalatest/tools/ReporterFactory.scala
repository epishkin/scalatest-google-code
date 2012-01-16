package org.scalatest.tools
import org.scalatest.Reporter
import org.scalatest.Resources
import org.scalatest.DispatchReporter

class ReporterFactory {
  
  private[tools] def configSetMinusNonFilterParams(configSet: Set[ReporterConfigParam]) =
    (((configSet - PresentShortStackTraces) - PresentFullStackTraces) - PresentWithoutColor) - PresentAllDurations
    
  private[tools] def getCustomReporter(reporterClassName: String, loader: ClassLoader, argString: String): Reporter = {
    try {
      val reporterClass: java.lang.Class[_] = loader.loadClass(reporterClassName) 
      reporterClass.newInstance.asInstanceOf[Reporter]
    }    // Could probably catch ClassCastException too
    catch {
      case e: ClassNotFoundException => {

        val msg1 = Resources("cantLoadReporterClass", reporterClassName)
        val msg2 = Resources("probarg", argString)
        val msg = msg1 + "\n" + msg2
    
        val iae = new IllegalArgumentException(msg)
        iae.initCause(e)
        throw iae
      }
      case e: InstantiationException => {

        val msg1 = Resources("cantInstantiateReporter", reporterClassName)
        val msg2 = Resources("probarg", argString)
        val msg = msg1 + "\n" + msg2
    
        val iae = new IllegalArgumentException(msg)
        iae.initCause(e)
        throw iae
      }
      case e: IllegalAccessException => {

        val msg1 = Resources("cantInstantiateReporter", reporterClassName)
        val msg2 = Resources("probarg", argString)
        val msg = msg1 + "\n" + msg2
    
        val iae = new IllegalArgumentException(msg)
        iae.initCause(e)
        throw iae
      }
    }
  }
  
  protected def createStandardOutReporter(configSet: Set[ReporterConfigParam]) = {
    if (configSetMinusNonFilterParams(configSet).isEmpty)
      new StandardOutReporter(
        configSet.contains(PresentAllDurations),
        !configSet.contains(PresentWithoutColor),
        configSet.contains(PresentShortStackTraces) || configSet.contains(PresentFullStackTraces),
        configSet.contains(PresentFullStackTraces) // If they say both S and F, F overrules
      )
    else
      new FilterReporter(
        new StandardOutReporter(
          configSet.contains(PresentAllDurations),
          !configSet.contains(PresentWithoutColor),
          configSet.contains(PresentShortStackTraces) || configSet.contains(PresentFullStackTraces),
          configSet.contains(PresentFullStackTraces) // If they say both S and F, F overrules
        ),
        configSet
      )
  }
  
  protected def createStandardErrReporter(configSet: Set[ReporterConfigParam]) = {
    if (configSetMinusNonFilterParams(configSet).isEmpty)
      new StandardErrReporter(
        configSet.contains(PresentAllDurations),
        !configSet.contains(PresentWithoutColor),
        configSet.contains(PresentShortStackTraces) || configSet.contains(PresentFullStackTraces),
        configSet.contains(PresentFullStackTraces) // If they say both S and F, F overrules
      )
      else
        new FilterReporter(
          new StandardErrReporter(
            configSet.contains(PresentAllDurations),
            !configSet.contains(PresentWithoutColor),
            configSet.contains(PresentShortStackTraces) || configSet.contains(PresentFullStackTraces),
            configSet.contains(PresentFullStackTraces) // If they say both S and F, F overrules
          ),
          configSet
        )
  }
  
  protected def createFileReporter(configSet: Set[ReporterConfigParam], fileName: String) = {
    if (configSetMinusNonFilterParams(configSet).isEmpty)
      new FileReporter(
        fileName,
        configSet.contains(PresentAllDurations),
        !configSet.contains(PresentWithoutColor),
        configSet.contains(PresentShortStackTraces) || configSet.contains(PresentFullStackTraces),
        configSet.contains(PresentFullStackTraces) // If they say both S and F, F overrules
      )
      else
        new FilterReporter(
          new FileReporter(
            fileName,
            configSet.contains(PresentAllDurations),
            !configSet.contains(PresentWithoutColor),
            configSet.contains(PresentShortStackTraces) || configSet.contains(PresentFullStackTraces),
            configSet.contains(PresentFullStackTraces) // If they say both S and F, F overrules
          ),
          configSet
        )
  }
  
  protected def createXmlReporter(configSet: Set[ReporterConfigParam], directory: String) = {
    new XmlReporter(directory)
  }
  
  protected def createHtmlReporter(configSet: Set[ReporterConfigParam], fileName: String) = {
    if (configSetMinusNonFilterParams(configSet).isEmpty)
      new HtmlReporter(
        fileName,
        configSet.contains(PresentAllDurations),
        !configSet.contains(PresentWithoutColor),
        configSet.contains(PresentShortStackTraces) || configSet.contains(PresentFullStackTraces),
        configSet.contains(PresentFullStackTraces) // If they say both S and F, F overrules
      )
      else
        new FilterReporter(
          new HtmlReporter(
            fileName,
            configSet.contains(PresentAllDurations),
            !configSet.contains(PresentWithoutColor),
            configSet.contains(PresentShortStackTraces) || configSet.contains(PresentFullStackTraces),
            configSet.contains(PresentFullStackTraces) // If they say both S and F, F overrules
          ),
          configSet
        )
  }
  
  protected def createCustomReporter(configSet: Set[ReporterConfigParam], reporterClassName: String, loader: ClassLoader) = {
    val customReporter = getCustomReporter(reporterClassName, loader, "-r... " + reporterClassName)
    if (configSet.isEmpty)
      customReporter
    else
      new FilterReporter(customReporter, configSet)
  }
  
  private[scalatest] def getDispatchReporter(reporterSpecs: ReporterConfigurations, graphicReporter: Option[Reporter], passFailReporter: Option[Reporter], loader: ClassLoader) = {

    def getReporterFromConfiguration(configuration: ReporterConfiguration): Reporter =

      configuration match {
        case StandardOutReporterConfiguration(configSet) => createStandardOutReporter(configSet)
        case StandardErrReporterConfiguration(configSet) => createStandardErrReporter(configSet)
        case FileReporterConfiguration(configSet, fileName) => createFileReporter(configSet, fileName)
        case XmlReporterConfiguration(configSet, directory) => createXmlReporter(configSet, directory)
        case HtmlReporterConfiguration(configSet, fileName) => createHtmlReporter(configSet, fileName)
        case CustomReporterConfiguration(configSet, reporterClassName) => createCustomReporter(configSet, reporterClassName, loader) 
        case GraphicReporterConfiguration(configSet) => throw new RuntimeException("Should never happen.")
    }

    val reporterSeq =
      (for (spec <- reporterSpecs)
        yield getReporterFromConfiguration(spec))

    val almostFullReporterList: List[Reporter] =
      graphicReporter match {
        case None => reporterSeq.toList
        case Some(gRep) => gRep :: reporterSeq.toList
      }
      
    val fullReporterList: List[Reporter] =
      passFailReporter match {
        case Some(pfr) => pfr :: almostFullReporterList
        case None => almostFullReporterList
      }

    new DispatchReporter(fullReporterList)
  }
}

object ReporterFactory extends ReporterFactory