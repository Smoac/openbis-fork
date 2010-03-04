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

package eu.basysbio.cisd.dss;

import static eu.basysbio.cisd.dss.TimeSeriesAndTimePointDataSetHandler.HELPDESK_EMAIL;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.collection.IsArray;
import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=TimeSeriesAndTimePointDataSetHandler.class)
public class TimeSeriesAndTimePointDataSetHandlerTest extends AbstractFileSystemTestCase
{
    private static final class StringMatcher extends BaseMatcher<String>
    {
        private final String expectedString;

        StringMatcher(String expectedString)
        {
            this.expectedString = expectedString;
        }

        public boolean matches(Object item)
        {
            if (item instanceof String)
            {
                String string = (String) item;
                assertEquals(expectedString, string);
                return true;
            }
            return false;
        }

        public void describeTo(Description description)
        {
            description.appendText(expectedString);
        }
    }
    
    private Mockery context;
    private IDataSetHandler delegator;
    private IDataSetHandler timePointDataSetHandler;
    private IMailClient mailClient;
    private IDataSetHandler handler;
    private File dropBox;
    private File tinePointFolder;
    private ITimeProvider timeProvider;

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        super.setUp();
        LogInitializer.init();
        context = new Mockery();
        delegator = context.mock(IDataSetHandler.class, "delegator");
        timePointDataSetHandler = context.mock(IDataSetHandler.class, "timePointDataSetHandler");
        mailClient = context.mock(IMailClient.class);
        timeProvider = context.mock(ITimeProvider.class);
        dropBox = new File(workingDirectory, "drop-box");
        tinePointFolder = new File(workingDirectory, "time-point-folder");
        tinePointFolder.mkdirs();
        handler =
                new TimeSeriesAndTimePointDataSetHandler(delegator, mailClient,
                        timePointDataSetHandler, tinePointFolder, timeProvider);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }
    
    @Test
    public void testFailingTimeSeriesRegistration()
    {
        context.checking(new Expectations()
            {
                {
                    one(delegator).handleDataSet(dropBox);
                    will(returnValue(Arrays.asList()));
                }
            });
        
        List<DataSetInformation> dataSets = handler.handleDataSet(dropBox);
        
        assertEquals(0, dataSets.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testHandleSuccesfullyTwoTimePointDataSets() throws IOException
    {
        final File tp1 = new File(tinePointFolder, "tp1");
        tp1.createNewFile();
        final DataSetInformation ds1 = new DataSetInformation();
        final File tp2 = new File(tinePointFolder, "tp2");
        tp2.createNewFile();
        final DataSetInformation ds2 = new DataSetInformation();
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setUploadingUserEmail("john.doe@abc.de");
        String expectedSubject =
                "BaSysBio: Successful uploading of " + "time series data set 'drop-box'";
        String expectedMessage =
                "The time series data set 'drop-box' "
                        + "has been successfully uploaded and registered in openBIS.";
        prepareSendingEMail(expectedSubject, expectedMessage, dataSetInformation, false);
        context.checking(new Expectations()
            {
                {
                    one(delegator).handleDataSet(dropBox);
                    will(returnValue(Arrays.asList(dataSetInformation)));
                    
                    one(timePointDataSetHandler).handleDataSet(tp1);
                    will(returnValue(Arrays.asList(ds1)));
                    
                    one(timePointDataSetHandler).handleDataSet(tp2);
                    will(returnValue(Arrays.asList(ds2)));
                }
            });

        List<DataSetInformation> dataSets = handler.handleDataSet(dropBox);

        assertEquals(3, dataSets.size());
        assertSame(dataSetInformation, dataSets.get(0));
        assertSame(ds1, dataSets.get(1));
        assertSame(ds2, dataSets.get(2));
        context.assertIsSatisfied();
    }

    
    @Test
    public void testHandleNotSuccesfullyTwoTimePointDataSets() throws IOException
    {
        final File tp1 = new File(tinePointFolder, "tp1");
        tp1.createNewFile();
        final File tp2 = new File(tinePointFolder, "tp2");
        tp2.createNewFile();
        final DataSetInformation ds2 = new DataSetInformation();
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setUploadingUserEmail("john.doe@abc.de");
        String expectedSubject =
                "BaSysBio: Failed uploading of " + "time series data set 'drop-box'";
        String expectedMessage =
                "Uploading of time series data set 'drop-box' failed "
                        + "because only 1 of 2 time point data sets could be registered in openBIS.\n\n"
                        + "Please, contact the help desk for support: " + HELPDESK_EMAIL + "\n"
                        + "(Time stamp of failure: 1970-01-01 01:01:14 +0100)";
        prepareSendingEMail(expectedSubject, expectedMessage, dataSetInformation, true);
        context.checking(new Expectations()
            {
                {
                    one(delegator).handleDataSet(dropBox);
                    will(returnValue(Arrays.asList(dataSetInformation)));

                    one(timePointDataSetHandler).handleDataSet(tp1);
                    will(returnValue(Arrays.asList()));

                    one(timePointDataSetHandler).handleDataSet(tp2);
                    will(returnValue(Arrays.asList(ds2)));

                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(42 * 42 * 42L));
                }
            });

        List<DataSetInformation> dataSets = handler.handleDataSet(dropBox);

        assertEquals(2, dataSets.size());
        assertSame(dataSetInformation, dataSets.get(0));
        assertSame(ds2, dataSets.get(1));
        context.assertIsSatisfied();
    }

    private void prepareSendingEMail(final String expectedSubject, final String expectedMessage,
            final DataSetInformation dataSetInformation, final boolean alsoToHelpDesk)
    {
        context.checking(new Expectations()
            {
                {
                    StringMatcher[] recipients = new StringMatcher[alsoToHelpDesk ? 2 : 1];
                    recipients[0] =
                            new StringMatcher(dataSetInformation.tryGetUploadingUserEmail());
                    if (alsoToHelpDesk)
                    {
                        recipients[1] = new StringMatcher(HELPDESK_EMAIL);
                    }
                    one(mailClient).sendMessage(string(expectedSubject), string(expectedMessage),
                            with(new IsNull<String>()), with(new IsNull<From>()),
                            with(new IsArray<String>(recipients)));
                }

                private String string(String expectedString)
                {
                    return with(new StringMatcher(expectedString));
                }
            });
    }

}
