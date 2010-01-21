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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXUploader;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExternalDataTranslator;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = UploadingCommand.class)
public class UploadingCommandTest extends AssertJUnit
{
    private static final String ZIP_FILENAME = "myData";

    private static final String INFO_UPLOAD_PREFIX = "INFO  OPERATION.UploadingCommand - ";

    private static final String WARN_UPLOAD_PREFIX = "WARN  OPERATION.UploadingCommand - ";

    private static final String INFO_MAIL_PREFIX = "INFO  OPERATION.MailClient - ";

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

    private ICIFEXComponent cifex;

    private ICIFEXUploader uploader;

    private MailClientParameters mailClientParameters;

    private DataSetUploadContext uploadContext;

    private DataSetUploadContext uploadContextNoPasswordAuthenticated;

    private DataSetUploadContext uploadContextNoPasswordNotAuthenticated;

    private List<ExternalData> dataSets;

    private UploadingCommand command;

    private UploadingCommand commandAdminSession;

    private UploadingCommand commandAdminSessionNotAuthenticated;

    private File ds2;

    @BeforeMethod
    public void setup()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        factory = context.mock(ICIFEXRPCServiceFactory.class);
        cifex = context.mock(ICIFEXComponent.class);
        uploader = context.mock(ICIFEXUploader.class);
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
        uploadContext.setFileName(ZIP_FILENAME);
        uploadContextNoPasswordAuthenticated = new DataSetUploadContext();
        uploadContextNoPasswordAuthenticated.setCifexURL("cifexURL");
        uploadContextNoPasswordAuthenticated.setUserID("user");
        uploadContextNoPasswordAuthenticated.setPassword("");
        uploadContextNoPasswordAuthenticated.setSessionUserID("user");
        uploadContextNoPasswordAuthenticated.setUserEMail("user@bc.de");
        uploadContextNoPasswordAuthenticated.setFileName(ZIP_FILENAME);
        uploadContextNoPasswordNotAuthenticated = new DataSetUploadContext();
        uploadContextNoPasswordNotAuthenticated.setCifexURL("cifexURL");
        uploadContextNoPasswordNotAuthenticated.setUserID("user");
        uploadContextNoPasswordNotAuthenticated.setPassword("");
        uploadContextNoPasswordNotAuthenticated.setSessionUserID("anotherUser");
        uploadContextNoPasswordNotAuthenticated.setUserEMail("user@bc.de");
        uploadContextNoPasswordNotAuthenticated.setFileName(ZIP_FILENAME);
        createTestData(LOCATION1);
        ds2 = createTestData(LOCATION2);
        ExternalData dataSet1 =
                ExternalDataTranslator.translate(createDataSet("1", LOCATION1), "?", "?",
                        ExperimentTranslator.LoadableFields.PROPERTIES);
        System.out.println("ds1:" + dataSet1.getExperiment().getProperties());
        ExternalData dataSet2 =
                ExternalDataTranslator.translate(createDataSet("2", LOCATION2), "?", "?",
                        ExperimentTranslator.LoadableFields.PROPERTIES);
        dataSets = Arrays.<ExternalData> asList(dataSet1, dataSet2);
        command =
                new UploadingCommand(factory, mailClientParameters, dataSets, uploadContext, null,
                        null);
        commandAdminSession =
                new UploadingCommand(factory, mailClientParameters, dataSets,
                        uploadContextNoPasswordAuthenticated, "admin", "admpwd");
        commandAdminSessionNotAuthenticated =
                new UploadingCommand(factory, mailClientParameters, dataSets,
                        uploadContextNoPasswordNotAuthenticated, "admin", "admpwd");
        command.deleteAfterUploading = false;
        commandAdminSession.deleteAfterUploading = false;
        commandAdminSessionNotAuthenticated.deleteAfterUploading = false;
    }

    private ExternalDataPE createDataSet(String code, String location)
    {
        ExternalDataPE externalData = new ExternalDataPE();
        externalData.setCode(code);
        externalData.setLocation(location);
        externalData.setDerived(true); // measured == (derived == false)
        DataSetTypePE dataSetTypePE = new DataSetTypePE();
        dataSetTypePE.setCode("D");
        externalData.setDataSetType(dataSetTypePE);
        externalData.setExperiment(createExperiment());
        // TODO 2009-09-15, Piotr Buczek: write a test with no parents but with sample connection
        // Does it make any difference how many parents are added here?
        externalData.addParent(createParent("parent1"));
        externalData.addParent(createParent("parent2"));
        externalData.setDataStore(new DataStorePE());
        return externalData;
    }

    private DataPE createParent(String code)
    {
        DataPE data = new DataPE();
        data.setCode(code);
        return data;
    }

    private ExperimentPE createExperiment()
    {
        ExperimentPE experiment = new ExperimentPE();
        experiment.setCode("exp1");
        ExperimentTypePE experimentTypePE = new ExperimentTypePE();
        experimentTypePE.setCode("E");
        experiment.setExperimentType(experimentTypePE);
        experiment.setRegistrationDate(new Date(0));
        PersonPE person = new PersonPE();
        person.setFirstName("Charles");
        person.setLastName("Darwin");
        person.setEmail("cd@cd.org");
        experiment.setRegistrator(person);
        ProjectPE project = new ProjectPE();
        project.setCode("p1");
        GroupPE group = new GroupPE();
        group.setCode("g1");
        DatabaseInstancePE instance = new DatabaseInstancePE();
        instance.setCode("instance");
        instance.setOriginalSource(true);
        group.setDatabaseInstance(instance);
        project.setGroup(group);
        experiment.setProject(project);
        LinkedHashSet<EntityPropertyPE> properties = new LinkedHashSet<EntityPropertyPE>();
        experiment.setProperties(properties);
        return experiment;
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
    public void testExecute() throws Exception
    {
        uploadContext.setPassword("pwd");
        context.checking(new Expectations()
            {
                {
                    one(factory).createCIFEXComponent();
                    will(returnValue(cifex));

                    one(cifex).login(uploadContext.getUserID(), uploadContext.getPassword());
                    will(returnValue(SESSION_TOKEN));

                    one(cifex).createUploader(SESSION_TOKEN);
                    will(returnValue(uploader));

                    final IProgressListener[] listener = new IProgressListener[1];
                    one(uploader).addProgressListener(with(any(IProgressListener.class)));
                    will(new CustomAction("store listener")
                        {
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                listener[0] = (IProgressListener) invocation.getParameter(0);
                                return null;
                            }
                        });

                    one(uploader).upload(
                            Collections.singletonList(new File(TMP, ZIP_FILENAME + ".zip")),
                            "id:user", null);
                    will(new CustomAction("report 'finish' to listener")
                        {
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                listener[0].finished(true, Collections.<String> emptyList(),
                                        Collections.<Throwable> emptyList());
                                return null;
                            }
                        });
                }
            });

        logRecorder.resetLogContent();
        command.execute(STORE);

        assertEquals("no emails expected", false, EMAILS.exists());
        assertEquals(1, TMP.listFiles().length);
        checkZipFileContent(TMP.listFiles()[0]);
        assertEquals(INFO_UPLOAD_PREFIX
                + "Zip file <zipfile> with 2 data sets has been successfully created."
                + OSUtilities.LINE_SEPARATOR + INFO_UPLOAD_PREFIX
                + "Zip file <zipfile> has been successfully uploaded.", getNormalizedLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteAdminSession() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    one(factory).createCIFEXComponent();
                    will(returnValue(cifex));

                    one(cifex).login("admin", "admpwd");
                    will(returnValue(SESSION_TOKEN));

                    one(cifex).setSessionUser(SESSION_TOKEN,
                            uploadContextNoPasswordAuthenticated.getUserID());

                    one(cifex).createUploader(SESSION_TOKEN);
                    will(returnValue(uploader));

                    final IProgressListener[] listener = new IProgressListener[1];
                    one(uploader).addProgressListener(with(any(IProgressListener.class)));
                    will(new CustomAction("store listener")
                        {
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                listener[0] = (IProgressListener) invocation.getParameter(0);
                                return null;
                            }
                        });

                    one(uploader).upload(
                            Collections.singletonList(new File(TMP, ZIP_FILENAME + ".zip")),
                            "id:user", null);
                    will(new CustomAction("report 'finish' to listener")
                        {
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                listener[0].finished(true, Collections.<String> emptyList(),
                                        Collections.<Throwable> emptyList());
                                return null;
                            }
                        });
                }
            });

        logRecorder.resetLogContent();
        commandAdminSession.execute(STORE);

        assertEquals("no emails expected", false, EMAILS.exists());
        assertEquals(1, TMP.listFiles().length);
        checkZipFileContent(TMP.listFiles()[0]);
        assertEquals(INFO_UPLOAD_PREFIX
                + "Zip file <zipfile> with 2 data sets has been successfully created."
                + OSUtilities.LINE_SEPARATOR + INFO_UPLOAD_PREFIX
                + "Zip file <zipfile> has been successfully uploaded.", getNormalizedLogContent());
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testExecuteAdminSessionNotAuthenticated() throws Exception
    {
        try
        {
            context.checking(new Expectations()
                {
                    {
                        one(factory).createCIFEXComponent();
                        will(returnValue(cifex));
    
                        one(cifex).login(uploadContextNoPasswordNotAuthenticated.getUserID(),
                                uploadContextNoPasswordNotAuthenticated.getPassword());
                        will(throwException(new AuthorizationFailureException("forget it!")));
                    }
                });
    
            logRecorder.resetLogContent();
            commandAdminSessionNotAuthenticated.execute(STORE);
        } finally
        {
            context.assertIsSatisfied();
        }
    }

    @Test
    public void testExecuteWithFailedZipFileCreation()
    {
        uploadContext.setPassword("pwd");
        FileUtilities.deleteRecursively(ds2);
        command.execute(STORE);

        checkEmail("Couldn't create zip file");
        assertEquals("ERROR NOTIFY.UploadingCommand - Data set 'ds2' does not exist."
                + OSUtilities.LINE_SEPARATOR + INFO_MAIL_PREFIX
                + "Sending message from 'a@bc.de' to recipients '[user@bc.de]'",
                getNormalizedLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithFailedUpload()
    {
        uploadContext.setPassword("pwd");
        context.checking(new Expectations()
            {
                {
                    one(factory).createCIFEXComponent();
                    will(returnValue(cifex));

                    one(cifex).login(uploadContext.getUserID(), uploadContext.getPassword());
                    will(returnValue(SESSION_TOKEN));

                    one(cifex).createUploader(SESSION_TOKEN);
                    will(returnValue(uploader));

                    final IProgressListener[] listener = new IProgressListener[1];
                    one(uploader).addProgressListener(with(any(IProgressListener.class)));
                    will(new CustomAction("store listener")
                        {
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                listener[0] = (IProgressListener) invocation.getParameter(0);
                                return null;
                            }
                        });

                    one(uploader).upload(
                            Collections.singletonList(new File(TMP, ZIP_FILENAME + ".zip")),
                            "id:user", null);
                    will(new CustomAction("report 'abort' to listener")
                        {
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                listener[0].finished(false, Collections.<String> emptyList(),
                                        Collections.<Throwable> emptyList());
                                return null;
                            }
                        });
                }
            });

        command.execute(STORE);

        checkEmail("Uploading of zip file");
        assertEquals(INFO_UPLOAD_PREFIX
                + "Zip file <zipfile> with 2 data sets has been successfully created."
                + OSUtilities.LINE_SEPARATOR + WARN_UPLOAD_PREFIX
                + "Uploading of zip file <zipfile> has been aborted or failed."
                + OSUtilities.LINE_SEPARATOR + INFO_MAIL_PREFIX
                + "Sending message from 'a@bc.de' to recipients '[user@bc.de]'",
                getNormalizedLogContent());
        context.assertIsSatisfied();
    }

    private void checkZipFileContent(File file) throws Exception
    {
        assertEquals(ZIP_FILENAME + ".zip", file.getName());
        ZipFile zipFile = null;
        try
        {
            String prefix = "g1/p1/exp1/";
            List<String> paths = new ArrayList<String>();
            zipFile = new ZipFile(file);
            for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries
                    .hasMoreElements();)
            {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                assertTrue("actual path: " + name, name.startsWith(prefix));
                paths.add(name.substring(prefix.length()));
            }
            Collections.sort(paths);
            assertEquals("[1/README.TXT, 1/data/data.txt, 1/meta-data.tsv, "
                    + "2/README.TXT, 2/data/data.txt, 2/meta-data.tsv]", paths.toString());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copy(zipFile.getInputStream(zipFile.getEntry(prefix + "1/meta-data.tsv")),
                    outputStream);
            try
            {
                assertEquals("data_set\tcode\t1\n" + "data_set\tproduction_timestamp\t\n"
                        + "data_set\tproducer_code\t\n" + "data_set\tdata_set_type\tD\n"
                        + "data_set\tis_measured\tFALSE\n" + "data_set\tis_complete\tFALSE\n"
                        + "data_set\tparent_codes\tparent2,parent1\n"
                        + "experiment\tgroup_code\tg1\n" + "experiment\tproject_code\tp1\n"
                        + "experiment\texperiment_code\texp1\n"
                        + "experiment\texperiment_type_code\tE\n"
                        + "experiment\tregistration_timestamp\t1970-01-01 01:00:00 +0100\n"
                        + "experiment\tregistrator\tCharles Darwin <cd@cd.org>\n", outputStream
                        .toString());
            } catch (AssertionError err)
            {
                // We have an ambiguity here: sometimes we get "parent1,parent2", sometimes we get
                // "parent2,parent1"
                assertEquals("data_set\tcode\t1\n" + "data_set\tproduction_timestamp\t\n"
                        + "data_set\tproducer_code\t\n" + "data_set\tdata_set_type\tD\n"
                        + "data_set\tis_measured\tFALSE\n" + "data_set\tis_complete\tFALSE\n"
                        + "data_set\tparent_codes\tparent1,parent2\n"
                        + "experiment\tgroup_code\tg1\n" + "experiment\tproject_code\tp1\n"
                        + "experiment\texperiment_code\texp1\n"
                        + "experiment\texperiment_type_code\tE\n"
                        + "experiment\tregistration_timestamp\t1970-01-01 01:00:00 +0100\n"
                        + "experiment\tregistrator\tCharles Darwin <cd@cd.org>\n", outputStream
                        .toString());
            }
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

    }
}
