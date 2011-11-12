package org.scalatest.tools

private[scalatest] class FriendlyParamsTranslator {
  
  private[scalatest] val validConfigMap = Map(
                                             "dropteststarting" -> "N", 
                                             "droptestsucceeded" -> "C", 
                                             "droptestignored" -> "X", 
                                             "droptestpending" -> "E", 
                                             "dropsuitestarting" -> "H", 
                                             "dropsuitecompleted" -> "L", 
                                             "dropinfoprovided" -> "O", 
                                             "nocolor" -> "W", 
                                             "shortstacks" -> "S", 
                                             "fullstacks" -> "F", 
                                             "durations" -> "D"
                                           )
  
  private [scalatest] def extractContentInBracket(raw:String, it:Iterator[String], expected:String):String = {
    if(!raw.startsWith("("))
      throw new IllegalArgumentException("Invalid configuration, example valid configuration: " + expected)
    val withBrackets = if(raw.endsWith(")"))
                         raw
                       else 
                         parseUntilFound(raw, ")", it)
    withBrackets.substring(1, withBrackets.length() - 1)
  }
    
  private[scalatest] def parseUntilFound(value:String, endsWith:String, it:Iterator[String]):String = {
    if(it.hasNext) {
      val next = it.next()
      if(next.endsWith(endsWith))
        value + next
      else
        parseUntilFound(value + next, endsWith, it)
    }
    else
      throw new IllegalArgumentException("Unable to find '" + endsWith + "'")
  }
    
  private[scalatest] def parseCompoundParams(rawParamsStr:String, it:Iterator[String], expected:String):Array[String] = {
    val rawClassArr = extractContentInBracket(rawParamsStr, it, expected).split(",")
    for(rawClass <- rawClassArr) yield {
      val trimmed = rawClass.trim()
      if(trimmed.length() > 1 && trimmed.startsWith("\"") && trimmed.endsWith("\""))
        trimmed.substring(1, trimmed.length() - 1)
      else
        trimmed
    }
  }
    
  private[scalatest] def translateCompoundParams(rawParamsStr:String, it:Iterator[String], expected:String):String = {
    val paramsArr = parseCompoundParams(rawParamsStr, it, expected)
    paramsArr.mkString(" ")
  }
    
  private[scalatest] def parseParams(rawParamsStr:String, it:Iterator[String], validParamSet:Set[String], expected:String):Map[String, String] = {    
    if(rawParamsStr.length() > 0) {
      val paramsStr = extractContentInBracket(rawParamsStr, it, expected)
      val configsArr:Array[String] = paramsStr.split(",")
      val tuples = for(configStr <- configsArr) yield {
        val keyValueArr = configStr.trim().split("=")
        if(keyValueArr.length == 2) {
          // Value config param
          val key:String = keyValueArr(0).trim()
          if(!validParamSet.contains(key))
            throw new IllegalArgumentException("Invalid configuration: " + key)
          val rawValue = keyValueArr(1).trim()
          val value:String = 
            if(rawValue.startsWith("\"") && rawValue.endsWith("\"") && rawValue.length() > 1) 
              rawValue.substring(1, rawValue.length() - 1)
            else
              rawValue
          (key -> value)
        }
        else
          throw new IllegalArgumentException("Invalid configuration: " + configStr)
      }
      Map[String, String]() ++ tuples
    }
    else
      Map[String, String]()
  }
    
  private[scalatest] def translateConfigs(rawConfigs:String):String = {
    val configArr = rawConfigs.split(" ")
    val translatedArr = configArr.map {config => 
          val translatedOpt:Option[String] = validConfigMap.get(config)
          translatedOpt match {
            case Some(translated) => translated
            case None => throw new IllegalArgumentException("Invalid config value: " + config)
          }
        }
    translatedArr.mkString
  }
    
  private[scalatest] def getTranslatedConfig(paramsMap:Map[String, String]):String = {
    val configOpt:Option[String] = paramsMap.get("config")
	configOpt match {
	  case Some(configStr) => translateConfigs(configStr)
	  case None => ""
	}
  }
  
  private[scalatest] def validateSupportedPropsAndTags(s:String) {
    
  }

  private[scalatest] def parsePropsAndTags(args: Array[String]) = {

    import collection.mutable.ListBuffer

    val props = new ListBuffer[String]()
    val includes = new ListBuffer[String]()
    val excludes = new ListBuffer[String]()
    var repoArgs = new ListBuffer[String]()

    val it = args.iterator
    while (it.hasNext) {

      val s = it.next

      validateSupportedPropsAndTags(s)
      
      if (s.startsWith("-D")) {
        props += s
      }
      else if (s.startsWith("-n")) {
        println("-n is deprecated, use include instead.")
        includes += s
        if (it.hasNext)
          includes += it.next
      }
      else if (s.startsWith("include")) {
        includes += "-n" 
        includes += translateCompoundParams(s.substring("include".length()), it, "include(a, b, c)")
      }
      else if (s.startsWith("-l")) {
        println("-l is deprecated, use exclude instead.")
        excludes += s
        if (it.hasNext)
          excludes += it.next
      }
      else if (s.startsWith("exclude")) {
        excludes += "-l"
        excludes += translateCompoundParams(s.substring("exclude".length()), it, "exclude(a, b, c)")
      }
	  else if (s.startsWith("-o")) {
        // May be we can use a logger later
        println("-o is deprecated, use stdout instead.")
        repoArgs += s
      }
      else if (s.startsWith("stdout")) {
        val paramsMap:Map[String, String] = parseParams(s.substring("stdout".length()), it, Set("config"), "stdout")
        repoArgs += "-o" + getTranslatedConfig(paramsMap:Map[String, String])
      }
      else if (s.startsWith("-e")) {
        println("-e is deprecated, use stderr instead.")
        repoArgs += s
      }
      else if (s.startsWith("stderr")) {
        val paramsMap:Map[String, String] = parseParams(s.substring("stderr".length()), it, Set("config"), "stderr")
        repoArgs += "-e" + getTranslatedConfig(paramsMap:Map[String, String])
      }
      // Not to enable until we have the pararrel execution problem sorted out.
      
      else if (s.startsWith("-g")) {
        repoArgs += s
      }
      else if (s.startsWith("-f")) {
        println("-f is deprecated, use file(directory=\"xxx\") instead.")
        repoArgs += s
        if (it.hasNext)
          repoArgs += it.next
      }
      else if (s.startsWith("file")) {
        val paramsMap:Map[String, String] = parseParams(s.substring("file".length()), it, Set("filename", "config"), "junitxml(directory=\"xxx\")")
        repoArgs += "-f" + getTranslatedConfig(paramsMap:Map[String, String])
        val filenameOpt:Option[String] = paramsMap.get("filename")
        filenameOpt match {
          case Some(filename) => repoArgs += filename
          case None => throw new IllegalArgumentException("file requires filename to be specified, example: file(filename=\"xxx\")")
        }
      }
      else if (s.startsWith("-u")) {
        println("-u is deprecated, use junitxml(directory=\"xxx\") instead.")
        repoArgs += s
        if (it.hasNext)
          repoArgs += it.next
      }
      else if(s.startsWith("junitxml")) {
        repoArgs += "-u"
        val paramsMap:Map[String, String] = parseParams(s.substring("junitxml".length()), it, Set("directory"), "junitxml(directory=\"xxx\")")
        val directoryOpt:Option[String] = paramsMap.get("directory")
        directoryOpt match {
          case Some(dir) => repoArgs += dir
          case None => throw new IllegalArgumentException("junitxml requires directory to be specified, example: junitxml(directory=\"xxx\")")
        }
      }
      else if (s.startsWith("-d")) {
        repoArgs += s
        if (it.hasNext)
          repoArgs += it.next
      }
      else if (s.startsWith("-a")) {
        repoArgs += s
        if (it.hasNext)
          repoArgs += it.next
      }
      else if (s.startsWith("-x")) {
        repoArgs += s
        if (it.hasNext)
          repoArgs += it.next
      }
      else if (s.startsWith("-h")) {
        repoArgs += s
        if (it.hasNext)
          repoArgs += it.next
      }
        
        //      else if (s.startsWith("-t")) {
        //
        //        testNGXMLFiles += s
        //        if (it.hasNext)
        //          testNGXMLFiles += it.next
        //      }
      else {
          throw new IllegalArgumentException("Unrecognized argument: " + s)
      }
    }
    (props.toList, includes.toList, excludes.toList, repoArgs.toList)
  }

}