package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.ServiceFinderUtils;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;

public class SynchronizerFacade implements ISynchronizerFacade {
	private static final String INFO_MESSAGE = "The following %s will be %s";

	private static final Object separatorStr = "---------------------";

	final String sessionToken;

    final ICommonServer commonServer;
    
    final Logger operationLog;
    
    Map<String,String> fileformatTypesToUpdate = new HashMap<String, String>();
    Set<String> fileformatTypesToAdd = new HashSet<String>();
    
    Set<NewETPTAssignment> propertyAssignmentsToUpdate = new HashSet<>();
    Set<NewETPTAssignment> propertyAssignmentsToAdd = new HashSet<>();
    Map<String, String> propertyAssignmentsToBreak = new HashMap<String, String>();

    Map<String, String> propertyTypesToUpdate = new HashMap<String, String>();
    Set<String> propertyTypesToAdd = new HashSet<String>();

    Map<String, String> validationPluginsToUpdate = new HashMap<String, String>();
    Set<String> validationPluginsToAdd = new HashSet<String>();

    final boolean dryRun;

	Set<String> vocabulariesToAdd = new HashSet<String>();
	Map<String, String> vocabulariesToUpdate = new HashMap<String, String>();

	Map<String, List<VocabularyTerm>> vocabularyTermsToDelete = new HashMap<String, List<VocabularyTerm>>();
	Map<String, String> vocabularyTermsToUpdate = new HashMap<String, String>();
	Map<String, List<VocabularyTerm>> vocabularyTermsToAdd = new HashMap<String, List<VocabularyTerm>>();

	Set<String> sampleTypesToAdd = new HashSet<String>();
	Set<String> experimentTypesToAdd = new HashSet<String>();
	Set<String> dataSetTypesToAdd = new HashSet<String>();
	Set<String> materialTypesToAdd = new HashSet<String>();

	Map<String, String> sampleTypesToUpdate = new HashMap<String, String>();
	Map<String, String> experimentTypesToUpdate = new HashMap<String, String>();
	Map<String, String> dataSetTypesToUpdate = new HashMap<String, String>();
	Map<String, String> materialTypesToUpdate = new HashMap<String, String>();

    public SynchronizerFacade(String openBisServerUrl, String harvesterUser, String harvesterPassword, boolean dryRun, Logger operationLog) {
        this.commonServer = ServiceFinderUtils.getCommonServer(openBisServerUrl);
        this.sessionToken = ServiceFinderUtils.login(commonServer, harvesterUser, harvesterPassword);
        this.dryRun = dryRun;
        this.operationLog = operationLog;
    }

	@Override
	public void updateFileFormatType(AbstractType type) {
		if(dryRun) {
			String change = "Description :" + type.getDescription();
			fileformatTypesToUpdate.put(type.getCode(), change);
		}
		else {
			commonServer.updateFileFormatType(sessionToken, type);
		}
	}

	@Override
	public void registerFileFormatType(FileFormatType type) {
		if(dryRun) {
			fileformatTypesToAdd.add(type.getCode());
		}
		else {
			commonServer.registerFileFormatType(sessionToken, type);
		}
	}

	@Override
	public void updatePropertyTypeAssignment(NewETPTAssignment newETPTAssignment) {
		if(dryRun) {
			propertyAssignmentsToUpdate.add(newETPTAssignment);
		}
		else {
			commonServer.updatePropertyTypeAssignment(sessionToken, newETPTAssignment);
		}
	}

	@Override
	public void assignPropertyType(NewETPTAssignment newETPTAssignment) {
		if(dryRun) {
			propertyAssignmentsToAdd.add(newETPTAssignment);
		}
		else {
			commonServer.assignPropertyType(sessionToken, newETPTAssignment);
		}
	}

	@Override
	public void unassignPropertyType(EntityKind entityKind, String propertyTypeCode, String entityTypeCode) {
		if(dryRun) {
			propertyAssignmentsToBreak.put(entityTypeCode + "(" + entityKind.name() + ")", propertyTypeCode);
		}
		else {
			commonServer.unassignPropertyType(sessionToken, entityKind, propertyTypeCode, entityTypeCode);
		}
	}

	@Override
	public void updatePropertyType(PropertyType propertyType) {
		if(dryRun) {
			String change = "Label :" + propertyType.getLabel() + " , description :" +  propertyType.getDescription();
			propertyTypesToUpdate.put(propertyType.getCode(), change);
		}
		else {
			commonServer.updatePropertyType(sessionToken, propertyType);
		}
	}

	@Override
	public void registerPropertyType(PropertyType propertyType) {
		if (dryRun) {
			propertyTypesToAdd.add(propertyType.getCode());
		}
		else {
			commonServer.registerPropertyType(sessionToken, propertyType);
		}
	}

	@Override
	public void updateValidationPlugin(Script script) {
		if (dryRun) {//"Name :" + script.getName() + ", 
			String change =  "Description :" + script.getDescription() + ", script :" +  script.getScript();
			validationPluginsToUpdate.put(script.getName(), change);
		}
		else {
			commonServer.updateScript(sessionToken, script);
		}
	}

	@Override
	public void registerValidationPlugin(Script script) {
		if (dryRun) {
			validationPluginsToAdd.add(script.getName());
		}
		else {
			commonServer.registerScript(sessionToken, script);
		}
	}

	@Override
	public void registerVocabulary(NewVocabulary vocab) {
		if(dryRun == true) {
			vocabulariesToAdd.add(vocab.getCode());
		}
		else {
			commonServer.registerVocabulary(sessionToken, vocab);
		}
	}
		

	@Override
	public void updateVocabulary(Vocabulary vocab) {
		if(dryRun == true) {
			String change = "Code : " + vocab.getCode() + ", description :" + vocab.getDescription();
			vocabulariesToUpdate.put(vocab.getCode(), change);
		}
		else {
			commonServer.updateVocabulary(sessionToken, vocab);
		}
	}

	@Override
	public void registerSampleType(SampleType sampleType) {
		if(dryRun == true) {
			sampleTypesToAdd.add(sampleType.getCode());
		}
		else {
			commonServer.registerSampleType(sessionToken, sampleType);
		}
	}

	@Override
	public void registerDataSetType(DataSetType dataSetType) {
		if(dryRun == true) {
			dataSetTypesToAdd.add(dataSetType.getCode());
		}
		else {
			commonServer.registerDataSetType(sessionToken, dataSetType);
		}
	}

	@Override
	public void registerExperimentType(ExperimentType experimentType) {
		if(dryRun == true) {
			experimentTypesToAdd.add(experimentType.getCode());
		}
		else {
			commonServer.registerExperimentType(sessionToken, experimentType);
		}
	}

	@Override
	public void registerMaterialType(MaterialType materialType) {
		if(dryRun == true) {
			materialTypesToAdd.add(materialType.getCode());
		}
		else {
			commonServer.registerMaterialType(sessionToken, materialType);
		}
	}

	@Override
	public void deleteVocabularyTerms(TechId vocabularyId, String vocabularyCode, List<VocabularyTerm> termsToBeDeleted,
			List<VocabularyTermReplacement> termsToBeReplaced) {
		if(dryRun == true) {
			vocabularyTermsToDelete.put(vocabularyCode, termsToBeDeleted);
		}
		else {
			commonServer.deleteVocabularyTerms(sessionToken, vocabularyId, termsToBeDeleted, termsToBeReplaced);
		}
	}

	@Override
	public void updateVocabularyTerm(VocabularyTerm term) {
		if(dryRun == true) {
			String change =  "Label :" + term.getLabel() + ", Ordinal :" + term.getOrdinal() + 
					", Description :" + term.getDescription();
			vocabulariesToUpdate.put(term.getCode(), change);
		}
		else {
			commonServer.updateVocabularyTerm(sessionToken, term);
		}
	}

	@Override
	public void updateSampleType(EntityType entityType) {
		if(dryRun == true) {
			String change =  "Code" + entityType.getCode() + ", Description :" + entityType.getDescription(); 
			sampleTypesToUpdate.put(entityType.getCode(), change);
		}
		else {
			commonServer.updateSampleType(sessionToken, entityType);
		}
	}

	@Override
	public void updateDataSetType(EntityType entityType) {
		if(dryRun == true) {
			String change =  "Code" + entityType.getCode() + ", Description :" + entityType.getDescription(); 
			dataSetTypesToUpdate.put(entityType.getCode(), change);
		}
		else {
			commonServer.updateDataSetType(sessionToken, entityType);
		}
	}

	@Override
	public void updateExperimentType(EntityType entityType) {
		if(dryRun == true) {
			String change =  "Code" + entityType.getCode() + ", Description :" + entityType.getDescription(); 
			experimentTypesToUpdate.put(entityType.getCode(), change);
		}
		else {
			commonServer.updateExperimentType(sessionToken, entityType);
		}
	}

	@Override
	public void updateMaterialType(EntityType entityType) {
		if(dryRun == true) {
			String change =  "Code" + entityType.getCode() + ", Description :" + entityType.getDescription(); 
			materialTypesToUpdate.put(entityType.getCode(), change);
		}
		else {
			commonServer.updateMaterialType(sessionToken, entityType);
		}
	}

	@Override
	public void addVocabularyTerms(String vocabularyCode, TechId techId, List<VocabularyTerm> termsToBeAdded) {
		if(dryRun == true) {
			vocabularyTermsToAdd.put(vocabularyCode, termsToBeAdded);
		}
		else {
			commonServer.addVocabularyTerms(sessionToken, techId, termsToBeAdded, null, true);
		}
	}

	@Override
	public void printSummary() {
		printSummary(fileformatTypesToAdd, "file format types",  "added");
		printSummary(fileformatTypesToUpdate, "file format types", "updated");

		printSummary(validationPluginsToAdd, "validation plugins", "added");
		printSummary(validationPluginsToUpdate, "validation plugins", "updated");

		printSummary(propertyTypesToAdd, "property types"," added");
		printSummary(propertyTypesToUpdate, "property types", "updated");

		printSummary(vocabulariesToAdd, "vocabularies", " added");
		printSummary(vocabulariesToUpdate, "vocabularies", "updated");

		printSummaryVocabularyTerm(vocabularyTermsToDelete, "vocabulary terms",  "deleted");
		printSummary(vocabularyTermsToUpdate, "vocabulary terms" , "updated");
		printSummary(vocabulariesToAdd, "vocabulary terms", "added");

		printSummary(sampleTypesToAdd, "sample types", "added");
		printSummary(experimentTypesToAdd, "experiment types", "added");
		printSummary(dataSetTypesToAdd, "data set types", "added");
		printSummary(materialTypesToAdd, "material types", "added");

		printSummary(sampleTypesToUpdate, "sample types",  "updated");
		printSummary(experimentTypesToUpdate, "experiment types", "updated");
		printSummary(dataSetTypesToUpdate, "data set types",  "updated");
		printSummary(materialTypesToUpdate, "material types", "updated");
		
		printSummaryPropertyAssignments(propertyAssignmentsToAdd, "property assignments", "added");
		printSummaryPropertyAssignments(propertyAssignmentsToUpdate, "property assignments", "updated");
		printSummary(propertyAssignmentsToBreak, "property assignments", "removed");
	}
	
	private void printSummary(Set<String> set, String type, String operation) {
		if (set.isEmpty() == true) 
			return;
        operationLog.info(separatorStr);
        String message = String.format(INFO_MESSAGE, type, operation);
		operationLog.info(message);
        operationLog.info(separatorStr);
		for (String str : set) {
			operationLog.info(str);
		}
	}

	private void printSummary(Map<String, String> map, String type, String operation) {
		if (map.isEmpty() == true) 
			return;
        operationLog.info(separatorStr);
        String message = String.format(INFO_MESSAGE, type, operation);
		operationLog.info(message);
        operationLog.info(separatorStr);
		for (String key : map.keySet()) {
			operationLog.info(key + " - " + map.get(key));
		}
	}
	
	private void printSummaryVocabularyTerm(Map<String, List<VocabularyTerm>> map, String type, String operation) {
		if (map.isEmpty() == true) 
			return;
        operationLog.info(separatorStr);
        String message = String.format(INFO_MESSAGE, type, operation);
		operationLog.info(message);
        operationLog.info(separatorStr);
		for (String key : map.keySet()) {
			String termStr = "";
			List<VocabularyTerm> terms = map.get(key);
			for (VocabularyTerm vocabularyTerm : terms) {
				termStr += vocabularyTerm.getCode();
				termStr += ", ";
			}
			operationLog.info(key + " - " + map.get(key) + ":" + termStr.substring(0, termStr.length()-1));
		}
	}
	
	private void printSummaryPropertyAssignments(Set<NewETPTAssignment> set, String type, String operation) {
		if (set.isEmpty() == true) 
			return;
        operationLog.info(separatorStr);
        String message = String.format(INFO_MESSAGE, type, operation);
		operationLog.info(message);
        operationLog.info(separatorStr);
		for (NewETPTAssignment assignment : set) {
			operationLog.info(assignment.getEntityTypeCode() + "(" + assignment.getEntityKind().name() + ") : " + assignment.getPropertyTypeCode() );
		}
	}
}
