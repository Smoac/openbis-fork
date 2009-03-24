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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.client.Uploader;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.TokenGenerator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class UploadingCommand implements IDataSetCommand
{
    private static final class ProgressListener implements IProgressListener
    {
        private final File zipFile;

        private ProgressListener(File zipFile)
        {
            this.zipFile = zipFile;
        }

        public void warningOccured(String warningMessage)
        {
            operationLog.warn(warningMessage);
        }

        public void start(File file, long fileSize)
        {
            if (notificationLog.isInfoEnabled())
            {
                notificationLog.info("Start uploading of zip file " + file);
            }
        }

        public void reportProgress(int percentage, long numberOfBytes)
        {
        }

        public void finished(boolean successful)
        {
            if (successful)
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Zip file " + zipFile + " has been successfully uploaded.");
                }
            } else
            {
                operationLog.warn("Uploading of zip file " + zipFile + " has been aborted or failed.");
            }
        }

        public void exceptionOccured(Throwable throwable)
        {
            notificationLog.error("An error occured during uploading of zip file " + zipFile + ".", throwable);
        }
    }

    private static final long serialVersionUID = 1L;
    
    private static final Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, UploadingCommand.class);

    private static final Logger notificationLog =
        LogFactory.getLogger(LogCategory.NOTIFY, UploadingCommand.class);
    
    private final ICIFEXRPCServiceFactory cifexServiceFactory;
    private final List<String> dataSetLocations;
    private final String comment;
    private final String userID;
    private final String password;
    private final TokenGenerator tokenGenerator;

    UploadingCommand(ICIFEXRPCServiceFactory cifexServiceFactory, List<String> dataSetLocations,
            DataSetUploadContext context)
    {
        this.cifexServiceFactory = cifexServiceFactory;
        this.dataSetLocations = dataSetLocations;
        this.userID = context.getUserID();
        this.password = context.getPassword();
        this.comment = context.getComment();
        tokenGenerator = new TokenGenerator();
    }

    public void execute(File store)
    {
        File tempFolder = new File(store, "tmp");
        tempFolder.mkdirs();
        String token = tokenGenerator.getNewToken(System.currentTimeMillis());
        final File zipFile = new File(tempFolder, token + ".zip");
        boolean successful = fillZipFile(store, zipFile);
        if (successful)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Zip file " + zipFile + " with " + dataSetLocations.size()
                        + " data sets has been successfully created.");
            }
            ICIFEXRPCService cifexService = cifexServiceFactory.createService();
            String sessionToken = cifexService.login(userID, password);
            Uploader uploader = new Uploader(cifexService, sessionToken);
            uploader.addProgressListener(new ProgressListener(zipFile));
            uploader.upload(Arrays.asList(zipFile), Constants.USER_ID_PREFIX + userID, comment);
        }
    }

    private boolean fillZipFile(File store, File zipFile)
    {
        OutputStream outputStream = null;
        ZipOutputStream zipOutputStream = null;
        try
        {
            outputStream = new FileOutputStream(zipFile);
            zipOutputStream = new ZipOutputStream(outputStream);
            for (String location : dataSetLocations)
            {
                try
                {
                    addTo(zipOutputStream, new File(store, location));
                } catch (IOException ex)
                {
                    notificationLog.error("Couldn't add data set '" + location + "' to zip file.",
                            ex);
                    return false;
                }
            }
            return true;
        } catch (IOException ex)
        {
            notificationLog.error("Couldn't create zip file for uploading", ex);
            return false;
        } finally
        {
            if (zipOutputStream != null)
            {
                try
                {
                    zipOutputStream.close();
                } catch (IOException ex)
                {
                    // ignored
                }
            }
            IOUtils.closeQuietly(outputStream);
        }

    }

    private void addTo(ZipOutputStream zipOutputStream, File file) throws IOException
    {
        if (file.isFile())
        {
            FileInputStream in = null;
            try
            {
                in = new FileInputStream(file);
                zipOutputStream.putNextEntry(new ZipEntry(file.getPath()));
                int len;
                byte[] buffer = new byte[1024];
                while ((len = in.read(buffer)) > 0)
                {
                    zipOutputStream.write(buffer, 0, len);
                }
            } finally
            {
                IOUtils.closeQuietly(in);
                zipOutputStream.closeEntry();
            }
        } else
        {
            File[] files = file.listFiles();
            for (File childFile : files)
            {
                addTo(zipOutputStream, childFile);
            }
        }
    }

}
