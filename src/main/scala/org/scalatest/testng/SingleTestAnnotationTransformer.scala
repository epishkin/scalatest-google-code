package org.scalatest.testng

import org.testng.IAnnotationTransformer
import org.testng.annotations.ITestAnnotation
import java.lang.reflect.Method
import java.lang.reflect.Constructor

class SingleTestAnnotationTransformer(testName: String) extends IAnnotationTransformer {
  override def transform( annotation: ITestAnnotation, testClass: java.lang.Class[_], testConstructor: Constructor[_], testMethod: Method) {
    if (testName.equals(testMethod.getName)) 
      annotation.setGroups(Array("org.scalatest.testng.singlemethodrun.methodname"))  
  }
}