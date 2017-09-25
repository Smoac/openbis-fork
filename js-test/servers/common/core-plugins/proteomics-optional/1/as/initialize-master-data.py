import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

tr = service.transaction()

file_type_HTML = tr.getOrCreateNewFileFormatType('HTML')
file_type_HTML.setDescription('HTML File')

file_type_MZDATA = tr.getOrCreateNewFileFormatType('MZDATA')
file_type_MZDATA.setDescription('Mass spectrometry data format.')

file_type_MZML = tr.getOrCreateNewFileFormatType('MZML')
file_type_MZML.setDescription('Mass spectrometry data format. \
Unifiying mzXML and mzData formats, as released at the \
2008 American Society for Mass Spectrometry Meeting.')

file_type_MZXML = tr.getOrCreateNewFileFormatType('MZXML')
file_type_MZXML.setDescription('Mass spectrometry data format.')

file_type_RAW = tr.getOrCreateNewFileFormatType('RAW')
file_type_RAW.setDescription('Proprietary file format for Thermo mass sepectrometry data.')

file_type_TGZ = tr.getOrCreateNewFileFormatType('TGZ')
file_type_TGZ.setDescription('gzipped tar')

file_type_WIFF = tr.getOrCreateNewFileFormatType('WIFF')
file_type_WIFF.setDescription('Proprieatry file format for Sciex and Agilent mass spectrometry data.')

file_type_ZIP = tr.getOrCreateNewFileFormatType('ZIP')
file_type_ZIP.setDescription('A zipped package')

vocabulary_TREATMENT_TYPE = tr.getOrCreateNewVocabulary('TREATMENT_TYPE')

vocabulary_term_TREATMENT_TYPE_PH = tr.createNewVocabularyTerm('PH')
vocabulary_term_TREATMENT_TYPE_PH.setDescription(None)
vocabulary_term_TREATMENT_TYPE_PH.setUrl(None)
vocabulary_term_TREATMENT_TYPE_PH.setLabel('ph')
vocabulary_term_TREATMENT_TYPE_PH.setOrdinal(1)
vocabulary_TREATMENT_TYPE.addTerm(vocabulary_term_TREATMENT_TYPE_PH)

exp_type_BIOLOGICAL_EXPERIMENT = tr.getOrCreateNewExperimentType('BIOLOGICAL_EXPERIMENT')
exp_type_BIOLOGICAL_EXPERIMENT.setDescription('A biological experiment')

exp_type_MS_INJECT = tr.getOrCreateNewExperimentType('MS_INJECT')
exp_type_MS_INJECT.setDescription('MS injection experiment')

exp_type_MS_QUANTIFICATION = tr.getOrCreateNewExperimentType('MS_QUANTIFICATION')
exp_type_MS_QUANTIFICATION.setDescription('Quantification of LC-MS data')

data_set_type_MZXML_DATA = tr.getOrCreateNewDataSetType('MZXML_DATA')
data_set_type_MZXML_DATA.setDescription('standardized format for ms data')

data_set_type_RAW_DATA = tr.getOrCreateNewDataSetType('RAW_DATA')
data_set_type_RAW_DATA.setDescription(None)

prop_type_PARENTDATASETCODES = tr.getOrCreateNewPropertyType('PARENT-DATA-SET-CODES', DataType.VARCHAR)
prop_type_PARENTDATASETCODES.setLabel('Parent Dataset Codes')
prop_type_PARENTDATASETCODES.setManagedInternally(False)
prop_type_PARENTDATASETCODES.setInternalNamespace(False)

prop_type_TREATMENT_TYPE1 = tr.getOrCreateNewPropertyType('TREATMENT_TYPE1', DataType.CONTROLLEDVOCABULARY)
prop_type_TREATMENT_TYPE1.setLabel('Treatment Type 1')
prop_type_TREATMENT_TYPE1.setManagedInternally(False)
prop_type_TREATMENT_TYPE1.setInternalNamespace(False)

prop_type_TREATMENT_TYPE1.setVocabulary(vocabulary_TREATMENT_TYPE)

prop_type_TREATMENT_TYPE2 = tr.getOrCreateNewPropertyType('TREATMENT_TYPE2', DataType.CONTROLLEDVOCABULARY)
prop_type_TREATMENT_TYPE2.setLabel('Treatment Type 2')
prop_type_TREATMENT_TYPE2.setManagedInternally(False)
prop_type_TREATMENT_TYPE2.setInternalNamespace(False)

prop_type_TREATMENT_TYPE2.setVocabulary(vocabulary_TREATMENT_TYPE)

prop_type_TREATMENT_TYPE3 = tr.getOrCreateNewPropertyType('TREATMENT_TYPE3', DataType.CONTROLLEDVOCABULARY)
prop_type_TREATMENT_TYPE3.setLabel('Treatment Type 3')
prop_type_TREATMENT_TYPE3.setManagedInternally(False)
prop_type_TREATMENT_TYPE3.setInternalNamespace(False)

prop_type_TREATMENT_TYPE3.setVocabulary(vocabulary_TREATMENT_TYPE)


