/* Copyright 2016 ETH Zuerich, SIS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

/**
 * @author Ganime Betul Akin
 */

public class SynchronizerFacade implements ISynchronizerFacade
{
    private static final String INFO_MESSAGE = "The following %s will be %s";

    private static final Object separatorStr = "---------------------";

    final String sessionToken;

    final ICommonServer commonServer;

    final Logger operationLog;

    Map<String, String> fileformatTypesToUpdate = new HashMap<String, String>();

    Set<String> fileformatTypesToAdd = new HashSet<String>();

    Set<NewETPTAssignment> propertyAssignmentsToUpdate = new HashSet<>();

    Set<NewETPTAssignment> propertyAssignmentsToAdd = new HashSet<>();

    Map<String, String> propertyAssignmentsToBreak = new HashMap<String, String>();

    Map<String, String> propertyTypesToUpdate = new HashMap<String, String>();

    Set<String> propertyTypesToAdd = new HashSet<String>();

    Map<String, String> validationPluginsToUpdate = new HashMap<String, String>();

    Set<String> validationPluginsToAdd = new HashSet<String>();

    final boolean dryRun;
    
    final boolean verbose;

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

    public SynchronizerFacade(String openBisServerUrl, String harvesterUser, String harvesterPassword, boolean dryRun, boolean verbose, Logger operationLog)
    {
        this.commonServer = ServiceFinderUtils.getCommonServer(openBisServerUrl);
        this.sessionToken = ServiceFinderUtils.login(commonServer, harvesterUser, harvesterPassword);
        this.dryRun = dryRun;
        this.verbose = verbose || dryRun;
        this.operationLog = operationLog;
    }

    @Override
    public void updateFileFormatType(AbstractType type)
    {
        if (verbose == true)
        {
            String change = "Description :" + type.getDescription();
            fileformatTypesToUpdate.put(type.getCode(), change);
        } 
        if(dryRun == false)
        {
            commonServer.updateFileFormatType(sessionToken, type);
        }
    }

    @Override
    public void registerFileFormatType(FileFormatType type)
    {
        if (verbose == true)
        {
            fileformatTypesToAdd.add(type.getCode());
        }         
        if(dryRun == false)
        {
            commonServer.registerFileFormatType(sessionToken, type);
        }
    }

    @Override
    public void updatePropertyTypeAssignment(NewETPTAssignment newETPTAssignment)
    {
        if (verbose == true)
        {
            propertyAssignmentsToUpdate.add(newETPTAssignment);
        } 
        if(dryRun == false)
        {
            commonServer.updatePropertyTypeAssignment(sessionToken, newETPTAssignment);
        }
    }

    @Override
    public void assignPropertyType(NewETPTAssignment newETPTAssignment)
    {
        if (verbose == true)
        {
            propertyAssignmentsToAdd.add(newETPTAssignment);
        }
        if(dryRun == false)
        {
            commonServer.assignPropertyType(sessionToken, newETPTAssignment);
        }
    }

    @Override
    public void unassignPropertyType(EntityKind entityKind, String propertyTypeCode, String entityTypeCode)
    {
        if (verbose == true)
        {
            propertyAssignmentsToBreak.put(entityTypeCode + "(" + entityKind.name() + ")", propertyTypeCode);
        } 
        if(dryRun == false)
        {
            commonServer.unassignPropertyType(sessionToken, entityKind, propertyTypeCode, entityTypeCode);
        }
    }

    @Override
    public void updatePropertyType(PropertyType propertyType)
    {
        if (verbose == true)
        {
            String change = "Label :" + propertyType.getLabel() + " , description :" + propertyType.getDescription();
            propertyTypesToUpdate.put(propertyType.getCode(), change);
        } 
        if(dryRun == false)
        {
            commonServer.updatePropertyType(sessionToken, propertyType);
        }
    }

    @Override
    public void registerPropertyType(PropertyType propertyType)
    {
        if (verbose == true)
        {
            propertyTypesToAdd.add(propertyType.getCode());
        }
        if(dryRun == false)
        {
            commonServer.registerPropertyType(sessionToken, propertyType);
        }
    }

    @Override
    public void updateValidationPlugin(Script script)
    {
        if (verbose == true)
        {
            String change = "Description :" + script.getDescription(); //+ ", script :" + script.getScript();
            validationPluginsToUpdate.put(script.getName(), change);
        } 
        if(dryRun == false)
        {
            commonServer.updateScript(sessionToken, script);
        }
    }

    @Override
    public void registerValidationPlugin(Script script)
    {
        if (verbose == true)
        {
            validationPluginsToAdd.add(script.getName());
        } 
        if(dryRun == false)
        {
            commonServer.registerScript(sessionToken, script);
        }
    }

    @Override
    public void registerVocabulary(NewVocabulary vocab)
    {
        if (verbose == true)
        {
            vocabulariesToAdd.add(vocab.getCode());
        } 
        if(dryRun == false)
        {
            commonServer.registerVocabulary(sessionToken, vocab);
        }
    }

    @Override
    public void updateVocabulary(Vocabulary vocab)
    {
        if (verbose == true)
        {
            String change = "Code : " + vocab.getCode() + ", description :" + vocab.getDescription();
            vocabulariesToUpdate.put(vocab.getCode(), change);
        } 
        if(dryRun == false)
        {
            commonServer.updateVocabulary(sessionToken, vocab);
        }
    }

    @Override
    public void registerSampleType(SampleType sampleType)
    {
        if (verbose == true)
        {
            sampleTypesToAdd.add(sampleType.getCode());
        } 
        if(dryRun == false)
        {
            commonServer.registerSampleType(sessionToken, sampleType);
        }
    }

    @Override
    public void registerDataSetType(DataSetType dataSetType)
    {
        if (verbose == true)
        {
            dataSetTypesToAdd.add(dataSetType.getCode());
        } 
        if(dryRun == false)
        {
            commonServer.registerDataSetType(sessionToken, dataSetType);
        }
    }

    @Override
    public void registerExperimentType(ExperimentType experimentType)
    {
        if (verbose == true)
        {
            experimentTypesToAdd.add(experimentType.getCode());
        } 
        if(dryRun == false)
        {
            commonServer.registerExperimentType(sessionToken, experimentType);
        }
    }

    @Override
    public void registerMaterialType(MaterialType materialType)
    {
        if (verbose == true)
        {
            materialTypesToAdd.add(materialType.getCode());
        }
        if(dryRun == false)
        {
            commonServer.registerMaterialType(sessionToken, materialType);
        }
    }

    @Override
    public void deleteVocabularyTerms(TechId vocabularyId, String vocabularyCode, List<VocabularyTerm> termsToBeDeleted,
            List<VocabularyTermReplacement> termsToBeReplaced)
    {
        if (verbose == true)
        {
            vocabularyTermsToDelete.put(vocabularyCode, termsToBeDeleted);
        } 
        if(dryRun == false)
        {
            commonServer.deleteVocabularyTerms(sessionToken, vocabularyId, termsToBeDeleted, termsToBeReplaced);
        }
    }

    @Override
    public void updateVocabularyTerm(VocabularyTerm term)
    {
        if (verbose == true)
        {
            String change = "Label :" + term.getLabel() + ", Ordinal :" + term.getOrdinal() +
                    ", Description :" + term.getDescription();
            vocabulariesToUpdate.put(term.getCode(), change);
        }
        if(dryRun == false)
        {
            commonServer.updateVocabularyTerm(sessionToken, term);
        }
    }

    @Override
    public void updateSampleType(EntityType entityType)
    {
        if (verbose == true)
        {
            String change = "Code" + entityType.getCode() + ", Description :" + entityType.getDescription();
            sampleTypesToUpdate.put(entityType.getCode(), change);
        }        
        if(dryRun == false)
        {
            commonServer.updateSampleType(sessionToken, entityType);
        }
    }

    @Override
    public void updateDataSetType(EntityType entityType)
    {
        if (verbose == true)
        {
            String change = "Code" + entityType.getCode() + ", Description :" + entityType.getDescription();
            dataSetTypesToUpdate.put(entityType.getCode(), change);
        } 
        if(dryRun == false)
        {
            commonServer.updateDataSetType(sessionToken, entityType);
        }
    }

    @Override
    public void updateExperimentType(EntityType entityType)
    {
        if (verbose == true)
        {
            String change = "Code" + entityType.getCode() + ", Description :" + entityType.getDescription();
            experimentTypesToUpdate.put(entityType.getCode(), change);
        }
        if(dryRun == false)
        {
            commonServer.updateExperimentType(sessionToken, entityType);
        }
    }

    @Override
    public void updateMaterialType(EntityType entityType)
    {
        if (verbose == true)
        {
            String change = "Code" + entityType.getCode() + ", Description :" + entityType.getDescription();
            materialTypesToUpdate.put(entityType.getCode(), change);
        } 
        if(dryRun == false)
        {
            commonServer.updateMaterialType(sessionToken, entityType);
        }
    }

    @Override
    public void addVocabularyTerms(String vocabularyCode, TechId techId, List<VocabularyTerm> termsToBeAdded)
    {
        if (verbose == true)
        {
            vocabularyTermsToAdd.put(vocabularyCode, termsToBeAdded);
        } 
        if(dryRun == false)
        {
            commonServer.addVocabularyTerms(sessionToken, techId, termsToBeAdded, null, true);
        }
    }

    @Override
    public void printSummary()
    {
        if (verbose == false) {
            return;
        }
        printSummary(fileformatTypesToAdd, "file format types", "added");
        printSummary(fileformatTypesToUpdate, "file format types", "updated");

        printSummary(validationPluginsToAdd, "validation plugins", "added");
        printSummary(validationPluginsToUpdate, "validation plugins", "updated");

        printSummary(propertyTypesToAdd, "property types", " added");
        printSummary(propertyTypesToUpdate, "property types", "updated");

        printSummary(vocabulariesToAdd, "vocabularies", " added");
        printSummary(vocabulariesToUpdate, "vocabularies", "updated");

        printSummaryVocabularyTerm(vocabularyTermsToDelete, "vocabulary terms", "deleted");
        printSummary(vocabularyTermsToUpdate, "vocabulary terms", "updated");
        printSummary(vocabulariesToAdd, "vocabulary terms", "added");

        printSummary(sampleTypesToAdd, "sample types", "added");
        printSummary(experimentTypesToAdd, "experiment types", "added");
        printSummary(dataSetTypesToAdd, "data set types", "added");
        printSummary(materialTypesToAdd, "material types", "added");

        printSummary(sampleTypesToUpdate, "sample types", "updated");
        printSummary(experimentTypesToUpdate, "experiment types", "updated");
        printSummary(dataSetTypesToUpdate, "data set types", "updated");
        printSummary(materialTypesToUpdate, "material types", "updated");

        printSummaryPropertyAssignments(propertyAssignmentsToAdd, "property assignments", "added");
        printSummaryPropertyAssignments(propertyAssignmentsToUpdate, "property assignments", "updated");
        printSummary(propertyAssignmentsToBreak, "property assignments", "removed");
    }

    private void printSummary(Set<String> set, String type, String operation)
    {
        if (set.isEmpty() == true)
            return;
        operationLog.info(separatorStr);
        String message = String.format(INFO_MESSAGE, type, operation);
        operationLog.info(message);
        operationLog.info(separatorStr);
        for (String str : set)
        {
            operationLog.info(str);
        }
    }

    private void printSummary(Map<String, String> map, String type, String operation)
    {
        if (map.isEmpty() == true)
            return;
        operationLog.info(separatorStr);
        String message = String.format(INFO_MESSAGE, type, operation);
        operationLog.info(message);
        operationLog.info(separatorStr);
        for (String key : map.keySet())
        {
            operationLog.info(key + " - " + map.get(key));
        }
    }

    private void printSummaryVocabularyTerm(Map<String, List<VocabularyTerm>> map, String type, String operation)
    {
        if (map.isEmpty() == true)
            return;
        operationLog.info(separatorStr);
        String message = String.format(INFO_MESSAGE, type, operation);
        operationLog.info(message);
        operationLog.info(separatorStr);
        for (String key : map.keySet())
        {
            String termStr = "";
            List<VocabularyTerm> terms = map.get(key);
            for (VocabularyTerm vocabularyTerm : terms)
            {
                termStr += vocabularyTerm.getCode();
                termStr += ", ";
            }
            operationLog.info(key + " - " + map.get(key) + ":" + termStr.substring(0, termStr.length() - 1));
        }
    }

    private void printSummaryPropertyAssignments(Set<NewETPTAssignment> set, String type, String operation)
    {
        if (set.isEmpty() == true)
            return;
        operationLog.info(separatorStr);
        String message = String.format(INFO_MESSAGE, type, operation);
        operationLog.info(message);
        operationLog.info(separatorStr);
        for (NewETPTAssignment assignment : set)
        {
            operationLog.info(assignment.getEntityTypeCode() + "(" + assignment.getEntityKind().name() + ") : " + assignment.getPropertyTypeCode());
        }
    }
}
