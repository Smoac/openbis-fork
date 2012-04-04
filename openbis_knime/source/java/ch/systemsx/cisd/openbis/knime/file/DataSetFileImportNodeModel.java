/*
 * Copyright 2011 ETH Zuerich, CISD
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
import java.util.Arrays;

import javax.activation.MimetypesFileTypeMap;

import org.knime.core.data.url.MIMEType;
import org.knime.core.data.url.URIContent;
import org.knime.core.data.url.port.MIMEURIPortObject;
import org.knime.core.data.url.port.MIMEURIPortObjectSpec;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeModel;

/**
 * Node model for importing a file/folder from Data Store Server.
 *
 * @author Franz-Josef Elmer
 */
public class DataSetFileImportNodeModel extends AbstractOpenBisNodeModel
{
    static final String DATA_SET_CODE_KEY = "data-set-code";
    static final String FILE_PATH_KEY = "file-path";
    static final String DOWNLOADS_PATH_KEY = "downloads-path";
    
    private String dataSetCode = "";
    private String filePath = "";
    private String dowloadsPath = "";

    public DataSetFileImportNodeModel()
    {
        super(new PortType[]{}, new PortType[]{new PortType(MIMEURIPortObject.class)});
    }

    @Override
    protected void loadAdditionalValidatedSettingsFrom(NodeSettingsRO settings)
            throws InvalidSettingsException
    {
        dataSetCode = settings.getString(DATA_SET_CODE_KEY);
        filePath = settings.getString(FILE_PATH_KEY);
        dowloadsPath = settings.getString(DOWNLOADS_PATH_KEY);
    }

    @Override
    protected void saveAdditionalSettingsTo(NodeSettingsWO settings)
    {
        settings.addString(DATA_SET_CODE_KEY, dataSetCode);
        settings.addString(FILE_PATH_KEY, filePath);
        settings.addString(DOWNLOADS_PATH_KEY, dowloadsPath);
    }

    @Override
    protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException
    {
        MIMEType type = createType();
        return new PortObjectSpec[]
            { new MIMEURIPortObjectSpec(type) };
    }
    
    @Override
    protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception
    {
        InputStream in = null;
        OutputStream out = null;
        try
        {
            DataSet dataSet = DataSetUtil.getDataSetProxy(url, userID, password, dataSetCode);
            in = dataSet.getFile(filePath);
            File file = new File(dowloadsPath, dataSetCode + "/" + filePath);
            file.getParentFile().mkdirs();
            out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int size = 0;
            while ((size = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, size);
            }
            MIMEType type = createType();
            logger.info("Content MIME type: " + type);
            return new PortObject[]
                { new MIMEURIPortObject(Arrays.asList(new URIContent(file.toURI())), type) };
        } finally
        {
            if (out != null)
            {
                out.close();
            }
            if (in != null)
            {
                in.close();
            }
        }
    }
    
    private MIMEType createType()
    {
        String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filePath);
        return MIMEType.getType(contentType);
    }

}
