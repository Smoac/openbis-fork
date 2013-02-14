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
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IExperimentAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.ISampleAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;

/**
 * {@link IEntityAdaptor} implementation for {@link ExperimentPE}.
 * 
 * @author Piotr Buczek
 */
public class ExperimentAdaptor extends AbstractEntityAdaptor implements IExperimentAdaptor,
        INonAbstractEntityAdapter
{
    private final ExperimentPE experimentPE;

    private final Session session;

    public ExperimentAdaptor(ExperimentPE experimentPE, IDynamicPropertyEvaluator evaluator,
            Session session)
    {
        super(experimentPE, evaluator);
        this.session = session;
        this.experimentPE = experimentPE;
    }

    public ExperimentPE experimentPE()
    {
        return experimentPE;
    }

    @Override
    public ExperimentPE entityPE()
    {
        return experimentPE();
    }

    @Override
    public Iterable<ISampleAdaptor> samples()
    {
        return samplesOfType(ENTITY_TYPE_ANY_CODE_REGEXP);
    }

    @Override
    public Iterable<ISampleAdaptor> samplesOfType(String typeRegexp)
    {
        Query typeConstraint = regexpConstraint(ENTITY_TYPE_CODE_FIELD, typeRegexp.toLowerCase());
        Query experimentCodeConstraint =
                constraint(SearchFieldConstants.EXPERIMENT_ID, Long.toString(experimentPE.getId()));
        Query query = and(typeConstraint, experimentCodeConstraint);

        ScrollableResults results = execute(query, SamplePE.class, session);
        return new EntityAdaptorIterator<ISampleAdaptor>(results, evaluator, session);
    }

    @Override
    public Iterable<IDataAdaptor> dataSets()
    {
        return dataSetsOfType(ENTITY_TYPE_ANY_CODE_REGEXP);
    }

    @Override
    public Iterable<IDataAdaptor> dataSetsOfType(String typeRegexp)
    {
        Query typeConstraint = regexpConstraint(ENTITY_TYPE_CODE_FIELD, typeRegexp.toLowerCase());
        Query experimentCodeConstraint =
                constraint(SearchFieldConstants.EXPERIMENT_ID, Long.toString(experimentPE.getId()));
        Query query = and(typeConstraint, experimentCodeConstraint);

        ScrollableResults results = execute(query, DataPE.class, session);
        return new EntityAdaptorIterator<IDataAdaptor>(results, evaluator, session);
    }
}
