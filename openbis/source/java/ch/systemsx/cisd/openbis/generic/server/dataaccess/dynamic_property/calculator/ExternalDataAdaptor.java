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

import ch.systemsx.cisd.common.resource.ReleasableIterable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDataAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IExperimentAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.ISampleAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;

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
        IExperimentAdaptor adaptor = EntityAdaptorFactory.create(externalDataPE.getExperiment(), evaluator, session);
        getResources().add(adaptor);
        return adaptor;
    }

    @Override
    public ISampleAdaptor sample()
    {
        SamplePE sample = externalDataPE.tryGetSample();
        if (sample != null)
        {
            ISampleAdaptor adaptor = EntityAdaptorFactory.create(sample, evaluator, session);
            getResources().add(adaptor);
            return adaptor;
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
        ReleasableIterable<IDataAdaptor> iterable =
                new ReleasableIterable<IDataAdaptor>(new ExternalDataAdaptorRelationsLoader(externalDataPE, evaluator, session)
                        .parentsOfType(typeCodeRegexp));
        getResources().add(iterable);
        return iterable;
    }

    @Override
    public Iterable<IDataAdaptor> children()
    {
        return childrenOfType(ENTITY_TYPE_ANY_CODE_REGEXP);
    }

    @Override
    public Iterable<IDataAdaptor> childrenOfType(String typeCodeRegexp)
    {
        ReleasableIterable<IDataAdaptor> iterable =
                new ReleasableIterable<IDataAdaptor>(new ExternalDataAdaptorRelationsLoader(externalDataPE, evaluator, session)
                        .childrenOfType(typeCodeRegexp));
        getResources().add(iterable);
        return iterable;
    }

    @Override
    public IDataAdaptor container()
    {
        DataPE container = externalDataPE.getContainer();
        if (container != null)
        {
            IDataAdaptor adaptor = EntityAdaptorFactory.create(container, evaluator, session);
            getResources().add(adaptor);
            return adaptor;
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
        EntityAdaptorIterator<IDataAdaptor> iterator = new EntityAdaptorIterator<IDataAdaptor>(results, evaluator, session);
        getResources().add(iterator);
        return iterator;
    }

}
