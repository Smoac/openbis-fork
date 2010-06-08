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

package ch.systemsx.cisd.cina.dss;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.cina.dss.info.CinaDataSetInfoExtractor;
import ch.systemsx.cisd.cina.dss.info.CinaTypeExtractor;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class StorageProcessorTest extends AbstractFileSystemTestCase
{
    Mockery context;

    IEncapsulatedOpenBISService openbisService;

    CinaDataSetInfoExtractor extractor;

    CinaTypeExtractor typeExtractor;

    StorageProcessor storageProcessor;

    IMailClient mailClient;

    File rootDir;

    File emailDir;

    private void initializeDSSInfrastructure()
    {
        context = new Mockery();
        openbisService = context.mock(IEncapsulatedOpenBISService.class);
        extractor = new CinaDataSetInfoExtractor(new Properties());
        typeExtractor = new CinaTypeExtractor(new Properties());
        rootDir = new File("sourceTest/java/ch/systemsx/cisd/cina/dss/info/");
    }

    private void initializeMailClient()
    {
        String emailFolderPath = workingDirectory.getPath() + "/emails";
        mailClient = new MailClient("sender", "file://" + emailFolderPath);
        emailDir = new File(emailFolderPath);
    }

    private void initializeStorageProcessor()
    {
        final Properties props = new Properties();
        props.setProperty("data-store-server-code", "DSS1");
        // Don't use this
        // props.setProperty("data-folder", "targets/playground/data") ;
        props.setProperty("storeroot-dir", "store");
        // workingDirectory
        props.setProperty("mail.smtp.host", emailDir.getPath());
        props.setProperty("mail.from", "datastore_server@localhost");
        props.setProperty("processor", "ch.systemsx.cisd.cina.dss.MockDefaultStorageProcessor");
        storageProcessor = new StorageProcessor(props);
    }

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        // Override setUp and call super first to make sure the working directory has been created
        super.setUp();

        // create openbis, extractor, other necessary infrastructure
        initializeDSSInfrastructure();

        initializeMailClient();

        initializeStorageProcessor();
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterExperiment()
    {
        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryToGetExperiment(with(any(ExperimentIdentifier.class)));
                    will(returnValue(null));
                    one(openbisService).registerExperiment(with(any(NewExperiment.class)));
                }
            });

        // "store" the data set
        doStoreData(new File(
                "sourceTest/java/ch/systemsx/cisd/cina/dss/info/experiment-data-folder"));
    }

    private void doStoreData(File datasetFolder)
    {
        DataSetInformation dataSetInformation =
                extractor.getDataSetInformation(datasetFolder, openbisService);

        storageProcessor.storeData(dataSetInformation, typeExtractor, mailClient, datasetFolder,
                rootDir);
    }
}
