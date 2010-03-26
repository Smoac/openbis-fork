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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXUploader;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.common.utilities.TokenGenerator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;

/**
 * A command which zips the given data sets and uploads the ZIP file to CIFEX.
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

        public void start(File file, long fileSize, Long fileIdOrNull)
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

        public void warningOccured(String warningMessage)
        {
            operationLog.warn(warningMessage);
        }

    }

    private static final class MetaDataBuilder
    {
        private static final String DATA_SET = "data_set";

        private static final String SAMPLE = "sample";

        private static final String EXPERIMENT = "experiment";

        private static final char DELIM = '\t';

        private static final DateFormat DATE_FORMAT_PATTERN =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

        private final StringBuilder builder = new StringBuilder();

        void dataSetProperties(List<IEntityProperty> properties)
        {
            addProperties(DATA_SET, properties);
        }

        void sampleProperties(List<IEntityProperty> properties)
        {
            addProperties(SAMPLE, properties);
        }

        void experimentProperties(List<IEntityProperty> properties)
        {
            addProperties(EXPERIMENT, properties);
        }

        void addProperties(String category, List<IEntityProperty> properties)
        {
            for (IEntityProperty property : properties)
            {
                addRow(category, property.getPropertyType().getCode(), property.tryGetAsString());
            }
        }

        void dataSet(String key, String value)
        {
            addRow(DATA_SET, key, value);
        }

        void dataSet(String key, Date date)
        {
            addRow(DATA_SET, key, date);
        }

        void dataSet(String key, boolean flag)
        {
            addRow(DATA_SET, key, flag);
        }

        void sample(String key, String value)
        {
            addRow(SAMPLE, key, value);
        }

        void sample(String key, Person person)
        {
            addRow(SAMPLE, key, person);
        }

        void sample(String key, Date date)
        {
            addRow(SAMPLE, key, date);
        }

        void experiment(String key, String value)
        {
            addRow(EXPERIMENT, key, value);
        }

        void experiment(String key, Person person)
        {
            addRow(EXPERIMENT, key, person);
        }

        void experiment(String key, Date date)
        {
            addRow(EXPERIMENT, key, date);
        }

        private void addRow(String category, String key, Person person)
        {
            StringBuilder stringBuilder = new StringBuilder();
            if (person != null)
            {
                String firstName = person.getFirstName();
                String lastName = person.getLastName();
                if (firstName != null && lastName != null)
                {
                    stringBuilder.append(firstName).append(' ').append(lastName);
                } else
                {
                    stringBuilder.append(person.getUserId());
                }
                String email = person.getEmail();
                if (email != null)
                {
                    stringBuilder.append(" <").append(email).append(">");
                }
            }
            addRow(category, key, stringBuilder.toString());
        }

        private void addRow(String category, String key, Date date)
        {
            addRow(category, key, date == null ? null : DATE_FORMAT_PATTERN.format(date));
        }

        private void addRow(String category, String key, boolean flag)
        {
            addRow(category, key, Boolean.valueOf(flag).toString().toUpperCase());
        }

        private void addRow(String category, String key, String value)
        {
            builder.append(category).append(DELIM).append(key).append(DELIM);
            builder.append(value == null ? "" : value).append('\n');
        }

        @Override
        public String toString()
        {
            return builder.toString();
        }
    }

    private final ICIFEXRPCServiceFactory cifexServiceFactory;

    private final List<ExternalData> dataSets;

    private final String fileName;

    private final String comment;

    private final String userID;

    private final String password;

    private final String userEMail;

    private final boolean userAuthenticated;

    private final String cifexAdminUserOrNull;

    private final String cifexAdminPasswordOrNull;

    private final MailClientParameters mailClientParameters;

    private final TokenGenerator tokenGenerator;

    @Private
    boolean deleteAfterUploading = true;

    UploadingCommand(ICIFEXRPCServiceFactory cifexServiceFactory,
            MailClientParameters mailClientParameters, List<ExternalData> dataSets,
            DataSetUploadContext context, String cifexAdminUserOrNull,
            String cifexAdminPasswordOrNull)
    {
        this.cifexServiceFactory = cifexServiceFactory;
        this.mailClientParameters = mailClientParameters;
        this.dataSets = dataSets;
        this.userID = context.getUserID();
        this.password = context.getPassword();
        this.userAuthenticated = context.isUserAuthenticated();
        this.cifexAdminUserOrNull = cifexAdminUserOrNull;
        this.cifexAdminPasswordOrNull = cifexAdminPasswordOrNull;
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
                operationLog.info("Zip file " + zipFile + " with " + dataSets.size()
                        + " data sets has been successfully created.");
            }
            ICIFEXComponent cifex = cifexServiceFactory.createCIFEXComponent();
            String sessionToken = getCIFEXSession(cifex);
            ICIFEXUploader uploader = cifex.createUploader(sessionToken);
            uploader.addProgressListener(new ProgressListener(zipFile));
            uploader.upload(Arrays.asList(zipFile), Constants.USER_ID_PREFIX + userID, comment);
        } else
        {
            sendEMail("Couldn't create zip file " + zipFile.getName() + " with requested data sets");
        }
        if (deleteAfterUploading)
        {
            zipFile.delete();
        }
    }

    private String getCIFEXSession(ICIFEXComponent cifex)
    {
        return getCIFEXSession(cifex, userAuthenticated, userID, password, cifexAdminUserOrNull,
                cifexAdminPasswordOrNull);
    }

    private static String getCIFEXSession(ICIFEXComponent cifex, boolean userAuthenticated,
            String userID, String password, String cifexAdminUserOrNull,
            String cifexAdminPasswordOrNull)
    {
        if (userAuthenticated && StringUtils.isBlank(password)
                && StringUtils.isNotBlank(cifexAdminUserOrNull)
                && StringUtils.isNotBlank(cifexAdminPasswordOrNull))
        {
            final String token = cifex.login(cifexAdminUserOrNull, cifexAdminPasswordOrNull);
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format(
                        "Calling setSessionUser() on CIFEX session to userID=%s", userID));
            }
            cifex.setSessionUser(token, userID);
            return token;
        } else
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("Directly logging into CIFEX as userID=%s "
                        + "(user authenticated=%s, password provided=%s", userID,
                        userAuthenticated, StringUtils.isNotBlank(password)));
            }
            return cifex.login(userID, password);
        }
    }

    static boolean canLoginToCIFEX(ICIFEXComponent cifex, boolean userAuthenticated, String userID,
            String password, String cifexAdminUserOrNull, String cifexAdminPasswordOrNull)
    {
        final String tokenOrNull =
                getCIFEXSession(cifex, userAuthenticated, userID, password, cifexAdminUserOrNull,
                        cifexAdminPasswordOrNull);
        if (tokenOrNull != null)
        {
            cifex.logout(tokenOrNull);
            return true;
        } else
        {
            return false;
        }
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
            outputStream = new FileOutputStream(zipFile);
            zipOutputStream = new ZipOutputStream(outputStream);
            for (ExternalData dataSet : dataSets)
            {
                String location = dataSet.getLocation();
                File dataSetFile = new File(store, location);
                if (dataSetFile.exists() == false)
                {
                    notificationLog.error("Data set '" + location + "' does not exist.");
                    return false;
                }
                String newRootPath = createRootPath(dataSet);
                try
                {
                    addEntry(zipOutputStream, newRootPath + "/meta-data.tsv", System
                            .currentTimeMillis(), new ByteArrayInputStream(createMetaData(dataSet)
                            .getBytes()));
                } catch (IOException ex)
                {
                    notificationLog.error("Couldn't add meta date for data set '"
                            + dataSet.getCode() + "' to zip file.", ex);
                    return false;
                }
                try
                {
                    addTo(zipOutputStream, dataSetFile.getCanonicalPath().length(), newRootPath,
                            dataSetFile);
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

    private String createMetaData(ExternalData dataSet)
    {
        MetaDataBuilder builder = new MetaDataBuilder();
        builder.dataSet("code", dataSet.getCode());
        builder.dataSet("production_timestamp", dataSet.getProductionDate());
        builder.dataSet("producer_code", dataSet.getDataProducerCode());
        builder.dataSet("data_set_type", dataSet.getDataSetType().getCode());
        builder.dataSet("is_measured", dataSet.isDerived() == false);
        builder.dataSet("is_complete", BooleanOrUnknown.T.equals(dataSet.getComplete()));
        builder.dataSetProperties(dataSet.getProperties());

        StringBuilder stringBuilder = new StringBuilder();
        Collection<ExternalData> parents = dataSet.getParents();
        if (parents.isEmpty() == false)
        {
            for (ExternalData parent : parents)
            {
                if (stringBuilder.length() > 0)
                {
                    stringBuilder.append(',');
                }
                stringBuilder.append(parent.getCode());
            }
        }
        builder.dataSet("parent_codes", stringBuilder.toString());
        Sample sample = dataSet.getSample();
        if (sample != null)
        {
            builder.sample("type_code", sample.getSampleType().getCode());
            builder.sample("code", sample.getCode());
            Space space = sample.getSpace();
            builder.sample("space_code", space == null ? "(shared)" : space.getCode());
            // group->space
            builder.sample("registration_timestamp", sample.getRegistrationDate());
            builder.sample("registrator", sample.getRegistrator());
            builder.sampleProperties(sample.getProperties());
        }
        Experiment experiment = dataSet.getExperiment();
        Project project = experiment.getProject();
        builder.experiment("space_code", project.getSpace().getCode());
        builder.experiment("project_code", project.getCode());
        builder.experiment("experiment_code", experiment.getCode());
        builder.experiment("experiment_type_code", experiment.getExperimentType().getCode());
        builder.experiment("registration_timestamp", experiment.getRegistrationDate());
        builder.experiment("registrator", experiment.getRegistrator());
        builder.experimentProperties(experiment.getProperties());
        return builder.toString();
    }

    private String createRootPath(ExternalData dataSet)
    {
        Sample sample = dataSet.getSample();
        Experiment experiment = dataSet.getExperiment();
        Project project = experiment.getProject();
        return project.getSpace().getCode() + "/" + project.getCode() + "/" + experiment.getCode()
                + "/" + (sample == null ? "" : sample.getCode() + "/") + dataSet.getCode();
    }

    private void addTo(ZipOutputStream zipOutputStream, int oldRootPathLength, String newRootPath,
            File file) throws IOException
    {
        if (file.isFile())
        {
            String zipEntryPath =
                    newRootPath + file.getCanonicalPath().substring(oldRootPathLength);
            addEntry(zipOutputStream, zipEntryPath, file.lastModified(), new FileInputStream(file));
        } else
        {
            File[] files = file.listFiles();
            for (File childFile : files)
            {
                addTo(zipOutputStream, oldRootPathLength, newRootPath, childFile);
            }
        }
    }

    private void addEntry(ZipOutputStream zipOutputStream, String zipEntryPath, long lastModified,
            InputStream in) throws IOException
    {
        try
        {
            ZipEntry zipEntry = new ZipEntry(zipEntryPath.replace('\\', '/'));
            zipEntry.setTime(lastModified);
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
    }

    private void sendEMail(String message)
    {
        String from = mailClientParameters.getFrom();
        String smtpHost = mailClientParameters.getSmtpHost();
        String smtpUser = mailClientParameters.getSmtpUser();
        String smtpPassword = mailClientParameters.getSmtpPassword();
        IMailClient mailClient = new MailClient(from, smtpHost, smtpUser, smtpPassword);
        mailClient
                .sendMessage("[Data Set Server] Uploading failed", message, null, null, userEMail);
    }

    public String getDescription()
    {
        final StringBuilder b = new StringBuilder();
        b.append("Upload data sets to CIFEX: ");
        for (ExternalData dataset : dataSets)
        {
            b.append(dataset.getCode());
            b.append(',');
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }

}
