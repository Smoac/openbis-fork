/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.collections.IValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationShipSkeleton;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleSkeleton;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SampleLoader implements ISampleLoader
{
    private final Session session;
    private final IDAOFactory daoFactory;
    private final ICommonBusinessObjectFactory businessObjectFactory;

    public SampleLoader(Session session, IDAOFactory daoFactory,
            ICommonBusinessObjectFactory businessObjectFactory)
    {
        this.session = session;
        this.daoFactory = daoFactory;
        this.businessObjectFactory = businessObjectFactory;
        
    }
    
    public List<Sample> listSamplesWithParentsByTypeAndSpace(String sampleTypeCode, String spaceCode)
    {
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        Set<Long> filteredSampleIDs = getSampleIDs(sampleLister, sampleTypeCode, spaceCode);
        ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(filteredSampleIDs);
        criteria.setEnrichDependentSamplesWithProperties(true);
        List<Sample> samples = sampleLister.list(criteria);
        ArrayList<Sample> samplesWithParent = new ArrayList<Sample>();
        for (Sample sample : samples)
        {
            if (sample.getParents().size() == 1)
            {
                samplesWithParent.add(sample);
            }
        }
        return samplesWithParent;
    }

    private Set<Long> getSampleIDs(ISampleLister sampleLister, String sampleTypeCode,
            String spaceCode)
    {
        SampleTypePE sampleTypePE =
                daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode);
        final Long sampleTypeID = sampleTypePE.getId();
        GroupPE space =
                daoFactory.getGroupDAO().tryFindGroupByCodeAndDatabaseInstance(spaceCode,
                        daoFactory.getHomeDatabaseInstance());
        final Long spaceID = space.getId();
        List<SampleSkeleton> sampleSkeletons =
                sampleLister.listSampleBy(new IValidator<SampleSkeleton>()
                    {
                        public boolean isValid(SampleSkeleton sampleSkeleton)
                        {
                            return spaceID.equals(sampleSkeleton.getSpaceID())
                                    && sampleTypeID.equals(sampleSkeleton.getTypeID());
                        }
                    });
        final Set<Long> sampleIDs = new HashSet<Long>();
        for (SampleSkeleton sampleSkeleton : sampleSkeletons)
        {
            sampleIDs.add(sampleSkeleton.getId());
        }
        final long relationshipTypeID =
                sampleLister
                        .getRelationshipTypeID(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
        List<SampleRelationShipSkeleton> relationshipSkeletons =
                sampleLister.listSampleRelationShipsBy(new IValidator<SampleRelationShipSkeleton>()
                    {
                        public boolean isValid(SampleRelationShipSkeleton skeleton)
                        {
                            return skeleton.getRelationShipTypeID() == relationshipTypeID
                                    && sampleIDs.contains(skeleton.getChildSampleID());
                        }
                    });
        Set<Long> filteredSampleIDs = new HashSet<Long>();
        Set<Long> parentIDs = new HashSet<Long>();
        for (SampleRelationShipSkeleton sampleRelationShipSkeleton : relationshipSkeletons)
        {
            filteredSampleIDs.add(sampleRelationShipSkeleton.getChildSampleID());
            parentIDs.add(sampleRelationShipSkeleton.getParentSampleID());
        }
        return filteredSampleIDs;
    }
    
}
