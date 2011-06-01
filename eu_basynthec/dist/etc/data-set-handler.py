from datetime import datetime

def retrieve_or_create_experiment(tr):
	# Create an experiment
	now_str = datetime.today().strftime('%Y%m%d-%H%M%S')
	expid = "/SHARED/SHARED/" + now_str
	exp = tr.getExperiment(expid)

	if None == exp:
		exp = tr.createNewExperiment(expid, 'BASYNTHEC')
		exp.setPropertyValue("DESCRIPTION", "An experiment created on " + now_str)
		
	return exp

tr = service.transaction(incoming)
exp = retrieve_or_create_experiment(tr)
dataSet = tr.createNewDataSet()
dataSet.setExperiment(exp)
tr.moveFile(incoming.getAbsolutePath(), dataSet)

