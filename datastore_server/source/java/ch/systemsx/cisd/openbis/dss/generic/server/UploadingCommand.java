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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.client.Uploader;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.utilities.TokenGenerator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class UploadingCommand implements IDataSetCommand
{
    private static final long serialVersionUID = 1L;
    
    private static final Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, UploadingCommand.class);

    private static final Logger notificationLog =
        LogFactory.getLogger(LogCategory.NOTIFY, UploadingCommand.class);
    
    private final class ProgressListener implements IProgressListener
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
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Start uploading of zip file " + file);
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
                operationLog.warn("Uploading of zip file " + zipFile
                        + " has been aborted or failed.");
                sendEMail("Uploading of zip file " + zipFile.getName()
                        + " with requested data sets failed.");
            }
        }

        public void exceptionOccured(Throwable throwable)
        {
            notificationLog.error("An error occured during uploading of zip file " + zipFile + ".",
                    throwable);
        }
    }

    private final ICIFEXRPCServiceFactory cifexServiceFactory;
    private final List<String> dataSetLocations;
    private final String fileName;
    private final String comment;
    private final String userID;
    private final String password;
    private final String userEMail;
    private final MailClientParameters mailClientParameters;
    private final TokenGenerator tokenGenerator;


    UploadingCommand(ICIFEXRPCServiceFactory cifexServiceFactory,
            MailClientParameters mailClientParameters, List<String> dataSetLocations,
            DataSetUploadContext context)
    {
        this.cifexServiceFactory = cifexServiceFactory;
        this.mailClientParameters = mailClientParameters;
        this.dataSetLocations = dataSetLocations;
        this.userID = context.getUserID();
        this.password = context.getPassword();
        fileName = context.getFileName();
        userEMail = context.getUserEMail();
        this.comment = context.getComment();
        tokenGenerator = new TokenGenerator();
    }

    public void execute(File store)
    {
        File tempFolder = new File(store, "tmp");
        tempFolder.mkdirs();
        final File zipFile = new File(tempFolder, createFileName());
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
        } else
        {
            sendEMail("Couldn't create zip file " + zipFile.getName() + " with requested data sets");
        }
        zipFile.delete();
    }

    private String createFileName()
    {
        if (StringUtils.isBlank(fileName))
        {
            return tokenGenerator.getNewToken(System.currentTimeMillis()) + ".zip";
        }
        return fileName.toLowerCase().endsWith(".zip") ? fileName : fileName + ".zip"; 
    }

    private boolean fillZipFile(File store, File zipFile)
    {
        OutputStream outputStream = null;
        ZipOutputStream zipOutputStream = null;
        try
        {
            String rootPath = store.getCanonicalPath();
            outputStream = new FileOutputStream(zipFile);
            zipOutputStream = new ZipOutputStream(outputStream);
            for (String location : dataSetLocations)
            {
                File dataSetFile = new File(store, location);
                if (dataSetFile.exists() == false)
                {
                    notificationLog.error("Data set '" + location + "' does not exist.");
                    return false;
                }
                try
                {
                    addTo(zipOutputStream, rootPath, dataSetFile);
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
                    notificationLog.error("Couldn't close zip file", ex);
                }
            }
        }
    }

    private void addTo(ZipOutputStream zipOutputStream, String rootPath, File file) throws IOException
    {
        if (file.isFile())
        {
            FileInputStream in = null;
            try
            {
                in = new FileInputStream(file);
                String path = file.getCanonicalPath().substring(rootPath.length() + 1);
                ZipEntry zipEntry = new ZipEntry(path);
                zipEntry.setTime(file.lastModified());
                zipEntry.setMethod(ZipEntry.DEFLATED);
                zipOutputStream.putNextEntry(zipEntry);
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
                addTo(zipOutputStream, rootPath, childFile);
            }
        }
    }

    private void sendEMail(String message)
    {
        String from = mailClientParameters.getFrom();
        String smtpHost = mailClientParameters.getSmtpHost();
        String smtpUser = mailClientParameters.getSmtpUser();
        String smtpPassword = mailClientParameters.getSmtpPassword();
        IMailClient mailClient = new MailClient(from, smtpHost, smtpUser, smtpPassword);
        mailClient.sendMessage("[Data Set Server] Uploading failed", message, null, userEMail);
    }

}
