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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Level;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.UploadState;
import ch.systemsx.cisd.cifex.rpc.UploadStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class UploadingCommandTest extends AssertJUnit
{
    private static final String INFO_UPLOAD_PREFIX = "INFO  OPERATION.UploadingCommand - ";
    private static final String WARN_UPLOAD_PREFIX = "WARN  OPERATION.UploadingCommand - ";
    private static final String INFO_MAIL_PREFIX = "INFO  OPERATION.MailClient - ";

    private static final class ZipFileMatcher extends BaseMatcher<String[]>
    {
        private final List<String> expectedPaths;
        
        private String msg;

        ZipFileMatcher(File... dataSets)
        {
            expectedPaths = new ArrayList<String>();
            for (File file : dataSets)
            {
                addPaths(expectedPaths, file);
            }
            Collections.sort(expectedPaths);
        }
        
        private void addPaths(List<String> paths, File file)
        {
            if (file.isFile())
            {
                paths.add(file.getPath().substring(STORE.getPath().length() + 1));
            } else
            {
                File[] files = file.listFiles();
                for (File child : files)
                {
                    addPaths(paths, child);
                }
            }
        }
        
        public void describeTo(Description description)
        {
            description.appendText(msg);
        }

        public boolean matches(Object item)
        {
            String[] paths = (String[]) item;
            if (paths.length != 1)
            {
                msg = "not one path but " + paths.length;
                return false;
            }
            String zipFile = paths[0];
            if (zipFile.endsWith(".zip") == false)
            {
                msg = "not a zip file: " + zipFile;
                return false;
            }
            return checkContent(zipFile);
        }
        
        private boolean checkContent(String file)
        {
            ZipFile zipFile = null;
            try
            {
                List<String> paths = new ArrayList<String>();
                zipFile = new ZipFile(file);
                for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements(); )
                {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    paths.add(name);
                }
                Collections.sort(paths);
                if (expectedPaths.equals(paths))
                {
                    return true;
                }
                msg = "Expected:\n" + expectedPaths + "\nActual:\n" + paths;
            } catch (Exception ex)
            {
                ex.printStackTrace();
                msg = ex.toString();
            } finally
            {
                if (zipFile != null)
                {
                    try
                    {
                        zipFile.close();
                    } catch (IOException ex)
                    {
                        // ignored
                    }
                }
            }
            return false;
        }
    }

    private static final File TEST_FOLDER = new File("targets/upload-test");
    private static final File STORE = new File(TEST_FOLDER, "store");
    private static final File TMP = new File(STORE, "tmp");
    private static final File EMAILS = new File(TEST_FOLDER, "emails");
    private static final String LOCATION1 = "ds1";
    private static final String LOCATION2 = "ds2";
    private static final String SESSION_TOKEN = "session42";
    
    private BufferedAppender logRecorder;
    private Mockery context;
    private ICIFEXRPCServiceFactory factory;
    private ICIFEXRPCService cifexService;
    private MailClientParameters mailClientParameters;
    private DataSetUploadContext uploadContext;
    private UploadingCommand command;
    private File ds1;
    private File ds2;

    @BeforeMethod
    public void setup()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        factory = context.mock(ICIFEXRPCServiceFactory.class);
        cifexService = context.mock(ICIFEXRPCService.class);
        mailClientParameters = new MailClientParameters();
        mailClientParameters.setFrom("a@bc.de");
        mailClientParameters.setSmtpHost("file://" + EMAILS);
        FileUtilities.deleteRecursively(TEST_FOLDER);
        STORE.mkdirs();
        uploadContext = new DataSetUploadContext();
        uploadContext.setCifexURL("cifexURL");
        uploadContext.setUserID("user");
        uploadContext.setPassword("pwd");
        uploadContext.setUserEMail("user@bc.de");
        List<String> locations = Arrays.asList(LOCATION1, LOCATION2);
        ds1 = createTestData(LOCATION1);
        ds2 = createTestData(LOCATION2);
        command = new UploadingCommand(factory, mailClientParameters, locations, uploadContext);
    }
    
    private File createTestData(String location)
    {
        File dataSet = new File(STORE, location);
        dataSet.mkdir();
        FileUtilities.writeToFile(new File(dataSet, "README.TXT"), "Data set " + location);
        File dataFolder = new File(dataSet, "data");
        dataFolder.mkdir();
        FileUtilities.writeToFile(new File(dataFolder, "data.txt"), "1 2 3 for " + location);
        return dataSet;
    }
    
    private void checkEmail(String messageStart)
    {
        File[] emails = EMAILS.listFiles();
        assertEquals("One email expected", 1, emails.length);
        List<String> email = FileUtilities.loadToStringList(emails[0]);
        assertEquals("Subj: [Data Set Server] Uploading failed", email.get(0));
        assertEquals("From: a@bc.de", email.get(1));
        assertEquals("To:   user@bc.de", email.get(2));
        assertTrue("Actual: " + email.get(5), email.get(5).startsWith(messageStart));
    }
    
    private String getNormalizedLogContent()
    {
        return logRecorder.getLogContent().replaceAll(" [^ ]*\\.zip", " <zipfile>");
    }
    
    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testExecute()
    {
        context.checking(new Expectations()
            {
                {
                    one(factory).createService();
                    will(returnValue(cifexService));
                    
                    one(cifexService).login(uploadContext.getUserID(), uploadContext.getPassword());
                    will(returnValue(SESSION_TOKEN));
                    
                    allowing(cifexService).getVersion();
                    will(returnValue(ICIFEXRPCService.VERSION));
                    
                    one(cifexService).checkSession(SESSION_TOKEN);
                    
                    one(cifexService).getUploadStatus(SESSION_TOKEN);
                    UploadStatus uploadStatus = new UploadStatus();
                    uploadStatus.setUploadState(UploadState.INITIALIZED);
                    will(returnValue(uploadStatus));
                    
                    one(cifexService).defineUploadParameters(with(equal(SESSION_TOKEN)),
                            with(new ZipFileMatcher(ds1, ds2)), with(equal("id:user")),
                            with(aNull(String.class)));
                    
                    one(cifexService).getUploadStatus(SESSION_TOKEN);
                    uploadStatus = new UploadStatus();
                    uploadStatus.setUploadState(UploadState.FINISHED);
                    will(returnValue(uploadStatus));
                    
                    one(cifexService).finish(SESSION_TOKEN, true);
                }
            });
        
        logRecorder.resetLogContent();
        command.execute(STORE);
        
        assertEquals("no emails expected", false, EMAILS.exists());
        assertEquals("Empty tmp folder expected", 0, TMP.listFiles().length);
        assertEquals(INFO_UPLOAD_PREFIX
                + "Zip file <zipfile> with 2 data sets has been successfully created."
                + OSUtilities.LINE_SEPARATOR + INFO_UPLOAD_PREFIX
                + "Zip file <zipfile> has been successfully uploaded.", getNormalizedLogContent());        
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithFailedZipFileCreation()
    {
        FileUtilities.deleteRecursively(ds2);
        command.execute(STORE);
    
        checkEmail("Couldn't create zip file");
        assertEquals("Empty tmp folder expected", 0, TMP.listFiles().length);
        assertEquals("ERROR NOTIFY.UploadingCommand - Data set 'ds2' does not exist."
                + OSUtilities.LINE_SEPARATOR + INFO_MAIL_PREFIX
                + "Sending message from 'a@bc.de' to recipients '[user@bc.de]'",
                getNormalizedLogContent());        
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithFailedUpload()
    {
        context.checking(new Expectations()
            {
                {
                    one(factory).createService();
                    will(returnValue(cifexService));

                    one(cifexService).login(uploadContext.getUserID(), uploadContext.getPassword());
                    will(returnValue(SESSION_TOKEN));

                    allowing(cifexService).getVersion();
                    will(returnValue(ICIFEXRPCService.VERSION));

                    one(cifexService).checkSession(SESSION_TOKEN);

                    one(cifexService).getUploadStatus(SESSION_TOKEN);
                    UploadStatus uploadStatus = new UploadStatus();
                    uploadStatus.setUploadState(UploadState.ABORTED);
                    will(returnValue(uploadStatus));

                    one(cifexService).finish(SESSION_TOKEN, false);
                }
            });

        command.execute(STORE);
        
        checkEmail("Uploading of zip file");
        assertEquals("Empty tmp folder expected", 0, TMP.listFiles().length);
        assertEquals(INFO_UPLOAD_PREFIX
                + "Zip file <zipfile> with 2 data sets has been successfully created."
                + OSUtilities.LINE_SEPARATOR + WARN_UPLOAD_PREFIX
                + "Uploading of zip file <zipfile> has been aborted or failed."
                + OSUtilities.LINE_SEPARATOR + INFO_MAIL_PREFIX
                + "Sending message from 'a@bc.de' to recipients '[user@bc.de]'",
                getNormalizedLogContent());        
        context.assertIsSatisfied();
    }
}
