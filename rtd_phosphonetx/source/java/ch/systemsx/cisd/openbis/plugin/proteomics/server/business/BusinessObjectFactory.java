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

package ch.systemsx.cisd.openbis.plugin.proteomics.server.business;

import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.AbstractPluginBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.dataaccess.IPhosphoNetXDAOFactory;

/**
 * @author Franz-Josef Elmer
 */
public class BusinessObjectFactory extends AbstractPluginBusinessObjectFactory implements
        IBusinessObjectFactory
{
    private final IDAOFactory daoFactory;

    private final IPhosphoNetXDAOFactory specificDAOFactory;

    private final ICommonBusinessObjectFactory businessObjectFactory;

    public BusinessObjectFactory(IDAOFactory daoFactory, IPhosphoNetXDAOFactory specificDAOFactory,
            ICommonBusinessObjectFactory businessObjectFactory)
    {
        this.daoFactory = daoFactory;
        this.specificDAOFactory = specificDAOFactory;
        this.businessObjectFactory = businessObjectFactory;
    }

    @Override
    public ISampleLister createSampleLister(Session session)
    {
        return getCommonBusinessObjectFactory().createSampleLister(session);
    }

    @Override
    public IAbundanceColumnDefinitionTable createAbundanceColumnDefinitionTable(Session session)
    {
        return new AbundanceColumnDefinitionTable(daoFactory, specificDAOFactory, session);
    }

    @Override
    public IProteinInfoTable createProteinInfoTable(Session session, ISampleProvider sampleProvider)
    {
        return new ProteinInfoTable(daoFactory, specificDAOFactory, session, sampleProvider);
    }

    @Override
    public IProteinSummaryTable createProteinSummaryTable(Session session)
    {
        return new ProteinSummaryTable(daoFactory, specificDAOFactory, session);
    }

    @Override
    public IProteinSequenceTable createProteinSequenceTable(Session session)
    {
        return new ProteinSequenceTable(daoFactory, specificDAOFactory, session);
    }

    @Override
    public IDataSetProteinTable createDataSetProteinTable(Session session)
    {
        return new DataSetProteinTable(daoFactory, specificDAOFactory, session);
    }

    @Override
    public IProteinDetailsBO createProteinDetailsBO(Session session)
    {
        return new ProteinDetailsBO(daoFactory, specificDAOFactory, session);
    }

    @Override
    public IProteinRelatedSampleTable createProteinRelatedSampleTable(Session session)
    {
        return new ProteinRelatedSampleTable(daoFactory, specificDAOFactory,
                getManagedPropertyEvaluatorFactory());
    }

    @Override
    public ISampleTable createSampleTable(Session session)
    {
        return new SampleTable(daoFactory, specificDAOFactory, session,
                getManagedPropertyEvaluatorFactory());
    }

    @Override
    public ISampleIDProvider createSampleIDProvider(Session session)
    {
        return new SampleIDProvider(daoFactory.getSampleDAO());
    }

    @Override
    public ISampleProvider createSampleProvider(Session session)
    {
        return new SampleProvider(session, this);
    }

    @Override
    public ISampleLoader createSampleLoader(Session session)
    {
        return new SampleLoader(session, daoFactory, businessObjectFactory);
    }

}
