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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class StoreShareFileUploadServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("hiding")
    protected static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            StoreShareFileUploadServlet.class);

    public static final String DATA_SET_TYPE_PARAM = "dataSetType";
    public static final String UPLOAD_ID_PARAM = "uploadID";

    @Override
    public final void init(final ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        StoreShareFileUploadRequest uploadRequest =
                new StoreShareFileUploadRequest(request);

        uploadRequest.validate();
        
        String dataSetTypeCodeOrNull = uploadRequest.getDataSetType();
        String uploadId = uploadRequest.getUploadId();

        try
        {
            FileItemIterator iterator = uploadRequest.getFiles();

            String permIdOrNull = null;
            while (iterator.hasNext())
            {
                FileItemStream file = null;
                InputStream stream = null;

                try
                {
                    file = iterator.next();
                    stream = file.openStream();
                    permIdOrNull = putFileToStoreShare(uploadRequest.getSessionId(), file.getName(), dataSetTypeCodeOrNull, permIdOrNull, stream);
                } 
                catch (Exception e) {
                   operationLog.error(e.getMessage());
                }
                finally
                {
                    IOUtils.closeQuietly(stream);
                }
            }

        } catch (FileUploadException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }
    
    private String putFileToStoreShare(String sessionToken, String filePath, String dataSetTypeCodeOrNull, String permIdOrNull, InputStream inputStream) {
        {
            try
            {
                getOpenbisService().checkSession(sessionToken);
                if (filePath.contains("../"))
                {
                    throw new IOExceptionUnchecked("filePath must not contain '../'");
                }
                String uniqueFolderName = permIdOrNull == null? getOpenbisService().createPermId(): permIdOrNull;
                File temporaryDataSetDir =
                        new File(getTemporaryIncomingRoot(dataSetTypeCodeOrNull), uniqueFolderName);
                if(false == temporaryDataSetDir.exists()) {
                    temporaryDataSetDir.mkdir();
                }
                final String subDir = FilenameUtils.getFullPath(filePath);
                final String filename = FilenameUtils.getName(filePath);
                final File dir = new File(temporaryDataSetDir, subDir);
                dir.mkdirs();
                final File file = new File(dir, filename);
                OutputStream ostream = null;
                try
                {
                    ostream = new FileOutputStream(file);
                    long size = IOUtils.copyLarge(inputStream, ostream);
                    ostream.close();
                    return uniqueFolderName;
                } catch (IOException ex)
                {
                    file.delete();
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                } finally
                {
                    IOUtils.closeQuietly(ostream);
                }
            }
            catch (UserFailureException e)
            {
                throw new IllegalArgumentException(e);
            } 
        }
    }
    
    private File getTemporaryIncomingRoot(String dataSetTypeCodeOrNull)
    {
        File storeRoot = ServiceProvider.getConfigProvider().getStoreRoot();;
        String shareId = ServiceProvider.getShareIdManager().getShareId(dataSetTypeCodeOrNull);
        if (false == StringUtils.isBlank(shareId))
        {
            File shareRoot = new File(storeRoot, shareId);
            if (shareRoot.isDirectory())
            {
                File incomingDir = new File(shareRoot, "rpc-incoming");
                incomingDir.mkdir();
                if (incomingDir.isDirectory())
                {
                    return incomingDir;
                }
            }
        }
        return storeRoot;
    }

    private IEncapsulatedOpenBISService getOpenbisService()
    {
        return ServiceProvider.getOpenBISService();
    }
    
    private class StoreShareFileUploadRequest
    {

        private HttpServletRequest request;

        public StoreShareFileUploadRequest(HttpServletRequest request)
        {
            this.request = request;
        }

        public String getSessionId()
        {
            return request.getParameter(Utils.SESSION_ID_PARAM);
        }
        public String getDataSetType()
        {
            return request.getParameter(DATA_SET_TYPE_PARAM);
        }

        public String getUploadId()
        {
            return request.getParameter(UPLOAD_ID_PARAM);
        }

        public FileItemIterator getFiles() throws FileUploadException, IOException
        {
            ServletFileUpload upload = new ServletFileUpload();
            return upload.getItemIterator(request);
        }

        public void validate()
        {
            if (ServletFileUpload.isMultipartContent(request) == false)
            {
                throw new IllegalArgumentException(
                        "The session workspace form upload accepts only multipart requests");
            }
            if (getSessionId() == null)
            {
                throw new IllegalArgumentException(Utils.SESSION_ID_PARAM
                        + " parameter cannot be null");
            }
            
            if (getUploadId() == null)
            {
                throw new IllegalArgumentException(UPLOAD_ID_PARAM + " parameter cannot be null");
            }
        }

    }
}
