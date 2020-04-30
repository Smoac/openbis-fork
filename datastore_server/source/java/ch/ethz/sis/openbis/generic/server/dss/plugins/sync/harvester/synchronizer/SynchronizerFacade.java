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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    private static final String ADDED = "ADDED";

    private static final String UPDATED = "UPDATED";

    private static final String INFO_MESSAGE = "The following %s have been %s";

    private static final Object separatorStr = "---------------------";

    private final String sessionToken;

    private final ICommonServer commonServer;

    private final Logger operationLog;

    private Map<String, String> fileformatTypesToUpdate = new TreeMap<String, String>();

    private Set<String> fileformatTypesToAdd = new TreeSet<String>();

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

    private Map<String, EntityTypeSummary> sampleTypesToUpdate = new TreeMap<>();

    private Map<String, EntityTypeSummary> experimentTypesToUpdate = new TreeMap<>();

    private Map<String, EntityTypeSummary> dataSetTypesToUpdate = new TreeMap<>();

    private Map<String, EntityTypeSummary> materialTypesToUpdate = new HashMap<>();

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
        fileformatTypesToUpdate.put(type.getCode(), "Description: " + type.getDescription());
        if (dryRun == false)
        {
            commonServer.updateFileFormatType(sessionToken, type);
        }
    }

    @Override
    public void registerFileFormatType(FileFormatType type)
    {
        fileformatTypesToAdd.add(type.getCode());
        if (dryRun == false)
        {
            commonServer.registerFileFormatType(sessionToken, type);
        }
    }

    @Override
    public void updatePropertyTypeAssignment(NewETPTAssignment newETPTAssignment, String diff)
    {
        Map<String, EntityTypeSummary> summaryMap = getEntityTypeSummaryMap(newETPTAssignment.getEntityKind());
        getEntityTypeSummary(summaryMap, newETPTAssignment.getEntityTypeCode())
                .updateAssignment(newETPTAssignment.getPropertyTypeCode(), diff);
        if (dryRun == false)
        {
            commonServer.updatePropertyTypeAssignment(sessionToken, newETPTAssignment);
        }
    }

    @Override
    public void assignPropertyType(NewETPTAssignment newETPTAssignment)
    {
        Map<String, EntityTypeSummary> summaryMap = getEntityTypeSummaryMap(newETPTAssignment.getEntityKind());
        getEntityTypeSummary(summaryMap, newETPTAssignment.getEntityTypeCode())
                .addAssignment(newETPTAssignment.getPropertyTypeCode());
        if (dryRun == false)
        {
            commonServer.assignPropertyType(sessionToken, newETPTAssignment);
        }
    }

    @Override
    public void unassignPropertyType(EntityKind entityKind, String propertyTypeCode, String entityTypeCode)
    {
        getEntityTypeSummary(getEntityTypeSummaryMap(entityKind), entityTypeCode).removeAssignment(propertyTypeCode);
        if (dryRun == false)
        {
            commonServer.unassignPropertyType(sessionToken, entityKind, propertyTypeCode, entityTypeCode);
        }
    }

    private Map<String, EntityTypeSummary> getEntityTypeSummaryMap(EntityKind entityKind)
    {
        switch (entityKind)
        {
            case SAMPLE:
                return sampleTypesToUpdate;
            case EXPERIMENT:
                return experimentTypesToUpdate;
            case DATA_SET:
                return dataSetTypesToUpdate;
            case MATERIAL:
                return materialTypesToUpdate;
            default:
                throw new RuntimeException("Unknown entity kind: " + entityKind);
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

    private EntityTypeSummary getEntityTypeSummary(Map<String, EntityTypeSummary> summariesByType, String entityTypeCode)
    {
        EntityTypeSummary summary = summariesByType.get(entityTypeCode);
        if (summary == null)
        {
            summary = new EntityTypeSummary();
            summariesByType.put(entityTypeCode, summary);
        }
        return summary;
    }

    @Override
    public void updateSampleType(EntityType entityType, String diff)
    {
        getEntityTypeSummary(sampleTypesToUpdate, entityType.getCode()).updateType(diff);
        if (dryRun == false)
        {
            commonServer.updateSampleType(sessionToken, entityType);
        }
    }

    @Override
    public void updateDataSetType(EntityType entityType, String diff)
    {
        getEntityTypeSummary(dataSetTypesToUpdate, entityType.getCode()).updateType(diff);
        if (dryRun == false)
        {
            commonServer.updateDataSetType(sessionToken, entityType);
        }
    }

    @Override
    public void updateExperimentType(EntityType entityType, String diff)
    {
        getEntityTypeSummary(experimentTypesToUpdate, entityType.getCode()).updateType(diff);
        if (dryRun == false)
        {
            commonServer.updateExperimentType(sessionToken, entityType);
        }
    }

    @Override
    public void updateMaterialType(EntityType entityType, String diff)
    {
        getEntityTypeSummary(materialTypesToUpdate, entityType.getCode()).updateType(diff);
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
        printSummary(fileformatTypesToAdd, "file format types", ADDED);
        printSummary(fileformatTypesToUpdate, "file format types", UPDATED);

        printSummary(validationPluginsToAdd, "validation plugins", ADDED);
        printSummary(validationPluginsToUpdate, "validation plugins", UPDATED);

        printSummary(propertyTypesToAdd, "property types", ADDED);
        printSummary(propertyTypesToUpdate, "property types", UPDATED);

        printSummary(vocabulariesToAdd, "vocabularies", ADDED);
        printSummary(vocabulariesToUpdate, "vocabularies", UPDATED);

        printSummary(vocabularyTermsToUpdate, "vocabulary terms", UPDATED);
        printSummary(vocabulariesToAdd, "vocabulary terms", ADDED);

        printSummary(sampleTypesToAdd, "sample types", ADDED);
        printSummary(experimentTypesToAdd, "experiment types", ADDED);
        printSummary(dataSetTypesToAdd, "data set types", ADDED);
        printSummary(materialTypesToAdd, "material types", ADDED);

        printEntityTypeSummary(sampleTypesToAdd, sampleTypesToUpdate, "sample types");
        printEntityTypeSummary(experimentTypesToAdd, experimentTypesToUpdate, "experiment types");
        printEntityTypeSummary(dataSetTypesToAdd, dataSetTypesToUpdate, "data set types");
        printEntityTypeSummary(materialTypesToAdd, materialTypesToUpdate, "material types");
    }

    private void printSummary(Set<String> set, String type, String operation)
    {
        if (set.isEmpty())
        {
            return;
        }
        operationLog.info(separatorStr);
        operationLog.info(String.format(INFO_MESSAGE, type, operation));
        operationLog.info(separatorStr);
        for (String str : set)
        {
            operationLog.info(str);
        }
    }

    private void printEntityTypeSummary(Set<String> addedEntityTypes, Map<String, EntityTypeSummary> summaries, String itemType)
    {
        if (summaries.isEmpty())
        {
            return;
        }
        operationLog.info(separatorStr);
        operationLog.info(String.format(INFO_MESSAGE, itemType, UPDATED));
        operationLog.info(separatorStr);
        Set<Entry<String, EntityTypeSummary>> entrySet = summaries.entrySet();
        for (Entry<String, EntityTypeSummary> entry : entrySet)
        {
            String entityType = entry.getKey();
            if (addedEntityTypes.contains(entityType))
            {
                continue;
            }
            EntityTypeSummary summary = entry.getValue();
            String diff = summary.getDiff();
            operationLog.info(entityType + " " + (diff == null ? "no basic changes" : diff));
            Map<String, String> assignmentChanges = summary.getAssignmentChanges();
            for (Entry<String, String> entry2 : assignmentChanges.entrySet())
            {
                operationLog.info("    " + entry2.getKey() + ": " + entry2.getValue());
            }
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

    private static final class EntityTypeSummary
    {
        private String diff;
        private Map<String, String> assignmentChanges = new TreeMap<>();

        void updateType(String diff)
        {
            this.diff = diff;
        }

        void updateAssignment(String propertyType, String diff)
        {
            assignmentChanges.put(propertyType, diff);
        }

        void addAssignment(String propertyType)
        {
            assignmentChanges.put(propertyType, ADDED);
        }

        void removeAssignment(String propertyType)
        {
            assignmentChanges.put(propertyType, "REMOVED");
        }

        public String getDiff()
        {
            return diff;
        }

        public Map<String, String> getAssignmentChanges()
        {
            return assignmentChanges;
        }
    }
}
