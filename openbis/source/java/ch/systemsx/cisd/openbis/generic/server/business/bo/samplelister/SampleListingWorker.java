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

package ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.List;

import net.lemnik.eodsql.DataIterator;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesHolderResolver;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * A business worker object for fast sample listing. It has only one interface method, which is
 * {@link #load()}. This method deals with
 * <ul>
 * <li>information stored in the sample table</li>
 * <li>sample type information</li>
 * <li>assigned experiment, project, group and database instance</li>
 * <li>sample relationships (parent-child and contained-container)</li>
 * </ul>
 * It delegates the work of enriching the samples with properties to an implementation of a
 * {@link IEntityPropertiesEnricher}. The worker follows the logic that only the samples specified
 * by the {@link ListSampleCriteria}, the <i>primary samples</i>, should be enriched with properties
 * (as only primary samples are shown in a separate row of the list). From <i>dependent samples</i>,
 * only basic information is obtained.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses={ExperimentProjectGroupCodeRecord.class, SampleRecord.class, ISampleListingQuery.class})
final class SampleListingWorker
{
    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, SampleListingWorker.class);

    //
    // Input
    //

    private final long databaseInstanceId;

    private final DatabaseInstance databaseInstance;

    private final ListSampleCriteria criteria;

    //
    // Output
    //

    private final List<Sample> sampleList = new ArrayList<Sample>(ISampleListingQuery.FETCH_SIZE);

    //
    // Working interfaces
    //

    private final ISampleListingQuery query;

    private final ISampleSetListingQuery setQuery;

    private final IEntityPropertiesEnricher samplePropertiesEnricherOrNull;

    //
    // Working data structures
    //

    /**
     * A record for storing a samples and a related (parent or container) sample id.
     */
    private static class RelatedSampleRecord
    {
        Sample sample;

        long relatedSampleId;

        RelatedSampleRecord(Sample sample, long relatedSampleId)
        {
            this.sample = sample;
            this.relatedSampleId = relatedSampleId;
        }
    }

    private final Long2ObjectOpenHashMap<SampleType> sampleTypes =
            new Long2ObjectOpenHashMap<SampleType>();

    private final Long2ObjectMap<Person> persons = new Long2ObjectOpenHashMap<Person>();

    private final Long2ObjectMap<Experiment> experiments = new Long2ObjectOpenHashMap<Experiment>();

    private final Long2ObjectMap<RelatedSampleRecord> samplesAwaitingParentResolution =
            new Long2ObjectOpenHashMap<RelatedSampleRecord>();

    private final Long2ObjectMap<RelatedSampleRecord> samplesAwaitingContainerResolution =
            new Long2ObjectOpenHashMap<RelatedSampleRecord>();

    private final Long2IntMap requestedSamples = new Long2IntOpenHashMap();

    private boolean singleSampleTypeMode;

    private int maxSampleParentResolutionDepth;

    private int maxSampleContainerResolutionDepth;

    private final Long2ObjectMap<Sample> sampleMap = new Long2ObjectOpenHashMap<Sample>();

    //
    // Constructors
    //

    SampleListingWorker(final ListSampleCriteria criteria, final SampleListerDAO dao)
    {
        this(criteria, dao.getDatabaseInstanceId(), dao.getDatabaseInstance(), dao.getQuery(), dao
                .getIdSetQuery(), new EntityPropertiesEnricher(dao.getQuery(), dao
                .getIdSetQuery()));
    }

    // For unit tests
    SampleListingWorker(final ListSampleCriteria criteria, final long databaseInstanceId,
            final DatabaseInstance databaseInstance, final ISampleListingQuery query,
            final ISampleSetListingQuery setQuery,
            IEntityPropertiesEnricher samplePropertiesEnricherOrNull)
    {
        assert criteria != null;
        assert databaseInstance != null;
        assert query != null;
        assert setQuery != null;

        this.criteria = criteria;
        this.databaseInstanceId = databaseInstanceId;
        this.databaseInstance = databaseInstance;
        this.query = query;
        this.setQuery = setQuery;
        this.samplePropertiesEnricherOrNull = samplePropertiesEnricherOrNull;
    }

    //
    // Public interface
    //

    /**
     * Load the samples defined by the criteria given to the constructor. The samples will be
     * enriched with sample properties and dependencies to parents and container will be resolved.
     */
    public List<Sample> load()
    {
        try
        {
            final StopWatch watch = new StopWatch();
            watch.start();
            final Experiment expOrNull = tryLoadExperiment();
            final Group groupOrNull = tryLoadGroup(expOrNull);
            final String baseIndexURL = criteria.getBaseIndexUrl();
            loadSampleTypes();
            retrievePrimaryBasicSamples(tryGetIteratorForGroupSamples(), groupOrNull, baseIndexURL);
            retrievePrimaryBasicSamples(tryGetIteratorForSharedSamples(), groupOrNull, baseIndexURL);
            retrievePrimaryBasicSamples(tryGetIteratorForExperimentSamples(), groupOrNull,
                    baseIndexURL);
            retrievePrimaryBasicSamples(tryGetIteratorForContainedSamples(), groupOrNull,
                    baseIndexURL);
            if (operationLog.isDebugEnabled())
            {
                watch.stop();
                operationLog.debug(String.format("Basic retrieval of %d samples took %s s",
                        sampleList.size(), watch.toString()));
                watch.reset();
                watch.start();
            }

            // Only enrich the "primary" samples (matching the criteria) with properties, not
            // dependent samples.
            if (samplePropertiesEnricherOrNull != null)
            {
                samplePropertiesEnricherOrNull.enrich(sampleMap.keySet(),
                        new IEntityPropertiesHolderResolver()
                            {
                                public Sample get(long id)
                                {
                                    return sampleMap.get(id);
                                }
                            });
                if (operationLog.isDebugEnabled())
                {
                    watch.stop();
                    operationLog.debug(String.format("Enrichment with properties took %s s", watch
                            .toString()));
                }
            }

            retrieveDependentSamplesRecursively();
            resolveParents();
            resolveContainers();

            return sampleList;
        } finally
        {
            // Commit transaction, no need to rollback even when failed as it is readonly.
            query.commit();
        }
    }

    //
    // Private worker methods
    //

    private Experiment tryLoadExperiment()
    {
        final TechId experimentTechId = criteria.getExperimentId();
        if (experimentTechId == null)
        {
            return null;
        }
        final long id = experimentTechId.getId();
        final ExperimentProjectGroupCodeRecord codes =
                query.getExperimentAndProjectAndGroupCodeForId(id);
        final Experiment experiment = createExperiment(id, codes, null);
        experiments.put(id, experiment);
        return experiment;
    }

    private Group tryLoadGroup(final Experiment expOrNull)
    {
        if (criteria.isIncludeGroup() == false && expOrNull == null)
        {
            return null;
        }
        if (criteria.getGroupCode() != null)
        {
            final Group group = new Group();
            group.setCode(StringEscapeUtils.escapeHtml(criteria.getGroupCode()));
            group.setInstance(databaseInstance);
            return group;
        } else if (expOrNull != null)
        {
            return expOrNull.getProject().getGroup();
        } else
        {
            throw new IllegalStateException("No group definition available.");
        }
    }

    private void loadSampleTypes()
    {
        final String sampleTypeCodeOrNull = tryGetSampleTypeCode();
        this.singleSampleTypeMode = (sampleTypeCodeOrNull != null);
        if (singleSampleTypeMode)
        {
            final SampleType sampleType = query.getSampleType(sampleTypeCodeOrNull);
            sampleTypes.put(sampleType.getId(), sampleType);
            this.maxSampleParentResolutionDepth = sampleType.getGeneratedFromHierarchyDepth();
            this.maxSampleContainerResolutionDepth = sampleType.getContainerHierarchyDepth();
        } else
        {
            for (SampleType type : query.getSampleTypes())
            {
                sampleTypes.put(type.getId(), type);
                maxSampleContainerResolutionDepth =
                        Math.max(maxSampleContainerResolutionDepth, type
                                .getContainerHierarchyDepth());
                maxSampleParentResolutionDepth =
                        Math.max(maxSampleParentResolutionDepth, type
                                .getGeneratedFromHierarchyDepth());
            }
            sampleTypes.trim();
        }
    }

    private String tryGetSampleTypeCode()
    {
        final SampleType sampleTypeOrNull = criteria.getSampleType();
        return (sampleTypeOrNull == null || sampleTypeOrNull.isAllTypesCode()) ? null
                : sampleTypeOrNull.getCode();
    }

    private DataIterator<SampleRecord> tryGetIteratorForGroupSamples()
    {
        if (criteria.isIncludeGroup() == false)
        {
            return null;
        }
        if (criteria.isExcludeWithoutExperiment())
        {
            if (singleSampleTypeMode)
            {
                // sampleType contains only one entry which corresponds to sampleTypeCodeOrNull
                final long sampleTypeId = sampleTypes.keySet().iterator().nextLong();
                return query.getGroupSamplesForSampleTypeWithExperiment(databaseInstanceId,
                        criteria.getGroupCode(), sampleTypeId);
            } else
            {
                return query.getGroupSamplesWithExperiment(databaseInstanceId, criteria
                        .getGroupCode());
            }
        } else
        {
            if (singleSampleTypeMode)
            {
                // sampleType contains only one entry which corresponds to sampleTypeCodeOrNull
                final long sampleTypeId = sampleTypes.keySet().iterator().nextLong();
                return query.getGroupSamplesForSampleType(databaseInstanceId, criteria
                        .getGroupCode(), sampleTypeId);
            } else
            {
                return query.getGroupSamples(databaseInstanceId, criteria.getGroupCode());
            }
        }
    }

    private DataIterator<SampleRecord> tryGetIteratorForExperimentSamples()
    {
        final TechId experimentTechId = criteria.getExperimentId();
        if (experimentTechId == null)
        {
            return null;
        }
        return query.getSamplesForExperiment(experimentTechId.getId());
    }

    private DataIterator<SampleRecord> tryGetIteratorForContainedSamples()
    {
        final TechId containerTechId = criteria.getContainerSampleId();
        if (containerTechId == null)
        {
            return null;
        }
        return query.getSamplesForContainer(containerTechId.getId());
    }

    private DataIterator<SampleRecord> tryGetIteratorForSharedSamples()
    {
        if (criteria.isIncludeInstance() == false)
        {
            return null;
        }
        if (singleSampleTypeMode)
        {
            // sampleType contains only one entry which corresponds to sampleTypeCodeOrNull
            final long sampleTypeId = sampleTypes.keySet().iterator().nextLong();
            return query.getSharedSamplesForSampleType(databaseInstanceId, sampleTypeId);
        } else
        {
            return query.getSharedSamples(databaseInstanceId);
        }
    }

    private void retrievePrimaryBasicSamples(final DataIterator<SampleRecord> sampleIteratorOrNull,
            final Group groupOrNull, final String baseIndexURL)
    {
        assert baseIndexURL != null;
        assert sampleList != null;

        retrieveBasicSamples(sampleIteratorOrNull, groupOrNull, baseIndexURL, sampleList);
    }

    private void retrieveDependentBasicSamples(final Iterable<SampleRecord> sampleIteratorOrNull)
    {
        retrieveBasicSamples(sampleIteratorOrNull, null, null, null);
    }

    private void retrieveBasicSamples(final Iterable<SampleRecord> sampleIteratorOrNull,
            final Group groupOrNull, final String baseIndexURLOrNull,
            final List<Sample> sampleListOrNull)
    {
        if (sampleIteratorOrNull == null)
        {
            return;
        }
        final boolean primarySample = (sampleListOrNull != null);
        for (SampleRecord row : sampleIteratorOrNull)
        {
            final Sample sample = createSample(row, groupOrNull, baseIndexURLOrNull, primarySample);
            sampleMap.put(sample.getId(), sample);
            if (sampleListOrNull != null)
            {
                sampleListOrNull.add(sample);
            }
        }
    }

    private Sample createSample(SampleRecord row, final Group groupOrNull,
            final String baseIndexURLOrNull, final boolean primarySample)
    {
        final Sample sample = new Sample();
        sample.setId(row.id);
        sample.setCode(IdentifierHelper.convertCode(row.code, null));
        sample.setSubCode(IdentifierHelper.convertSubCode(row.code));
        sample.setSampleType(sampleTypes.get(row.saty_id));
        if (primarySample)
        {
            if (groupOrNull != null)
            {
                sample.setGroup(groupOrNull);
                final GroupIdentifier groupId =
                        new GroupIdentifier(databaseInstance.getCode(), groupOrNull.getCode());
                sample.setIdentifier(new SampleIdentifier(groupId, sample.getCode()).toString());
            } else
            {
                sample.setDatabaseInstance(databaseInstance);
                final DatabaseInstanceIdentifier dbId =
                        new DatabaseInstanceIdentifier(databaseInstance.getCode());
                sample.setIdentifier(new SampleIdentifier(dbId, sample.getCode()).toString());
            }
            sample.setPermId(StringEscapeUtils.escapeHtml(row.perm_id));
            sample.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURLOrNull,
                    EntityKind.SAMPLE, row.perm_id));
            sample.setRegistrationDate(row.registration_timestamp);
            sample.setProperties(new ArrayList<IEntityProperty>());
            if (row.inva_id != null)
            {
                final Invalidation invalidation = new Invalidation();
                sample.setInvalidation(invalidation);
            }
            sample.setRegistrator(getOrCreateRegistrator(row));
            if (row.expe_id != null)
            {
                sample.setExperiment(getOrCreateExperiment(row, groupOrNull));
            }
        }
        if (row.samp_id_generated_from != null & maxSampleParentResolutionDepth > 0)
        {
            if (samplesAwaitingParentResolution.containsKey(row.id) == false)
            {
                samplesAwaitingParentResolution.put(row.id, new RelatedSampleRecord(sample,
                        row.samp_id_generated_from));
            }
            addToRequested(row.samp_id_generated_from, row.id, maxSampleParentResolutionDepth,
                    primarySample);
        }
        if (row.samp_id_part_of != null & maxSampleContainerResolutionDepth > 0)
        {
            if (samplesAwaitingContainerResolution.containsKey(row.id) == false)
            {
                samplesAwaitingContainerResolution.put(row.id, new RelatedSampleRecord(sample,
                        row.samp_id_part_of));
            }
            addToRequested(row.samp_id_part_of, row.id, maxSampleContainerResolutionDepth,
                    primarySample);
        }
        return sample;
    }

    private void addToRequested(long newId, long oldId, int initialDepth, boolean primarySample)
    {
        if (primarySample)
        {
            requestedSamples.put(newId, initialDepth);
        } else
        {
            final int depthLeft = requestedSamples.get(oldId) - 1;
            if (depthLeft > 0)
            {
                requestedSamples.put(newId, depthLeft);
            }
        }
    }

    private Experiment getOrCreateExperiment(SampleRecord row, final Group groupOrNull)
    {
        Experiment experiment = experiments.get(row.expe_id);
        if (experiment == null)
        {
            final ExperimentProjectGroupCodeRecord codes =
                    query.getExperimentAndProjectCodeForId(row.expe_id);
            experiment = createExperiment(row.expe_id, codes, groupOrNull);
        }
        return experiment;
    }

    private Experiment createExperiment(final long experimentId,
            final ExperimentProjectGroupCodeRecord codes, final Group groupOrNull)
    {
        final Group group;
        if (groupOrNull != null)
        {
            group = groupOrNull;
        } else
        {
            group = new Group();
            group.setCode(StringEscapeUtils.escapeHtml(codes.g_code));
            group.setInstance(databaseInstance);
        }
        final Experiment experiment = new Experiment();
        experiment.setId(experimentId);
        experiment.setCode(StringEscapeUtils.escapeHtml(codes.e_code));
        experiment.setIdentifier(new ExperimentIdentifier(null, group.getCode(), codes.p_code,
                codes.e_code).toString());
        final Project project = new Project();
        project.setCode(StringEscapeUtils.escapeHtml(codes.p_code));
        project.setGroup(group);
        experiment.setProject(project);
        final ExperimentType experimentType = new ExperimentType();
        experimentType.setCode(StringEscapeUtils.escapeHtml(codes.et_code));
        experiment.setExperimentType(experimentType);
        return experiment;
    }

    private Person getOrCreateRegistrator(SampleRecord row)
    {
        Person registrator = persons.get(row.pers_id_registerer);
        if (registrator == null)
        {
            registrator = query.getPersonById(row.pers_id_registerer);
            registrator.setUserId(StringEscapeUtils.escapeHtml(registrator.getUserId()));
            registrator.setEmail(StringEscapeUtils.escapeHtml(registrator.getEmail()));
            registrator.setFirstName(StringEscapeUtils.escapeHtml(registrator.getFirstName()));
            registrator.setLastName(StringEscapeUtils.escapeHtml(registrator.getLastName()));
            persons.put(row.pers_id_registerer, registrator);
        }
        return registrator;
    }

    private void retrieveDependentSamplesRecursively()
    {
        if (requestedSamples.size() == 0)
        {
            return;
        }
        requestedSamples.keySet().removeAll(sampleMap.keySet());
        retrieveDependentBasicSamples(setQuery.getSamples(requestedSamples.keySet()));
        retrieveDependentSamplesRecursively();
    }

    private void resolveParents()
    {
        for (Long2ObjectMap.Entry<RelatedSampleRecord> e : samplesAwaitingParentResolution
                .long2ObjectEntrySet())
        {
            final RelatedSampleRecord record = e.getValue();
            final Sample parent = sampleMap.get(record.relatedSampleId);
            record.sample.setGeneratedFrom(parent);
        }
    }

    private void resolveContainers()
    {
        for (Long2ObjectMap.Entry<RelatedSampleRecord> e : samplesAwaitingContainerResolution
                .long2ObjectEntrySet())
        {
            final RelatedSampleRecord record = e.getValue();
            final Sample container = sampleMap.get(record.relatedSampleId);
            record.sample.setContainer(container);
            record.sample.setCode(IdentifierHelper.convertCode(record.sample.getSubCode(),
                    container.getCode()));
        }
    }
}
