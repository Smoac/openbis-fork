/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.harvester.synchronizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownload;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadReader;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.systemsx.cisd.common.parser.MemorySizeFormatter;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetUpdatable;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IngestionService;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

class DataSetRegistrationIngestionService extends IngestionService<DataSetInformation>
{
    private static final long serialVersionUID = 1L;

    private List<String> dataSetCodes;

    private final NewExternalData dataSet;

    private final String loginUser;

    private final String loginPass;

    private final String asUrl;

    private final String dssUrl;

    private final String harvesterTempDir;

    private final int SUCCESS = 1;

    private final int FAILURE = 0;

    private final Logger log;

    public DataSetRegistrationIngestionService(Properties properties, File storeRoot, List<String> dataSetCodes, NewExternalData ds,
            Logger operationLog)
    {
        super(properties, storeRoot);
        this.dataSetCodes = dataSetCodes;
        this.dataSet = ds;
        this.loginUser = properties.getProperty("user");
        this.loginPass = properties.getProperty("pass");
        this.asUrl = properties.getProperty("as-url");
        this.dssUrl = properties.getProperty("dss-url");
        this.harvesterTempDir = properties.getProperty("harvester-temp-dir");
        this.log = operationLog;
    }

    @Override
    protected TableModel process(IDataSetRegistrationTransactionV2 transaction, Map<String, Object> parameters, DataSetProcessingContext context)
    {
        IDataSetUpdatable dataSetForUpdate = transaction.getDataSetForUpdate(dataSet.getCode());
        ISampleImmutable sample = null;

        if (dataSet.getSampleIdentifierOrNull() != null)
        {
            sample = transaction.getSampleForUpdate(dataSet.getSampleIdentifierOrNull().toString());
        }
        IExperimentImmutable experiment = null;
        if (dataSet.getExperimentIdentifierOrNull() != null)
        {
            experiment = transaction.getExperimentForUpdate(dataSet.getExperimentIdentifierOrNull().toString());
        }

        List<NewProperty> dataSetProperties = dataSet.getDataSetProperties();

        if (dataSetForUpdate == null)
        {
            String dataSetCode = dataSet.getCode();
            File storeRoot = transaction.getGlobalState().getStoreRootDir();
            File temp = new File(storeRoot, this.harvesterTempDir);
            temp.mkdirs();
            File dir = new File(temp, dataSetCode);
            dir.mkdirs();

            int status = downloadDataSetFiles(dir, dataSetCode);
            if (status == SUCCESS)
            {
                // REGISTER NEW DATA SET
                IDataSet ds = transaction.createNewDataSet(dataSet.getDataSetType().getCode(), dataSet.getCode());
                dataSetCodes.add(dataSetCode);
                ds.setSample(sample);
                ds.setExperiment(experiment);
                ds.setParentDatasets(dataSet.getParentDataSetCodes());
                for (NewProperty newProperty : dataSetProperties)
                {
                    ds.setPropertyValue(newProperty.getPropertyCode(), newProperty.getValue());
                }

                for (File f : dir.listFiles())
                {
                    transaction.moveFile(f.getAbsolutePath(), ds);
                }
            }
            else
            {
                log.error("Data set with code :" + dataSetCode
                        + " could not be synced because one or more files could not be downloaded correctly");
            }
        }
        else
        {
            // UPDATE data set meta data excluding the container/contained relationships
            dataSetForUpdate.setSample(sample);
            dataSetForUpdate.setExperiment(experiment);
            dataSetForUpdate.setParentDatasets(dataSet.getParentDataSetCodes());
            for (NewProperty newProperty : dataSetProperties)
            {
                dataSetForUpdate.setPropertyValue(newProperty.getPropertyCode(), newProperty.getValue());
            }
        }
        return null;
    }

    class FileDetails
    {
        final int crc32checksum;

        final long fileLength;

        public FileDetails(int crc32checksum, long fileLength)
        {
            super();
            this.crc32checksum = crc32checksum;
            this.fileLength = fileLength;
        }

        public int getCrc32checksum()
        {
            return crc32checksum;
        }

        public long getFileLength()
        {
            return fileLength;
        }
    }

    private int downloadDataSetFiles(File dir, String dataSetCode)
    {
        DSSFileUtils dssFileUtils = DSSFileUtils.create(asUrl, dssUrl);
        String sessionToken = dssFileUtils.login(loginUser, loginPass);
        SearchResult<DataSetFile> result = dssFileUtils.searchWithDataSetCode(sessionToken, dataSetCode, new DataSetFileFetchOptions());
        List<DataSetFile> files = result.getObjects();

        List<IDataSetFileId> fileIds = new LinkedList<IDataSetFileId>();
        Map<DataSetFilePermId, FileDetails> fileDetailsMap = new HashMap<DataSetFilePermId, FileDetails>();
        for (DataSetFile f : files)
        {
            fileIds.add(f.getPermId());
            fileDetailsMap.put(f.getPermId(), new FileDetails(f.getChecksumCRC32(), f.getFileLength()));
        }
        // Download the files & print the contents
        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
        options.setRecursive(false);
        InputStream stream = dssFileUtils.downloadFiles(sessionToken, fileIds, options);
        DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);
        DataSetFileDownload fileDownload = null;
        while ((fileDownload = reader.read()) != null)
        {
            DataSetFile orgFile = fileDownload.getDataSetFile();
            if (orgFile.getPath().equals(""))
                continue;
            // if (dsFile.getPath().equals("original"))
            // continue;
            String filePath = orgFile.getPath();// .substring("original/".length());
            File output = new File(dir.getAbsolutePath(), filePath);
            if (orgFile.isDirectory())
            {
                output.mkdirs();
            }
            else
            {
                DataSetFilePermId filePermId = orgFile.getPermId();
                FileDetails fileDetails = fileDetailsMap.get(filePermId);

                System.out.println("Downloaded " + orgFile.getPath() + " "
                        + MemorySizeFormatter.format(orgFile.getFileLength()));

                Path path = Paths.get(dir.getAbsolutePath(), filePath);
                try
                {
                    ChecksummmingInputStream cis = new ChecksummmingInputStream(fileDownload.getInputStream());
                    Files.copy(cis, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    if (cis.checksum() != fileDetails.getCrc32checksum()
                            || cis.getLength() != fileDetails.getFileLength())
                    {
                        log.error("Crc32 or file length does not match for " + orgFile.getPath());
                        return FAILURE;
                    }
                } catch (IOException e)
                {
                    log.error(e.getMessage());
                    return FAILURE;
                }
            }
        }
        return SUCCESS;
    }
}