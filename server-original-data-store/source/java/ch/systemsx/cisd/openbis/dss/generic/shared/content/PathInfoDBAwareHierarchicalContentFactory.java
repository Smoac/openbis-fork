/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;

/**
 * The implementation of {@link IHierarchicalContentFactory} that aware of Path Info DB.
 * 
 * @author Piotr Buczek
 */
public class PathInfoDBAwareHierarchicalContentFactory extends
        DefaultFileBasedHierarchicalContentFactory
{

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PathInfoDBAwareHierarchicalContentFactory.class);

    /**
     * Returns implementation of {@link IHierarchicalContentFactory} based on configuration of Path Info DB. If the DB is not configured than file
     * system based implementation will be used. Otherwise the implementation will use the DB to retrieve file metadata.
     */
    public static IHierarchicalContentFactory create()
    {
        if (PathInfoDataSourceProvider.isDataSourceDefined())
        {
            operationLog.debug("Path Info DB is properly configured");
            return new PathInfoDBAwareHierarchicalContentFactory(
                    ServiceProvider.getDataSetPathInfoProvider());
        } else
        {
            operationLog.warn("Path Info DB was NOT configured. "
                    + "File system based implementation will be used.");
            return new DefaultFileBasedHierarchicalContentFactory();
        }
    }

    private final IDataSetPathInfoProvider pathInfoProvider;

    @Private
    PathInfoDBAwareHierarchicalContentFactory(IDataSetPathInfoProvider pathInfoProvider)
    {
        this.pathInfoProvider = pathInfoProvider;
    }

    @Override
    public IHierarchicalContent asHierarchicalContent(File file, IDelegatedAction onCloseAction)
    {
        final String dataSetCode = file.getName();
        ISingleDataSetPathInfoProvider dataSetPathInfoProvider =
                pathInfoProvider.tryGetSingleDataSetPathInfoProvider(dataSetCode);
        if (dataSetPathInfoProvider != null) // data set exists in DB
        {
            operationLog.debug("Data set " + dataSetCode + " was found in Path Info DB.");
            return new PathInfoProviderBasedHierarchicalContent(dataSetPathInfoProvider, file,
                    onCloseAction);
        } else
        {
            operationLog.warn("Data set " + dataSetCode + " was NOT found in Path Info DB. "
                    + "Falling back to file system based implementation.");
            return super.asHierarchicalContent(file, onCloseAction);
        }
    }

}