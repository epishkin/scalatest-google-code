package org.scalatest.tools;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

import java.util.ArrayList;

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
 *   <li>  stdout           </li>
 *   <li>  stderr           </li>
 *   <li>  reporterclass    </li>
 * </ul>
 *
 * <p>
 * Each may include a config attribute to specify the reporter configuration.
 * Types 'file' and 'reporterclass' require additional attributes 'filename'
 * and 'classname', respectively.  E.g.:
 * </p>
 *
 * <pre>
 *   &lt;scalatest&gt;
 *     &lt;reporter type="stdout"        config="FAB"/&gt;
 *     &lt;reporter type="file"          filename="test.out"/&gt;
 *     &lt;reporter type="reporterclass" classname="my.ReporterClass"/&gt;
 * </pre>
 *
 * <p>
 * Specify group includes and excludes using &lt;includes&gt; and
 * &lt;excludes&gt; elements.  E.g.:
 * </p>
 *
 * <pre>
 *   &lt;scalatest&gt;
 *     &lt;includes&gt;
 *         CheckinTests
 *         FunctionalTests
 *     &lt;/includes&gt;
 *
 *     &lt;excludes&gt;
 *         SlowTests
 *         NetworkTests
 *     &lt;/excludes&gt;
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
 * Use attribute haltonfailure="true" to cause ant to fail the
 * build if there's a test failure.
 * </p>
 *
 * @author George Berger
 */
public class ScalaTestTask extends Task {
    private String includes;
    private String excludes;
    private boolean concurrent;
    private ArrayList<String> runpath = new ArrayList<String>();
    private ArrayList<String> suites = new ArrayList<String>();
    private ArrayList<String> membersonlys = new ArrayList<String>();
    private ArrayList<String> wildcards = new ArrayList<String>();
    private ArrayList<String> testNGSuites = new ArrayList<String>();
    private ArrayList<ReporterElement> reporters =
        new ArrayList<ReporterElement>();
    private ArrayList<NameValuePair> properties =
        new ArrayList<NameValuePair>();
    private boolean haltonfailure = false;

    //
    // Executes the task.
    //
    public void execute() throws BuildException {
        ArrayList<String> args = new ArrayList<String>();

        addSuiteArgs(args);
        addReporterArgs(args);
        addPropertyArgs(args);
        addIncludesArgs(args);
        addExcludesArgs(args);
        addRunpathArgs(args);
        addTestNGSuiteArgs(args);
        addConcurrentArg(args);

        String[] argsArray = args.toArray(new String[args.size()]);
        boolean success = Runner.runTests(argsArray);

        if (!success && haltonfailure) {
            throw new BuildException("scalatest test failed");
        }
    }

    //
    // Adds '-p runpath' arg pair to args list if a runpath
    // element or attribute was specified for task.
    //
    private void addRunpathArgs(ArrayList<String> args) {
        if (runpath.size() > 0) {
            args.add("-p");
            args.add(getSpacedOutPathStr(runpath));
        }
    }

    private void addTestNGSuiteArgs(ArrayList<String> args) {
        if (testNGSuites.size() > 0) {
            args.add("-t");
            args.add(getSpacedOutPathStr(testNGSuites));
        }
    }
    
    //
    // Adds '-c' arg to args list if 'concurrent' attribute was
    // specified true for task.
    //
    private void addConcurrentArg(ArrayList<String> args) {
        if (concurrent) {
            args.add("-c");
        }
    }

    //
    // Adds '-n includes-list' arg pair to args list if an <includes>
    // element was supplied for task.
    //
    private void addIncludesArgs(ArrayList<String> args) {
        if (includes != null) {
            args.add("-n");
            args.add(singleSpace(includes));
        }
    }

    //
    // Adds '-x excludes-list' arg pair to args list if an <excludes>
    // element was supplied for task.
    //
    private void addExcludesArgs(ArrayList<String> args) {
        if (excludes != null) {
            args.add("-x");
            args.add(singleSpace(excludes));
        }
    }

    //
    // Adds '-Dname=value' argument to args list for each nested
    // <property> element supplied for task.
    //
    private void addPropertyArgs(ArrayList<String> args) {
        for (NameValuePair pair: properties) {
            args.add("-D" + pair.getName() + "=" + pair.getValue());
        }
    }

    //
    // Adds '-s classname' argument to args list for each suite
    // specified for task.  Adds '-m packagename' for each
    // membersonly element specified, and '-w packagename' for
    // each wildcard element specified.
    //
    private void addSuiteArgs(ArrayList<String> args) throws BuildException {
        for (String suite: suites) {
            if (suite == null) {
                throw new BuildException(
                    "missing classname attribute for <suite> element");
            }
            args.add("-s");
            args.add(suite);
        }

        for (String packageName: membersonlys) {
            if (packageName == null) {
                throw new BuildException(
                    "missing package attribute for <membersonly> element");
            }
            args.add("-m");
            args.add(packageName);
        }

        for (String packageName: wildcards) {
            if (packageName == null) {
                throw new BuildException(
                    "missing package attribute for <wildcard> element");
            }
            args.add("-w");
            args.add(packageName);
        }
    }

    //
    // Adds appropriate reporter options to args list for each
    // nested reporter element specified for task.  Defaults to
    // stdout if no reporter specified.
    //
    private void addReporterArgs(ArrayList<String> args)
    throws BuildException {

        if (reporters.size() == 0) {
            args.add("-o");
        }

        for (ReporterElement reporter: reporters) {
            String type = reporter.getType();

            if (type == null) {
                throw new BuildException(
                    "missing type attribute for <reporter> element");
            }

            if (type.equals("stdout")) {
                addReporterOption(args, reporter, "-o");
            }
            else if (type.equals("stderr")) {
                addReporterOption(args, reporter, "-e");
            }
            else if (type.equals("graphic")) {
                addReporterOption(args, reporter, "-g");
            }
            else if (type.equals("file")) {
                addFileReporter(args, reporter);
            }
            else if (type.equals("reporterclass")) {
                addReporterClass(args, reporter);
            }
            else {
                throw new BuildException("unknown reporter type [" +
                                         type + "]");
            }
        }
    }

    //
    // Adds specified option to args for reporter.  Appends reporter
    // config string to option if specified, e.g. "-eFAB".
    //
    private void addReporterOption(ArrayList<String> args,
                                   ReporterElement   reporter,
                                   String            option) {
        String config = reporter.getConfig();

        if (config == null) {
            args.add(option);
        }
        else {
            args.add(option + config);
        }
    }

    //
    // Adds '-f' file reporter option to args.  Appends reporter
    // config string to option if specified.  Adds reporter's
    // filename as additional argument, e.g. "-fFAB", "filename".
    //
    private void addFileReporter(ArrayList<String> args,
                                 ReporterElement   reporter)
    throws BuildException {
        addReporterOption(args, reporter, "-f");

        if (reporter.getFilename() == null) {
            throw new BuildException(
                "reporter type 'file' requires 'filename' attribute");
        }
        args.add(reporter.getFilename());
    }

    //
    // Adds '-r' reporter class option to args.  Appends
    // reporter config string to option if specified.  Adds
    // reporter's classname as additional argument, e.g. "-rFAB",
    // "my.ReporterClass".
    //
    private void addReporterClass(ArrayList<String> args,
                                  ReporterElement   reporter)
    throws BuildException {
        addReporterOption(args, reporter, "-r");

        if (reporter.getClassName() == null) {
            throw new BuildException(
                "reporter type 'reporterclass' requires 'classname' " +
                "attribute");
        }
        args.add(reporter.getClassName());
    }

    //
    // Sets value of 'runpath' attribute.
    //
    public void setRunpath(Path runpath) {
        for (String element: runpath.list()) {
            this.runpath.add(element);
        }
    }
    
    //
    // Sets value of 'haltonfailure' attribute.
    //
    public void setHaltonfailure(boolean haltonfailure) {
        this.haltonfailure = haltonfailure;
    }
    
    public void setTestNGSuites(Path testNGSuitePath) {
        for (String element: testNGSuitePath.list()) {
            this.testNGSuites.add(element);
        }
    }

    //
    // Sets value of 'concurrent' attribute.
    //
    public void setConcurrent(boolean concurrent) {
        this.concurrent = concurrent;
    }

    //
    // Sets value from nested element 'runpath'.
    //
    public void addConfiguredRunpath(Path runpath) {
        for (String element: runpath.list()) {
            this.runpath.add(element);
        }
    }
 
    public void addConfiguredTestNGSuites(Path testNGSuitePath) {
        for (String element: testNGSuitePath.list()) {
            this.testNGSuites.add(element);
        }
    }

    //
    // Sets value from nested element 'runpathurl'.
    //
    public void addConfiguredRunpathUrl(RunpathUrl runpathurl) {
            runpath.add(runpathurl.getUrl());
    }

    //
    // Sets values from nested element 'property'.
    //
    public void addConfiguredProperty(NameValuePair property) {
        properties.add(property);
    }

    //
    // Sets value of 'suite' attribute.
    //
    public void setSuite(String suite) {
        suites.add(suite);
    }

    //
    // Sets value of 'membersonly' attribute.
    //
    public void setMembersonly(String packageName) {
        membersonlys.add(packageName);
    }

    //
    // Sets value of 'wildcard' attribute.
    //
    public void setWildcard(String packageName) {
        wildcards.add(packageName);
    }

    //
    // Sets value from nested element 'suite'.
    //
    public void addConfiguredSuite(SuiteElement suite) {
        suites.add(suite.getClassName());
    }

    //
    // Sets value from nested element 'membersonly'.
    //
    public void addConfiguredMembersOnly(PackageElement membersonly) {
        membersonlys.add(membersonly.getPackage());
    }

    //
    // Sets value from nested element 'wildcard'.
    //
    public void addConfiguredWildcard(PackageElement wildcard) {
        wildcards.add(wildcard.getPackage());
    }

    //
    // Sets value from nested element 'reporter'.
    //
    public void addConfiguredReporter(ReporterElement reporter) {
        reporters.add(reporter);
    }

    //
    // Sets value from nested element 'includes'.
    //
    public void addConfiguredIncludes(TextElement includes) {
        this.includes = includes.getText();
    }

    //
    // Sets value from nested element 'excludes'.
    //
    public void addConfiguredExcludes(TextElement excludes) {
        this.excludes = excludes.getText();
    }

    //
    // Translates a list of strings making up a path into a
    // single space-delimited string.
    //
    private String getSpacedOutPathStr(ArrayList<String> path) {
        StringBuffer buf = new StringBuffer();

        String prefix = "";
        for (String elem: path) {
            buf.append(prefix);
            buf.append(elem);
            prefix = " ";
        }
        return buf.toString();
    }

    //
    // Translates a whitespace-delimited string into a
    // whitespace-delimited string, but not the same whitespace.  Trims
    // off leading and trailing whitespace and converts inter-element
    // whitespace to a single space.
    //
    private String singleSpace(String str) {
        return str.trim().replaceAll("\\s+", " ");
    }

    //
    // Class to hold data from <membersonly> and <wildcard> elements.
    //
    public static class PackageElement {
        private String packageName;

        public void setPackage(String packageName) {
            this.packageName = packageName;
        }

        public String getPackage() { return packageName; }
    }

    //
    // Class to hold data from <suite> elements.
    //
    public static class SuiteElement {
        private String className;

        public void setClassName(String className) {
            this.className = className;
        }

        public String getClassName() { return className; }
    }

    //
    // Class to hold data from <includes> and <excludes> elements.
    //
    public static class TextElement {
        private String text;

        public void addText(String text) { this.text = text; }

        public String getText() { return text; }
    }

    //
    // Class to hold data from <reporter> elements.
    //
    public static class ReporterElement {
        private String type;
        private String config;
        private String filename;
        private String classname;

        public void setType(String type)         { this.type      = type;     }
        public void setConfig(String config)     { this.config    = config;   }
        public void setFilename(String filename) { this.filename  = filename; }
        public void setClassName(String classname) {
            this.classname = classname;
        }

        public String getType()      { return type;      }
        public String getConfig()    { return config;    }
        public String getFilename()  { return filename;  }
        public String getClassName() { return classname; }
    }

    //
    // Class to hold data from <property> elements.
    //
    public static class NameValuePair {
        private String name;
        private String value;

        public void setName(String name)   { this.name = name;   }
        public void setValue(String value) { this.value = value; }

        public String getName()  { return name; }
        public String getValue() { return value; }
    }

    //
    // Class to hold data from <runpathurl> elements.
    //
    public static class RunpathUrl {
        private String url;

        public void   setUrl(String url) { this.url = url; }
        public String getUrl()           { return url;     }
    }
}
