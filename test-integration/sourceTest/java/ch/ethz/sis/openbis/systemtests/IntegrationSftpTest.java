package ch.ethz.sis.openbis.systemtests;

import static org.testng.Assert.assertEquals;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.systemtests.common.AbstractIntegrationTest;

public class IntegrationSftpTest extends AbstractIntegrationTest
{

    private static final String SFTP_HOST = "localhost";

    private static final int SFTP_PORT = 2222;

    private static final long SFTP_TIMEOUT = 5000;

    private static final String ENTITY_CODE_PREFIX = "SFTP_TEST_";

    private static final String DSS_DATA_SET_FILE_NAME = "test-file-1.txt";

    private static final String AFS_DATA_SET_FILE_NAME = "test-file-2.txt";

    private static final String DSS_DATA_SET_FILE_CONTENT = "test-content-1";

    private static final String AFS_DATA_SET_FILE_CONTENT = "test-content-2";

    private DataSet dssDataSet;

    private DataSet afsDataSet;

    @BeforeClass public void beforeClass() throws Exception
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.login(INSTANCE_ADMIN, PASSWORD);

        Space space = createSpace(openBIS, "SFTP");
        Project project = createProject(openBIS, space.getPermId(), "SFTP");
        Experiment experiment = createExperiment(openBIS, project.getPermId(), "SFTP");

        dssDataSet = createDataSet(openBIS, experiment.getPermId(), ENTITY_CODE_PREFIX + UUID.randomUUID(), DSS_DATA_SET_FILE_NAME,
                DSS_DATA_SET_FILE_CONTENT.getBytes());

        openBIS.getAfsServerFacade().write(experiment.getPermId().getPermId(), AFS_DATA_SET_FILE_NAME, 0L, AFS_DATA_SET_FILE_CONTENT.getBytes());
        DataSetPermId afsDataSetId = new DataSetPermId(experiment.getPermId().getPermId());
        afsDataSet = openBIS.getDataSets(List.of(afsDataSetId), new DataSetFetchOptions()).get(afsDataSetId);

        log("Created DSS data set " + dssDataSet.getPermId());
        log("Created AFS data set " + afsDataSet.getPermId());

        openBIS.logout();
    }

    @Test
    public void testRootFolderContainsDefaultAndElnViews() throws Exception
    {
        final String folder = "/";

        testSftp(INSTANCE_ADMIN, sftp ->
        {
            List<SftpClient.DirEntry> dirEntries = listDir(sftp, folder);

            assertEquals(dirEntries.size(), 3);
            assertEquals(dirEntries.get(0).getFilename(), ".");
            assertEquals(dirEntries.get(1).getFilename(), "DEFAULT");
            assertEquals(dirEntries.get(2).getFilename(), "ELN-LIMS");
        });
    }

    @Test
    public void testDefaultViewShowsOnlyDssDataSets() throws Exception
    {
        final String folder = "/DEFAULT/SFTP/SFTP/SFTP";

        testSftp(INSTANCE_ADMIN, sftp ->
        {
            List<SftpClient.DirEntry> dirEntries = listDir(sftp, folder);

            assertEquals(dirEntries.size(), 3);
            assertEquals(dirEntries.get(0).getFilename(), ".");
            assertEquals(dirEntries.get(1).getFilename(), "..");
            assertEquals(dirEntries.get(2).getFilename(), dssDataSet.getPermId().getPermId());

            byte[] dssFileContent = readFile(sftp, folder + "/" + dssDataSet.getPermId().getPermId() + "/" + DSS_DATA_SET_FILE_NAME);
            assertEquals(dssFileContent, DSS_DATA_SET_FILE_CONTENT.getBytes());
        });
    }

    @Test
    public void testElnViewShowsBothDssAndAfsDataSets() throws Exception
    {
        final String folder = "/ELN-LIMS/Lab Notebook/SFTP/SFTP/SFTP";

        testSftp(INSTANCE_ADMIN, sftp ->
        {
            List<SftpClient.DirEntry> dirEntries = listDir(sftp, folder);

            assertEquals(dirEntries.size(), 4);
            assertEquals(dirEntries.get(0).getFilename(), ".");
            assertEquals(dirEntries.get(1).getFilename(), "..");
            assertEquals(dirEntries.get(2).getFilename(), afsDataSet.getPermId().getPermId());
            assertEquals(dirEntries.get(3).getFilename(), dssDataSet.getPermId().getPermId());

            byte[] afsFileContent = readFile(sftp, folder + "/" + afsDataSet.getPermId().getPermId() + "/" + AFS_DATA_SET_FILE_NAME);
            assertEquals(afsFileContent, AFS_DATA_SET_FILE_CONTENT.getBytes());

            byte[] dssFileContent = readFile(sftp, folder + "/" + dssDataSet.getPermId().getPermId() + "/" + DSS_DATA_SET_FILE_NAME);
            assertEquals(dssFileContent, DSS_DATA_SET_FILE_CONTENT.getBytes());
        });
    }

    private List<SftpClient.DirEntry> listDir(SftpClient sftp, String dirPath) throws Exception
    {
        try (SftpClient.CloseableHandle handle = sftp.openDir(dirPath))
        {
            return sftp.readDir(handle);
        }
    }

    private byte[] readFile(SftpClient sftp, String filePath) throws Exception
    {
        try (InputStream inputStream = sftp.read(filePath))
        {
            return IOUtils.toByteArray(inputStream);
        }
    }

    private void testSftp(String user, SftpTestAction action) throws Exception
    {
        try (SshClient client = SshClient.setUpDefaultClient())
        {
            client.start();

            try (ClientSession session = client.connect(user, SFTP_HOST, SFTP_PORT)
                    .verify(SFTP_TIMEOUT)
                    .getSession())
            {
                session.addPasswordIdentity(PASSWORD);
                session.auth().verify(SFTP_TIMEOUT);

                try (SftpClient sftp = SftpClientFactory.instance().createSftpClient(session))
                {
                    action.execute(sftp);
                }
            } finally
            {
                client.stop();
            }
        }
    }

    private interface SftpTestAction
    {

        void execute(SftpClient sftp) throws Exception;

    }
}
