/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.IStorageProcessor.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

public abstract class TransferredDataSetHandlerDataSetRegistrationAlgorithm
{
    @Private
    public static final String EMAIL_SUBJECT_TEMPLATE = "Success: data set for experiment '%s";

    @Private
    static final String DATA_SET_REGISTRATION_FAILURE_TEMPLATE =
            "Registration of data set '%s' failed.";

    @Private
    static final String DATA_SET_STORAGE_FAILURE_TEMPLATE = "Storing data set '%s' failed.";

    @Private
    static final String SUCCESSFULLY_REGISTERED = "Successfully registered data set: [";

    protected final IDelegatedActionWithResult<Boolean> cleanAftrewardsAction;

    protected final File incomingDataSetFile;

    protected final DataSetInformation dataSetInformation;

    protected final IDataStoreStrategy dataStoreStrategy;

    protected final DataSetType dataSetType;

    protected final File storeRoot;

    private final IPreRegistrationAction preRegistrationAction;

    private final IPostRegistrationAction postRegistrationAction;

    protected BaseDirectoryHolder baseDirectoryHolder;

    protected String errorMessageTemplate;

    public TransferredDataSetHandlerDataSetRegistrationAlgorithm(File incomingDataSetFile,
            IDelegatedActionWithResult<Boolean> cleanAftrewardsAction,
            IPreRegistrationAction preRegistrationAction,
            IPostRegistrationAction postRegistrationAction)
    {
        this.preRegistrationAction = preRegistrationAction;
        this.postRegistrationAction = postRegistrationAction;
        this.errorMessageTemplate = TransferredDataSetHandlerDataSetRegistrationAlgorithm.DATA_SET_STORAGE_FAILURE_TEMPLATE;
        this.incomingDataSetFile = incomingDataSetFile;
        this.cleanAftrewardsAction = cleanAftrewardsAction;
        this.dataSetInformation = extractDataSetInformation(incomingDataSetFile);
        if (dataSetInformation.getDataSetCode() == null)
        {
            // Extractor didn't extract an externally generated data set code, so request one
            // from the openBIS server.
            dataSetInformation.setDataSetCode(getOpenBisService().createDataSetCode());
        }
        this.dataStoreStrategy =
                getDataStrategyStore()
                        .getDataStoreStrategy(dataSetInformation, incomingDataSetFile);
        this.dataSetType = getTypeExtractor().getDataSetType(incomingDataSetFile);
        dataSetInformation.setDataSetType(dataSetType);
        this.storeRoot = getStorageProcessor().getStoreRootDirectory();
    }

    /**
     * Return the data set information.
     */
    public DataSetInformation getDataSetInformation()
    {
        return dataSetInformation;
    }

    /**
     * Prepare registration of a data set.
     */
    public final void prepare()
    {
        final File baseDirectory =
                createBaseDirectory(dataStoreStrategy, storeRoot, dataSetInformation);
        baseDirectoryHolder =
                new BaseDirectoryHolder(dataStoreStrategy, baseDirectory, incomingDataSetFile);
    }

    public final boolean hasDataSetBeenIdentified()
    {
        return dataStoreStrategy.getKey() == DataStoreStrategyKey.IDENTIFIED;
    }

    /**
     * Register the data set. This method is only ever called for identified data sets.
     */
    public final List<DataSetInformation> registerDataSet()
    {
        String processorID = getTypeExtractor().getProcessorType(incomingDataSetFile);
        try
        {
            getDataSetValidator().assertValidDataSet(dataSetType, incomingDataSetFile);
            registerDataSetAndInitiateProcessing(processorID);
            logAndNotifySuccessfulRegistration(getEmail());
            if (getFileOperations().exists(incomingDataSetFile)
                    && removeAndLog("clean up failed") == false)
            {
                getOperationLog().error(
                        "Cannot delete '" + incomingDataSetFile.getAbsolutePath() + "'.");
            }

            clean();
            return Collections.singletonList(dataSetInformation);
        } catch (final HighLevelException ex)
        {
            final String userEmailOrNull = dataSetInformation.tryGetUploadingUserEmail();
            boolean deleted = false;
            if (userEmailOrNull != null)
            {
                final String errorMessage =
                        "Error when trying to register data set '" + incomingDataSetFile.getName()
                                + "'.";
                getMailClient()
                        .sendMessage(
                                String.format(errorMessage, dataSetInformation
                                        .getExperimentIdentifier().getExperimentCode()),
                                ex.getMessage(), null, null, userEmailOrNull);
                if (shouldDeleteUnidentified())
                {
                    deleted = removeAndLog(errorMessage + " [" + ex.getMessage() + "]");
                }
            }
            if (deleted == false)
            {
                rollback(ex);
            }
            return Collections.emptyList();
        } catch (final Throwable throwable)
        {
            rollback(throwable);
            return Collections.emptyList();
        }
    }

    private String getEmail()
    {
        Experiment experiment = dataSetInformation.tryToGetExperiment();
        if (experiment == null)
        {
            throw new UserFailureException("Unknown experiment of data set " + dataSetInformation);
        }
        return experiment.getRegistrator().getEmail();
    }

    protected UnstoreDataAction rollbackStorageProcessor(final Throwable throwable)
    {
        UnstoreDataAction action =
                getStorageProcessor().rollback(incomingDataSetFile,
                        baseDirectoryHolder.getBaseDirectory(), throwable);
        return action;
    }

    /**
     * Registers the data set.
     */
    private void registerDataSetAndInitiateProcessing(final String procedureTypeCode)
            throws Throwable
    {
        final File markerFile = createProcessingMarkerFile();
        try
        {
            String entityDescription = createEntityDescription();
            if (TransferredDataSetHandler.operationLog.isInfoEnabled())
            {
                TransferredDataSetHandler.operationLog.info("Start storing data set for "
                        + entityDescription + ".");
            }
            final StopWatch watch = new StopWatch();
            watch.start();
            NewExternalData data = createExternalData();
            preRegistrationAction.execute(data.getCode(), incomingDataSetFile.getAbsolutePath());
            File dataFile =
                    getStorageProcessor().storeData(dataSetInformation, getTypeExtractor(),
                            getMailClient(), incomingDataSetFile,
                            baseDirectoryHolder.getBaseDirectory());
            if (getOperationLog().isInfoEnabled())
            {
                getOperationLog().info(
                        "Finished storing data set for " + entityDescription + ", took " + watch);
            }
            assert dataFile != null : "The folder that contains the stored data should not be null.";
            final String relativePath = FileUtilities.getRelativeFile(storeRoot, dataFile);
            String absolutePath = dataFile.getAbsolutePath();
            assert relativePath != null : String.format(
                    TransferredDataSetHandler.TARGET_NOT_RELATIVE_TO_STORE_ROOT, absolutePath,
                    storeRoot.getAbsolutePath());
            final StorageFormat availableFormat = getStorageProcessor().getStorageFormat();
            final BooleanOrUnknown isCompleteFlag = dataSetInformation.getIsCompleteFlag();
            // Ensure that we either register the data set and initiate the processing copy or
            // do none of both.
            getRegistrationLock().lock();
            try
            {
                errorMessageTemplate =
                        TransferredDataSetHandlerDataSetRegistrationAlgorithm.DATA_SET_REGISTRATION_FAILURE_TEMPLATE;
                plainRegisterDataSet(data, relativePath, availableFormat, isCompleteFlag);
                postRegistrationAction.execute(data.getCode(), absolutePath);
                clean();
            } finally
            {
                getRegistrationLock().unlock();
            }
            getStorageProcessor().commit(incomingDataSetFile,
                    baseDirectoryHolder.getBaseDirectory());
        } finally
        {
            getFileOperations().delete(markerFile);
        }
    }

    private String createEntityDescription()
    {
        SampleIdentifier sampleIdentifier = dataSetInformation.getSampleIdentifier();
        if (sampleIdentifier != null)
        {
            return "sample '" + sampleIdentifier + "'";
        }
        return "experiment '" + dataSetInformation.getExperimentIdentifier() + "'";
    }

    private final File createProcessingMarkerFile()
    {
        final File baseDirectory = baseDirectoryHolder.getBaseDirectory();
        final File baseParentDirectory = baseDirectory.getParentFile();
        final String processingDirName = baseDirectory.getName();
        final File markerFile =
                new File(baseParentDirectory, Constants.PROCESSING_PREFIX + processingDirName);
        try
        {
            getFileOperations().createNewFile(markerFile);
        } catch (final IOExceptionUnchecked ex)
        {
            throw EnvironmentFailureException.fromTemplate(ex, "Cannot create marker file '%s'.",
                    markerFile.getPath());
        }
        return markerFile;
    }

    /**
     * This method is only ever called for unidentified data sets.
     */
    public final void dealWithUnidentifiedDataSet()
    {
        final boolean ok =
                shouldDeleteUnidentified() ? (removeAndLog(incomingDataSetFile.getName()
                        + " could not be identified.")) : FileRenamer.renameAndLog(
                        incomingDataSetFile, baseDirectoryHolder.getTargetFile());
        if (ok)
        {
            clean();
        }
    }

    private boolean removeAndLog(String msg)
    {
        final boolean ok = getFileOperations().removeRecursivelyQueueing(incomingDataSetFile);
        if (getOperationLog().isInfoEnabled())
        {
            getOperationLog().info("Dataset deleted in registration: " + msg);
        }
        return ok;
    }

    private final void plainRegisterDataSet(NewExternalData data, final String relativePath,
            final StorageFormat storageFormat, final BooleanOrUnknown isCompleteFlag)
            throws Throwable
    {
        updateExternalData(data, relativePath, storageFormat, isCompleteFlag);
        // Finally: register the data set in the database.
        registerDataSetInApplicationServer(data);
    }

    /**
     * Contact openBis and register the data set there. Subclasses may override.
     * 
     * @throws Throwable
     */
    protected void registerDataSetInApplicationServer(NewExternalData data) throws Throwable
    {
        getOpenBisService().registerDataSet(dataSetInformation, data);
    }

    private void logAndNotifySuccessfulRegistration(final String email)
    {
        String msg = null;
        if (TransferredDataSetHandler.operationLog.isInfoEnabled())
        {
            msg = getSuccessRegistrationMessage();
            TransferredDataSetHandler.operationLog.info(msg);
        }
        if (shouldNotifySuccessfulRegistration())
        {
            if (msg == null)
            {
                msg = getSuccessRegistrationMessage();
            }
            if (TransferredDataSetHandler.notificationLog.isInfoEnabled())
            {
                TransferredDataSetHandler.notificationLog.info(msg);
            }
            if (StringUtils.isBlank(email) == false)
            {
                getMailClient().sendMessage(
                        String.format(getEmailSubjectTemplate(), dataSetInformation
                                .getExperimentIdentifier().getExperimentCode()), msg, null, null,
                        email);
            }
        }
    }

    private final String getSuccessRegistrationMessage()
    {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(TransferredDataSetHandlerDataSetRegistrationAlgorithm.SUCCESSFULLY_REGISTERED);
        String userID = dataSetInformation.getUploadingUserIdOrNull();
        String userEMail = dataSetInformation.tryGetUploadingUserEmail();
        if (userID != null || userEMail != null)
        {
            appendNameAndObject(buffer, "User", userID == null ? userEMail : userID);
        }
        appendNameAndObject(buffer, "Data Set Code", dataSetInformation.getDataSetCode());
        appendNameAndObject(buffer, "Data Set Type", dataSetType.getCode());
        appendNameAndObject(buffer, "Experiment Identifier",
                dataSetInformation.getExperimentIdentifier());
        appendNameAndObject(buffer, "Sample Identifier", dataSetInformation.getSampleIdentifier());
        appendNameAndObject(buffer, "Producer Code", dataSetInformation.getProducerCode());
        appendNameAndObject(buffer, "Production Date",
                formatDate(dataSetInformation.getProductionDate()));
        final List<String> parentDataSetCodes = dataSetInformation.getParentDataSetCodes();
        if (parentDataSetCodes.isEmpty() == false)
        {
            appendNameAndObject(buffer, "Parent Data Sets",
                    StringUtils.join(parentDataSetCodes, ' '));
        }
        appendNameAndObject(buffer, "Is complete", dataSetInformation.getIsCompleteFlag());
        buffer.setLength(buffer.length() - 1);
        buffer.append(']');
        return buffer.toString();
    }

    private String formatDate(Date productionDate)
    {
        return productionDate == null ? "" : Constants.DATE_FORMAT.get().format(productionDate);
    }

    private final void appendNameAndObject(final StringBuilder buffer, final String name,
            final Object object)
    {
        if (object != null)
        {
            buffer.append(name).append("::").append(object).append(";");
        }
    }

    /**
     * From given <var>incomingDataSetPath</var> extracts a <code>DataSetInformation</code>.
     * 
     * @return never <code>null</code> but prefers to throw an exception.
     */
    protected DataSetInformation extractDataSetInformation(final File incomingDataSetPath)
    {
        String errorMessage =
                "Error when trying to identify data set '" + incomingDataSetPath.getAbsolutePath()
                        + "'.";
        DataSetInformation dataSetInfo = null;
        try
        {
            dataSetInfo =
                    getDataSetInfoExtractor().getDataSetInformation(incomingDataSetPath,
                            getOpenBisService());
            dataSetInfo.setInstanceCode(getHomeDatabaseInstance().getCode());
            dataSetInfo.setInstanceUUID(getHomeDatabaseInstance().getUuid());
            if (getOperationLog().isDebugEnabled())
            {
                getOperationLog().debug(
                        String.format("Extracting data set information '%s' from incoming "
                                + "data set path '%s'", dataSetInfo, incomingDataSetPath));
            }
            return dataSetInfo;
        } catch (final HighLevelException e)
        {
            if (dataSetInfo != null)
            {
                String email = dataSetInfo.tryGetUploadingUserEmail();
                if (StringUtils.isBlank(email) == false)
                {
                    getMailClient().sendMessage(errorMessage, e.getMessage(), null, null, email);
                }
            }
            throw e;
        } catch (final RuntimeException ex)
        {
            throw new EnvironmentFailureException(errorMessage, ex);
        }
    }

    protected final File createBaseDirectory(final IDataStoreStrategy strategy, final File baseDir,
            final DataSetInformation dataSetInfo)
    {
        final File baseDirectory = strategy.getBaseDirectory(baseDir, dataSetInfo, dataSetType);
        baseDirectory.mkdirs();
        if (getFileOperations().isDirectory(baseDirectory) == false)
        {
            throw EnvironmentFailureException.fromTemplate(
                    "Creating data set base directory '%s' for data set '%s' failed.",
                    baseDirectory.getAbsolutePath(), incomingDataSetFile);
        }
        return baseDirectory;
    }

    private final NewExternalData updateExternalData(NewExternalData data,
            final String relativePath, final StorageFormat storageFormat,
            final BooleanOrUnknown isCompleteFlag)
    {
        data.setComplete(isCompleteFlag);
        data.setLocation(relativePath);
        data.setStorageFormat(storageFormat);
        return data;
    }

    private NewExternalData createExternalData()
    {
        final NewExternalData data = new NewExternalData();
        data.setUserId(dataSetInformation.getUploadingUserIdOrNull());
        data.setUserEMail(dataSetInformation.tryGetUploadingUserEmail());
        data.setExtractableData(dataSetInformation.getExtractableData());
        data.setLocatorType(getTypeExtractor().getLocatorType(incomingDataSetFile));
        data.setDataSetType(getTypeExtractor().getDataSetType(incomingDataSetFile));
        data.setFileFormatType(getTypeExtractor().getFileFormatType(incomingDataSetFile));
        data.setMeasured(getTypeExtractor().isMeasuredData(incomingDataSetFile));
        data.setDataStoreCode(getDataStoreCode());
        return data;
    }

    protected final void writeThrowable(final Throwable throwable)
    {
        final String fileName = incomingDataSetFile.getName() + ".exception";
        final File file = new File(baseDirectoryHolder.getTargetFile().getParentFile(), fileName);
        FileWriter writer = null;
        try
        {
            writer = new FileWriter(file);
            throwable.printStackTrace(new PrintWriter(writer));
        } catch (final IOException e)
        {
            TransferredDataSetHandler.operationLog.warn(String.format(
                    "Could not write out the exception '%s' in file '%s'.", fileName,
                    file.getAbsolutePath()), e);
        } finally
        {
            IOUtils.closeQuietly(writer);
        }
    }

    protected boolean clean()
    {
        return cleanAftrewardsAction.execute();
    }

    // subclass responsibility
    protected abstract void rollback(Throwable ex);

    // state accessors
    protected abstract IEncapsulatedOpenBISService getOpenBisService();

    protected abstract ITypeExtractor getTypeExtractor();

    protected abstract IStorageProcessor getStorageProcessor();

    protected abstract IDataSetInfoExtractor getDataSetInfoExtractor();

    protected abstract DatabaseInstance getHomeDatabaseInstance();

    protected abstract Logger getOperationLog();

    protected abstract Logger getNotificationLog();

    protected abstract IMailClient getMailClient();

    protected abstract IFileOperations getFileOperations();

    protected abstract String getDataStoreCode();

    protected abstract IDataStrategyStore getDataStrategyStore();

    protected abstract IDataSetValidator getDataSetValidator();

    protected abstract Lock getRegistrationLock();

    protected abstract boolean shouldDeleteUnidentified();

    protected abstract boolean shouldNotifySuccessfulRegistration();

    protected abstract String getEmailSubjectTemplate();
}