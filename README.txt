ScalaTest 0.9.2

ScalaTest is a free, open-source testing toolkit for Scala and Java
programmers. 

GETTING STARTED

This release is a sneak preview of ScalaTest. To learn how to
use it, please open in your browser the scaladoc documentation in the
/scalatest-0.9.2/doc directory. Look first at the documentation for trait
org.scalatest.Suite, which gives a decent intro. All the other types are documented as
well, so you can hop around to learn more. org.scalatest.tools.Runner explains how to use the
application. The Ignore class is written in Java, and isn't currently shown
in the Scaladoc.

The try it out, you can use ScalaTest to run its own tests, i.e., the tests used to
test ScalaTest itself. This command will run the GUI:

scala -classpath scalatest-0.9.2.jar org.scalatest.tools.Runner -p "scalatest-0.9.2-tests.jar" -g -s org.scalatest.SuiteSuite

This command will run and just print results to the standard output:

scala -classpath scalatest-0.9.2.jar org.scalatest.tools.Runner -p "scalatest-0.9.2-tests.jar" -o -s org.scalatest.SuiteSuite

I've been using Scala version 2.7.1-final, so it is not guaranteed to work with earlier Scala versions.

ABOUT SCALATEST

ScalaTest was written by Bill Venners, Josh Cough, and George Berger starting in late 2007. ScalaTest,
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

