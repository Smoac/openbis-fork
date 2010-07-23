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
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.Collection;
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
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.ExperimentProjectGroupCodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.SearchlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
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
    { ExperimentProjectGroupCodeRecord.class, SampleRecord.class, SampleRelationRecord.class,
            ISampleListingQuery.class })
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

    private final LongSet idsOfSamplesAwaitingParentResolution = new LongOpenHashSet();

    private final Long2ObjectMap<RelatedSampleRecord> samplesAwaitingParentResolution =
            new Long2ObjectOpenHashMap<RelatedSampleRecord>();

    private final Long2ObjectMap<RelatedSampleRecord> samplesAwaitingContainerResolution =
            new Long2ObjectOpenHashMap<RelatedSampleRecord>();

    private final Long2IntMap requestedContainerSamples = new Long2IntOpenHashMap();

    private final Long2IntMap requestedParentSamples = new Long2IntOpenHashMap();

    private boolean singleSampleTypeMode;

    private boolean enrichDependentSamples;

    private int maxSampleParentResolutionDepth;

    private final Long2ObjectMap<Sample> sampleMap = new Long2ObjectOpenHashMap<Sample>();

    private final Long2ObjectMap<Space> spaceMap = new Long2ObjectOpenHashMap<Space>();

    private final long parentRelationhipTypeId;

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
        this.enrichDependentSamples = criteria.isEnrichDependentSamplesWithProperties();
        this.referencedEntityDAO = referencedEntityDAO;
        this.parentRelationhipTypeId =
                getRelationId(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
    }

    private long getRelationId(String fullRelationCode)
    {
        return query.getRelationshipTypeId(CodeConverter.tryToDatabase(fullRelationCode),
                CodeConverter.isInternalNamespace(fullRelationCode));
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
        loadGroups();
        loadSampleTypes();
        retrievePrimaryBasicSamples(tryGetIteratorForSamplesByIds());
        retrievePrimaryBasicSamples(tryGetIteratorForSamplesByCodes());
        retrievePrimaryBasicSamples(tryGetIteratorForGroupSamples());
        retrievePrimaryBasicSamples(tryGetIteratorForSharedSamples());
        retrievePrimaryBasicSamples(tryGetIteratorForExperimentSamples());
        retrievePrimaryBasicSamples(tryGetIteratorForContainedSamples());
        retrievePrimaryBasicSamples(tryGetIteratorForParentSamples());
        retrievePrimaryBasicSamples(tryGetIteratorForNewTrackedSamples());
        if (operationLog.isDebugEnabled())
        {
            watch.stop();
            operationLog.debug(String.format("Basic retrieval of %d samples took %s s", sampleList
                    .size(), watch.toString()));
            watch.reset();
            watch.start();
        }
        if (enrichDependentSamples == false)
        {
            // only 'primary' samples were retrieved up to this point
            enrichRetrievedSamplesWithProperties(watch);
        }
        retrieveDependentSamplesRecursively(true);
        resolveParents();
        resolveContainers();
        if (enrichDependentSamples)
        {
            // dependent samples will also be enriched
            enrichRetrievedSamplesWithProperties(watch);
        }

        return sampleList;
    }

    //
    // Private worker methods
    //

    private void enrichRetrievedSamplesWithProperties(StopWatch watch)
    {
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
    }

    private void loadGroups()
    {
        // all groups are needed for parent samples identifiers
        final Space[] spaces = referencedEntityDAO.getAllSpaces(databaseInstanceId);
        for (Space space : spaces)
        {
            space.setInstance(databaseInstance);
            spaceMap.put(space.getId(), space);
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
        }

    }

    private Experiment createAndSaveExperiment(final long experimentId)
    {
        final Experiment experiment = referencedEntityDAO.tryGetExperiment(experimentId);
        experiments.put(experimentId, experiment);
        return experiment;
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

    private Iterable<SampleRecord> tryGetIteratorForSamplesByCodes()
    {
        String[] codes = criteria.trySampleCodes();
        if (codes == null)
        {
            return null;
        }
        return query.getSamplesForCodes(codes);
    }

    private Iterable<SampleRecord> tryGetIteratorForGroupSamples()
    {
        if (criteria.isIncludeSpace() == false)
        {
            return null;
        }
        if (criteria.isExcludeWithoutExperiment())
        {
            if (singleSampleTypeMode)
            {
                return getGroupSampleForSampleTypeWithExperiment();
            } else
            {
                return getGroupSamplesWithExperiment();
            }
        } else
        {
            if (singleSampleTypeMode)
            {
                return getGroupSamplesForSampleType();
            } else
            {
                return getGroupSamples();
            }
        }
    }

    private DataIterator<SampleRecord> getGroupSamples()
    {
        String groupCode = criteria.getSpaceCode();
        if (groupCode == null)
        {
            return query.getAllListableGroupSamples(databaseInstanceId);
        }
        return query.getListableGroupSamples(databaseInstanceId, groupCode);
    }

    private Iterable<SampleRecord> getGroupSamplesForSampleType()
    {
        final long sampleTypeId = getSampleTypeId();
        String groupCode = criteria.getSpaceCode();
        if (groupCode == null)
        {
            return query.getAllGroupSamplesForSampleType(databaseInstanceId, sampleTypeId);
        }
        return query.getGroupSamplesForSampleType(databaseInstanceId, groupCode, sampleTypeId);
    }

    private DataIterator<SampleRecord> getGroupSamplesWithExperiment()
    {
        String groupCode = criteria.getSpaceCode();
        if (groupCode == null)
        {
            return query.getAllGroupSamplesWithExperiment(databaseInstanceId);
        }
        return query.getGroupSamplesWithExperiment(databaseInstanceId, groupCode);
    }

    private DataIterator<SampleRecord> getGroupSampleForSampleTypeWithExperiment()
    {
        final long sampleTypeId = getSampleTypeId();
        String groupCode = criteria.getSpaceCode();
        if (groupCode == null)
        {
            return query.getAllGroupSamplesForSampleTypeWithExperiment(databaseInstanceId,
                    sampleTypeId);
        }
        return query.getGroupSamplesForSampleTypeWithExperiment(databaseInstanceId, groupCode,
                sampleTypeId);
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

    private Iterable<SampleRecord> tryGetIteratorForParentSamples()
    {
        final TechId parentTechId = criteria.getParentSampleId();
        if (parentTechId == null)
        {
            return null;
        }
        return query.getChildrenSamplesForParent(parentRelationhipTypeId, parentTechId.getId());
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
            return query.getListableSharedSamples(databaseInstanceId);
        }
    }

    private Iterable<SampleRecord> tryGetIteratorForNewTrackedSamples()
    {
        if (criteria.getSampleTypeCode() == null)
        {
            return null;
        }
        final long sampleTypeId =
                referencedEntityDAO.getSampleTypeIdForSampleTypeCode(criteria.getSampleTypeCode());
        final String propertyTypeCode = criteria.getPropertyTypeCode();
        final String propertyValue = criteria.getPropertyValue();
        return query.getSamplesWithPropertyValue(sampleTypeId, propertyTypeCode, propertyValue,
                new LongOpenHashSet(criteria.getAlreadyTrackedSampleIds()));
    }

    private void retrievePrimaryBasicSamples(final Iterable<SampleRecord> sampleIteratorOrNull)
    {
        assert sampleList != null;

        retrieveBasicSamples(sampleIteratorOrNull, sampleList);
    }

    private void retrieveDependentBasicSamples(final Iterable<SampleRecord> sampleIteratorOrNull)
    {
        retrieveBasicSamples(sampleIteratorOrNull, null);
    }

    private void retrieveBasicSamples(final Iterable<SampleRecord> sampleIteratorOrNull,
            final List<Sample> sampleListOrNull)
    {
        if (sampleIteratorOrNull == null)
        {
            return;
        }
        final boolean primarySample = (sampleListOrNull != null);
        for (SampleRecord row : sampleIteratorOrNull)
        {
            final Sample sampleOrNull = tryCreateSample(row, primarySample);
            if (sampleOrNull != null) // null == different db instance
            {
                sampleMap.put(sampleOrNull.getId(), sampleOrNull);
                if (sampleListOrNull != null)
                {
                    sampleListOrNull.add(sampleOrNull);
                }
            }
        }
    }

    private Sample tryCreateSample(SampleRecord row, final boolean primarySample)
    {
        final Sample sample = new Sample();
        sample.setId(row.id);
        sample.setPermId(StringEscapeUtils.escapeHtml(row.perm_id));
        sample.setCode(IdentifierHelper.convertCode(row.code, null));
        sample.setSubCode(IdentifierHelper.convertSubCode(row.code));
        sample.setSampleType(sampleTypes.get(row.saty_id));
        // set group or instance
        if (row.grou_id == null)
        {
            if (row.dbin_id.equals(databaseInstanceId))
            {
                setDatabaseInstance(sample);
            } else
            // different db instance
            {
                return null;
            }
        } else
        {
            final Space spaceOrNull = spaceMap.get(row.grou_id);
            if (spaceOrNull != null)
            {
                setSpace(sample, spaceMap.get(row.grou_id));
            } else
            // different db instance
            {
                return null;
            }
        }
        // set properties needed for primary samples
        // (dependent samples too if they need to be enriched e.g. for entity tracking)
        if (primarySample || enrichDependentSamples)
        {
            // initializing property collection - without this enricher will not work properly
            sample.setProperties(new ArrayList<IEntityProperty>());
            sample.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL, EntityKind.SAMPLE,
                    row.perm_id));
            sample.setSearchlink(SearchlinkUtilities.createSearchlinkURL(baseIndexURL,
                    EntityKind.SAMPLE, row.code));
            sample.setRegistrationDate(row.registration_timestamp);
            if (row.inva_id != null)
            {
                final Invalidation invalidation = new Invalidation();
                sample.setInvalidation(invalidation);
            }
            sample.setRegistrator(getOrCreateRegistrator(row));
            if (row.expe_id != null)
            {
                sample.setExperiment(getOrCreateExperiment(row));
            }
        }
        // prepare loading related samples
        if (maxSampleParentResolutionDepth > 0)
        {
            if (idsOfSamplesAwaitingParentResolution.contains(row.id) == false)
            {
                idsOfSamplesAwaitingParentResolution.add(row.id);
            }
        }
        // even though sample container resolution depth may be 0 we still need to load container
        // to create a 'full' code with container code part
        if (row.samp_id_part_of != null)
        {
            if (samplesAwaitingContainerResolution.containsKey(row.id) == false)
            {
                samplesAwaitingContainerResolution.put(row.id, new RelatedSampleRecord(sample,
                        row.samp_id_part_of));
            }
            addRelatedContainerSampleToRequested(row.samp_id_part_of);
        }
        return sample;
    }

    private void setSpace(final Sample sample, final Space space)
    {
        sample.setSpace(space);
        final GroupIdentifier groupId =
                new GroupIdentifier(databaseInstance.getCode(), space.getCode());
        sample.setIdentifier(new SampleIdentifier(groupId, sample.getCode()).toString());
    }

    private void setDatabaseInstance(final Sample sample)
    {
        sample.setDatabaseInstance(databaseInstance);
        final DatabaseInstanceIdentifier dbId =
                new DatabaseInstanceIdentifier(databaseInstance.getCode());
        sample.setIdentifier(new SampleIdentifier(dbId, sample.getCode()).toString());
    }

    private void addRelatedContainerSampleToRequested(long containerId)
    {
        // for containers we need to load only their codes - connected samples are not needed
        addOrUpdateRequestedContainerSample(containerId, 0);
    }

    private void addOrUpdateRequestedContainerSample(long sampleId, int newDepth)
    {
        // if sample was already requested update depth to maximum of old and new depth
        int oldDepth = requestedContainerSamples.get(sampleId);
        requestedContainerSamples.put(sampleId, Math.max(oldDepth, newDepth));
    }

    private void addRelatedParentSampleToRequested(long parentId, long oldSampleId,
            int initialDepth, boolean primarySample)
    {
        if (primarySample)
        {
            addOrUpdateRequestedParentSample(parentId, initialDepth);
        } else
        {
            final int depthLeft = requestedParentSamples.get(oldSampleId) - 1;
            if (depthLeft > 0)
            {
                addOrUpdateRequestedParentSample(parentId, depthLeft);
            }
        }
    }

    private void addOrUpdateRequestedParentSample(long sampleId, int newDepth)
    {
        // if sample was already requested update depth to maximum of old and new depth
        int oldDepth = requestedParentSamples.get(sampleId);
        requestedParentSamples.put(sampleId, Math.max(oldDepth, newDepth));
    }

    private Experiment getOrCreateExperiment(SampleRecord row)
    {
        Experiment experiment = experiments.get(row.expe_id);
        if (experiment == null)
        {
            experiment = createAndSaveExperiment(row.expe_id);
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

    private void retrieveDependentSamplesRecursively(boolean primary)
    {
        Iterable<SampleRelationRecord> parentRelations =
                query.getParentRelations(parentRelationhipTypeId,
                        idsOfSamplesAwaitingParentResolution);
        for (SampleRelationRecord relation : parentRelations)
        {
            samplesAwaitingParentResolution.put(relation.sample_id_child, new RelatedSampleRecord(
                    sampleMap.get(relation.sample_id_child), relation.sample_id_parent));
            addRelatedParentSampleToRequested(relation.sample_id_parent, relation.sample_id_child,
                    maxSampleParentResolutionDepth, primary);
        }
        requestedContainerSamples.keySet().removeAll(sampleMap.keySet());
        requestedParentSamples.keySet().removeAll(sampleMap.keySet());
        if (requestedContainerSamples.size() + requestedParentSamples.size() == 0)
        {
            return;
        }
        retrieveDependentBasicSamples(query.getSamples(requestedContainerSamples.keySet()));
        retrieveDependentBasicSamples(query.getSamples(requestedParentSamples.keySet()));
        retrieveDependentSamplesRecursively(false);
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
