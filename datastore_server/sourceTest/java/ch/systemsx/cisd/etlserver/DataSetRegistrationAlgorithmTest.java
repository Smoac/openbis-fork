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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm.DataSetRegistrationAlgorithmState;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm.IRollbackDelegate;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationAlgorithmTest extends AbstractFileSystemTestCase
{

    private static final String DATA_STORE_CODE = "data-store";

    private static final String DATA_SET_CODE = "data-set-code";

    private static final LocatorType LOCATOR_TYPE = new LocatorType("L1");

    private static final DataSetType DATA_SET_TYPE = new DataSetType("O1");

    private static final FileFormatType FILE_FORMAT_TYPE = new FileFormatType("FF1");

    private static final String EXAMPLE_PROCESSOR_ID = "DATA_ACQUISITION";

    private DataSetRegistrationAlgorithm registrationAlgorithm;

    private Mockery context;

    private IEncapsulatedOpenBISService openBisService;

    private IRollbackDelegate rollbackDelegate;

    private IDataSetInApplicationServerRegistrator appServerRegistrator;

    private IDelegatedActionWithResult<Boolean> cleanAftrewardsAction;

    private IPreRegistrationAction preRegistrationAction;

    private IPostRegistrationAction postRegistrationAction;

    private IDataStoreStrategy dataStoreStrategy;

    private ITypeExtractor typeExtractor;

    private IStorageProcessor storageProcessor;

    private IFileOperations fileOperations;

    private IDataSetValidator dataSetValidator;

    private IMailClient mailClient;

    private Lock registrationLock;

    private RecordingMatcher<Throwable> exceptionMatcher;

    private DataSetInformation dataSetInformation;

    private File incomingDataSetFile;

    private File storedDataSetFile;

    @SuppressWarnings("unchecked")
    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        context = new Mockery();
        openBisService = context.mock(IEncapsulatedOpenBISService.class);
        rollbackDelegate = context.mock(IRollbackDelegate.class);
        appServerRegistrator = context.mock(IDataSetInApplicationServerRegistrator.class);
        cleanAftrewardsAction = context.mock(IDelegatedActionWithResult.class);
        preRegistrationAction = context.mock(IPreRegistrationAction.class);
        postRegistrationAction = context.mock(IPostRegistrationAction.class);

        dataStoreStrategy = context.mock(IDataStoreStrategy.class);
        typeExtractor = context.mock(ITypeExtractor.class);
        storageProcessor = context.mock(IStorageProcessor.class);
        // fileOperations = context.mock(IFileOperations.class);
        fileOperations = FileOperations.getInstance();
        dataSetValidator = context.mock(IDataSetValidator.class);
        mailClient = context.mock(IMailClient.class);
        registrationLock = context.mock(Lock.class);
        exceptionMatcher = new RecordingMatcher<Throwable>();

        dataSetInformation = new DataSetInformation();
    }

    @Test
    public void testSuccessfulRegistrationWithoutNotification() throws Throwable
    {
        incomingDataSetFile = createDirectory("data_set");
        storedDataSetFile = createDirectory("stored_data_set");

        FileUtilities.writeToFile(new File(incomingDataSetFile, "read.me"), "hello world");
        setUpExpectations(incomingDataSetFile);

        createAlgorithmAndState(true, false);

        new DataSetRegistrationAlgorithmRunner(registrationAlgorithm).runAlgorithm();

        for (Throwable ex : exceptionMatcher.getRecordedObjects())
        {
            ex.printStackTrace();
        }

        context.assertIsSatisfied();
    }

    private void setUpExpectations(final File incomingDataSetFile) throws Throwable
    {
        setUpOpenBisExpectations();
        setUpTypeExtractorExpectations();
        setUpStorageProcessorExpectations();
        setUpDataSetValidatorExpectations();
        setUpDataStoreStrategyExpectations();
        setUpPreAndPostRegistrationExpectations();
        setUpLockExpectations();
        setUpDataSetRegistratorExpectations();
        setUpCleanAfterwardsExpectations();
        setUpRollbackExpectations();
    }

    private void setUpOpenBisExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));
                }
            });
    }

    private void setUpTypeExtractorExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(typeExtractor).getLocatorType(incomingDataSetFile);
                    will(returnValue(LOCATOR_TYPE));

                    one(typeExtractor).getFileFormatType(incomingDataSetFile);
                    will(returnValue(FILE_FORMAT_TYPE));

                    one(typeExtractor).getProcessorType(incomingDataSetFile);
                    will(returnValue(EXAMPLE_PROCESSOR_ID));

                    one(typeExtractor).isMeasuredData(incomingDataSetFile);
                    will(returnValue(true));

                    one(typeExtractor).getDataSetType(incomingDataSetFile);
                    will(returnValue(DATA_SET_TYPE));

                    one(typeExtractor).getDataSetType(with(any(File.class)));
                    will(returnValue(DATA_SET_TYPE));
                }
            });
    }

    private void setUpStorageProcessorExpectations()
    {
        final Action storeDataAction = new CustomAction("StoreData")
            {

                public Object invoke(Invocation invocation) throws Throwable
                {
                    fileOperations.move(incomingDataSetFile, storedDataSetFile);
                    return storedDataSetFile;
                }
            };

        context.checking(new Expectations()
            {
                {
                    one(storageProcessor).getStoreRootDirectory();
                    will(returnValue(workingDirectory));

                    one(storageProcessor).storeData(dataSetInformation, typeExtractor, mailClient,
                            incomingDataSetFile, workingDirectory);
                    will(storeDataAction);

                    one(storageProcessor).getStorageFormat();
                    will(returnValue(StorageFormat.PROPRIETARY));

                    one(storageProcessor).commit(incomingDataSetFile, workingDirectory);
                }
            });
    }

    private void setUpDataStoreStrategyExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(dataStoreStrategy).getBaseDirectory(with(workingDirectory),
                            with(any(DataSetInformation.class)), with(DATA_SET_TYPE));
                    will(returnValue(workingDirectory));

                    one(dataStoreStrategy).getKey();
                    will(returnValue(DataStoreStrategyKey.IDENTIFIED));
                }
            });
    }

    private void setUpDataSetValidatorExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE, incomingDataSetFile);
                }
            });
    }

    private void setUpPreAndPostRegistrationExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(preRegistrationAction).execute(DATA_SET_CODE,
                            incomingDataSetFile.getAbsolutePath());

                    one(postRegistrationAction).execute(with(DATA_SET_CODE),
                            with(any(String.class)));
                }
            });
    }

    private void setUpLockExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(registrationLock).lock();
                    one(registrationLock).unlock();
                }
            });
    }

    private void setUpDataSetRegistratorExpectations() throws Throwable
    {
        context.checking(new Expectations()
            {
                {
                    one(appServerRegistrator).registerDataSetInApplicationServer(
                            with(any(NewExternalData.class)));
                }
            });
    }

    private void setUpCleanAfterwardsExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(cleanAftrewardsAction).execute();
                    will(returnValue(Boolean.TRUE));
                }
            });
    }

    private void setUpRollbackExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(rollbackDelegate).rollback(
                            with(any(DataSetRegistrationAlgorithm.class)), with(exceptionMatcher));
                }
            });
    }

    private void createAlgorithmAndState(boolean shouldDeleteUnidentified,
            boolean shouldNotifySuccessfulRegistration)
    {

        String dataStoreCode = DATA_STORE_CODE;
        DataSetRegistrationAlgorithmState state =
                new DataSetRegistrationAlgorithmState(incomingDataSetFile, openBisService,
                        cleanAftrewardsAction, preRegistrationAction, postRegistrationAction,
                        dataSetInformation, dataStoreStrategy, typeExtractor, storageProcessor,
                        fileOperations, dataSetValidator, mailClient, shouldDeleteUnidentified,
                        registrationLock, dataStoreCode, shouldNotifySuccessfulRegistration);
        registrationAlgorithm =
                new DataSetRegistrationAlgorithm(state, rollbackDelegate, appServerRegistrator);
    }

    private File createDirectory(final String directoryName)
    {
        final File file = new File(workingDirectory, directoryName);
        file.mkdir();
        return file;
    }
}
