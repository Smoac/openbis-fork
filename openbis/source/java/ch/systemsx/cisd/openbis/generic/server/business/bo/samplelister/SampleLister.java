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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * A business object for providing lists of samples (more precisely sets of samples) for the purpose
 * of showing them and browsing through them. It is optimized for speed, using a custom strategy to
 * get the samples from the database.
 * 
 * @author Bernd Rinn
 */
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

}
