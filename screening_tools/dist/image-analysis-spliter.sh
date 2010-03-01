LIB=:lib/
CP=.${LIB}openbis-metadata-transformer.jar${LIB}screening.jar${LIB}commons-io.jar${LIB}commons-lang.jar${LIB}csv.jar
java -cp $CP ch.systemsx.cisd.openbis.metadata.ImageAnalysisLMCSplitter $*