/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython;

import java.util.Map;

import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * Interface to be implemented for a script runner of a db-modifying aggregation service.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDbModifyingAggregationServiceReportingPluginScriptRunner
{
    public void process(IDataSetRegistrationTransactionV2 transaction,
            Map<String, Object> parameters, ISimpleTableModelBuilderAdaptor tableBuilder)
            throws EvaluatorException;

    public void releaseResources();
}
