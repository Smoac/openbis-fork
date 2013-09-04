
def process(tr, parameters, tableBuilder):
	method = parameters.get("method");
	
	isOk = False;
	if method == "insertSample":
		isOk = insertSample(tr, parameters, tableBuilder);
	if method == "updateSample":
		isOk = insertSample(tr, parameters, tableBuilder);

	if isOk:
		tableBuilder.addHeader("STATUS");
		tableBuilder.addHeader("MESSAGE");
		row = tableBuilder.addRow();
		row.setCell("STATUS","OK");
		row.setCell("MESSAGE", "Operation Successful");
	else :
		tableBuilder.addHeader("STATUS");
		tableBuilder.addHeader("MESSAGE");
		row = tableBuilder.addRow();
		row.setCell("STATUS","FAIL");
		row.setCell("MESSAGE", "Operation Failed");

def insertSample(tr, parameters, tableBuilder):
	#Mandatory parameters
	sampleSpace = parameters.get("sampleSpace"); #String
	sampleProject = parameters.get("sampleProject"); #String
	sampleCode = parameters.get("sampleCode"); #String
	sampleType = parameters.get("sampleType"); #String
	sampleProperties = parameters.get("sampleProperties"); #java.util.LinkedHashMap<String, String> where the key is the name
	
	#Optional parameters
	sampleParents = parameters.get("sampleParents"); #List<String> Identifiers are in SPACE/CODE format
	
	#Only used to create an experiment with the same code as the sample for the case of the ELN experiment
	sampleExperimentCreate = parameters.get("sampleExperimentCreate"); #Boolean
	sampleExperimentCode = parameters.get("sampleExperimentCode"); #String
	sampleExperimentType = parameters.get("sampleExperimentType"); #String
	sampleExperimentProject = parameters.get("sampleExperimentProject"); #String
	
	#Create/Get for update sample	
	sampleIdentifier = '/' + sampleSpace + '/' + sampleCode
	
	method = parameters.get("method");
	if method == "insertSample":
		sample = tr.createNewSample(sampleIdentifier, sampleType); #Create Sample given his id
		
		#Assign sample to a newly created experiment
		if sampleExperimentCreate and sampleExperimentProject != None and sampleExperimentCode != None and sampleExperimentType != None:
			experiment = tr.createNewExperiment('/' +sampleSpace+ '/' + sampleExperimentProject + '/' +sampleExperimentCode, sampleExperimentType)
			sample.setExperiment(experiment)
		
	if method == "updateSample":
		sample = tr.getSampleForUpdate(sampleIdentifier) #Retrieve Sample
	
	#Assign sample properties
	for key in sampleProperties.keySet():
		propertyValue = unicode(sampleProperties[key]);
		if propertyValue == "":
			propertyValue = None;
		
		sample.setPropertyValue(key,propertyValue);
		
	#Add sample parents
	if sampleParents != None:
		sample.setParentSampleIdentifiers(sampleParents);
	
	#This requires to access a private method
	return tr.transaction.commit();
	