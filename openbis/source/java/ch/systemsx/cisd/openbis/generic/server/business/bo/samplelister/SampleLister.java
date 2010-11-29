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

import java.util.ArrayList;
import java.util.List;

import net.lemnik.eodsql.DataIterator;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.collections.IValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationShipSkeleton;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleSkeleton;

/**
 * A business object for providing lists of samples (more precisely sets of samples) for the purpose
 * of showing them and browsing through them. It is optimized for speed, using a custom strategy to
 * get the samples from the database.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses =
    { ISampleListingQuery.class, SampleRecord.class, SampleRelationRecord.class })
public class SampleLister implements ISampleLister
{
    private final SampleListerDAO dao;

    private final SecondaryEntityDAO referencedEntityDAO;

    private final String baseIndexURL;

    public static SampleLister create(IDAOFactory daoFactory, String baseIndexURL)
    {
        SampleListerDAO sampleListerDAO = SampleListerDAO.create(daoFactory);
        SecondaryEntityDAO referencedEntityDAO = SecondaryEntityDAO.create(daoFactory);
        return new SampleLister(baseIndexURL, sampleListerDAO, referencedEntityDAO);
    }

    private SampleLister(String baseIndexURL, SampleListerDAO dao,
            SecondaryEntityDAO referencedEntityDAO)
    {
        this.baseIndexURL = baseIndexURL;
        this.dao = dao;
        this.referencedEntityDAO = referencedEntityDAO;
    }

    public List<Sample> list(final ListOrSearchSampleCriteria criteria)
    {
        return SampleListingWorker.create(criteria, baseIndexURL, dao, referencedEntityDAO).load();
    }

    public long getRelationshipTypeID(String code)
    {
        return SampleListingWorker.getRelationId(dao.getQuery(), code);
    }

    public List<SampleSkeleton> listSampleBy(IValidator<SampleSkeleton> criteria)
    {
        DataIterator<SampleRecord> sampleSkeletons = dao.getQuery().getSampleSkeletons();
        List<SampleSkeleton> result = new ArrayList<SampleSkeleton>();
        for (SampleRecord sampleRecord : sampleSkeletons)
        {
            SampleSkeleton sampleSkeleton = new SampleSkeleton();
            sampleSkeleton.setId(sampleRecord.id);
            sampleSkeleton.setExperimentID(sampleRecord.expe_id);
            sampleSkeleton.setSpaceID(sampleRecord.space_id);
            sampleSkeleton.setTypeID(sampleRecord.saty_id);
            sampleSkeleton.setDatabaseInstanceID(sampleRecord.dbin_id);
            if (criteria.isValid(sampleSkeleton))
            {
                result.add(sampleSkeleton);
            }
        }
        return result;
    }

    public List<SampleRelationShipSkeleton> listSampleRelationShipsBy(
            IValidator<SampleRelationShipSkeleton> criteria)
    {
        DataIterator<SampleRelationRecord> records = dao.getQuery().getSampleRelationshipSkeletons();
        List<SampleRelationShipSkeleton> result = new ArrayList<SampleRelationShipSkeleton>();
        for (SampleRelationRecord record : records)
        {
            SampleRelationShipSkeleton skeleton = new SampleRelationShipSkeleton();
            skeleton.setRelationShipTypeID(record.relationship_id);
            skeleton.setParentSampleID(record.sample_id_parent);
            skeleton.setChildSampleID(record.sample_id_child);
            if (criteria.isValid(skeleton))
            {
                result.add(skeleton);
            }
        }
        return result;
    }

}
