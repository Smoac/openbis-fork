transaction = service.transaction(incoming, factory)
dataSet = transaction.createNewDataSet()
transaction.moveFile(incoming.getPath() + '/sub_data_set_1', dataSet)
dataSet.setDataSetType('O1')

experiment = transaction.createNewExperiment('/SPACE/PROJECT/EXP')
experiment.setPropertyValue('propCode', 'propValue')
experiment.setType('experiment_type')

sample = transaction.createNewSample('db:/PROJECT/SAMPLE')
sample.setType('sample_type')
sample.setPropertyValue('samplePropCode', 'samplePropValue')
sample.setExperiment(experiment)

dataSet.setSample(sample)

