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

package ch.systemsx.cisd.etlserver.registrator;

import static ch.systemsx.cisd.common.Constants.IS_FINISHED_PREFIX;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.hamcrest.core.IsAnything;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonTopLevelDataSetRegistratorRollbackTest extends AbstractJythonDataSetHandlerTest
{
    private static final String SCRIPTS_FOLDER =
            "sourceTest/java/ch/systemsx/cisd/etlserver/registrator/";

    private static final String DATA_SET_CODE = "data-set-code";

    private static final DataSetType DATA_SET_TYPE = new DataSetType("O1");

    private static final String EXPERIMENT_IDENTIFIER = "/SPACE/PROJECT/EXP";

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();

        didDataSetRollbackHappen = false;
        didServiceRollbackHappen = false;
    }

    @Test
    public void testSimpleTransactionRollback()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("simple-transaction.py");
        properties.setProperty(IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY,
                MockStorageProcessor.class.getName());

        createHandler(properties, false, false);
        createData();
        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));
                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));
                    one(openBisService)
                            .performEntityOperations(
                                    with(new IsAnything<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>()));
                    will(throwException(new AssertionError("Fail")));
                }
            });

        try
        {
            handler.handle(markerFile);
            fail("No IOException thrown");
        } catch (IOExceptionUnchecked e)
        {
            // Make the file system available again and rollback
            makeFileSystemAvailable(workingDirectory);
            DataSetRegistrationTransaction.rollbackDeadTransactions(workingDirectory);
        }
        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);
        assertEquals("[]", Arrays.asList(stagingDirectory.list()).toString());
        assertEquals(
                "hello world1",
                FileUtilities.loadToString(
                        new File(workingDirectory, "data_set/sub_data_set_1/read1.me")).trim());
        assertEquals(
                "hello world2",
                FileUtilities.loadToString(
                        new File(workingDirectory, "data_set/sub_data_set_2/read2.me")).trim());

        TestingDataSetHandler theHandler = (TestingDataSetHandler) handler;
        assertFalse(theHandler.didRollbackDataSetRegistrationFunctionRun);
        assertFalse(theHandler.didRollbackServiceFunctionRun);
        assertTrue(theHandler.didTransactionRollbackHappen);
        assertTrue(theHandler.didRollbackTransactionFunctionRunHappen);

        context.assertIsSatisfied();
    }

    private void createData()
    {
        incomingDataSetFile = createDirectory(workingDirectory, "data_set");

        subDataSet1 = createDirectory(incomingDataSetFile, "sub_data_set_1");
        subDataSet2 = createDirectory(incomingDataSetFile, "sub_data_set_2");

        FileUtilities.writeToFile(new File(subDataSet1, "read1.me"), "hello world1");
        FileUtilities.writeToFile(new File(subDataSet2, "read2.me"), "hello world2");

        markerFile = new File(workingDirectory, IS_FINISHED_PREFIX + "data_set");
        FileUtilities.writeToFile(markerFile, "");
    }

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return SCRIPTS_FOLDER;
    }

    protected SearchCriteria createTestSearchCriteria(String typeString)
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, typeString));
        sc.addMatchClause(MatchClause.createPropertyMatch("PROP", "VALUE"));
        return sc;
    }

    public static final class MockStorageProcessor extends
            AbstractJythonDataSetHandlerTest.MockStorageProcessor
    {
        private static final long serialVersionUID = 1L;

        static MockStorageProcessor instance;

        int calledGetStoreRootDirectoryCount = 0;

        int calledCommitCount = 0;

        File storeRootDirectory;

        String dataSetInfoString;

        protected List<File> incomingDirs = new ArrayList<File>();

        protected List<File> rootDirs = new ArrayList<File>();

        public MockStorageProcessor(ExtendedProperties props)
        {
            super(props);
            instance = this;
        }

        public File getStoreRootDirectory()
        {
            calledGetStoreRootDirectoryCount++;
            return storeRootDirectory;
        }

        public void setStoreRootDirectory(File storeRootDirectory)
        {
            this.storeRootDirectory = storeRootDirectory;
        }

        public StorageFormat getStorageFormat()
        {
            return StorageFormat.PROPRIETARY;
        }

        public UnstoreDataAction getDefaultUnstoreDataAction(Throwable exception)
        {
            return UnstoreDataAction.LEAVE_UNTOUCHED;
        }

        public IStorageProcessorTransaction createTransaction(
                StorageProcessorTransactionParameters parameters)
        {
            final File rootDir = parameters.getRootDir();
            dataSetInfoString = parameters.getDataSetInformation().toString();
            return new IStorageProcessorTransaction()
                {

                    private static final long serialVersionUID = 1L;

                    private File storedFolder;

                    public void storeData(ITypeExtractor typeExtractor, IMailClient mailClient,
                            File incomingDataSetFile)
                    {

                        incomingDirs.add(incomingDataSetFile);
                        rootDirs.add(rootDir);

                        try
                        {
                            if (incomingDataSetFile.isDirectory())
                            {
                                FileUtils.moveDirectoryToDirectory(incomingDataSetFile, new File(
                                        rootDir, "original"), true);
                            } else
                            {
                                FileUtils.moveFileToDirectory(incomingDataSetFile, new File(
                                        rootDir, "original"), false);
                            }
                            makeFileSystemUnavailable(getStoreRootDirectory());
                        } catch (IOException ex)
                        {
                            throw new IOExceptionUnchecked(ex);
                        }
                        storedFolder = rootDir;
                    }

                    public UnstoreDataAction rollback(Throwable exception)
                    {
                        return null;
                    }

                    public File getStoredDataDirectory()
                    {
                        return storedFolder;
                    }

                    public void commit()
                    {
                        calledCommitCount++;
                    }

                    public File tryGetProprietaryData()
                    {
                        return null;
                    }
                };
        }
    }

    /**
     * Simulate the file system becoming unavailable
     */
    private static void makeFileSystemUnavailable(File storeRootDirectory)
    {
        new File(storeRootDirectory, "1").renameTo(new File(storeRootDirectory, "1.unavailable"));

        new File(storeRootDirectory, "staging").renameTo(new File(storeRootDirectory,
                "staging.unavailable"));
    }

    /**
     * Simulate the file system becoming available again
     */
    private static void makeFileSystemAvailable(File storeRootDirectory)
    {
        new File(storeRootDirectory, "1.unavailable").renameTo(new File(storeRootDirectory, "1"));

        new File(storeRootDirectory, "staging.unavailable").renameTo(new File(storeRootDirectory,
                "staging"));
    }
}
