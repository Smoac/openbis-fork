import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

tr = service.transaction()
#
#  Materials
#
material_type_COMPOUND = tr.getMaterialType('COMPOUND')
if not material_type_COMPOUND:
    material_type_COMPOUND = tr.createNewMaterialType('COMPOUND')
    material_type_COMPOUND.setDescription('Compound material')

material_type_COMPOUND_BATCH = tr.createNewMaterialType('COMPOUND_BATCH')
material_type_COMPOUND_BATCH.setDescription('Compound batch material')

prop_type_COMPOUND = tr.createNewPropertyType('COMPOUND', DataType.MATERIAL)
prop_type_COMPOUND.setLabel('Compound material')
tr.assignPropertyType(material_type_COMPOUND_BATCH, prop_type_COMPOUND)


#
# Experiment + Properties
#

experiment_type_COMPOUND_HCS = tr.getExperimentType('COMPOUND_HCS')

prop_type_OBSERVER_EMAILS = tr.createNewPropertyType('OBSERVER_EMAILS', DataType.VARCHAR)
prop_type_OBSERVER_EMAILS.setLabel('Observer e-mails')
tr.assignPropertyType(experiment_type_COMPOUND_HCS, prop_type_OBSERVER_EMAILS)

prop_type_LIBRARY_TEMPLATE = tr.createNewPropertyType('LIBRARY_TEMPLATE', DataType.MULTILINE_VARCHAR)
prop_type_LIBRARY_TEMPLATE.setLabel('Library Template')
tr.assignPropertyType(experiment_type_COMPOUND_HCS, prop_type_LIBRARY_TEMPLATE)

#
# Samples
#

samp_type_POSITIVE_CONTROL = tr.createNewSampleType('POSITIVE_CONTROL')
samp_type_POSITIVE_CONTROL.setListable(False)
samp_type_POSITIVE_CONTROL.setGeneratedCodePrefix('P')


samp_type_NEGATIVE_CONTROL = tr.createNewSampleType('NEGATIVE_CONTROL')
samp_type_NEGATIVE_CONTROL.setListable(False)
samp_type_NEGATIVE_CONTROL.setGeneratedCodePrefix('N')


samp_type_COMPOUND_WELL = tr.createNewSampleType('COMPOUND_WELL')
samp_type_COMPOUND_WELL.setListable(False)
samp_type_COMPOUND_WELL.setGeneratedCodePrefix('C')



prop_type_COMPOUND_BATCH = tr.createNewPropertyType('COMPOUND_BATCH', DataType.MATERIAL)
prop_type_COMPOUND_BATCH.setLabel('Compound batch material')
tr.assignPropertyType(samp_type_COMPOUND_WELL, prop_type_COMPOUND_BATCH)

tr.assignPropertyType(samp_type_COMPOUND_WELL, prop_type_COMPOUND)

prop_type_CONCENTRATION_M = tr.createNewPropertyType('CONCENTRATION_M', DataType.REAL)
prop_type_CONCENTRATION_M.setLabel('Concentration (M)')
tr.assignPropertyType(samp_type_COMPOUND_WELL, prop_type_CONCENTRATION_M)

#
# Data Sets
#
data_set_type_HCS_IMAGE_RAW = tr.getDataSetType('HCS_IMAGE_RAW')

prop_type_ACQUISITION_BATCH = tr.createNewPropertyType('ACQUISITION_BATCH', DataType.VARCHAR)
prop_type_ACQUISITION_BATCH.setLabel('Acquisition Batch')
tr.assignPropertyType(data_set_type_HCS_IMAGE_RAW, prop_type_ACQUISITION_BATCH)

