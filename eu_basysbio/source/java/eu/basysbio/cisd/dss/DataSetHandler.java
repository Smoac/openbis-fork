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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.etlserver.AbstractPostRegistrationDataSetHandlerForFileBasedUndo;
import ch.systemsx.cisd.etlserver.IDataSetUploader;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;


/**
 * @author Franz-Josef Elmer
 */
class DataSetHandler extends AbstractPostRegistrationDataSetHandlerForFileBasedUndo implements IDataSetUploader, IFileManager
{
    static final String TIME_SERIES = "TIME_SERIES";
    static final String LCA_MTP_TIME_SERIES = "LCA_MTP_TIME_SERIES";
    static final String LCA_MTP_PCAV_TIME_SERIES = "LCA_MTP_PCAV_TIME_SERIES";
    static final String LCA_MIC_TIME_SERIES = "LCA_MIC_TIME_SERIES";
    static final String LCA_MIC = "LCA_MIC";

    private final IEncapsulatedOpenBISService service;

    private final DataSource dataSource;

    private final TimeSeriesDataSetUploaderParameters parameters;
    
    private final TimePointDataDropBoxFeeder dropBoxFeeder;
    
    final DataSetUploaderFactory factory;
    
    private eu.basysbio.cisd.dss.IDataSetUploader uploader;

    DataSetHandler(Properties properties, IEncapsulatedOpenBISService service)
    {
        this(properties, DBUtils.createAndInitDBContext(properties).getDataSource(), service);
    }
    
    DataSetHandler(Properties properties, DataSource dataSource,
            IEncapsulatedOpenBISService service)
    {
        super(FileOperations.getInstance());
        this.dataSource = dataSource;
        this.service = service;
        parameters = new TimeSeriesDataSetUploaderParameters(properties, true);
        dropBoxFeeder = new TimePointDataDropBoxFeeder(properties, this, service);
        factory = new DataSetUploaderFactory();
        factory.register(TIME_SERIES, TimeSeriesDataSetUploader.FACTORY);
        factory.register(LCA_MTP_PCAV_TIME_SERIES, TimeSeriesDataSetUploader.FACTORY_WO_TIME_POINT);
        factory.register(LCA_MTP_TIME_SERIES, TimeSeriesDataSetUploader.FACTORY_WO_TIME_POINT);
        factory.register(LCA_MIC_TIME_SERIES, TimeSeriesDataSetUploader.FACTORY_WO_TIME_POINT);
        factory.register(LCA_MIC, LcaMicDataSetUploader.FACTORY);
    }
    
    @Override
    public IFileOperations getFileOperations()
    {
        return super.getFileOperations();
    }
    
    @Override
    public void addFileForUndo(File file)
    {
        super.addFileForUndo(file);
    }

    public void commit()
    {
        if (uploader != null)
        {
            uploader.commit();
        }
    }

    public void rollback()
    {
        if (uploader != null)
        {
            uploader.rollback();
        }
    }

    public void upload(File dataSet, DataSetInformation dataSetInformation)
            throws EnvironmentFailureException
    {
        handle(dataSet, dataSetInformation, null);
    }

    public Status handle(File originalData, DataSetInformation dataSetInformation, Map<String, String> parameterBindings)
    {
        uploader = createUploader(dataSetInformation);
        uploader.upload(originalData, dataSetInformation, dropBoxFeeder);
        return Status.OK;
    }

    @Private eu.basysbio.cisd.dss.IDataSetUploader createUploader(
            DataSetInformation dataSetInformation)
    {
        return factory.create(dataSetInformation, dataSource, service, parameters);
    }
    
}
