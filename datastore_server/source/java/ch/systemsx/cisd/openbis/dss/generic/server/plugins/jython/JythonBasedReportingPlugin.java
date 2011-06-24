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

import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IDataSet;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractTableModelReportingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * Reporting plugin which delegates the creation of report to a Jython script.
 * 
 * @author Piotr Buczek
 */
public class JythonBasedReportingPlugin extends AbstractTableModelReportingPlugin
{
    private static final long serialVersionUID = 1L;

    public JythonBasedReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    public TableModel createReport(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        operationLog.info("Reporting for the following datasets has been requested: " + datasets);
        IHierarchicalContentProvider contentProvider =
                ServiceProvider.getHierarchicalContentProvider();
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();

        for (DatasetDescription dataset : datasets)
        {
            describe(builder, dataset, contentProvider);
        }
        return builder.getTableModel();
    }

    private void describe(SimpleTableModelBuilder builder, DatasetDescription dataset,
            IHierarchicalContentProvider contentProvider)
    {
        String dataSetCode = dataset.getDataSetCode();
        IHierarchicalContent content = null;
        try
        {
            content = contentProvider.asContent(dataSetCode);
            IDataSet iDataSet = JythonBasedPluginUtils.createDataSet(dataset, content);
            delegateDescribe(builder, iDataSet);
        } finally
        {
            if (content != null)
            {
                content.close();
            }
        }
    }

    private void delegateDescribe(SimpleTableModelBuilder builder, IDataSet iDataSet)
    {
        // TODO Auto-generated method stub
    }

}
