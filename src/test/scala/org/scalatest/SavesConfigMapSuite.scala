package org.scalatest

import SavesConfigMapSuite.theConfigMap

@WrapWith(classOf[ConfigMapWrapperSuite])
class SavesConfigMapSuite(configMap: Map[String, Any]) extends FunSuite {
  theConfigMap = Some(configMap)
}

object SavesConfigMapSuite {
  private var theConfigMap: Option[Map[String, Any]] = None
  def savedConfigMap = theConfigMap
  def resetConfigMap() { theConfigMap = None }
}