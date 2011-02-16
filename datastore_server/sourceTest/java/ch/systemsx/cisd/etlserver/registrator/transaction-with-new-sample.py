transaction = service.transaction(incoming, factory)
dataSet = transaction.createNewDataSet()
transaction.moveFile(incoming.getPath() + '/sub_data_set_1', dataSet)
dataSet.setDataSetType('O1')

experiment = transaction.createNewExperiment('/SPACE/PROJECT/EXP', 'EXP_TYPE')
experiment.setPropertyValue('propCode', 'propValue')
experiment.setExperimentType('experiment_type')

sample = transaction.createNewSample('db:/PROJECT/SAMPLE', 'SAMP_TYPE')
sample.setSampleType('sample_type')
sample.setPropertyValue('samplePropCode', 'samplePropValue')
sample.setExperiment(experiment)

dataSet.setSample(sample)

