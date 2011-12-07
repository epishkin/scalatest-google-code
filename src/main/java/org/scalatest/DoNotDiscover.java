package org.scalatest;

import java.lang.annotation.*; 

@TagAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DoNotDiscover {

}
