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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

/**
 * @author Ganime Betul Akin
 */

public class SynchronizerFacade implements ISynchronizerFacade
{
    private static final String INFO_MESSAGE = "The following %s will be %s";

    private static final Object separatorStr = "---------------------";

    private final String sessionToken;

    private final ICommonServer commonServer;

    private final Logger operationLog;

    private Map<String, String> fileformatTypesToUpdate = new TreeMap<String, String>();

    private Set<String> fileformatTypesToAdd = new TreeSet<String>();

    private Set<NewETPTAssignment> propertyAssignmentsToUpdate = new HashSet<>();

    private Set<NewETPTAssignment> propertyAssignmentsToAdd = new HashSet<>();

    private Map<String, String> propertyAssignmentsToBreak = new TreeMap<String, String>();

    private Map<String, String> propertyTypesToUpdate = new TreeMap<String, String>();

    private Set<String> propertyTypesToAdd = new TreeSet<String>();

    private Map<String, String> validationPluginsToUpdate = new TreeMap<String, String>();

    private Set<String> validationPluginsToAdd = new TreeSet<String>();

    private final boolean dryRun;

    private final boolean verbose;

    private Set<String> vocabulariesToAdd = new TreeSet<String>();

    private Map<String, String> vocabulariesToUpdate = new TreeMap<String, String>();

    private Map<String, String> vocabularyTermsToUpdate = new TreeMap<String, String>();

    private Map<String, List<VocabularyTerm>> vocabularyTermsToAdd = new TreeMap<String, List<VocabularyTerm>>();

    private Set<String> sampleTypesToAdd = new TreeSet<String>();

    private Set<String> experimentTypesToAdd = new TreeSet<String>();

    private Set<String> dataSetTypesToAdd = new TreeSet<String>();

    private Set<String> materialTypesToAdd = new TreeSet<String>();

    private Map<String, String> sampleTypesToUpdate = new TreeMap<String, String>();

    private Map<String, String> experimentTypesToUpdate = new TreeMap<String, String>();

    private Map<String, String> dataSetTypesToUpdate = new TreeMap<String, String>();

    private Map<String, String> materialTypesToUpdate = new HashMap<String, String>();
    

    public SynchronizerFacade(String openBisServerUrl, String harvesterUser, String harvesterPassword, boolean dryRun, boolean verbose,
            Logger operationLog)
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
        if (dryRun == false)
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
        if (dryRun == false)
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
        if (dryRun == false)
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
        if (dryRun == false)
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
        if (dryRun == false)
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
        if (dryRun == false)
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
        if (dryRun == false)
        {
            commonServer.registerPropertyType(sessionToken, propertyType);
        }
    }

    @Override
    public void updateValidationPlugin(Script script)
    {
        if (verbose == true)
        {
            String change = "Description :" + script.getDescription(); // + ", script :" + script.getScript();
            validationPluginsToUpdate.put(script.getName(), change);
        }
        if (dryRun == false)
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
        if (dryRun == false)
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
        if (dryRun == false)
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
        if (dryRun == false)
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
        if (dryRun == false)
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
        if (dryRun == false)
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
        if (dryRun == false)
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
        if (dryRun == false)
        {
            commonServer.registerMaterialType(sessionToken, materialType);
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
        if (dryRun == false)
        {
            commonServer.updateVocabularyTerm(sessionToken, term);
        }
    }

    @Override
    public void updateSampleType(EntityType entityType, String diff)
    {
        if (verbose == true)
        {
            sampleTypesToUpdate.put(entityType.getCode(), diff);
        }
        if (dryRun == false)
        {
            commonServer.updateSampleType(sessionToken, entityType);
        }
    }

    @Override
    public void updateDataSetType(EntityType entityType, String diff)
    {
        if (verbose == true)
        {
            dataSetTypesToUpdate.put(entityType.getCode(), diff);
        }
        if (dryRun == false)
        {
            commonServer.updateDataSetType(sessionToken, entityType);
        }
    }

    @Override
    public void updateExperimentType(EntityType entityType, String diff)
    {
        if (verbose == true)
        {
            experimentTypesToUpdate.put(entityType.getCode(), diff);
        }
        if (dryRun == false)
        {
            commonServer.updateExperimentType(sessionToken, entityType);
        }
    }

    @Override
    public void updateMaterialType(EntityType entityType, String diff)
    {
        if (verbose == true)
        {
            materialTypesToUpdate.put(entityType.getCode(), diff);
        }
        if (dryRun == false)
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
        if (dryRun == false)
        {
            commonServer.addVocabularyTerms(sessionToken, techId, termsToBeAdded, null, true);
        }
    }

    @Override
    public void printSummary()
    {
        if (verbose == false)
        {
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
        if (set.isEmpty())
        {
            return;
        }
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
        if (map.isEmpty())
        {
            return;
        }
        operationLog.info(separatorStr);
        String message = String.format(INFO_MESSAGE, type, operation);
        operationLog.info(message);
        operationLog.info(separatorStr);
        for (String key : map.keySet())
        {
            operationLog.info(key + " - " + map.get(key));
        }
    }

    private void printSummaryPropertyAssignments(Set<NewETPTAssignment> set, String type, String operation)
    {
        if (set.isEmpty())
        {
            return;
        }
        operationLog.info(separatorStr);
        String message = String.format(INFO_MESSAGE, type, operation);
        operationLog.info(message);
        operationLog.info(separatorStr);
        List<String> lines = new ArrayList<String>();
        for (NewETPTAssignment assignment : set)
        {
            lines.add(assignment.getEntityTypeCode() + "(" + assignment.getEntityKind().name() + "): "
                    + assignment.getPropertyTypeCode());
        }
        Collections.sort(lines);
        for (String line : lines)
        {
            operationLog.info(line);
        }
    }
    
    private static final class Summary
    {
        
    }
}
