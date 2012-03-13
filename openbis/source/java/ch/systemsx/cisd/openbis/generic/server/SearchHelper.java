/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.business.search.DataSetSearchManager;
import ch.systemsx.cisd.openbis.generic.server.business.search.MaterialSearchManager;
import ch.systemsx.cisd.openbis.generic.server.business.search.SampleSearchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * A class with helper methods for implementing search. This is the shared code for that searches
 * originating from CommonServer and ETLService.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SearchHelper
{
    private final Session session;

    private final ICommonBusinessObjectFactory businessObjectFactory;

    private final IDAOFactory daoFactory;

    public SearchHelper(Session session, ICommonBusinessObjectFactory businessObjectFactory,
            IDAOFactory daoFactory)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
        this.daoFactory = daoFactory;
    }

    public List<Sample> searchForSamples(DetailedSearchCriteria criteria)
    {
        final ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        final IHibernateSearchDAO searchDAO = daoFactory.getHibernateSearchDAO();
        return new SampleSearchManager(searchDAO, sampleLister).searchForSamples(criteria);
    }

    public List<ExternalData> searchForDataSets(DetailedSearchCriteria detailedSearchCriteria)
    {
        IHibernateSearchDAO searchDAO = daoFactory.getHibernateSearchDAO();
        IDatasetLister dataSetLister = businessObjectFactory.createDatasetLister(session);
        return new DataSetSearchManager(searchDAO, dataSetLister)
                .searchForDataSets(detailedSearchCriteria);
    }

    public List<Material> searchForMaterials(DetailedSearchCriteria detailedSearchCriteria)
    {
        IHibernateSearchDAO searchDAO = daoFactory.getHibernateSearchDAO();
        IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);
        return new MaterialSearchManager(searchDAO, materialLister)
                .searchForMaterials(detailedSearchCriteria);
    }
}
