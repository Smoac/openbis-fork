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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDataAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IExperimentAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;

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
    public List<IDataAdaptor> parents()
    {
        List<IDataAdaptor> list = new ArrayList<IDataAdaptor>();
        for (DataSetRelationshipPE relationship : externalDataPE.getParentRelationships())
        {
            DataPE parent = relationship.getParentDataSet();
            list.add(EntityAdaptorFactory.create(parent, evaluator, session));
        }
        return list;
    }

    @Override
    public List<IDataAdaptor> children()
    {
        List<IDataAdaptor> list = new ArrayList<IDataAdaptor>();
        for (DataSetRelationshipPE relationship : externalDataPE.getChildRelationships())
        {
            DataPE child = relationship.getChildDataSet();
            list.add(EntityAdaptorFactory.create(child, evaluator, session));
        }
        return list;
    }

    @Override
    public List<IDataAdaptor> contained()
    {
        List<IDataAdaptor> list = new ArrayList<IDataAdaptor>();
        for (DataPE contained : externalDataPE.getContainedDataSets())
        {
            list.add(EntityAdaptorFactory.create(contained, evaluator, session));
        }
        return list;
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
}
