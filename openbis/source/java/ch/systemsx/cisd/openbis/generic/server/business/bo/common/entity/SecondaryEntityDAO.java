/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.sql.Connection;

import net.lemnik.eodsql.QueryTool;

import org.springframework.dao.EmptyResultDataAccessException;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.DatabaseInstanceTranslator;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { SampleReferenceRecord.class, ExperimentProjectGroupCodeRecord.class,
            ISecondaryEntityListingQuery.class })
public class SecondaryEntityDAO
{
    /**
     * Creates a new instance based on {@link PersistencyResources} and home
     * {@link DatabaseInstancePE} of specified DAO factory.
     */
    public static SecondaryEntityDAO create(IDAOFactory daoFactory)
    {
        Connection connection = DatabaseContextUtils.getConnection(daoFactory);
        ISecondaryEntityListingQuery query =
                QueryTool.getQuery(connection, ISecondaryEntityListingQuery.class);
        return create(daoFactory, query);
    }

    @Private
    public static SecondaryEntityDAO create(IDAOFactory daoFactory,
            ISecondaryEntityListingQuery query)
    {
        return new SecondaryEntityDAO(query, daoFactory.getHomeDatabaseInstance());
    }

    private final ISecondaryEntityListingQuery query;

    private final DatabaseInstance databaseInstance;

    private SecondaryEntityDAO(final ISecondaryEntityListingQuery query,
            final DatabaseInstancePE databaseInstancePE)
    {
        this.query = query;
        this.databaseInstance = DatabaseInstanceTranslator.translate(databaseInstancePE);
    }

    public Experiment tryGetExperiment(final long experimentId)
    {
        final ExperimentProjectGroupCodeRecord record =
                query.getExperimentAndProjectAndGroupCodeForId(experimentId);
        if (record == null)
        {
            throw new EmptyResultDataAccessException(1);
        }
        return tryCreateExperiment(experimentId, record);
    }

    private Experiment tryCreateExperiment(final long experimentId,
            final ExperimentProjectGroupCodeRecord record)
    {
        if (record.dbin_id.equals(databaseInstance.getId()) == false)
        {
            return null; // experiment is connected (through group) with different db instance
        }
        final Space space = new Space();
        space.setCode(record.g_code);
        space.setInstance(databaseInstance);

        final Experiment experiment = new Experiment();
        experiment.setId(experimentId);
        experiment.setCode(record.e_code);
        experiment.setPermId(record.e_permid);
        experiment.setIdentifier(new ExperimentIdentifier(null, space.getCode(), record.p_code,
                record.e_code).toString());
        final Project project = new Project();
        project.setId(record.p_id);
        project.setCode(record.p_code);
        project.setSpace(space);
        experiment.setProject(project);
        final ExperimentType experimentType = new ExperimentType();
        experimentType.setCode(record.et_code);
        experiment.setExperimentType(experimentType);

        return experiment;
    }

    public Person getPerson(long personId)
    {
        Person registrator = query.getPersonById(personId);
        if (registrator == null)
        {
            throw new EmptyResultDataAccessException(1);
        }
        registrator.setUserId(registrator.getUserId());
        registrator.setEmail(registrator.getEmail());
        registrator.setFirstName(registrator.getFirstName());
        registrator.setLastName(registrator.getLastName());
        registrator.setDatabaseInstance(databaseInstance);
        return registrator;
    }

    public Long getSampleTypeIdForSampleTypeCode(String sampleTypeCode)
    {
        Long id = query.getSampleTypeIdForSampleTypeCode(sampleTypeCode, databaseInstance.getId());
        if (id == null)
        {
            throw UserFailureException
                    .fromTemplate("No sample type with code '%s' could be found in the database.",
                            sampleTypeCode);
        }
        return id;
    }

    public Space[] getAllSpaces(long databaseInstanceId)
    {
        return query.getAllGroups(databaseInstanceId);
    }

    public long getGroupIdForCode(String groupCode)
    {
        return query.getGroupIdForCode(groupCode);
    }

    public Long2ObjectMap<Sample> getSamples(LongSet sampleIds)
    {
        final Iterable<SampleReferenceRecord> sampleRecords = query.getSamples(sampleIds);
        Long2ObjectMap<Sample> result = new Long2ObjectOpenHashMap<Sample>();
        for (SampleReferenceRecord record : sampleRecords)
        {
            result.put(record.id, createSample(record, databaseInstance));
        }
        return result;
    }

    public LongSet getSampleDescendantIdsAndSelf(Long sampleId)
    {
        LongSet results = new LongOpenHashSet();
        LongSet currentLayer = new LongOpenHashSet();
        currentLayer.add(sampleId);
        // go layer by layer into children samples
        LongSet nextLayer;
        while (currentLayer.isEmpty() == false)
        {
            results.addAll(currentLayer);
            nextLayer = new LongOpenHashSet(query.getChildrenIds(currentLayer));
            nextLayer.removeAll(results); // don't go twice through the same sample
            currentLayer = nextLayer;
        }
        return results;
    }

    private static Sample createSample(SampleReferenceRecord record,
            DatabaseInstance databaseInstance)
    {
        Sample sample = new Sample();
        sample.setId(record.id);
        sample.setCode(IdentifierHelper.convertCode(record.s_code, record.c_code));
        sample.setSampleType(createSampleType(record.st_code, databaseInstance));
        sample.setInvalidation(createInvalidation(record.inva_id));
        sample.setSpace(tryCreateGroup(record.g_code, databaseInstance));
        sample.setDatabaseInstance(tryGetDatabaseInstance(record.g_code, databaseInstance));
        sample.setPermId(record.perm_id);
        sample.setIdentifier(createIdentifier(sample).toString());
        return sample;
    }

    private static SampleIdentifier createIdentifier(Sample sample)
    {
        return IdentifierHelper.createSampleIdentifier(sample);
    }

    private static DatabaseInstance tryGetDatabaseInstance(String groupCodeOrNull,
            DatabaseInstance databaseInstance)
    {
        if (groupCodeOrNull == null)
        {
            return databaseInstance;
        } else
        {
            return null;
        }
    }

    private static Space tryCreateGroup(String codeOrNull, DatabaseInstance databaseInstance)
    {
        if (codeOrNull == null)
        {
            return null;
        } else
        {
            Space space = new Space();
            space.setCode(codeOrNull);
            space.setInstance(databaseInstance);
            return space;
        }
    }

    private static Invalidation createInvalidation(Long invalidationIdOrNull)
    {
        if (invalidationIdOrNull == null)
        {
            return null;
        } else
        {
            return new Invalidation();
        }
    }

    private static SampleType createSampleType(String code, DatabaseInstance databaseInstance)
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode(code);
        sampleType.setDatabaseInstance(databaseInstance);
        return sampleType;
    }
}
