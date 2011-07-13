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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IDataSet;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractTableModelReportingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * Reporting plugin which delegates the creation of report to a Jython script.
 * 
 * @author Piotr Buczek
 */
public class JythonBasedReportingPlugin extends AbstractTableModelReportingPlugin
{
    private static final long serialVersionUID = 1L;

    private static final Logger notifyLog = LogFactory.getLogger(LogCategory.NOTIFY,
            AbstractDatastorePlugin.class);

    protected static String getScriptPathProperty(Properties properties)
    {
        return JythonBasedProcessingPlugin.getScriptPathProperty(properties);
    }

    private final IPluginScriptRunnerFactory scriptRunnerFactory;

    transient IHierarchicalContentProvider hierarchicalContentProvider;

    public JythonBasedReportingPlugin(Properties properties, File storeRoot)
    {
        this(properties, storeRoot,
                new PluginScriptRunnerFactory(getScriptPathProperty(properties)), null);
    }

    // for tests
    protected JythonBasedReportingPlugin(Properties properties, File storeRoot,
            IPluginScriptRunnerFactory scriptRunnerFactory,
            IHierarchicalContentProvider contentProvider)
    {
        super(properties, storeRoot);
        this.scriptRunnerFactory = scriptRunnerFactory;
        this.hierarchicalContentProvider = contentProvider;
    }

    public TableModel createReport(List<DatasetDescription> dataSets,
            DataSetProcessingContext context)
    {
        return createReport(dataSets, context, scriptRunnerFactory,
                getHierarchicalContentProvider());
    }

    private IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        if (hierarchicalContentProvider == null)
        {
            hierarchicalContentProvider = ServiceProvider.getHierarchicalContentProvider();
        }
        return hierarchicalContentProvider;
    }

    public static TableModel createReport(List<DatasetDescription> dataSets,
            DataSetProcessingContext context, IPluginScriptRunnerFactory scriptRunnerFactory,
            IHierarchicalContentProvider contentProvider)
    {
        operationLog.info("Report for the following datasets has been requested: " + dataSets);
        try
        {
            final IReportingPluginScriptRunner scriptRunner =
                    scriptRunnerFactory.createReportingPluginRunner(context);
            final List<IDataSet> iDataSets =
                    JythonBasedPluginUtils.convert(dataSets, contentProvider);
            try
            {
                final ISimpleTableModelBuilderAdaptor builder =
                        SimpleTableModelBuilderAdaptor.create();
                delegateDescribe(scriptRunner, iDataSets, builder);
                return (TableModel) builder.getTableModel();
            } finally
            {
                operationLog.info("Reporting done");
                JythonBasedPluginUtils.closeContent(iDataSets);
            }
        } catch (EvaluatorException ex)
        {
            StringBuilder errorString = new StringBuilder();
            errorString
                    .append("Could not run report script " + scriptRunnerFactory.getScriptPath());
            if (null != ex.getCause())
            {
                notifyLog.error(errorString.toString(), ex.getCause());
            } else
            {
                notifyLog.error(errorString.toString(), ex);
            }
            throw new UserFailureException("Chosen plugin failed to create a report.");
        } catch (RuntimeException ex)
        {
            StringBuilder errorString = new StringBuilder();
            errorString
                    .append("Could not run report script " + scriptRunnerFactory.getScriptPath());
            notifyLog.error(errorString.toString(), ex);
            throw new UserFailureException("Chosen plugin failed to create a report.");
        }
    }

    private static void delegateDescribe(IReportingPluginScriptRunner scriptRunner,
            List<IDataSet> dataSets, ISimpleTableModelBuilderAdaptor tableBuilder)
    {
        scriptRunner.describe(dataSets, tableBuilder);
    }

}
