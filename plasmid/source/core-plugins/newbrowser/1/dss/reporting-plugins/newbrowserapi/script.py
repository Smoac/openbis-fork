
def process(tr, parameters, tableBuilder):
	method = parameters.get("method");
	
	isOk = False;
	if method == "insertSample":
		isOk = insertSample(tr, parameters, tableBuilder);
	
	if isOk:
		tableBuilder.addHeader("STATUS");
		tableBuilder.addHeader("MESSAGE");
		row = tableBuilder.addRow();
		row.setCell("STATUS","OK");
		row.setCell("MESSAGE", "Sample Created");
	else :
		tableBuilder.addHeader("STATUS");
		tableBuilder.addHeader("MESSAGE");
		row = tableBuilder.addRow();
		row.setCell("STATUS","FAIL");
		row.setCell("MESSAGE", "No method found");

def insertSample(tr, parameters, tableBuilder):
	#Mandatory parameters
	sampleSpace = parameters.get("sampleSpace"); #String
	sampleProject = parameters.get("sampleProject"); #String
	sampleCode = parameters.get("sampleCode"); #String
	sampleType = parameters.get("sampleType"); #String
	sampleProperties = parameters.get("sampleProperties"); #java.util.LinkedHashMap<String, String> where the key is the name
	
	#Optional parameters
	sampleParents = parameters.get("sampleParents"); #List<String> Identifiers are in SPACE/CODE format
	sampleExperimentCreate = parameters.get("sampleExperimentCreate"); #Boolean
	sampleExperimentCode = parameters.get("sampleExperimentCode"); #String
	sampleExperimentType = parameters.get("sampleExperimentType"); #String
	sampleExperimentProject = parameters.get("sampleExperimentProject"); #String
	
	#Assign sample space
	sample = tr.createNewSample('/' + sampleSpace + '/' + sampleCode, sampleType);
	
	#Assign sample properties
	for key in sampleProperties.keySet():
		sample.setPropertyValue(key,sampleProperties[key]);
	
	#Assign sample to a newly created experiment
	if sampleExperimentProject != None and sampleExperimentCode != None and sampleExperimentType != None and sampleExperimentCreate:
		experiment = tr.createNewExperiment('/' +sampleSpace+ '/' + sampleExperimentProject + '/' +sampleExperimentCode, sampleExperimentType)
		sample.setExperiment(experiment)
	
	#Add sample parents
	if sampleParents != None:
		sample.setParentSampleIdentifiers(sampleParents);
	
	#This requires to access a private method
	return tr.transaction.commit();