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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

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

    private final Set<String> sampleTypes = new HashSet<String>();

    private final Map<SpaceIdentifier, SpacePE> spaceIdentifiers =
            new HashMap<SpaceIdentifier, SpacePE>();

    private final Map<SampleIdentifier, SamplePE> identifierToSampleMap =
            new HashMap<SampleIdentifier, SamplePE>();

    EntityExistenceChecker(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    void assertExperimentExists(ExperimentIdentifier experimentIdentifier)
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
                throw new UserFailureException("Unknown experiment: " + experimentIdentifier);
            }
            ExperimentPE experiment =
                    daoFactory.getExperimentDAO().tryFindByCodeAndProject(project,
                            experimentIdentifier.getExperimentCode());
            if (experiment == null)
            {
                throw new UserFailureException("Unknown experiment: " + experimentIdentifier);
            }
            experimentIdentifers.add(experimentIdentifier);
        }
    }

    void assertSampleTypeExists(SampleType sampleType)
    {
        String sampleTypeCode = sampleType.getCode();
        if (sampleTypes.contains(sampleTypeCode) == false)
        {
            SampleTypePE type =
                    daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode);
            if (type == null)
            {
                throw new UserFailureException("Unknown sample type: " + sampleTypeCode);
            }
            sampleTypes.add(sampleTypeCode);
        }
    }

    SpacePE assertSpaceExists(SpaceIdentifier spaceIdentifier)
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
                throw new UserFailureException("Unknown space: " + spaceIdentifier);
            }
            spaceIdentifiers.put(spaceIdentifier, space);
        }
        return space;
    }

    SamplePE assertSampleExists(SampleIdentifier sampleIdentifier)
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
                throw new UserFailureException("Unkown sample; " + sampleIdentifier);
            }
        }
        return sample;
    }

    void addSample(SampleIdentifier newSampleIdentifier)
    {
        identifierToSampleMap.put(newSampleIdentifier, new SamplePE());

    }
}
