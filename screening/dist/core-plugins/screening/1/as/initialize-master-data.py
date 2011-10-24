import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

tr = service.transaction()

#
# File types existing by default
#
#file_type_HDF5 = tr.createNewFileFormatType('HDF5')
#file_type_HDF5.setDescription('Hierarchical Data Format File, version 5')

#file_type_PROPRIETARY = tr.createNewFileFormatType('PROPRIETARY')
#file_type_PROPRIETARY.setDescription('Proprietary Format File')

#file_type_SRF = tr.createNewFileFormatType('SRF')
#file_type_SRF.setDescription('Sequence Read Format File')

#file_type_TIFF = tr.createNewFileFormatType('TIFF')
#file_type_TIFF.setDescription('TIFF File')

#file_type_TSV = tr.createNewFileFormatType('TSV')
#file_type_TSV.setDescription('Tab Separated Values File')

#file_type_XML = tr.createNewFileFormatType('XML')
#file_type_XML.setDescription('XML File')

file_type_CSV = tr.createNewFileFormatType('CSV')
file_type_CSV.setDescription('files with values separated by comma or semicolon')

file_type_JPG = tr.createNewFileFormatType('JPG')
file_type_JPG.setDescription(None)

file_type_PNG = tr.createNewFileFormatType('PNG')
file_type_PNG.setDescription(None)

file_type_UNKNOWN = tr.createNewFileFormatType('UNKNOWN')
file_type_UNKNOWN.setDescription('Unknown file format')


vocabulary_PLATE_GEOMETRY = tr.createNewVocabulary('PLATE_GEOMETRY')
vocabulary_PLATE_GEOMETRY.setDescription('The geometry or dimensions of a plate')
vocabulary_PLATE_GEOMETRY.setUrlTemplate(None)
vocabulary_PLATE_GEOMETRY.setManagedInternally(True)
vocabulary_PLATE_GEOMETRY.setInternalNamespace(True)
vocabulary_PLATE_GEOMETRY.setChosenFromList(True)

vocabulary_term_PLATE_GEOMETRY_1536_WELLS_32X48 = tr.createNewVocabularyTerm('1536_WELLS_32X48')
vocabulary_term_PLATE_GEOMETRY_1536_WELLS_32X48.setDescription(None)
vocabulary_term_PLATE_GEOMETRY_1536_WELLS_32X48.setUrl(None)
vocabulary_term_PLATE_GEOMETRY_1536_WELLS_32X48.setLabel('1536 Wells, 32x48')
vocabulary_term_PLATE_GEOMETRY_1536_WELLS_32X48.setOrdinal(3)
vocabulary_PLATE_GEOMETRY.addTerm(vocabulary_term_PLATE_GEOMETRY_1536_WELLS_32X48)

vocabulary_term_PLATE_GEOMETRY_96_WELLS_8X12 = tr.createNewVocabularyTerm('96_WELLS_8X12')
vocabulary_term_PLATE_GEOMETRY_96_WELLS_8X12.setDescription(None)
vocabulary_term_PLATE_GEOMETRY_96_WELLS_8X12.setUrl(None)
vocabulary_term_PLATE_GEOMETRY_96_WELLS_8X12.setLabel('96 Wells, 8x12')
vocabulary_term_PLATE_GEOMETRY_96_WELLS_8X12.setOrdinal(2)
vocabulary_PLATE_GEOMETRY.addTerm(vocabulary_term_PLATE_GEOMETRY_96_WELLS_8X12)

vocabulary_term_PLATE_GEOMETRY_384_WELLS_16X24 = tr.createNewVocabularyTerm('384_WELLS_16X24')
vocabulary_term_PLATE_GEOMETRY_384_WELLS_16X24.setDescription(None)
vocabulary_term_PLATE_GEOMETRY_384_WELLS_16X24.setUrl(None)
vocabulary_term_PLATE_GEOMETRY_384_WELLS_16X24.setLabel('384 Wells, 16x24')
vocabulary_term_PLATE_GEOMETRY_384_WELLS_16X24.setOrdinal(1)
vocabulary_PLATE_GEOMETRY.addTerm(vocabulary_term_PLATE_GEOMETRY_384_WELLS_16X24)

#
# Exists in the database by default
#
#vocabulary_STORAGE_FORMAT = tr.createNewVocabulary('STORAGE_FORMAT')
#vocabulary_STORAGE_FORMAT.setDescription('The on-disk storage format of a data set')
#vocabulary_STORAGE_FORMAT.setUrlTemplate(None)
#vocabulary_STORAGE_FORMAT.setManagedInternally(True)
#vocabulary_STORAGE_FORMAT.setInternalNamespace(True)
#vocabulary_STORAGE_FORMAT.setChosenFromList(True)

#vocabulary_term_STORAGE_FORMAT_PROPRIETARY = tr.createNewVocabularyTerm('PROPRIETARY')
#vocabulary_term_STORAGE_FORMAT_PROPRIETARY.setDescription(None)
#vocabulary_term_STORAGE_FORMAT_PROPRIETARY.setUrl(None)
#vocabulary_term_STORAGE_FORMAT_PROPRIETARY.setLabel(None)
#vocabulary_term_STORAGE_FORMAT_PROPRIETARY.setOrdinal(1)
#vocabulary_STORAGE_FORMAT.addTerm(vocabulary_term_STORAGE_FORMAT_PROPRIETARY)
#
#vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY = tr.createNewVocabularyTerm('BDS_DIRECTORY')
#vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY.setDescription(None)
#vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY.setUrl(None)
#vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY.setLabel(None)
#vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY.setOrdinal(2)
#vocabulary_STORAGE_FORMAT.addTerm(vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY)

vocabulary_MICROSCOPE = tr.createNewVocabulary('MICROSCOPE')
vocabulary_MICROSCOPE.setDescription('Microscope used in an experiment.')
vocabulary_MICROSCOPE.setUrlTemplate(None)
vocabulary_MICROSCOPE.setManagedInternally(False)
vocabulary_MICROSCOPE.setInternalNamespace(False)
vocabulary_MICROSCOPE.setChosenFromList(True)

vocabulary_term_MICROSCOPE_MD_IMAGEXPRESS_MICRO_2 = tr.createNewVocabularyTerm('MD_IMAGEXPRESS_MICRO_2')
vocabulary_term_MICROSCOPE_MD_IMAGEXPRESS_MICRO_2.setDescription(None)
vocabulary_term_MICROSCOPE_MD_IMAGEXPRESS_MICRO_2.setUrl(None)
vocabulary_term_MICROSCOPE_MD_IMAGEXPRESS_MICRO_2.setLabel(None)
vocabulary_term_MICROSCOPE_MD_IMAGEXPRESS_MICRO_2.setOrdinal(3)
vocabulary_MICROSCOPE.addTerm(vocabulary_term_MICROSCOPE_MD_IMAGEXPRESS_MICRO_2)

vocabulary_term_MICROSCOPE_MD_IMAGEXPRESS_MICROLIVE = tr.createNewVocabularyTerm('MD_IMAGEXPRESS_MICROLIVE')
vocabulary_term_MICROSCOPE_MD_IMAGEXPRESS_MICROLIVE.setDescription(None)
vocabulary_term_MICROSCOPE_MD_IMAGEXPRESS_MICROLIVE.setUrl(None)
vocabulary_term_MICROSCOPE_MD_IMAGEXPRESS_MICROLIVE.setLabel(None)
vocabulary_term_MICROSCOPE_MD_IMAGEXPRESS_MICROLIVE.setOrdinal(2)
vocabulary_MICROSCOPE.addTerm(vocabulary_term_MICROSCOPE_MD_IMAGEXPRESS_MICROLIVE)

vocabulary_term_MICROSCOPE_BD_PATHWAY_855 = tr.createNewVocabularyTerm('BD_PATHWAY_855')
vocabulary_term_MICROSCOPE_BD_PATHWAY_855.setDescription(None)
vocabulary_term_MICROSCOPE_BD_PATHWAY_855.setUrl(None)
vocabulary_term_MICROSCOPE_BD_PATHWAY_855.setLabel(None)
vocabulary_term_MICROSCOPE_BD_PATHWAY_855.setOrdinal(1)
vocabulary_MICROSCOPE.addTerm(vocabulary_term_MICROSCOPE_BD_PATHWAY_855)

exp_type_COMPOUND_HCS = tr.createNewExperimentType('COMPOUND_HCS')
exp_type_COMPOUND_HCS.setDescription('Compound High Content Screening')

exp_type_SIRNA_HCS = tr.createNewExperimentType('SIRNA_HCS')
exp_type_SIRNA_HCS.setDescription('Small Interfering RNA High Content Screening')

samp_type_CONTROL_WELL = tr.createNewSampleType('CONTROL_WELL')
samp_type_CONTROL_WELL.setDescription(None)
samp_type_CONTROL_WELL.setListable(False)
samp_type_CONTROL_WELL.setSubcodeUnique(False)
samp_type_CONTROL_WELL.setAutoGeneratedCode(False)
samp_type_CONTROL_WELL.setGeneratedCodePrefix('C')

samp_type_LIBRARY = tr.createNewSampleType('LIBRARY')
samp_type_LIBRARY.setDescription(None)
samp_type_LIBRARY.setListable(False)
samp_type_LIBRARY.setSubcodeUnique(False)
samp_type_LIBRARY.setAutoGeneratedCode(False)
samp_type_LIBRARY.setGeneratedCodePrefix('L')

samp_type_PLATE = tr.createNewSampleType('PLATE')
samp_type_PLATE.setDescription('Cell Plate')
samp_type_PLATE.setListable(True)
samp_type_PLATE.setSubcodeUnique(False)
samp_type_PLATE.setAutoGeneratedCode(False)
samp_type_PLATE.setGeneratedCodePrefix('S')

samp_type_SIRNA_WELL = tr.createNewSampleType('SIRNA_WELL')
samp_type_SIRNA_WELL.setDescription(None)
samp_type_SIRNA_WELL.setListable(False)
samp_type_SIRNA_WELL.setSubcodeUnique(False)
samp_type_SIRNA_WELL.setAutoGeneratedCode(False)
samp_type_SIRNA_WELL.setGeneratedCodePrefix('O')

data_set_type_HCS_ANALYSIS_CELL_CLASS = tr.createNewDataSetType('HCS_ANALYSIS_CELL_CLASS')
data_set_type_HCS_ANALYSIS_CELL_CLASS.setDescription('HCS image analysis cell classification')
data_set_type_HCS_ANALYSIS_CELL_CLASS.setContainerType(False)

data_set_type_HCS_ANALYSIS_CELL_FEATURES = tr.createNewDataSetType('HCS_ANALYSIS_CELL_FEATURES')
data_set_type_HCS_ANALYSIS_CELL_FEATURES.setDescription('HCS image analysis cell feature vectors')
data_set_type_HCS_ANALYSIS_CELL_FEATURES.setContainerType(False)

data_set_type_HCS_ANALYSIS_CELL_SEGMENTATION = tr.createNewDataSetType('HCS_ANALYSIS_CELL_SEGMENTATION')
data_set_type_HCS_ANALYSIS_CELL_SEGMENTATION.setDescription('HCS image analysis cell segmentation')
data_set_type_HCS_ANALYSIS_CELL_SEGMENTATION.setContainerType(False)

data_set_type_HCS_ANALYSIS_WELL_FEATURES = tr.createNewDataSetType('HCS_ANALYSIS_WELL_FEATURES')
data_set_type_HCS_ANALYSIS_WELL_FEATURES.setDescription('HCS image analysis well feature vectors.')
data_set_type_HCS_ANALYSIS_WELL_FEATURES.setContainerType(False)

data_set_type_HCS_IMAGE_OVERVIEW = tr.createNewDataSetType('HCS_IMAGE_OVERVIEW')
data_set_type_HCS_IMAGE_OVERVIEW.setDescription('Overview High Content Screening Images. Generated from raw images.')
data_set_type_HCS_IMAGE_OVERVIEW.setContainerType(False)

data_set_type_HCS_IMAGE_RAW = tr.createNewDataSetType('HCS_IMAGE_RAW')
data_set_type_HCS_IMAGE_RAW.setDescription('Raw High Content Screening Images')
data_set_type_HCS_IMAGE_RAW.setContainerType(False)

data_set_type_HCS_IMAGE_SEGMENTATION = tr.createNewDataSetType('HCS_IMAGE_SEGMENTATION')
data_set_type_HCS_IMAGE_SEGMENTATION.setDescription('HCS Segmentation Images (overlays).')
data_set_type_HCS_IMAGE_SEGMENTATION.setContainerType(False)

data_set_type_HCS_IMAGE_CONTAINER = tr.createNewDataSetType('HCS_IMAGE_CONTAINER')
data_set_type_HCS_IMAGE_CONTAINER.setDescription('Container for HCS images of different resolutions (raw, overviews, thumbnails).')
data_set_type_HCS_IMAGE_CONTAINER.setContainerType(True)

# Exists in the database by default
#data_set_type_UNKNOWN = tr.createNewDataSetType('UNKNOWN')
#data_set_type_UNKNOWN.setDescription('Unknown')
#data_set_type_UNKNOWN.setContainerType(False)

material_type_BACTERIUM = tr.createNewMaterialType('BACTERIUM')
material_type_BACTERIUM.setDescription('Bacterium')

material_type_CELL_LINE = tr.createNewMaterialType('CELL_LINE')
material_type_CELL_LINE.setDescription('Cell Line or Cell Culture. The growing of cells under controlled conditions.')

material_type_COMPOUND = tr.createNewMaterialType('COMPOUND')
material_type_COMPOUND.setDescription('Compound')

material_type_CONTROL = tr.createNewMaterialType('CONTROL')
material_type_CONTROL.setDescription('Control of a control layout')

material_type_GENE = tr.createNewMaterialType('GENE')
material_type_GENE.setDescription('Gene')

material_type_SIRNA = tr.createNewMaterialType('SIRNA')
material_type_SIRNA.setDescription('Oligo nucleotide')

material_type_VIRUS = tr.createNewMaterialType('VIRUS')
material_type_VIRUS.setDescription('Virus')

prop_type_ANALYSIS_PROCEDURE = tr.createNewPropertyType('ANALYSIS_PROCEDURE', DataType.VARCHAR)
prop_type_ANALYSIS_PROCEDURE.setLabel('Analysis procedure')
prop_type_ANALYSIS_PROCEDURE.setManagedInternally(False)
prop_type_ANALYSIS_PROCEDURE.setInternalNamespace(True)


prop_type_PLATE_GEOMETRY = tr.createNewPropertyType('PLATE_GEOMETRY', DataType.CONTROLLEDVOCABULARY)
prop_type_PLATE_GEOMETRY.setLabel('Plate Geometry')
prop_type_PLATE_GEOMETRY.setManagedInternally(True)
prop_type_PLATE_GEOMETRY.setInternalNamespace(True)
prop_type_PLATE_GEOMETRY.setVocabulary(vocabulary_PLATE_GEOMETRY)

prop_type_CONTROL = tr.createNewPropertyType('CONTROL', DataType.MATERIAL)
prop_type_CONTROL.setLabel('Control')
prop_type_CONTROL.setManagedInternally(False)
prop_type_CONTROL.setInternalNamespace(False)
prop_type_CONTROL.setMaterialType(material_type_CONTROL)

# Already exists in the database
prop_type_DESCRIPTION = tr.getPropertyType('DESCRIPTION')

prop_type_GENE = tr.createNewPropertyType('GENE', DataType.MATERIAL)
prop_type_GENE.setLabel('Gene')
prop_type_GENE.setManagedInternally(False)
prop_type_GENE.setInternalNamespace(False)
prop_type_GENE.setMaterialType(material_type_GENE)

prop_type_GENE_SYMBOLS = tr.createNewPropertyType('GENE_SYMBOLS', DataType.VARCHAR)
prop_type_GENE_SYMBOLS.setLabel('Gene symbols')
prop_type_GENE_SYMBOLS.setManagedInternally(False)
prop_type_GENE_SYMBOLS.setInternalNamespace(False)


prop_type_INHIBITOR_OF = tr.createNewPropertyType('INHIBITOR_OF', DataType.MATERIAL)
prop_type_INHIBITOR_OF.setLabel('Inhibitor Of')
prop_type_INHIBITOR_OF.setManagedInternally(False)
prop_type_INHIBITOR_OF.setInternalNamespace(False)
prop_type_INHIBITOR_OF.setMaterialType(material_type_GENE)

prop_type_LIBRARY_ID = tr.createNewPropertyType('LIBRARY_ID', DataType.VARCHAR)
prop_type_LIBRARY_ID.setLabel('Library ID')
prop_type_LIBRARY_ID.setManagedInternally(False)
prop_type_LIBRARY_ID.setInternalNamespace(False)


prop_type_MICROSCOPE = tr.createNewPropertyType('MICROSCOPE', DataType.CONTROLLEDVOCABULARY)
prop_type_MICROSCOPE.setLabel('Microscope')
prop_type_MICROSCOPE.setManagedInternally(False)
prop_type_MICROSCOPE.setInternalNamespace(False)

prop_type_MICROSCOPE.setVocabulary(vocabulary_MICROSCOPE)

prop_type_NUCLEOTIDE_SEQUENCE = tr.createNewPropertyType('NUCLEOTIDE_SEQUENCE', DataType.VARCHAR)
prop_type_NUCLEOTIDE_SEQUENCE.setLabel('Nucleotide Sequence')
prop_type_NUCLEOTIDE_SEQUENCE.setManagedInternally(False)
prop_type_NUCLEOTIDE_SEQUENCE.setInternalNamespace(False)


prop_type_NUMBER_OF_CHANNEL = tr.createNewPropertyType('NUMBER_OF_CHANNEL', DataType.INTEGER)
prop_type_NUMBER_OF_CHANNEL.setLabel('Channels')
prop_type_NUMBER_OF_CHANNEL.setManagedInternally(False)
prop_type_NUMBER_OF_CHANNEL.setInternalNamespace(False)


prop_type_REFSEQ = tr.createNewPropertyType('REFSEQ', DataType.VARCHAR)
prop_type_REFSEQ.setLabel('RefSeq')
prop_type_REFSEQ.setManagedInternally(False)
prop_type_REFSEQ.setInternalNamespace(False)


prop_type_SIRNA = tr.createNewPropertyType('SIRNA', DataType.MATERIAL)
prop_type_SIRNA.setLabel('siRNA')
prop_type_SIRNA.setManagedInternally(False)
prop_type_SIRNA.setInternalNamespace(False)
prop_type_SIRNA.setMaterialType(material_type_SIRNA)

assignment_MATERIAL_BACTERIUM_DESCRIPTION = tr.assignPropertyType(material_type_BACTERIUM, prop_type_DESCRIPTION)
assignment_MATERIAL_BACTERIUM_DESCRIPTION.setMandatory(False)
assignment_MATERIAL_BACTERIUM_DESCRIPTION.setSection(None)
assignment_MATERIAL_BACTERIUM_DESCRIPTION.setPositionInForms(1)

assignment_MATERIAL_COMPOUND_DESCRIPTION = tr.assignPropertyType(material_type_COMPOUND, prop_type_DESCRIPTION)
assignment_MATERIAL_COMPOUND_DESCRIPTION.setMandatory(False)
assignment_MATERIAL_COMPOUND_DESCRIPTION.setSection(None)
assignment_MATERIAL_COMPOUND_DESCRIPTION.setPositionInForms(1)

assignment_EXPERIMENT_COMPOUND_HCS_DESCRIPTION = tr.assignPropertyType(exp_type_COMPOUND_HCS, prop_type_DESCRIPTION)
assignment_EXPERIMENT_COMPOUND_HCS_DESCRIPTION.setMandatory(True)
assignment_EXPERIMENT_COMPOUND_HCS_DESCRIPTION.setSection(None)
assignment_EXPERIMENT_COMPOUND_HCS_DESCRIPTION.setPositionInForms(1)

assignment_MATERIAL_CONTROL_DESCRIPTION = tr.assignPropertyType(material_type_CONTROL, prop_type_DESCRIPTION)
assignment_MATERIAL_CONTROL_DESCRIPTION.setMandatory(False)
assignment_MATERIAL_CONTROL_DESCRIPTION.setSection(None)
assignment_MATERIAL_CONTROL_DESCRIPTION.setPositionInForms(1)

assignment_SAMPLE_CONTROL_WELL_CONTROL = tr.assignPropertyType(samp_type_CONTROL_WELL, prop_type_CONTROL)
assignment_SAMPLE_CONTROL_WELL_CONTROL.setMandatory(False)
assignment_SAMPLE_CONTROL_WELL_CONTROL.setSection(None)
assignment_SAMPLE_CONTROL_WELL_CONTROL.setPositionInForms(1)

assignment_MATERIAL_GENE_DESCRIPTION = tr.assignPropertyType(material_type_GENE, prop_type_DESCRIPTION)
assignment_MATERIAL_GENE_DESCRIPTION.setMandatory(False)
assignment_MATERIAL_GENE_DESCRIPTION.setSection(None)
assignment_MATERIAL_GENE_DESCRIPTION.setPositionInForms(2)

assignment_MATERIAL_GENE_GENE_SYMBOLS = tr.assignPropertyType(material_type_GENE, prop_type_GENE_SYMBOLS)
assignment_MATERIAL_GENE_GENE_SYMBOLS.setMandatory(False)
assignment_MATERIAL_GENE_GENE_SYMBOLS.setSection(None)
assignment_MATERIAL_GENE_GENE_SYMBOLS.setPositionInForms(4)

assignment_DATA_SET_HCS_ANALYSIS_WELL_FEATURES_ANALYSIS_PROCEDURE = tr.assignPropertyType(data_set_type_HCS_ANALYSIS_WELL_FEATURES, prop_type_ANALYSIS_PROCEDURE)
assignment_DATA_SET_HCS_ANALYSIS_WELL_FEATURES_ANALYSIS_PROCEDURE.setMandatory(False)
assignment_DATA_SET_HCS_ANALYSIS_WELL_FEATURES_ANALYSIS_PROCEDURE.setSection(None)
assignment_DATA_SET_HCS_ANALYSIS_WELL_FEATURES_ANALYSIS_PROCEDURE.setPositionInForms(1)

assignment_DATA_SET_HCS_IMAGE_SEGMENTATION_ANALYSIS_PROCEDURE = tr.assignPropertyType(data_set_type_HCS_IMAGE_SEGMENTATION, prop_type_ANALYSIS_PROCEDURE)
assignment_DATA_SET_HCS_IMAGE_SEGMENTATION_ANALYSIS_PROCEDURE.setMandatory(False)
assignment_DATA_SET_HCS_IMAGE_SEGMENTATION_ANALYSIS_PROCEDURE.setSection(None)
assignment_DATA_SET_HCS_IMAGE_SEGMENTATION_ANALYSIS_PROCEDURE.setPositionInForms(1)

assignment_SAMPLE_PLATE_PLATE_GEOMETRY = tr.assignPropertyType(samp_type_PLATE, prop_type_PLATE_GEOMETRY)
assignment_SAMPLE_PLATE_PLATE_GEOMETRY.setMandatory(True)
assignment_SAMPLE_PLATE_PLATE_GEOMETRY.setSection(None)
assignment_SAMPLE_PLATE_PLATE_GEOMETRY.setPositionInForms(1)

assignment_MATERIAL_SIRNA_NUCLEOTIDE_SEQUENCE = tr.assignPropertyType(material_type_SIRNA, prop_type_NUCLEOTIDE_SEQUENCE)
assignment_MATERIAL_SIRNA_NUCLEOTIDE_SEQUENCE.setMandatory(True)
assignment_MATERIAL_SIRNA_NUCLEOTIDE_SEQUENCE.setSection(None)
assignment_MATERIAL_SIRNA_NUCLEOTIDE_SEQUENCE.setPositionInForms(1)

assignment_MATERIAL_SIRNA_DESCRIPTION = tr.assignPropertyType(material_type_SIRNA, prop_type_DESCRIPTION)
assignment_MATERIAL_SIRNA_DESCRIPTION.setMandatory(False)
assignment_MATERIAL_SIRNA_DESCRIPTION.setSection(None)
assignment_MATERIAL_SIRNA_DESCRIPTION.setPositionInForms(3)

assignment_MATERIAL_SIRNA_INHIBITOR_OF = tr.assignPropertyType(material_type_SIRNA, prop_type_INHIBITOR_OF)
assignment_MATERIAL_SIRNA_INHIBITOR_OF.setMandatory(True)
assignment_MATERIAL_SIRNA_INHIBITOR_OF.setSection(None)
assignment_MATERIAL_SIRNA_INHIBITOR_OF.setPositionInForms(4)

assignment_MATERIAL_SIRNA_LIBRARY_ID = tr.assignPropertyType(material_type_SIRNA, prop_type_LIBRARY_ID)
assignment_MATERIAL_SIRNA_LIBRARY_ID.setMandatory(False)
assignment_MATERIAL_SIRNA_LIBRARY_ID.setSection(None)
assignment_MATERIAL_SIRNA_LIBRARY_ID.setPositionInForms(5)

assignment_EXPERIMENT_SIRNA_HCS_DESCRIPTION = tr.assignPropertyType(exp_type_SIRNA_HCS, prop_type_DESCRIPTION)
assignment_EXPERIMENT_SIRNA_HCS_DESCRIPTION.setMandatory(True)
assignment_EXPERIMENT_SIRNA_HCS_DESCRIPTION.setSection(None)
assignment_EXPERIMENT_SIRNA_HCS_DESCRIPTION.setPositionInForms(1)

assignment_EXPERIMENT_SIRNA_HCS_MICROSCOPE = tr.assignPropertyType(exp_type_SIRNA_HCS, prop_type_MICROSCOPE)
assignment_EXPERIMENT_SIRNA_HCS_MICROSCOPE.setMandatory(False)
assignment_EXPERIMENT_SIRNA_HCS_MICROSCOPE.setSection(None)
assignment_EXPERIMENT_SIRNA_HCS_MICROSCOPE.setPositionInForms(2)

assignment_SAMPLE_SIRNA_WELL_SIRNA = tr.assignPropertyType(samp_type_SIRNA_WELL, prop_type_SIRNA)
assignment_SAMPLE_SIRNA_WELL_SIRNA.setMandatory(False)
assignment_SAMPLE_SIRNA_WELL_SIRNA.setSection(None)
assignment_SAMPLE_SIRNA_WELL_SIRNA.setPositionInForms(1)

assignment_SAMPLE_SIRNA_WELL_GENE = tr.assignPropertyType(samp_type_SIRNA_WELL, prop_type_GENE)
assignment_SAMPLE_SIRNA_WELL_GENE.setMandatory(False)
assignment_SAMPLE_SIRNA_WELL_GENE.setSection(None)
assignment_SAMPLE_SIRNA_WELL_GENE.setPositionInForms(2)

assignment_MATERIAL_VIRUS_DESCRIPTION = tr.assignPropertyType(material_type_VIRUS, prop_type_DESCRIPTION)
assignment_MATERIAL_VIRUS_DESCRIPTION.setMandatory(False)
assignment_MATERIAL_VIRUS_DESCRIPTION.setSection(None)
assignment_MATERIAL_VIRUS_DESCRIPTION.setPositionInForms(1)
