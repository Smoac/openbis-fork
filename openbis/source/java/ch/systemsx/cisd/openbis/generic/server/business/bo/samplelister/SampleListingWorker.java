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
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesHolderResolver;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.ExperimentProjectGroupCodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
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
@Friend(toClasses =
    { ExperimentProjectGroupCodeRecord.class, SampleRecord.class, ISampleListingQuery.class })
final class SampleListingWorker
{
    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, SampleListingWorker.class);

    //
    // Input
    //

    private final long databaseInstanceId;

    private final DatabaseInstance databaseInstance;

    private final ListOrSearchSampleCriteria criteria;

    private final String baseIndexURL;

    //
    // Output
    //

    private final List<Sample> sampleList = new ArrayList<Sample>(ISampleListingQuery.FETCH_SIZE);

    //
    // Working interfaces
    //

    private final ISampleListingQuery query;

    private final IEntityPropertiesEnricher samplePropertiesEnricherOrNull;

    private final SecondaryEntityDAO referencedEntityDAO;

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

    private final Long2ObjectMap<Group> groupMap = new Long2ObjectOpenHashMap<Group>();

    public static SampleListingWorker create(ListOrSearchSampleCriteria criteria,
            String baseIndexURL, SampleListerDAO dao, SecondaryEntityDAO referencedEntityDAO)
    {
        ISampleListingQuery query = dao.getQuery();
        EntityPropertiesEnricher propertiesEnricher =
                new EntityPropertiesEnricher(query, dao.getPropertySetQuery());
        return new SampleListingWorker(criteria, baseIndexURL, dao.getDatabaseInstanceId(), dao
                .getDatabaseInstance(), query, propertiesEnricher, referencedEntityDAO);
    }

    //
    // Constructors
    //

    // For unit tests
    SampleListingWorker(final ListOrSearchSampleCriteria criteria, final String baseIndexURL,
            final long databaseInstanceId, final DatabaseInstance databaseInstance,
            final ISampleListingQuery query,
            IEntityPropertiesEnricher samplePropertiesEnricherOrNull,
            SecondaryEntityDAO referencedEntityDAO)
    {
        assert criteria != null;
        assert baseIndexURL != null;
        assert databaseInstance != null;
        assert query != null;

        this.criteria = criteria;
        this.baseIndexURL = baseIndexURL;
        this.databaseInstanceId = databaseInstanceId;
        this.databaseInstance = databaseInstance;
        this.query = query;
        this.samplePropertiesEnricherOrNull = samplePropertiesEnricherOrNull;
        this.referencedEntityDAO = referencedEntityDAO;
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
        final StopWatch watch = new StopWatch();
        watch.start();
        final Experiment expOrNull = tryLoadExperiment();
        final boolean oneGroupPerSample = isOneGroupPerSamples();
        final Group groupOrNull;
        if (oneGroupPerSample)
        {
            groupOrNull = null;
            final Group[] groups = referencedEntityDAO.getAllGroups(databaseInstanceId);
            for (Group group : groups)
            {
                group.setInstance(databaseInstance);
                groupMap.put(group.getId(), group);
            }
        } else
        {
            groupOrNull = tryLoadGroup(expOrNull);
            if (groupOrNull != null)
            {
                // For use by dependent samples.
                groupMap.put(groupOrNull.getId(), groupOrNull);
            }
        }
        loadSampleTypes();
        retrievePrimaryBasicSamples(tryGetIteratorForSamplesByIds(), groupOrNull, oneGroupPerSample);
        retrievePrimaryBasicSamples(tryGetIteratorForGroupSamples(), groupOrNull, oneGroupPerSample);
        retrievePrimaryBasicSamples(tryGetIteratorForSharedSamples(), groupOrNull,
                oneGroupPerSample);
        retrievePrimaryBasicSamples(tryGetIteratorForExperimentSamples(), groupOrNull,
                oneGroupPerSample);
        retrievePrimaryBasicSamples(tryGetIteratorForContainedSamples(), groupOrNull,
                oneGroupPerSample);
        if (operationLog.isDebugEnabled())
        {
            watch.stop();
            operationLog.debug(String.format("Basic retrieval of %d samples took %s s", sampleList
                    .size(), watch.toString()));
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
        return createAndSaveExperiment(experimentTechId.getId(), null);
    }

    private Experiment createAndSaveExperiment(final long experimentId, final Group groupOrNull)
    {
        final Experiment experiment = referencedEntityDAO.getExperiment(experimentId);
        experiments.put(experimentId, experiment);
        return experiment;
    }

    private boolean isOneGroupPerSamples()
    {
        return criteria.getContainerSampleId() != null || criteria.getSampleIds() != null;
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
            group.setId(referencedEntityDAO.getGroupIdForCode(group.getCode()));
            return group;
        } else if (expOrNull != null)
        {
            final Group group = expOrNull.getProject().getGroup();
            group.setId(referencedEntityDAO.getGroupIdForCode(group.getCode()));
            return group;
        } else
        {
            throw new IllegalStateException("No group definition available.");
        }
    }

    private void loadSampleTypes()
    {
        final SampleType sampleTypeOrNull = tryGetSingleModeSampleType();
        this.singleSampleTypeMode = (sampleTypeOrNull != null);
        // all sample types are needed for parents
        for (SampleType type : query.getSampleTypes(databaseInstanceId))
        {
            sampleTypes.put(type.getId(), type);
            if (singleSampleTypeMode == false)
            {
                maxSampleContainerResolutionDepth =
                        Math.max(maxSampleContainerResolutionDepth, type
                                .getContainerHierarchyDepth());
                maxSampleParentResolutionDepth =
                        Math.max(maxSampleParentResolutionDepth, type
                                .getGeneratedFromHierarchyDepth());
            }
        }
        sampleTypes.trim();

        if (singleSampleTypeMode)
        {
            assert sampleTypeOrNull != null;
            this.maxSampleParentResolutionDepth = sampleTypeOrNull.getGeneratedFromHierarchyDepth();
            this.maxSampleContainerResolutionDepth = sampleTypeOrNull.getContainerHierarchyDepth();
        }

    }

    private SampleType tryGetSingleModeSampleType()
    {
        final SampleType sampleTypeOrNull = criteria.getSampleType();
        return (sampleTypeOrNull == null || sampleTypeOrNull.isAllTypesCode()) ? null
                : sampleTypeOrNull;
    }

    private long getSampleTypeId()
    {
        final SampleType sampleTypeOrNull = tryGetSingleModeSampleType();
        assert sampleTypeOrNull != null;
        return sampleTypeOrNull.getId();
    }

    private Iterable<SampleRecord> tryGetIteratorForSamplesByIds()
    {
        Collection<Long> ids = criteria.getSampleIds();
        if (ids == null)
        {
            return null;
        }
        return query.getSamples(new LongOpenHashSet(ids));
    }

    private Iterable<SampleRecord> tryGetIteratorForGroupSamples()
    {
        if (criteria.isIncludeGroup() == false)
        {
            return null;
        }
        if (criteria.isExcludeWithoutExperiment())
        {
            if (singleSampleTypeMode)
            {
                final long sampleTypeId = getSampleTypeId();
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
                final long sampleTypeId = getSampleTypeId();
                return query.getGroupSamplesForSampleType(databaseInstanceId, criteria
                        .getGroupCode(), sampleTypeId);
            } else
            {
                return query.getGroupSamples(databaseInstanceId, criteria.getGroupCode());
            }
        }
    }

    private Iterable<SampleRecord> tryGetIteratorForExperimentSamples()
    {
        final TechId experimentTechId = criteria.getExperimentId();
        if (experimentTechId == null)
        {
            return null;
        }
        return query.getSamplesForExperiment(experimentTechId.getId());
    }

    private Iterable<SampleRecord> tryGetIteratorForContainedSamples()
    {
        final TechId containerTechId = criteria.getContainerSampleId();
        if (containerTechId == null)
        {
            return null;
        }
        return query.getSamplesForContainer(containerTechId.getId());
    }

    private Iterable<SampleRecord> tryGetIteratorForSharedSamples()
    {
        if (criteria.isIncludeInstance() == false)
        {
            return null;
        }
        if (singleSampleTypeMode)
        {
            final long sampleTypeId = getSampleTypeId();
            return query.getSharedSamplesForSampleType(databaseInstanceId, sampleTypeId);
        } else
        {
            return query.getSharedSamples(databaseInstanceId);
        }
    }

    private void retrievePrimaryBasicSamples(final Iterable<SampleRecord> sampleIteratorOrNull,
            final Group groupOrNull, final boolean oneGroupPerSample)
    {
        assert sampleList != null;

        retrieveBasicSamples(sampleIteratorOrNull, groupOrNull, baseIndexURL, sampleList,
                oneGroupPerSample);
    }

    private void retrieveDependentBasicSamples(final Iterable<SampleRecord> sampleIteratorOrNull)
    {
        retrieveBasicSamples(sampleIteratorOrNull, null, null, null, true);
    }

    private void retrieveBasicSamples(final Iterable<SampleRecord> sampleIteratorOrNull,
            final Group groupOrNull, final String baseIndexURLOrNull,
            final List<Sample> sampleListOrNull, final boolean oneGroupPerSample)
    {
        if (sampleIteratorOrNull == null)
        {
            return;
        }
        final boolean primarySample = (sampleListOrNull != null);
        for (SampleRecord row : sampleIteratorOrNull)
        {
            final Sample sample =
                    createSample(row, groupOrNull, oneGroupPerSample, baseIndexURLOrNull,
                            primarySample);
            sampleMap.put(sample.getId(), sample);
            if (sampleListOrNull != null)
            {
                sampleListOrNull.add(sample);
            }
        }
    }

    private Sample createSample(SampleRecord row, final Group groupOrNull,
            final boolean oneGroupPerSample, final String baseIndexURLOrNull,
            final boolean primarySample)
    {
        final Sample sample = new Sample();
        sample.setId(row.id);
        sample.setCode(IdentifierHelper.convertCode(row.code, null));
        sample.setSubCode(IdentifierHelper.convertSubCode(row.code));
        sample.setSampleType(sampleTypes.get(row.saty_id));
        if (oneGroupPerSample)
        {
            if (row.grou_id == null)
            {
                setDatabaseInstance(sample);
            } else
            {
                setGroup(sample, groupMap.get(row.grou_id));
            }
        } else if (groupOrNull != null)
        {
            setGroup(sample, groupOrNull);
        } else
        {
            setDatabaseInstance(sample);
        }
        if (primarySample)
        {
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
        // even though maxSampleContainerResoutionDepth may be 0 we still need to load container
        // for primary samples with container to create a 'full' code with container code part
        if (row.samp_id_part_of != null & (primarySample || maxSampleContainerResolutionDepth > 0))
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

    private void setGroup(final Sample sample, final Group group)
    {
        sample.setGroup(group);
        final GroupIdentifier groupId =
                new GroupIdentifier(databaseInstance.getCode(), group.getCode());
        sample.setIdentifier(new SampleIdentifier(groupId, sample.getCode()).toString());
    }

    private void setDatabaseInstance(final Sample sample)
    {
        sample.setDatabaseInstance(databaseInstance);
        final DatabaseInstanceIdentifier dbId =
                new DatabaseInstanceIdentifier(databaseInstance.getCode());
        sample.setIdentifier(new SampleIdentifier(dbId, sample.getCode()).toString());
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
            experiment = createAndSaveExperiment(row.expe_id, groupOrNull);
        }
        return experiment;
    }

    private Person getOrCreateRegistrator(SampleRecord row)
    {
        return getOrCreateRegistrator(row.pers_id_registerer);
    }

    private Person getOrCreateRegistrator(long personId)
    {
        Person registrator = persons.get(personId);
        if (registrator == null)
        {
            registrator = referencedEntityDAO.getPerson(personId);
            persons.put(personId, registrator);
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
        retrieveDependentBasicSamples(query.getSamples(requestedSamples.keySet()));
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
            record.sample.setIdentifier(IdentifierHelper.createSampleIdentifier(record.sample)
                    .toString());
        }
    }

}
