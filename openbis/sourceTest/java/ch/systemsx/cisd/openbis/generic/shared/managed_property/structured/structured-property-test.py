#
# we use a function named "configureUI" to test something completely unrelated,
# because this is the easiest way to have the entire managed properties infrastrcture
# from the Java level 
#
def configureUI():

  # 
  # test creating the element structure
  #
  factory = createElementFactory()
  
  elements = [
      factory.createSampleLink("samplePermId"),
      factory.createMaterialLink("type", "typeCode"),
      factory.createElement("testElement").addAttribute("key1", "value1").addAttribute("key2", "value2")
  ]
  
  converter = createPropertyConverter()
  property.value = converter.convertToString(elements)


  
  #
  # test updating the property contents 
  #
  elements = converter.convertToElements(property.value)
  
  elements[0] = factory.createSampleLink("modifiedLink")
  elements[1].children = [
      factory.createElement("nested1").addAttribute("na1", "na2")
  ]
  elements[2].attributes["key2"] = "modifiedvalue"
  property.value = converter.convertToString(elements)
  

