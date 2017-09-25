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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.parallelizedExecutor;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownload;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadReader;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util.DSPropertyUtils;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util.V3Utils;
import ch.systemsx.cisd.common.io.IOUtilities;
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
import ch.systemsx.cisd.openbis.generic.shared.util.IRowBuilder;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

class DataSetRegistrationIngestionService extends IngestionService<DataSetInformation>
{
    private static final long serialVersionUID = 1L;
 
    private final NewExternalData dataSet;

    private final String loginUser;

    private final String loginPass;

    private final String asUrl;

    private final String dssUrl;

    private final String harvesterTempDir;

    public DataSetRegistrationIngestionService(Properties properties, File storeRoot, NewExternalData ds,
            Logger operationLog)
    {
        super(properties, storeRoot);
        this.dataSet = ds;
        this.loginUser = properties.getProperty("user");
        this.loginPass = properties.getProperty("pass");
        this.asUrl = properties.getProperty("as-url");
        this.dssUrl = properties.getProperty("dss-url");
        this.harvesterTempDir = properties.getProperty("harvester-temp-dir");
    }

    @Override
    protected TableModel process(IDataSetRegistrationTransactionV2 transaction, Map<String, Object> parameters, DataSetProcessingContext context)
    {
        String dataSetCode = dataSet.getCode();
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

        IDataSetUpdatable dataSetForUpdate = transaction.getDataSetForUpdate(dataSetCode);
        if (dataSetForUpdate == null)
        {
            // REGISTER NEW DATA SET after downloading the data set files
            File storeRoot = transaction.getGlobalState().getStoreRootDir();
            File temp = new File(storeRoot, this.harvesterTempDir);
            temp.mkdirs();
            File dir = new File(temp, dataSetCode);
            dir.mkdirs();

            try
            {
                downloadDataSetFiles(dir, dataSetCode);
            } catch (Exception e)
            {
                return errorTableModel(parameters, e);
            }

            IDataSet ds = transaction.createNewDataSet(dataSet.getDataSetType().getCode(), dataSet.getCode());
            ds.setDataSetKind(dataSet.getDataSetKind());
            ds.setSample(sample);
            ds.setExperiment(experiment);
            for (NewProperty newProperty : dataSetProperties)
            {
                ds.setPropertyValue(newProperty.getPropertyCode(), newProperty.getValue());
            }

            for (File f : dir.listFiles())
            {
                transaction.moveFile(f.getAbsolutePath(), ds);
            }
            return summaryTableModel(parameters, "Added");
        }
        else
        {
            // UPDATE data set meta data excluding the container/contained relationships
            dataSetForUpdate.setSample(sample);
            dataSetForUpdate.setExperiment(experiment);
            dataSetForUpdate.setParentDatasets(dataSet.getParentDataSetCodes());

            // synchronize property changes including properties that were set to empty values
            List<String> existingPropertyCodes = dataSetForUpdate.getAllPropertyCodes();
            Set<String> newPropertyCodes = DSPropertyUtils.extractPropertyNames(dataSetProperties);
            for (NewProperty newProperty : dataSetProperties)
            {
                dataSetForUpdate.setPropertyValue(newProperty.getPropertyCode(), newProperty.getValue());
            }
            // set the properties that are in the harvester but not in the data source anymore, to ""
            existingPropertyCodes.removeAll(newPropertyCodes);
            for (String propCode : existingPropertyCodes)
            {
                dataSetForUpdate.setPropertyValue(propCode, "");
            }
            return summaryTableModel(parameters, "Updated");
        }
    }

    private TableModel summaryTableModel(Map<String, Object> parameters, String summary)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader("Parameters");
        builder.addHeader(summary);
        IRowBuilder row = builder.addRow();
        row.setCell("Parameters", parameters.toString());
        return builder.getTableModel();
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

    private void downloadDataSetFiles(File dir, String dataSetCode) throws Exception
    {
        V3Utils dssFileUtils = V3Utils.create(asUrl, dssUrl);
        String sessionToken = dssFileUtils.login(loginUser, loginPass);
        DataSetFileFetchOptions dsFileFetchOptions = new DataSetFileFetchOptions();
        SearchResult<DataSetFile> result = dssFileUtils.searchWithDataSetCode(sessionToken, dataSetCode, dsFileFetchOptions);
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

                // System.out.println("Downloaded " + orgFile.getPath() + " "
                // + MemorySizeFormatter.format(orgFile.getFileLength()));

                Path path = Paths.get(dir.getAbsolutePath(), filePath);
                InputStream inputStream = fileDownload.getInputStream();
                OutputStream outputStream = Files.newOutputStream(path);
                int checksumCRC32 = IOUtilities.copyAndGetChecksumCRC32(inputStream, outputStream);
                File copiedFile = new File(path.normalize().toString());
                if (checksumCRC32 != fileDetails.getCrc32checksum()
                        || copiedFile.length() != fileDetails.getFileLength())
                {
                    throw new RuntimeException("Crc32 or file length does not match for  " + orgFile.getPath() + " calculated:" + checksumCRC32
                            + " expected:"
                            + fileDetails.getCrc32checksum());
                }
            }
        }
    }
}