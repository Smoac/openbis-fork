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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import org.apache.lucene.search.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDataAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IExperimentAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.ISampleAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * {@link IEntityAdaptor} implementation for {@link ExternalDataPE}.
 * 
 * @author Piotr Buczek
 */
public class ExternalDataAdaptor extends AbstractEntityAdaptor implements IDataAdaptor,
        INonAbstractEntityAdapter
{
    private final DataPE externalDataPE;

    private final Session session;

    public ExternalDataAdaptor(DataPE externalDataPE, IDynamicPropertyEvaluator evaluator,
            Session session)
    {
        super(externalDataPE, evaluator);
        this.session = session;
        this.externalDataPE = externalDataPE;
    }

    public DataPE externalDataPE()
    {
        return externalDataPE;
    }

    @Override
    public DataPE entityPE()
    {
        return externalDataPE();
    }

    @Override
    public IExperimentAdaptor experiment()
    {
        return EntityAdaptorFactory.create(externalDataPE.getExperiment(), evaluator, session);
    }

    @Override
    public ISampleAdaptor sample()
    {
        SamplePE sample = externalDataPE.tryGetSample();
        if (sample != null)
        {
            return EntityAdaptorFactory.create(sample, evaluator, session);
        } else
        {
            return null;
        }
    }

    @Override
    public Iterable<IDataAdaptor> parents()
    {
        return parentsOfType(ENTITY_TYPE_ANY_CODE_REGEXP);
    }

    @Override
    public Iterable<IDataAdaptor> parentsOfType(String typeCodeRegexp)
    {
        return new ExternalDataAdaptorRelationsLoader(externalDataPE, evaluator, session)
                .parentsOfType(typeCodeRegexp);
    }

    @Override
    public Iterable<IDataAdaptor> children()
    {
        return childrenOfType(ENTITY_TYPE_ANY_CODE_REGEXP);
    }

    @Override
    public Iterable<IDataAdaptor> childrenOfType(String typeCodeRegexp)
    {
        return new ExternalDataAdaptorRelationsLoader(externalDataPE, evaluator, session)
                .childrenOfType(typeCodeRegexp);
    }

    @Override
    public IDataAdaptor container()
    {
        DataPE container = externalDataPE.getContainer();
        if (container != null)
        {
            return EntityAdaptorFactory.create(container, evaluator, session);
        } else
        {
            return null;
        }
    }

    @Override
    public Iterable<IDataAdaptor> contained()
    {
        return containedOfType(ENTITY_TYPE_ANY_CODE_REGEXP);
    }

    @Override
    public Iterable<IDataAdaptor> containedOfType(String typeCodeRegexp)
    {
        Query typeConstraint =
                regexpConstraint(ENTITY_TYPE_CODE_FIELD, typeCodeRegexp.toLowerCase());
        Query containerConstraint =
                constraint(SearchFieldConstants.CONTAINER_ID, Long.toString(externalDataPE.getId()));
        Query query = and(typeConstraint, containerConstraint);

        ScrollableResults results = execute(query, DataPE.class, session);
        return new EntityAdaptorIterator<IDataAdaptor>(results, evaluator, session);
    }

}
