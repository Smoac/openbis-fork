/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ProprietaryStorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.RelativeLocationLocatorTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.FullDataSetCreation;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.upload.StoreShareFileUploadServlet;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * @author Ganime Betul Akin
 */
public class UploadAndCreateDataSetsTest extends AbstractFileTest
{
    private static final String SERVICE_URL = TestInstanceHostUtils.getDSSUrl() + "/datastore_server/"
            + "store_share_file_upload";

    protected StoreShareFileUploadServlet servlet;

    protected String sessionToken;

    protected static final String UNIT_TEST_WORKING_DIRECTORY = "unit-test-wd";

    protected static final String TARGETS_DIRECTORY = "targets";

    protected static final File UNIT_TEST_ROOT_DIRECTORY = new File(TARGETS_DIRECTORY
            + File.separator + UNIT_TEST_WORKING_DIRECTORY);

    /**
     * Create a dummy file of size <code>length</code> bytes.
     */
    private File createDummyFile(File dir, String name, int length) throws IOException
    {
        File dummyFile = new File(dir, name);
        dummyFile.createNewFile();
        PrintWriter out = new PrintWriter(dummyFile);
        for (int i = 0; i < length; ++i)
        {
            out.append('a');
        }
        out.flush();
        out.close();

        return dummyFile;
    }

    @Test
    public void testFileUpload()
    {
        HttpClient client = JettyHttpClientFactory.getHttpClient();
        Request requestEntity = client.newRequest(SERVICE_URL).method("POST");

        String sessionToken = gis.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        String uploadId = "1357";
        requestEntity.param("uploadID", uploadId);
        String dataSetType = "UNKNOWN";
        requestEntity.param("dataSetType", dataSetType);
        requestEntity.param("sessionID", sessionToken);

        final String CRLF = "\r\n";
        final String BOUNDARY = "MMMMM___MP_BOUNDARY___MMMMM";
        final String FILE_PART_NAME = "fastaFile";

        File fileToUpload;
        try
        {
            fileToUpload = createDummyFile(workingDirectory, "to-upload.txt", 80);
            String fileContent = FileUtilities.loadToString(fileToUpload);
            ContentProvider content = new StringContentProvider("--" + BOUNDARY + CRLF
                    + "Content-Disposition: form-data; name=\"" + FILE_PART_NAME + "\"; filename=\""
                    + fileToUpload.getName() + "\"" + CRLF
                    + "Content-Type: application/octet-stream" + CRLF + CRLF
                    + fileContent + CRLF + "--" + BOUNDARY + "--" + CRLF);
            requestEntity.content(content, "multipart/form-data; boundary=" + BOUNDARY);

            ContentResponse contentResponse;
            contentResponse = requestEntity.send();
            int statusCode = contentResponse.getStatus();

            if (statusCode != HttpStatus.Code.OK.getCode())
            {
                throw new RuntimeException("Status Code was " + statusCode + " instead of " + HttpStatus.Code.OK.getCode());
            }

            DataSetCreation metadataCreation = new DataSetCreation();
            metadataCreation.setTypeId(new EntityTypePermId(dataSetType));
            metadataCreation.setDataSetKind(DataSetKind.PHYSICAL);;
            metadataCreation.setAutoGeneratedCode(true);
            metadataCreation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
            metadataCreation.setDataStoreId(new DataStorePermId("STANDARD"));
            metadataCreation.setCreationId(new CreationId(uploadId));

            String code = UUID.randomUUID().toString();
            PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
            physicalCreation.setLocation("test/location/" + code);
            physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
            physicalCreation.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
            physicalCreation.setStorageFormatId(new ProprietaryStorageFormatPermId());

            metadataCreation.setPhysicalData(physicalCreation);
            FullDataSetCreation newDataSet = new FullDataSetCreation();

            newDataSet.setMetadataCreation(metadataCreation);
            List<DataSetPermId> createDataSets = dss.createDataSets(sessionToken, Arrays.asList(newDataSet));
        } catch (InterruptedException | TimeoutException | ExecutionException | IOException e)
        {
            e.printStackTrace();
        }
    }

    @BeforeMethod
    public void beforeMethod() throws MalformedURLException
    {
    }
}
