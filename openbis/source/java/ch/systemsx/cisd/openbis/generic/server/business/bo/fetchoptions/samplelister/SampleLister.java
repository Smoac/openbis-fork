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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.samplelister;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.PropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;

/**
 * @author Franz-Josef Elmer
 */
public class SampleLister implements ISampleLister
{
    private static final Comparator<Sample> SAMPLE_COMPARATOR = new Comparator<Sample>()
        {
            public int compare(Sample s1, Sample s2)
            {
                return s1.getIdentifier().compareTo(s2.getIdentifier());
            }
        };

    private static final IKeyExtractor<Long, SampleRecord> ID_EXTRACTOR =
            new IKeyExtractor<Long, SampleRecord>()
                {
                    public Long getKey(SampleRecord s)
                    {
                        return s.s_id;
                    }
                };

    private final ISampleListingQuery query;

    private Long relationID;

    public SampleLister(IDAOFactory daoFactory)
    {
        this(QueryTool.getQuery(DatabaseContextUtils.getConnection(daoFactory),
                ISampleListingQuery.class));
    }

    SampleLister(ISampleListingQuery query)
    {
        this.query = query;
    }

    private Long getRelationShipType()
    {
        if (relationID == null)
        {
            String code =
                    CodeConverter.tryToDatabase(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
            boolean internalNamespace =
                    CodeConverter
                            .isInternalNamespace(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
            relationID = query.getRelationshipTypeId(code, internalNamespace);
        }
        return relationID;
    }

    public List<Sample> getSamples(Collection<Long> sampleIDs,
            EnumSet<SampleFetchOption> fetchOptions)
    {
        LongSet sampleIdSet = new LongOpenHashSet(sampleIDs);
        LongSet rootSampleIdSet = new LongOpenHashSet(sampleIDs);
        List<SampleRelationshipRecord> descendants = Collections.emptyList();
        if (fetchOptions.contains(SampleFetchOption.DESCENDANTS))
        {
            descendants = query.getDescendants(getRelationShipType(), rootSampleIdSet);
        } else if (fetchOptions.contains(SampleFetchOption.CHILDREN))
        {
            descendants = query.getChildren(getRelationShipType(), rootSampleIdSet);
        }
        for (SampleRelationshipRecord relationShip : descendants)
        {
            sampleIdSet.add(relationShip.sample_id_child);
        }
        List<SampleRelationshipRecord> ancestors = Collections.emptyList();
        if (fetchOptions.contains(SampleFetchOption.ANCESTORS))
        {
            ancestors = query.getAncestors(getRelationShipType(), rootSampleIdSet);
        } else if (fetchOptions.contains(SampleFetchOption.PARENTS))
        {
            ancestors = query.getParents(getRelationShipType(), rootSampleIdSet);
        }
        for (SampleRelationshipRecord relationShip : ancestors)
        {
            sampleIdSet.add(relationShip.sample_id_parent);
        }
        List<SampleRecord> list = query.listSamplesByIds(sampleIdSet);
        TableMap<Long, SampleRecord> sampleRecords =
                new TableMap<Long, SampleRecord>(list, ID_EXTRACTOR);
        if (fetchOptions.contains(SampleFetchOption.PROPERTIES))
        {
            enrichWithProperties(sampleRecords, sampleIdSet);
        }
        enrichWithDescendants(descendants, sampleRecords);
        enrichWithAncestors(ancestors, sampleRecords);
        List<Sample> samples = new ArrayList<Sample>();
        for (Long rootSampleID : sampleIDs)
        {
            SampleRecord sampleRecord = sampleRecords.tryGet(rootSampleID);
            if (sampleRecord != null)
            {
                samples.add(createSample(sampleRecord, fetchOptions));
            }
        }
        Collections.sort(samples, SAMPLE_COMPARATOR);
        return samples;
    }

    public void enrichWithAncestors(List<SampleRelationshipRecord> ancestors,
            TableMap<Long, SampleRecord> sampleRecords)
    {
        for (SampleRelationshipRecord ancestor : ancestors)
        {
            SampleRecord parent = sampleRecords.tryGet(ancestor.sample_id_parent);
            SampleRecord child = sampleRecords.tryGet(ancestor.sample_id_child);
            if (parent != null && child != null)
            {
                if (child.parents == null)
                {
                    child.parents = new LinkedList<SampleRecord>();
                }
                child.parents.add(parent);
            }
        }
    }

    public void enrichWithDescendants(List<SampleRelationshipRecord> descendants,
            TableMap<Long, SampleRecord> sampleRecords)
    {
        for (SampleRelationshipRecord descendant : descendants)
        {
            SampleRecord parent = sampleRecords.tryGet(descendant.sample_id_parent);
            SampleRecord child = sampleRecords.tryGet(descendant.sample_id_child);
            if (parent != null && child != null)
            {
                if (parent.children == null)
                {
                    parent.children = new LinkedList<SampleRecord>();
                }
                parent.children.add(child);
            }
        }
    }

    private void enrichWithProperties(TableMap<Long, SampleRecord> sampleRecords, LongSet sampleIDs)
    {
        List<PropertyRecord> properties = query.getProperties(sampleIDs);
        for (PropertyRecord propertyRecord : properties)
        {
            SampleRecord sampleRecord = sampleRecords.tryGet(propertyRecord.entity_id);
            if (sampleRecord != null)
            {
                if (sampleRecord.properties == null)
                {
                    sampleRecord.properties = new HashMap<String, String>();
                }
                sampleRecord.properties.put(propertyRecord.code, propertyRecord.getValue());
            }
        }
    }

    private Sample createSample(SampleRecord sampleRecord, EnumSet<SampleFetchOption> fetchOptions)
    {
        Sample.SampleInitializer initializer = new Sample.SampleInitializer();
        initializer.setId(sampleRecord.s_id);
        String spaceCode = sampleRecord.sp_code;
        initializer.setSpaceCode(spaceCode);
        String sampleCode = sampleRecord.s_code;
        initializer.setCode(sampleCode);
        initializer.setIdentifier(spaceCode == null ? "/" + sampleCode : "/" + spaceCode + "/"
                + sampleCode);
        initializer.setPermId(sampleRecord.s_perm_id);
        EntityRegistrationDetailsInitializer detailsInitializer =
                new EntityRegistrationDetailsInitializer();
        detailsInitializer.setRegistrationDate(sampleRecord.s_registration_timestamp);
        detailsInitializer.setModificationDate(sampleRecord.s_modification_timestamp);
        detailsInitializer.setUserId(sampleRecord.pe_user_id);
        detailsInitializer.setEmail(sampleRecord.pe_email);
        detailsInitializer.setFirstName(sampleRecord.pe_first_name);
        detailsInitializer.setLastName(sampleRecord.pe_last_name);
        initializer.setRegistrationDetails(new EntityRegistrationDetails(detailsInitializer));
        initializer.setSampleTypeId(sampleRecord.st_id);
        initializer.setSampleTypeCode(sampleRecord.st_code);
        Map<String, String> properties = sampleRecord.properties;
        if (properties != null)
        {
            for (Entry<String, String> entry : properties.entrySet())
            {
                initializer.putProperty(entry.getKey(), entry.getValue());
            }
        }
        EnumSet<SampleFetchOption> rootFetchOptions = createAppropriateFetchOptions(fetchOptions);
        if (fetchOptions.contains(SampleFetchOption.CHILDREN)
                || fetchOptions.contains(SampleFetchOption.DESCENDANTS))
        {
            rootFetchOptions.add(SampleFetchOption.CHILDREN);
        }
        if (fetchOptions.contains(SampleFetchOption.PARENTS)
                || fetchOptions.contains(SampleFetchOption.ANCESTORS))
        {
            rootFetchOptions.add(SampleFetchOption.PARENTS);
        }
        initializer.setRetrievedFetchOptions(rootFetchOptions);
        if (sampleRecord.children != null)
        {
            EnumSet<SampleFetchOption> childrenOptions =
                    createAppropriateFetchOptions(fetchOptions);
            if (fetchOptions.contains(SampleFetchOption.DESCENDANTS))
            {
                childrenOptions.add(SampleFetchOption.CHILDREN);
            }
            initializer.setChildren(createChildren(sampleRecord.children, childrenOptions));
        }
        if (sampleRecord.parents != null)
        {
            EnumSet<SampleFetchOption> parentsOptions = createAppropriateFetchOptions(fetchOptions);
            if (fetchOptions.contains(SampleFetchOption.ANCESTORS))
            {
                parentsOptions.add(SampleFetchOption.PARENTS);
            }
            initializer.setParents(createParents(sampleRecord.parents, parentsOptions));
        }
        return new Sample(initializer);
    }

    private EnumSet<SampleFetchOption> createAppropriateFetchOptions(
            EnumSet<SampleFetchOption> fetchOptions)
    {
        return EnumSet
                .of(fetchOptions.contains(SampleFetchOption.PROPERTIES) ? SampleFetchOption.PROPERTIES
                        : SampleFetchOption.BASIC);
    }

    private List<Sample> createChildren(List<SampleRecord> childRecords,
            EnumSet<SampleFetchOption> fetchOptions)
    {
        List<Sample> children = new ArrayList<Sample>();
        for (SampleRecord childRecord : childRecords)
        {
            children.add(createSample(childRecord, fetchOptions));
        }
        Collections.sort(children, SAMPLE_COMPARATOR);
        return children;
    }

    private List<Sample> createParents(List<SampleRecord> parentRecords,
            EnumSet<SampleFetchOption> fetchOptions)
    {
        List<Sample> parents = new ArrayList<Sample>();
        for (SampleRecord parentRecord : parentRecords)
        {
            parents.add(createSample(parentRecord, fetchOptions));
        }
        Collections.sort(parents, SAMPLE_COMPARATOR);
        return parents;
    }

}
