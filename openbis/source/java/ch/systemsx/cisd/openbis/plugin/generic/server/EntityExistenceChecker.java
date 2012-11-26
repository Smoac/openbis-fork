/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Checker of entities. Asks DAO for existing entities and chaches already asked entities.
 * 
 * @author Franz-Josef Elmer
 */
class EntityExistenceChecker
{
    private final IDAOFactory daoFactory;

    private final Set<ExperimentIdentifier> experimentIdentifers =
            new HashSet<ExperimentIdentifier>();

    private final Map<String, Set<String>> materialTypeToPropertyTypesMap =
            new HashMap<String, Set<String>>();

    private final Map<String, Set<String>> sampleTypeToPropertyTypesMap =
            new HashMap<String, Set<String>>();

    private final Map<SpaceIdentifier, SpacePE> spaceIdentifiers =
            new HashMap<SpaceIdentifier, SpacePE>();

    private final Map<SampleIdentifier, SamplePE> identifierToSampleMap =
            new HashMap<SampleIdentifier, SamplePE>();

    private final Set<String> errors;

    EntityExistenceChecker(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
        this.errors = new HashSet<String>();
    }

    public List<String> getErrors()
    {
        return new ArrayList<String>(errors);
    }

    /**
     * Checks specified materials.A {@link UserFailureException} is thrown if
     * <ul>
     * <li>specified material type is not known or
     * <li>at least one material has a property which is not assigned to specified material type.
     * </ul>
     */
    void checkNewMaterials(List<NewMaterialsWithTypes> newMaterialsWithType)
    {
        for (NewMaterialsWithTypes newMaterialsWithTypes : newMaterialsWithType)
        {
            MaterialType materialType = newMaterialsWithTypes.getEntityType();
            if (assertMaterialTypeExists(materialType) == false)
            {
                continue;
            }
            List<NewMaterial> newMaterials = newMaterialsWithTypes.getNewEntities();
            for (NewMaterial newMaterial : newMaterials)
            {
                IEntityProperty[] properties = newMaterial.getProperties();
                assertValidPropertyTypes(materialType, materialTypeToPropertyTypesMap, properties);
            }
        }
    }

    private boolean assertMaterialTypeExists(MaterialType materialType)
    {
        String materialTypeCode = materialType.getCode();
        if (materialTypeToPropertyTypesMap.containsKey(materialTypeCode) == false)
        {
            MaterialTypePE type =
                    (MaterialTypePE) daoFactory.getEntityTypeDAO(EntityKind.MATERIAL)
                            .tryToFindEntityTypeByCode(materialTypeCode);
            if (type == null)
            {
                errors.add("Unknown material type: " + materialTypeCode);
                return false;
            }
            Set<MaterialTypePropertyTypePE> materialTypePropertyTypes =
                    type.getMaterialTypePropertyTypes();
            Set<String> propertyTypes = new HashSet<String>();
            for (MaterialTypePropertyTypePE sampleTypePropertyType : materialTypePropertyTypes)
            {
                propertyTypes.add(sampleTypePropertyType.getPropertyType().getCode());
            }
            materialTypeToPropertyTypesMap.put(materialTypeCode, propertyTypes);
        }
        return true;
    }

    /**
     * Checks specified samples. A {@link UserFailureException} is thrown if
     * <ul>
     * <li>specified sample type is not known,
     * <li>at least one sample has a property which is not assigned to the specified sample type,
     * <li>at least one sample is linked to an unknown experiment or an unknown container.
     * </ul>
     * Note, that the new samples are stored in the cache as known samples. Thus, they can be
     * referred as container samples in a second call of this method.
     */
    void checkNewSamples(List<NewSamplesWithTypes> newSamplesWithType)
    {
        for (NewSamplesWithTypes newSamplesWithTypes : newSamplesWithType)
        {
            SampleType sampleType = newSamplesWithTypes.getEntityType();
            if (assertSampleTypeExists(sampleType) == false)
            {
                continue;
            }

            List<NewSample> newSamples = newSamplesWithTypes.getNewEntities();
            for (NewSample newSample : newSamples)
            {
                IdentifersExtractor extractor = new IdentifersExtractor(newSample);
                ExperimentIdentifier experimentIdentifier =
                        extractor.getExperimentIdentifierOrNull();
                if (experimentIdentifier != null)
                {
                	if (assertExperimentExists(experimentIdentifier) == false) {
	                	continue;
                	}
                }
                String containerIdentifier = newSample.getContainerIdentifierForNewSample();
                if (containerIdentifier != null)
                {
                    String defaultSpaceIdentifier = newSample.getDefaultSpaceIdentifier();
                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(containerIdentifier,
                                    defaultSpaceIdentifier);
 
                    if (assertSampleExists(sampleIdentifier) == null) {
                    	continue;
                    }
                }
                addSample(extractor.getNewSampleIdentifier());
                IEntityProperty[] properties = newSample.getProperties();
                assertValidPropertyTypes(sampleType, sampleTypeToPropertyTypesMap, properties);
            }
        }
    }

    private boolean assertExperimentExists(ExperimentIdentifier experimentIdentifier)
    {
        if (experimentIdentifers.contains(experimentIdentifier) == false)
        {
            ProjectPE project =
                    daoFactory.getProjectDAO().tryFindProject(
                            daoFactory.getHomeDatabaseInstance().getCode(),
                            experimentIdentifier.getSpaceCode(),
                            experimentIdentifier.getProjectCode());
            if (project == null)
            {
                errors.add("Unknown experiment because of unknown project: "
                        + experimentIdentifier);
                return false;
            }
            ExperimentPE experiment =
                    daoFactory.getExperimentDAO().tryFindByCodeAndProject(project,
                            experimentIdentifier.getExperimentCode());
            if (experiment == null)
            {
                errors.add("Unknown experiment: " + experimentIdentifier);
                return false;
            }
            experimentIdentifers.add(experimentIdentifier);
        }
        return true;
    }

    private boolean assertSampleTypeExists(SampleType sampleType)
    {
        String sampleTypeCode = sampleType.getCode();
        if (sampleTypeToPropertyTypesMap.containsKey(sampleTypeCode) == false)
        {
            SampleTypePE type =
                    daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode);
            if (type == null)
            {
                errors.add("Unknown sample type: " + sampleTypeCode);
                return false;
            }
            Set<SampleTypePropertyTypePE> sampleTypePropertyTypes =
                    type.getSampleTypePropertyTypes();
            Set<String> propertyTypes = new HashSet<String>();
            for (SampleTypePropertyTypePE sampleTypePropertyType : sampleTypePropertyTypes)
            {
                propertyTypes.add(sampleTypePropertyType.getPropertyType().getCode());
            }
            sampleTypeToPropertyTypesMap.put(sampleTypeCode, propertyTypes);
        }
        return true;
    }

    private SpacePE assertSpaceExists(SpaceIdentifier spaceIdentifier)
    {
        SpacePE space = spaceIdentifiers.get(spaceIdentifier);
        if (spaceIdentifiers.containsKey(spaceIdentifier) == false)
        {
            DatabaseInstancePE homeDatabaseInstance = daoFactory.getHomeDatabaseInstance();
            space =
                    daoFactory.getSpaceDAO().tryFindSpaceByCodeAndDatabaseInstance(
                            spaceIdentifier.getSpaceCode(), homeDatabaseInstance);
            if (space == null)
            {
                errors.add("Unknown space: " + spaceIdentifier);
                return null;
            }
            spaceIdentifiers.put(spaceIdentifier, space);
        }
        return space;
    }

    private SamplePE assertSampleExists(SampleIdentifier sampleIdentifier)
    {
        SamplePE sample = identifierToSampleMap.get(sampleIdentifier);
        if (sample == null)
        {
            String sampleCode = sampleIdentifier.getSampleCode();
            ISampleDAO sampleDAO = daoFactory.getSampleDAO();
            if (sampleIdentifier.isSpaceLevel())
            {
                SpaceIdentifier spaceLevel = sampleIdentifier.getSpaceLevel();
                SpacePE space = assertSpaceExists(spaceLevel);
                if (space == null)
                {
                    return null;
                }
                sample = sampleDAO.tryFindByCodeAndSpace(sampleCode, space);
            } else
            {
                sample =
                        sampleDAO.tryFindByCodeAndDatabaseInstance(sampleCode,
                                daoFactory.getHomeDatabaseInstance());
            }
            identifierToSampleMap.put(sampleIdentifier, sample);
            if (sample == null)
            {
                errors.add("Unknown sample: " + sampleIdentifier);
                return null;
            }
        }
        return sample;
    }

    private void addSample(SampleIdentifier newSampleIdentifier)
    {
        identifierToSampleMap.put(newSampleIdentifier, new SamplePE());

    }

    private void assertValidPropertyTypes(EntityType entityType,
            Map<String, Set<String>> entityTypeToPropertyTypesMap, IEntityProperty[] properties)
    {
        String entityTypeCode = entityType.getCode();
        Set<String> propertyTypes = entityTypeToPropertyTypesMap.get(entityTypeCode);
        for (IEntityProperty property : properties)
        {
            String propertyTypeCode = property.getPropertyType().getCode().toUpperCase();
            if (propertyTypes.contains(propertyTypeCode) == false)
            {
                String typeName = entityType instanceof SampleType ? "Sample" : "Material";
                errors.add(typeName + " type " + entityTypeCode
                        + " has no property type " + propertyTypeCode + " assigned.");
            }
        }
    }

}
