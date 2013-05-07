/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.IOUtils;
import org.knime.core.data.uri.URIContent;
import org.knime.core.data.uri.URIPortObject;
import org.knime.core.data.uri.URIPortObjectSpec;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataStore;
import ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeModel;
import ch.systemsx.cisd.openbis.knime.common.ParameterBindings;
import ch.systemsx.cisd.openbis.knime.common.Util;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.FacadeFactory;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * Model which requests a data file created by an aggregation service.
 *
 * @author Franz-Josef Elmer
 */
public class AggregatedDataFileImportNodeModel extends AbstractOpenBisNodeModel
{
    static final String AGGREGATION_DESCRIPTION_KEY = "aggregation-description";

    public AggregatedDataFileImportNodeModel()
    {
        super(new PortType[] {}, new PortType[] { new PortType(URIPortObject.class) });
    }

    private AggregatedDataFileImportDescription description;
    private ParameterBindings parameterBindings = new ParameterBindings();

    @Override
    protected void loadAdditionalValidatedSettingsFrom(NodeSettingsRO settings)
            throws InvalidSettingsException
    {
        description = Util.deserializeDescription(
                settings.getByteArray(AGGREGATION_DESCRIPTION_KEY));
        parameterBindings.loadValidatedSettingsFrom(settings);
    }

    @Override
    protected void saveAdditionalSettingsTo(NodeSettingsWO settings)
    {
        settings.addByteArray(AGGREGATION_DESCRIPTION_KEY, Util.serializeDescription(description));
        parameterBindings.saveSettingsTo(settings);
    }

    @Override
    protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException
    {
        return new PortObjectSpec[] { new URIPortObjectSpec(createType("")) };
    }

    @Override
    protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception
    {
        IQueryApiFacade facade = FacadeFactory.create(url, userID, password);
        try
        {
            Map<String, Object> serviceParameters = new HashMap<String, Object>();
            serviceParameters.put("requestKey", "execute");
            for (Entry<String, String> entry : parameterBindings.getBindings().entrySet())
            {
                serviceParameters.put(entry.getKey(), entry.getValue());
            }
            QueryTableModel report =
                    facade.createReportFromAggregationService(
                            description.getAggregationServiceDescription(), serviceParameters);
            String fileName = (String) report.getRows().get(0)[0];
            String downloadUrl = getDataStore(facade).getDownloadUrl();
            File file = download(facade.getSessionToken(), fileName, downloadUrl);
            String type = createType(fileName);
            logger.info("Content MIME type: " + type);
            return new PortObject[]
                { new URIPortObject(new URIPortObjectSpec(type), Arrays.asList(new URIContent(file
                        .toURI(), type))) };
        } finally
        {
            facade.logout();
        }
    }
   
    private File download(String sessionToken, String fileName, String baseURL)
    {
        File systemTempDir = new File(System.getProperty("java.io.tmpdir"));
        File tempDir = new File(systemTempDir, "knime-openbis-" + sessionToken);
        tempDir.mkdirs();
        tempDir.deleteOnExit();
        File file = new File(tempDir, fileName);
        file.deleteOnExit();
        InputStream in = null;
        OutputStream out = null;
        try
        {
            in = new URL(baseURL + "/session_workspace_file_download?sessionID=" + sessionToken
                    + "&filePath=" + fileName).openStream();
            file.getParentFile().mkdirs();
            out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int size = 0;
            while ((size = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, size);
            }
            return file;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }
    
    private DataStore getDataStore(IQueryApiFacade facade)
    {
        String dataStoreCode = description.getAggregationServiceDescription().getDataStoreCode();
        String sessionToken = facade.getSessionToken();
        IGeneralInformationService service = facade.getGeneralInformationService();
        List<DataStore> dataStores = service.listDataStores(sessionToken);
        for (DataStore dataStore : dataStores)
        {
            if (dataStore.getCode().equals(dataStoreCode))
            {
                return dataStore;
            }
        }
        throw new IllegalArgumentException("Unknown data store: " + dataStoreCode);
    }

    private String createType(String fileName)
    {
        return MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(fileName);
    }
}
