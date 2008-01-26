ScalaTest 0.9.1

ScalaTest is a free, open-source testing toolkit for Scala and Java
programmers. 

GETTING STARTED

This release is a sneak preview of ScalaTest. To learn how to
use it, please open in your browser the scaladoc documentation in the
/scalatest-0.9.1/doc directory. Look first at the documentation for trait
Suite, which gives a decent intro. All the other types are documented as
well, so you can hop around to learn more. Runner explains how to use the
application. The Ignore class is written in Java, and isn't currently shown
in the Scaladoc.

The try it out, you can use ScalaTest to run its own tests, i.e., the tests used to
test ScalaTest itself. This command will run the GUI:

scala -classpath scalatest-0.9.1.jar org.scalatest.Runner -p "scalatest-0.9.1-tests.jar" -g -s org.scalatest.AllSuite

This command will run and just print results to the standard output:

scala -classpath scalatest-0.9.1.jar org.scalatest.Runner -p "scalatest-0.9.1-tests.jar" -o -s org.scalatest.AllSuite

I've been using Scala version 2.6.1-final, so it is not guaranteed to work with earlier Scala versions.

STILL TO DO

There are a few features that are still not finished:

1. Reloading classes from JAR files between runs in the GUI doesn't work, because URLClassLoader
uses a JAR cache. For now, you'll have to restart the app to pick up new classes in JAR files. But
a better alternative is to simply point ScalaTest to the build directory containing .class files produced
by the Scala and Java compiler. URLClassLoader will not cache these files, so changes to them will be
picked up each time you press the Run or Rerun buttons.

2. Integrate with ScalaCheck, JUnit, TestNG, Junit 4. Create an Ant task.

ABOUT SCALATEST

ScalaTest was written by Bill Venners, starting in late 2007. ScalaTest,
which is almost exclusively written in Scala, follows and improves upon
the Java code and design of Artima SuiteRunner, a testing tool also written
primarily by Bill Venners, starting in 2001. Over the years a few other
people have contributed to SuiteRunner as well, including:

Chris Daily
Chua Chee Seng
Frank Sommers
John Mitchel
Mark Brouwer
Matt Gerrans

Our goal is to have ScalaTest released 1.0 by the first week of May, 2008, just in time for JavaOne. 

