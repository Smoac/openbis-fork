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

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDbModifyingAggregationService;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * Implementation of DbModifyingAggregationServices that are implemented in jython.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonBasedDbModifyingAggregationServiceReportingPlugin extends
        AbstractDbModifyingAggregationService<DataSetInformation>
{
    private static final long serialVersionUID = 1L;

    private static final Logger notifyLog = LogFactory.getLogger(LogCategory.NOTIFY,
            JythonBasedDbModifyingAggregationServiceReportingPlugin.class);

    protected static String getScriptPathProperty(Properties properties)
    {
        return JythonBasedProcessingPlugin.getScriptPathProperty(properties);
    }

    private final IPluginScriptRunnerFactory scriptRunnerFactory;

    public JythonBasedDbModifyingAggregationServiceReportingPlugin(Properties properties,
            File storeRoot)
    {
        this(properties, storeRoot,
                new PluginScriptRunnerFactory(getScriptPathProperty(properties)));
    }

    protected JythonBasedDbModifyingAggregationServiceReportingPlugin(Properties properties,
            File storeRoot, IPluginScriptRunnerFactory scriptRunnerFactory)
    {
        super(properties, storeRoot);
        this.scriptRunnerFactory = scriptRunnerFactory;
    }

    @Override
    public TableModel process(final IDataSetRegistrationTransaction transaction,
            final Map<String, Object> parameters, final DataSetProcessingContext context)
    {
        ITableModelCreator generator = new ITableModelCreator()
            {
                @Override
                public void create(ISimpleTableModelBuilderAdaptor builder)
                {
                    operationLog.info("Aggregation report for the following parameters "
                            + "has been requested: " + parameters);
                    IAggregationServiceReportingPluginScriptRunner runner =
                            scriptRunnerFactory
                                    .createAggregationServiceReportingPluginRunner(context);
                    try
                    {
                        runner.process(transaction, parameters, builder);
                    } finally
                    {
                        operationLog.info("Aggregation reporting done.");
                        runner.releaseResources();
                    }
                }
            };
        return Utils.generateTableModel(generator, scriptRunnerFactory.getScriptPath(), notifyLog);
    }

}
