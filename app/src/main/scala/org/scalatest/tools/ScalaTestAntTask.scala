package org.scalatest.tools

import scala.collection.mutable.ListBuffer

import org.apache.tools.ant.BuildException
import org.apache.tools.ant.Task
import org.apache.tools.ant.types.Path

import org.apache.tools.ant.AntClassLoader
import org.apache.tools.ant.taskdefs.Java

/**
 * <p>
 * An ant task to run scalatest.  Instructions on how to specify various
 * options are below.  See the scaladocs for the Runner class for a description
 * of what each of the options does.
 * </p>
 *
 * <p>
 * Define task in your ant file using taskdef, e.g.:
 * </p>
 *
 * <pre>
 *  &lt;path id="scalatest.classpath"&gt;
 *    &lt;pathelement location="${lib}/scalatest.jar"/&gt;
 *    &lt;pathelement location="${lib}/scala-library-2.6.1-final.jar"/&gt;
 *  &lt;/path&gt;
 *
 *  &lt;target name="main" depends="dist"&gt;
 *    &lt;taskdef name="scalatest" classname="org.scalatest.tools.ScalaTestTask"&gt;
 *      &lt;classpath refid="scalatest.classpath"/&gt;
 *    &lt;/taskdef&gt;
 *
 *    &lt;scalatest ...
 *  &lt;/target&gt;
 * </pre>
 *
 * <p>
 * Specify user-defined properties using nested &lt;property&gt; elements,
 * e.g.:
 * </p>
 *
 * <pre>
 *   &lt;scalatest&gt;
 *     &lt;property name="dbname" value="testdb"/&gt;
 *     &lt;property name="server" value="192.168.1.188"/&gt;
 * </pre>
 *
 * <p>
 * Specify a runpath using either a 'runpath' attribute and/or nested
 * &lt;runpath&gt; elements, using standard ant path notation, e.g.:
 * </p>
 *
 * <pre>
 *   &lt;scalatest runpath="serviceuitest-1.1beta4.jar:myjini"&gt;
 * </pre>
 *
 * or
 *
 * <pre>
 *   &lt;scalatest&gt;
 *     &lt;runpath&gt;
 *       &lt;pathelement location="serviceuitest-1.1beta4.jar"/&gt;
 *       &lt;pathelement location="myjini"/&gt;
 *     &lt;/runpath&gt;
 * </pre>
 *
 * <p>
 * To add a url to your runpath, use a &lt;runpathurl&gt; element
 * (since ant paths don't support url's), e.g.:
 * </p>
 *
 * <pre>
 *   &lt;scalatest&gt;
 *     &lt;runpathurl url="http://foo.com/bar.jar"/&gt;
 * </pre>
 *
 * <p>
 * Specify reporters using nested &lt;reporter&gt; elements, where the 'type'
 * attribute must be one of the following:
 * </p>
 *
 * <ul>
 *   <li>  graphic          </li>
 *   <li>  file             </li>
 *   <li>  xml              </li>
 *   <li>  stdout           </li>
 *   <li>  stderr           </li>
 *   <li>  reporterclass    </li>
 * </ul>
 *
 * <p>
 * Each may include a config attribute to specify the reporter configuration.
 * Types 'file', 'xml' and 'reporterclass' require additional attributes
 * 'filename', 'directory', and 'classname', respectively.  E.g.:
 * </p>
 *
 * <pre>
 *   &lt;scalatest&gt;
 *     &lt;reporter type="stdout"        config="FAB"/&gt;
 *     &lt;reporter type="file"          filename="test.out"/&gt;
 *     &lt;reporter type="xml"           directory="target"/&gt;
 *     &lt;reporter type="reporterclass" classname="my.ReporterClass"/&gt;
 * </pre>
 *
 * <p>
 * Specify tag includes and excludes using &lt;tagsToInclude&gt; and
 * &lt;tagsToExclude&gt; elements.  E.g.:
 * </p>
 *
 * <pre>
 *   &lt;scalatest&gt;
 *     &lt;tagsToInclude&gt;
 *         CheckinTests
 *         FunctionalTests
 *     &lt;/tagsToInclude&gt;
 *
 *     &lt;tagsToExclude&gt;
 *         SlowTests
 *         NetworkTests
 *     &lt;/tagsToExclude&gt;
 * </pre>
 *
 * <p>
 * Specify suites using either a 'suite' attribute or nested
 * &lt;suite&gt; elements.  E.g.:
 * </p>
 *
 * <pre>
 *   &lt;scalatest suite="com.artima.serviceuitest.ServiceUITestkit"&gt;
 * </pre>
 *
 * <p>
 * or
 * </p>
 *
 * <pre>
 *   &lt;scalatest&gt;
 *     &lt;suite classname="com.artima.serviceuitest.ServiceUITestkit"/&gt;
 * </pre>
 *
 * <p>
 * To specify suites using members-only or wildcard package names, use
 * either the membersonly or wildcard attributes, or nested
 * &lt;membersonly&gt; or &lt;wildcard&gt; elements.  E.g.:
 * </p>
 *
 * <pre>
 *   &lt;scalatest membersonly="com.artima.serviceuitest"&gt;
 * </pre>
 *
 * <p>
 * or
 * </p>
 *
 * <pre>
 *   &lt;scalatest wildcard="com.artima.joker"&gt;
 * </pre>
 *
 * <p>
 * or
 * </p>
 *
 * <pre>
 *   &lt;scalatest&gt;
 *     &lt;membersonly package="com.artima.serviceuitest"/&gt;
 *     &lt;wildcard package="com.artima.joker"/&gt;
 * </pre>
 *
 * <p>
 * Use attribute parallel="true" to specify parallel execution of Suites.
 * When parallel is true, use optional numthreads attribute to specify number
 * of threads to be included in thread pool (e.g. numthreads="10").
 * </p>
 *
 * <p>
 * Use attribute haltonfailure="true" to cause ant to fail the
 * build if there's a test failure.
 * </p>
 *
 * <p>
 * Use attribute fork="true" to cause ant to run the tests in
 * a separate process.
 * </p>
 *
 * <p>
 * When fork is true, attribute maxmemory may be used to specify
 * the max memory size that will be passed to the forked jvm.&nbsp;
 * E.g.:
 *
 * <pre>
 *   &lt;scalatest maxmemory="1280M"&gt;
 * </pre>
 *
 * will cause "-Xmx1280M" to be passed to the java command used to
 * run the tests.
 * </p>
 *
 * <p>
 * When fork is true, nested &lt;jvmarg&gt; elements may be used
 * to pass additional arguments to the forked jvm.
 * E.g., if you are running into 'PermGen space' memory errors,
 * you could add this arg to bump up the jvm's MaxPermSize value:
 *
 * <pre>
 *   &lt;jvmarg value="-XX:MaxPermSize=128m"/&gt;
 * </pre>
 * </p>
 *
 * @author George Berger
 */
class ScalaTestAntTask extends Task {
  private var includes:  String = null
  private var excludes:  String = null
  private var maxMemory: String = null

  private var parallel      = false
  private var haltonfailure = false
  private var fork          = false

  private var numthreads = 0

  private val runpath      = new ListBuffer[String]
  private val jvmArgs      = new ListBuffer[String]
  private val suites       = new ListBuffer[String]
  private val membersonlys = new ListBuffer[String]
  private val wildcards    = new ListBuffer[String]
  private val testNGSuites = new ListBuffer[String]

  private val reporters  = new ListBuffer[ReporterElement]
  private val properties = new ListBuffer[NameValuePair]

  //
  // Executes the task.
  //
  override def execute {
    val args = new ListBuffer[String]

    addSuiteArgs(args)
    addReporterArgs(args)
    addPropertyArgs(args)
    addIncludesArgs(args)
    addExcludesArgs(args)
    addRunpathArgs(args)
    addTestNGSuiteArgs(args)
    addParallelArg(args)

    val argsArray = args.toArray

    val success = if (fork) javaTaskRunner(args.toList)
                  else      Runner.run(argsArray)

    if (!success && haltonfailure)
      throw new BuildException("ScalaTest run failed.")
  }

  private def javaTaskRunner(args: List[String]): Boolean = {
    val java = new Java
    java.bindToOwner(this)
    java.init()
    java.setFork(true)
    java.setClassname("org.scalatest.tools.Runner")

    val classLoader = getClass.getClassLoader.asInstanceOf[AntClassLoader]

    java.setClasspath(new Path(getProject, classLoader.getClasspath))

    if (maxMemory != null) java.createJvmarg.setValue("-Xmx" + maxMemory)

    for (jvmArg <- jvmArgs)
      java.createJvmarg.setValue(jvmArg)

    for (arg <- args)
      java.createArg.setValue(arg)

    val result = java.executeJava

    return (result == 0)
  }

  //
  // Adds '-p runpath' arg pair to args list if a runpath
  // element or attribute was specified for task.
  //
  private def addRunpathArgs(args: ListBuffer[String]) {
    if (runpath.size > 0) {
      args += "-p"
      args += getSpacedOutPathStr(runpath.toList)
    }
  }

  private def addTestNGSuiteArgs(args: ListBuffer[String]) {
    if (testNGSuites.size > 0) {
      args += "-t"
      args += getSpacedOutPathStr(testNGSuites.toList)
    }
  }
  
  //
  // Adds '-c' arg to args list if 'parallel' attribute was
  // specified true for task.
  //
  private def addParallelArg(args: ListBuffer[String]) {
    if (parallel) {
      args += "-c" + (if (numthreads > 0) ("" + numthreads) else "")
    }
  }

  //
  // Adds '-n includes-list' arg pair to args list if a <tagsToInclude>
  // element was supplied for task.
  //
  private def addIncludesArgs(args: ListBuffer[String]) {
    if (includes != null) {
      args += "-n"
      args += singleSpace(includes)
    }
  }

  //
  // Adds '-l excludes-list' arg pair to args list if an <excludes>
  // element was supplied for task.
  //
  private def addExcludesArgs(args: ListBuffer[String]) {
    if (excludes != null) {
      args += "-l"
      args += singleSpace(excludes)
    }
  }

  //
  // Adds '-Dname=value' argument to args list for each nested
  // <property> element supplied for task.
  //
  private def addPropertyArgs(args: ListBuffer[String]) {
    for (pair <- properties)
      args += "-D" + pair.getName + "=" + pair.getValue
  }

  //
  // Adds '-s classname' argument to args list for each suite
  // specified for task.  Adds '-m packagename' for each
  // membersonly element specified, and '-w packagename' for
  // each wildcard element specified.
  //
  private def addSuiteArgs(args: ListBuffer[String]) {
    for (suite <- suites) {
      if (suite == null)
        throw new BuildException(
          "missing classname attribute for <suite> element")
      args += "-s"
      args += suite
    }

    for (packageName <- membersonlys) {
      if (packageName == null)
        throw new BuildException(
          "missing package attribute for <membersonly> element")
      args += "-m"
      args += packageName
    }

    for (packageName <- wildcards) {
      if (packageName == null)
        throw new BuildException(
          "missing package attribute for <wildcard> element")
      args += "-w"
      args += packageName
    }
  }

  //
  // Adds appropriate reporter options to args list for each
  // nested reporter element specified for task.  Defaults to
  // stdout if no reporter specified.
  //
  private def addReporterArgs(args: ListBuffer[String]) {
    if (reporters.size == 0)
      args += "-o"

    for (reporter <- reporters) {
      reporter.getType match {
        case "stdout"        => addReporterOption(args, reporter, "-o")
        case "stderr"        => addReporterOption(args, reporter, "-e")
        case "graphic"       => addReporterOption(args, reporter, "-g")
        case "file"          => addFileReporter(args, reporter)
        case "xml"           => addXmlReporter(args, reporter)
        case "html"          => addHtmlReporter(args, reporter)
        case "reporterclass" => addReporterClass(args, reporter)

        case t =>
          throw new BuildException("unexpected reporter type [" + t + "]")
      }
    }
  }

  //
  // Adds specified option to args for reporter.  Appends reporter
  // config string to option if specified, e.g. "-eFAB".
  //
  private def addReporterOption(args: ListBuffer[String],
                                reporter: ReporterElement,
                                option: String)
  {
    val config = reporter.getConfig

    if (config == null) args += option
    else                args += option + config
  }

  //
  // Adds '-f' file reporter option to args.  Appends reporter
  // config string to option if specified.  Adds reporter's
  // filename as additional argument, e.g. "-fFAB", "filename".
  //
  private def addFileReporter(args: ListBuffer[String],
                              reporter: ReporterElement)
  {
    addReporterOption(args, reporter, "-f")

    if (reporter.getFilename == null)
      throw new BuildException(
        "reporter type 'file' requires 'filename' attribute")

    args += reporter.getFilename
  }

  //
  // Adds '-u' xml reporter option to args.  Adds reporter's
  // directory as additional argument, e.g. "-u", "directory".
  //
  private def addXmlReporter(args: ListBuffer[String],
                             reporter: ReporterElement)
  {
    addReporterOption(args, reporter, "-u")

    if (reporter.getDirectory == null)
      throw new BuildException(
        "reporter type 'xml' requires 'directory' attribute")

    args += reporter.getDirectory
  }

  //
  // Adds '-h' html reporter option to args.  Appends reporter
  // config string to option if specified.  Adds reporter's
  // filename as additional argument, e.g. "-hFAB", "filename".
  //
  private def addHtmlReporter(args: ListBuffer[String],
                              reporter: ReporterElement)
  {
    addReporterOption(args, reporter, "-h")

    if (reporter.getFilename == null)
      throw new BuildException(
        "reporter type 'html' requires 'filename' attribute")

    args += reporter.getFilename
  }

  //
  // Adds '-r' reporter class option to args.  Appends
  // reporter config string to option if specified.  Adds
  // reporter's classname as additional argument, e.g. "-rFAB",
  // "my.ReporterClass".
  //
  private def addReporterClass(args: ListBuffer[String],
                               reporter: ReporterElement)
  {
    addReporterOption(args, reporter, "-r")

    if (reporter.getClassName == null)
      throw new BuildException(
        "reporter type 'reporterclass' requires 'classname' attribute")

    args += reporter.getClassName
  }

  //
  // Sets value of 'runpath' attribute.
  //
  def setRunpath(runpath: Path) {
    for (element <- runpath.list) {
      this.runpath += element
    }
  }
  
  //
  // Sets value of 'haltonfailure' attribute.
  //
  def setHaltonfailure(haltonfailure: Boolean) {
    this.haltonfailure = haltonfailure
  }
  
  //
  // Sets value of 'fork' attribute.
  //
  def setFork(fork: Boolean) {
    this.fork = fork
  }
  
  //
  // Sets value of 'maxmemory' attribute.
  //
  def setMaxmemory(max: String) {
    this.maxMemory = max
  }
  
  def setTestNGSuites(testNGSuitePath: Path) {
    for (element <- testNGSuitePath.list)
      this.testNGSuites += element
  }

  //
  // Sets value of 'concurrent' attribute.
  //
  // DEPRECATED in 1.0
  //
  def setConcurrent(concurrent: Boolean) {
    Console.err.println("WARNING: 'concurrent' attribute is deprecated " +
                        "- please use 'parallel' instead")
    this.parallel = concurrent
  }

  //
  // Sets value of 'numthreads' attribute.
  //
  def setNumthreads(numthreads: Int) {
      this.numthreads = numthreads
  }

  //
  // Sets value of 'parallel' attribute.
  //
  def setParallel(parallel: Boolean) {
      this.parallel = parallel
  }

  //
  // Sets value from nested element 'runpath'.
  //
  def addConfiguredRunpath(runpath: Path) {
    for (element <- runpath.list)
      this.runpath += element
  }
 
  def addConfiguredTestNGSuites(testNGSuitePath: Path) {
    for (element <- testNGSuitePath.list)
      this.testNGSuites += element
  }

  //
  // Sets value from nested element 'runpathurl'.
  //
  def addConfiguredRunpathUrl(runpathurl: RunpathUrl) {
    runpath += runpathurl.getUrl
  }

  //
  // Sets value from nested element 'jvmarg'.
  //
  def addConfiguredJvmArg(arg: JvmArg) {
    jvmArgs += arg.getValue
  }

  //
  // Sets values from nested element 'property'.
  //
  // DEPRECATED in 0.9.6
  //
  def addConfiguredProperty(property: NameValuePair) {
    Console.err.println("WARNING: <property> is deprecated - " +
                        "please use <config> instead [name: " +
                        property.getName + "]")
    properties += property
  }

  //
  // Sets values from nested element 'config'.
  //
  def addConfiguredConfig(config: NameValuePair) {
    properties += config
  }

  //
  // Sets value of 'suite' attribute.
  //
  def setSuite(suite: String) {
    suites += suite
  }

  //
  // Sets value of 'membersonly' attribute.
  //
  def setMembersonly(packageName: String) {
    membersonlys += packageName
  }

  //
  // Sets value of 'wildcard' attribute.
  //
  def setWildcard(packageName: String) {
    wildcards += packageName
  }

  //
  // Sets value from nested element 'suite'.
  //
  def addConfiguredSuite(suite: SuiteElement) {
    suites += suite.getClassName
  }

  //
  // Sets value from nested element 'membersonly'.
  //
  def addConfiguredMembersOnly(membersonly: PackageElement) {
    membersonlys += membersonly.getPackage
  }

  //
  // Sets value from nested element 'wildcard'.
  //
  def addConfiguredWildcard(wildcard: PackageElement) {
    wildcards += wildcard.getPackage
  }

  //
  // Sets value from nested element 'reporter'.
  //
  def addConfiguredReporter(reporter: ReporterElement) {
    reporters += reporter
  }

  //
  // Sets value from nested element 'tagsToInclude'.
  //
  def addConfiguredTagsToInclude(tagsToInclude: TextElement) {
    this.includes = tagsToInclude.getText
  }

  //
  // Sets value from nested element 'includes'.
  //
  // DEPRECATED in 0.9.6
  //
  def addConfiguredIncludes(includes: TextElement) {
    Console.err.println("WARNING: 'includes' is deprecated - " +
                        "use 'tagsToInclude' instead [includes: " +
                        includes.getText + "]")
    this.includes = includes.getText
  }

  //
  // Sets value from nested element 'excludes'.
  //
  def addConfiguredTagsToExclude(tagsToExclude: TextElement) {
    this.excludes = tagsToExclude.getText
  }

  //
  // Sets value from nested element 'excludes'.
  //
  // DEPRECATED in 0.9.6
  //
  def addConfiguredExcludes(excludes: TextElement) {
    Console.err.println("WARNING: 'excludes' is deprecated - " +
                        "use 'tagsToExclude' instead [excludes: " +
                        excludes.getText + "]")
    this.excludes = excludes.getText
  }

  //
  // Translates a list of strings making up a path into a
  // single space-delimited string.
  //
  private def getSpacedOutPathStr(path: List[String]): String = {
    val buf = new StringBuffer

    var prefix = ""
    for (elem <- path) {
      buf.append(prefix)
      buf.append(elem)
      prefix = " "
    }
        System.out.println("gcbx buf [" + buf + "]");
        System.out.println("gcbx bbb [" + path.mkString("", " ", "") + "]");
    buf.toString
  }

  //
  // Translates a whitespace-delimited string into a
  // whitespace-delimited string, but not the same whitespace.  Trims
  // off leading and trailing whitespace and converts inter-element
  // whitespace to a single space.
  //
  private def singleSpace(str: String): String = {
    str.trim.replaceAll("\\s+", " ")
  }
}

  //
  // Class to hold data from <membersonly> and <wildcard> elements.
  //
  private class PackageElement {
    private var packageName: String = null

    def setPackage(packageName: String) {
      this.packageName = packageName
    }

    def getPackage = packageName
  }

  //
  // Class to hold data from <suite> elements.
  //
  private class SuiteElement {
    private var className: String = null

    def setClassName(className: String) {
      this.className = className
    }

    def getClassName = className
  }

  //
  // Class to hold data from <includes> and <excludes> elements.
  //
  private class TextElement {
      private var text: String = null

      def addText(text: String) {
        this.text = text
      }

      def getText = text
  }

  //
  // Class to hold data from <property> elements.
  //
  private class NameValuePair {
    private var name  : String = null
    private var value : String = null

    def setName(name   : String) { this.name  = name  }
    def setValue(value : String) { this.value = value }

    def getName  = name
    def getValue = value
  }

  //
  // Class to hold data from <runpathurl> elements.
  //
  private class RunpathUrl {
    private var url: String = null

    def setUrl(url: String) { this.url = url }
    def getUrl = url
  }

  //
  // Class to hold data from <jvmarg> elements.
  //
  private class JvmArg {
    private var value: String = null

    def setValue(value: String) { this.value = value }
    def getValue = value
  }

  //
  // Class to hold data from <reporter> elements.
  //
  private class ReporterElement {
    private var rtype     : String = null
    private var config    : String = null
    private var filename  : String = null
    private var directory : String = null
    private var classname : String = null

    def setType(rtype          : String) { this.rtype     = rtype     }
    def setConfig(config       : String) { this.config    = config    }
    def setFilename(filename   : String) { this.filename  = filename  }
    def setDirectory(directory : String) { this.directory = directory }
    def setClassName(classname : String) { this.classname = classname }

    def getType      = rtype
    def getConfig    = config
    def getFilename  = filename
    def getDirectory = directory
    def getClassName = classname
  }

