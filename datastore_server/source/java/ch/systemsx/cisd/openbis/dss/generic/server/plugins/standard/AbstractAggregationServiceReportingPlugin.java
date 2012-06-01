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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * The abstract superclass for plug-ins of type DSS_LINK.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractAggregationServiceReportingPlugin extends AbstractDatastorePlugin implements
        IReportingPluginTask
{
    private static final long serialVersionUID = 1L;

    protected AbstractAggregationServiceReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    @Override
    public ReportingPluginType getReportingPluginType()
    {
        return ReportingPluginType.AGGREGATION_TABLE_MODEL;
    }

    @Override
    public TableModel createReport(List<DatasetDescription> datasets, DataSetProcessingContext context)
    {
        throw createException();
    }

    @Override
    public LinkModel createLink(DatasetDescription dataset)
    {
        throw createException();
    }
    
    private IllegalArgumentException createException()
    {
        return new IllegalArgumentException(
                "The method createReport is not supported by " + getReportingPluginType()
                        + " tasks");
    }
    
}
