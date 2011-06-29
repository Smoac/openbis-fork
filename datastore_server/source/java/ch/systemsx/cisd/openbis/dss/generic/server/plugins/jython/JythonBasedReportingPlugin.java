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
import ch.systemsx.cisd.common.utilities.PropertyUtils;
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

    private static final String SCRIPT_PATH = "script-path";

    private static final Logger notifyLog = LogFactory.getLogger(LogCategory.NOTIFY,
            AbstractDatastorePlugin.class);

    private final String scriptPath;

    public JythonBasedReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        this.scriptPath = PropertyUtils.getMandatoryProperty(properties, SCRIPT_PATH);
    }

    public TableModel createReport(List<DatasetDescription> dataSets,
            DataSetProcessingContext context)
    {
        return createReport(dataSets, context, scriptPath);
    }

    public static TableModel createReport(List<DatasetDescription> dataSets,
            DataSetProcessingContext context, String scriptPath)
    {
        operationLog.info("Report for the following datasets has been requested: " + dataSets);
        try
        {
            final IReportingPluginScriptRunner scriptRunner =
                    PluginScriptRunner.createReportingPluginRunnerFromScriptPath(scriptPath,
                            context);
            final IHierarchicalContentProvider contentProvider =
                    ServiceProvider.getHierarchicalContentProvider();
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
            notifyLog.error(ex.getMessage());
            throw new UserFailureException("Chosen plugin failed to create a report.");
        }
    }

    private static void delegateDescribe(IReportingPluginScriptRunner scriptRunner,
            List<IDataSet> dataSets, ISimpleTableModelBuilderAdaptor tableBuilder)
    {
        scriptRunner.describe(dataSets, tableBuilder);
    }
}
