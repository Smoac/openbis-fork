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
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IExperimentAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.ISampleAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;

/**
 * {@link IEntityAdaptor} implementation for {@link SamplePE}.
 * 
 * @author Piotr Buczek
 */
public class SampleAdaptor extends AbstractEntityAdaptor implements ISampleAdaptor,
        INonAbstractEntityAdapter
{
    private final SamplePE samplePE;

    private final Session session;

    public SampleAdaptor(SamplePE samplePE, IDynamicPropertyEvaluator evaluator, Session session)
    {
        super(samplePE, evaluator);
        this.session = session;
        this.samplePE = samplePE;
    }

    public SamplePE samplePE()
    {
        return samplePE;
    }

    @Override
    public SamplePE entityPE()
    {
        return samplePE();
    }

    @Override
    public IExperimentAdaptor experiment()
    {
        return EntityAdaptorFactory.create(samplePE.getExperiment(), evaluator, session);
    }

    @Override
    public List<ISampleAdaptor> parents()
    {
        List<ISampleAdaptor> list = new ArrayList<ISampleAdaptor>();
        for (SamplePE parent : samplePE.getParents())
        {
            list.add(EntityAdaptorFactory.create(parent, evaluator, session));
        }
        return list;
    }

    @Override
    public List<ISampleAdaptor> children()
    {
        List<ISampleAdaptor> list = new ArrayList<ISampleAdaptor>();
        for (SampleRelationshipPE relationship : samplePE.getChildRelationships())
        {
            SamplePE child = relationship.getChildSample();
            list.add(EntityAdaptorFactory.create(child, evaluator, session));
        }
        return list;
    }

    @Override
    public List<ISampleAdaptor> contained()
    {
        List<ISampleAdaptor> list = new ArrayList<ISampleAdaptor>();
        for (SamplePE contained : samplePE.getContained())
        {
            list.add(EntityAdaptorFactory.create(contained, evaluator, session));
        }
        return list;
    }

    @Override
    public ISampleAdaptor container()
    {
        SamplePE container = samplePE.getContainer();
        if (container != null)
        {
            return EntityAdaptorFactory.create(container, evaluator, session);
        } else
        {
            return null;
        }
    }

}
