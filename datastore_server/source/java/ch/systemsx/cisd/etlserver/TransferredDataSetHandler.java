/*
 * Copyright 2007 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.common.Constants.IS_FINISHED_PREFIX;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.filesystem.IPathHandler;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.utilities.ISelfTestable;
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

/**
 * The class that handles the incoming data set.
 * 
 * @author Bernd Rinn
 */
public final class TransferredDataSetHandler implements IPathHandler, ISelfTestable,
        IDataSetHandler
{

    private static final String TARGET_NOT_RELATIVE_TO_STORE_ROOT =
            "Target path '%s' is not relative to store root directory '%s'.";

    @Private
    static final String DATA_SET_STORAGE_FAILURE_TEMPLATE = "Storing data set '%s' failed.";

    @Private
    static final String DATA_SET_REGISTRATION_FAILURE_TEMPLATE =
            "Registration of data set '%s' failed.";

    @Private
    static final String SUCCESSFULLY_REGISTERED = "Successfully registered data set: [";

    @Private
    static final String EMAIL_SUBJECT_TEMPLATE = "Success: data set for experiment '%s";

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, TransferredDataSetHandler.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, TransferredDataSetHandler.class);

    private static final NamedDataStrategy ERROR_DATA_STRATEGY =
            new NamedDataStrategy(DataStoreStrategyKey.ERROR);

    private final IStoreRootDirectoryHolder storeRootDirectoryHolder;

    private final IEncapsulatedOpenBISService limsService;

    private final IDataStrategyStore dataStrategyStore;

    private final IDataSetInfoExtractor dataSetInfoExtractor;

    private final IFileOperations fileOperations;

    private final Lock registrationLock;

    private final ITypeExtractor typeExtractor;

    private final IStorageProcessor storageProcessor;

    private final IMailClient mailClient;

    private final String dssCode;

    private final boolean notifySuccessfulRegistration;

    private final boolean useIsFinishedMarkerFile;

    private boolean stopped = false;

    private boolean deleteUnidentified = false;

    private DatabaseInstance homeDatabaseInstance;

    private final IDataSetHandler dataSetHandler;

    private final IDataSetValidator dataSetValidator;

    /**
     * @param dataSetValidator
     * @param useIsFinishedMarkerFile if true, file/directory is processed when a marker file for it
     *            appears. Otherwise processing starts if the file/directory is not modified for a
     *            certain amount of time (so called "quiet period").
     */
    public TransferredDataSetHandler(String dssCode, final IETLServerPlugin plugin,
            final IEncapsulatedOpenBISService limsService, final Properties mailProperties,
            IDataSetValidator dataSetValidator, final boolean notifySuccessfulRegistration,
            boolean useIsFinishedMarkerFile, boolean deleteUnidentified)

    {
        this(dssCode, plugin.getStorageProcessor(), plugin, limsService, new MailClient(
                mailProperties), dataSetValidator, notifySuccessfulRegistration,
                useIsFinishedMarkerFile, deleteUnidentified);
    }

    TransferredDataSetHandler(String dssCode,
            final IStoreRootDirectoryHolder storeRootDirectoryHolder,
            final IETLServerPlugin plugin, final IEncapsulatedOpenBISService limsService,
            final IMailClient mailClient, IDataSetValidator dataSetValidator,
            final boolean notifySuccessfulRegistration, boolean useIsFinishedMarkerFile,
            boolean deleteUnidentified)

    {
        assert dssCode != null : "Unspecified data store code";
        assert storeRootDirectoryHolder != null : "Given store root directory holder can not be null.";
        assert plugin != null : "IETLServerPlugin implementation can not be null.";
        assert limsService != null : "IEncapsulatedLimsService implementation can not be null.";
        assert mailClient != null : "IMailClient implementation can not be null.";

        this.dssCode = dssCode;
        this.storeRootDirectoryHolder = storeRootDirectoryHolder;
        this.dataSetInfoExtractor = plugin.getDataSetInfoExtractor();
        this.typeExtractor = plugin.getTypeExtractor();
        this.storageProcessor = plugin.getStorageProcessor();
        dataSetHandler = plugin.getDataSetHandler(this, limsService);
        this.dataSetValidator = dataSetValidator;
        this.limsService = limsService;
        this.mailClient = mailClient;
        this.dataStrategyStore = new DataStrategyStore(this.limsService, mailClient);
        this.notifySuccessfulRegistration = notifySuccessfulRegistration;
        this.registrationLock = new ReentrantLock();
        this.fileOperations = FileOperations.getMonitoredInstanceForCurrentThread();
        this.useIsFinishedMarkerFile = useIsFinishedMarkerFile;
        this.deleteUnidentified = deleteUnidentified;
    }

    /**
     * Returns the lock one needs to hold before one interrupts a data set registration.
     */
    public Lock getRegistrationLock()
    {
        return registrationLock;
    }

    //
    // IPathHandler
    //

    public final void handle(final File file)
    {
        if (stopped)
        {
            return;
        }
        dataSetHandler.handleDataSet(file);
    }

    public List<DataSetInformation> handleDataSet(final File dataSet)
    {
        final RegistrationHelper registrationHelper = createRegistrationHelper(dataSet);
        registrationHelper.prepare();
        if (registrationHelper.hasDataSetBeenIdentified())
        {
            return registrationHelper.registerDataSet();
        } else
        {
            registrationHelper.dealWithUnidentifiedDataSet();
            return Collections.emptyList();
        }
    }

    public boolean isStopped()
    {
        return stopped;
    }

    //
    // ISelfTestable
    //

    public final void check() throws ConfigurationFailureException, EnvironmentFailureException
    {
        final File storeRootDirectory = storeRootDirectoryHolder.getStoreRootDirectory();
        storeRootDirectory.mkdirs();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Checking store root directory '"
                    + storeRootDirectory.getAbsolutePath() + "'.");
        }
        final String errorMessage =
                fileOperations.checkDirectoryFullyAccessible(storeRootDirectory, "store root");
        if (errorMessage != null)
        {
            if (fileOperations.exists(storeRootDirectory) == false)
            {
                throw EnvironmentFailureException.fromTemplate(
                        "Store root directory '%s' does not exist.", storeRootDirectory
                                .getAbsolutePath());
            } else
            {
                throw new ConfigurationFailureException(errorMessage);
            }
        }
    }

    public boolean isRemote()
    {
        return true;
    }

    private DatabaseInstance getHomeDatabaseInstance()
    {
        if (homeDatabaseInstance == null)
        {
            homeDatabaseInstance = limsService.getHomeDatabaseInstance();
        }
        return homeDatabaseInstance;
    }

    //
    // Helper class
    //

    private RegistrationHelper createRegistrationHelper(File file)
    {
        if (useIsFinishedMarkerFile)
        {
            return createRegistrationHelperWithIsFinishedFile(file);
        } else
        {
            return createRegistrationHelperWithQuietPeriodFilter(file);
        }
    }

    private RegistrationHelper createRegistrationHelperWithIsFinishedFile(final File isFinishedFile)
    {
        assert isFinishedFile != null : "Unspecified is-finished file.";
        final String name = isFinishedFile.getName();
        assert name.startsWith(IS_FINISHED_PREFIX) : "A finished file must starts with '"
                + IS_FINISHED_PREFIX + "'.";

        File incomingDataSetFile = getIncomingDataSetPathFromMarker(isFinishedFile);
        IDelegatedActionWithResult<Boolean> cleanAftrewardsAction =
                new IDelegatedActionWithResult<Boolean>()
                    {
                        public Boolean execute()
                        {
                            return deleteAndLogIsFinishedMarkerFile(isFinishedFile);
                        }
                    };
        return new RegistrationHelper(incomingDataSetFile, cleanAftrewardsAction);
    }

    private RegistrationHelper createRegistrationHelperWithQuietPeriodFilter(
            File incomingDataSetFile)
    {
        IDelegatedActionWithResult<Boolean> cleanAftrewardsAction =
                new IDelegatedActionWithResult<Boolean>()
                    {
                        public Boolean execute()
                        {
                            return true; // do nothing
                        }
                    };
        return new RegistrationHelper(incomingDataSetFile, cleanAftrewardsAction);
    }

    /**
     * From given <var>isFinishedPath</var> gets the incoming data set path and checks it.
     * 
     * @return <code>null</code> if a problem has happened. Otherwise a useful and usable incoming
     *         data set path is returned.
     */
    private final File getIncomingDataSetPathFromMarker(final File isFinishedPath)
    {
        final File incomingDataSetPath =
                FileUtilities.removePrefixFromFileName(isFinishedPath, IS_FINISHED_PREFIX);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Getting incoming data set path '%s' from is-finished path '%s'",
                    incomingDataSetPath, isFinishedPath));
        }
        final String errorMsg =
                fileOperations.checkPathFullyAccessible(incomingDataSetPath, "incoming data set");
        if (errorMsg != null)
        {
            fileOperations.delete(isFinishedPath);
            throw EnvironmentFailureException.fromTemplate(String.format(
                    "Error moving path '%s' from '%s' to '%s': %s", incomingDataSetPath.getName(),
                    incomingDataSetPath.getParent(), storeRootDirectoryHolder
                            .getStoreRootDirectory(), errorMsg));
        }
        return incomingDataSetPath;
    }

    private boolean deleteAndLogIsFinishedMarkerFile(File isFinishedFile)
    {
        if (fileOperations.exists(isFinishedFile) == false)
        {
            return false;
        }
        final boolean ok = fileOperations.delete(isFinishedFile);
        final String absolutePath = isFinishedFile.getAbsolutePath();
        if (ok == false)
        {
            notificationLog.error(String.format("Removing file '%s' failed.", absolutePath));
        } else
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("File '%s' has been removed.", absolutePath));
            }
        }
        return ok;
    }

    private final class RegistrationHelper
    {
        private final IDelegatedActionWithResult<Boolean> cleanAftrewardsAction;

        private final File incomingDataSetFile;

        private final DataSetInformation dataSetInformation;

        private final IDataStoreStrategy dataStoreStrategy;

        private final DataSetType dataSetType;

        private final File storeRoot;

        private BaseDirectoryHolder baseDirectoryHolder;

        private String errorMessageTemplate;

        private RegistrationHelper(File incomingDataSetFile,
                IDelegatedActionWithResult<Boolean> cleanAftrewardsAction)
        {

            this.errorMessageTemplate = DATA_SET_STORAGE_FAILURE_TEMPLATE;
            this.incomingDataSetFile = incomingDataSetFile;
            this.cleanAftrewardsAction = cleanAftrewardsAction;
            this.dataSetInformation = extractDataSetInformation(incomingDataSetFile);
            if (dataSetInformation.getDataSetCode() == null)
            {
                // Extractor didn't extract an externally generated data set code, so request one
                // from the openBIS server.
                dataSetInformation.setDataSetCode(limsService.createDataSetCode());
            }
            this.dataStoreStrategy =
                    dataStrategyStore.getDataStoreStrategy(dataSetInformation, incomingDataSetFile);
            this.dataSetType = typeExtractor.getDataSetType(incomingDataSetFile);
            dataSetInformation.setDataSetType(dataSetType);
            this.storeRoot = storageProcessor.getStoreRootDirectory();
        }

        final void prepare()
        {
            final File baseDirectory =
                    createBaseDirectory(dataStoreStrategy, storeRoot, dataSetInformation);
            baseDirectoryHolder =
                    new BaseDirectoryHolder(dataStoreStrategy, baseDirectory, incomingDataSetFile);
        }

        final boolean hasDataSetBeenIdentified()
        {
            return dataStoreStrategy.getKey() == DataStoreStrategyKey.IDENTIFIED;
        }

        /**
         * This method is only ever called for identified data sets.
         */
        final List<DataSetInformation> registerDataSet()
        {
            String processorID = typeExtractor.getProcessorType(incomingDataSetFile);
            try
            {
                dataSetValidator.assertValidDataSet(dataSetType, incomingDataSetFile);
                registerDataSetAndInitiateProcessing(processorID);
                logAndNotifySuccessfulRegistration(getEmail());
                if (fileOperations.exists(incomingDataSetFile)
                        && removeAndLog("clean up failed") == false)
                {
                    operationLog.error("Cannot delete '" + incomingDataSetFile.getAbsolutePath()
                            + "'.");
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
                            "Error when trying to register data set '"
                                    + incomingDataSetFile.getName() + "'.";
                    mailClient.sendMessage(String.format(errorMessage, dataSetInformation
                            .getExperimentIdentifier().getExperimentCode()), ex.getMessage(), null,
                            null, userEmailOrNull);
                    if (deleteUnidentified)
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
                throw new UserFailureException("Unknown experiment of data set "
                        + dataSetInformation);
            }
            return experiment.getRegistrator().getEmail();
        }

        private void rollback(final Throwable throwable) throws Error
        {
            stopped |= throwable instanceof InterruptedExceptionUnchecked;
            if (stopped)
            {
                Thread.interrupted(); // Ensure the thread's interrupted state is cleared.
                operationLog.warn(String.format("Requested to stop registration of data set '%s'",
                        dataSetInformation));
            } else
            {
                notificationLog.error(String.format(errorMessageTemplate, dataSetInformation),
                        throwable);
            }
            // Errors which are not AssertionErrors leave the system in a state that we don't
            // know and can't trust. Thus we will not perform any operations any more in this
            // case.
            if (throwable instanceof Error && throwable instanceof AssertionError == false)
            {
                throw (Error) throwable;
            }
            UnstoreDataAction action =
                    storageProcessor.rollback(incomingDataSetFile, baseDirectoryHolder
                            .getBaseDirectory(), throwable);
            if (stopped == false)
            {
                if (action == UnstoreDataAction.MOVE_TO_ERROR)
                {
                    final File baseDirectory =
                            createBaseDirectory(ERROR_DATA_STRATEGY, storeRoot, dataSetInformation);
                    baseDirectoryHolder =
                            new BaseDirectoryHolder(ERROR_DATA_STRATEGY, baseDirectory,
                                    incomingDataSetFile);
                    boolean moveInCaseOfErrorOk =
                            FileRenamer.renameAndLog(incomingDataSetFile, baseDirectoryHolder
                                    .getTargetFile());
                    writeThrowable(throwable);
                    if (moveInCaseOfErrorOk)
                    {
                        clean();
                    }
                } else if (action == UnstoreDataAction.DELETE)
                {
                    FileUtilities.deleteRecursively(incomingDataSetFile, new Log4jSimpleLogger(
                            operationLog));
                }
            }
        }

        /**
         * Registers the data set.
         */
        private void registerDataSetAndInitiateProcessing(final String procedureTypeCode)
        {
            final File markerFile = createProcessingMarkerFile();
            try
            {
                String entityDescription = createEntityDescription();
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Start storing data set for " + entityDescription + ".");
                }
                final StopWatch watch = new StopWatch();
                watch.start();
                NewExternalData data = createExternalData();
                File dataFile =
                        storageProcessor.storeData(dataSetInformation, typeExtractor, mailClient,
                                incomingDataSetFile, baseDirectoryHolder.getBaseDirectory());
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Finished storing data set for " + entityDescription
                            + ", took " + watch);
                }
                assert dataFile != null : "The folder that contains the stored data should not be null.";
                final String relativePath = FileUtilities.getRelativeFile(storeRoot, dataFile);
                assert relativePath != null : String.format(TARGET_NOT_RELATIVE_TO_STORE_ROOT,
                        dataFile.getAbsolutePath(), storeRoot.getAbsolutePath());
                final StorageFormat availableFormat = storageProcessor.getStorageFormat();
                final BooleanOrUnknown isCompleteFlag = dataSetInformation.getIsCompleteFlag();
                // Ensure that we either register the data set and initiate the processing copy or
                // do none of both.
                getRegistrationLock().lock();
                try
                {
                    errorMessageTemplate = DATA_SET_REGISTRATION_FAILURE_TEMPLATE;
                    plainRegisterDataSet(data, relativePath, availableFormat, isCompleteFlag);
                    clean();
                } finally
                {
                    getRegistrationLock().unlock();
                }
                storageProcessor.commit();
            } finally
            {
                fileOperations.delete(markerFile);
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
                fileOperations.createNewFile(markerFile);
            } catch (final IOExceptionUnchecked ex)
            {
                throw EnvironmentFailureException.fromTemplate(ex,
                        "Cannot create marker file '%s'.", markerFile.getPath());
            }
            return markerFile;
        }

        /**
         * This method is only ever called for unidentified data sets.
         */
        final void dealWithUnidentifiedDataSet()
        {
            final boolean ok =
                    deleteUnidentified ? (removeAndLog(incomingDataSetFile.getName()
                            + " could not be identified.")) : FileRenamer.renameAndLog(
                            incomingDataSetFile, baseDirectoryHolder.getTargetFile());
            if (ok)
            {
                clean();
            }
        }

        private boolean removeAndLog(String msg)
        {
            final boolean ok = fileOperations.removeRecursivelyQueueing(incomingDataSetFile);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Dataset deleted in registration: " + msg);
            }
            return ok;
        }

        private final void plainRegisterDataSet(NewExternalData data, final String relativePath,
                final StorageFormat storageFormat, final BooleanOrUnknown isCompleteFlag)
        {
            updateExternalData(data, relativePath, storageFormat, isCompleteFlag);
            // Finally: register the data set in the database.
            limsService.registerDataSet(dataSetInformation, data);
        }

        private void logAndNotifySuccessfulRegistration(final String email)
        {
            String msg = null;
            if (operationLog.isInfoEnabled())
            {
                msg = getSuccessRegistrationMessage();
                operationLog.info(msg);
            }
            if (notifySuccessfulRegistration)
            {
                if (msg == null)
                {
                    msg = getSuccessRegistrationMessage();
                }
                if (notificationLog.isInfoEnabled())
                {
                    notificationLog.info(msg);
                }
                if (StringUtils.isBlank(email) == false)
                {
                    mailClient
                            .sendMessage(String.format(EMAIL_SUBJECT_TEMPLATE, dataSetInformation
                                    .getExperimentIdentifier().getExperimentCode()), msg, null,
                                    null, email);
                }
            }
        }

        private final String getSuccessRegistrationMessage()
        {
            final StringBuilder buffer = new StringBuilder();
            buffer.append(SUCCESSFULLY_REGISTERED);
            appendNameAndObject(buffer, "Data Set Code", dataSetInformation.getDataSetCode());
            appendNameAndObject(buffer, "Data Set Type", dataSetType.getCode());
            appendNameAndObject(buffer, "Experiment Identifier", dataSetInformation
                    .getExperimentIdentifier());
            appendNameAndObject(buffer, "Sample Identifier", dataSetInformation
                    .getSampleIdentifier());
            appendNameAndObject(buffer, "Producer Code", dataSetInformation.getProducerCode());
            appendNameAndObject(buffer, "Production Date", formatDate(dataSetInformation
                    .getProductionDate()));
            final List<String> parentDataSetCodes = dataSetInformation.getParentDataSetCodes();
            if (parentDataSetCodes.isEmpty() == false)
            {
                appendNameAndObject(buffer, "Parent Data Sets", StringUtils.join(
                        parentDataSetCodes, ' '));
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
        private final DataSetInformation extractDataSetInformation(final File incomingDataSetPath)
        {
            String errorMessage =
                    "Error when trying to identify data set '"
                            + incomingDataSetPath.getAbsolutePath() + "'.";
            DataSetInformation dataSetInfo = null;
            try
            {
                dataSetInfo =
                        dataSetInfoExtractor
                                .getDataSetInformation(incomingDataSetPath, limsService);
                dataSetInfo.setInstanceCode(getHomeDatabaseInstance().getCode());
                dataSetInfo.setInstanceUUID(getHomeDatabaseInstance().getUuid());
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(
                            "Extracting data set information '%s' from incoming "
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
                        mailClient.sendMessage(errorMessage, e.getMessage(), null, null, email);
                    }
                }
                throw e;
            } catch (final RuntimeException ex)
            {
                throw new EnvironmentFailureException(errorMessage, ex);
            }
        }

        private final File createBaseDirectory(final IDataStoreStrategy strategy,
                final File baseDir, final DataSetInformation dataSetInfo)
        {
            final File baseDirectory = strategy.getBaseDirectory(baseDir, dataSetInfo, dataSetType);
            baseDirectory.mkdirs();
            if (fileOperations.isDirectory(baseDirectory) == false)
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
            data.setExtractableData(dataSetInformation.getExtractableData());
            data.setLocatorType(typeExtractor.getLocatorType(incomingDataSetFile));
            data.setDataSetType(typeExtractor.getDataSetType(incomingDataSetFile));
            data.setFileFormatType(typeExtractor.getFileFormatType(incomingDataSetFile));
            data.setMeasured(typeExtractor.isMeasuredData(incomingDataSetFile));
            data.setDataStoreCode(dssCode);
            return data;
        }

        private final void writeThrowable(final Throwable throwable)
        {
            final String fileName = incomingDataSetFile.getName() + ".exception";
            final File file =
                    new File(baseDirectoryHolder.getTargetFile().getParentFile(), fileName);
            FileWriter writer = null;
            try
            {
                writer = new FileWriter(file);
                throwable.printStackTrace(new PrintWriter(writer));
            } catch (final IOException e)
            {
                operationLog.warn(String.format(
                        "Could not write out the exception '%s' in file '%s'.", fileName, file
                                .getAbsolutePath()), e);
            } finally
            {
                IOUtils.closeQuietly(writer);
            }
        }

        private boolean clean()
        {
            return cleanAftrewardsAction.execute();
        }
    }

}
