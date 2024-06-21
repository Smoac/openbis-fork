package ch.ethz.sis.openbis.systemtests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.systemtests.common.AbstractIntegrationTest;

public class IntegrationAfsDataTest extends AbstractIntegrationTest
{

    private static final String ENTITY_CODE_PREFIX = "AFS_DATA_TEST_";

    private static final long WAITING_TIME_FOR_ASYNC_TASKS = 5000L;

    @AfterMethod
    public void afterMethod(Method method) throws Exception
    {
        deleteLastSeenDeletionFile();
        super.afterMethod(method);
    }

    @Test
    public void testCreateAfsDataWithoutTransaction() throws Exception
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);

        openBIS.login(INSTANCE_ADMIN, PASSWORD);

        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
        sampleCreation.setSpaceId(new SpacePermId(DEFAULT_SPACE));
        sampleCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());

        // create sample at AS
        SamplePermId sampleId = openBIS.createSamples(List.of(sampleCreation)).get(0);

        assertSampleExistsAtAS(sampleId.getPermId(), true);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), false);
        assertDataSetExistsAtAFS(sampleId.getPermId(), false);

        // create data at AFS
        openBIS.getAfsServerFacade().write(sampleId.getPermId(), "test-file.txt", 0L, "test-content".getBytes());

        assertSampleExistsAtAS(sampleId.getPermId(), true);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), true);
        assertDataSetExistsAtAFS(sampleId.getPermId(), true);

        openBIS.getAfsServerFacade().write(sampleId.getPermId(), "test-file-2.txt", 0L, "test-content-2".getBytes());

        assertSampleExistsAtAS(sampleId.getPermId(), true);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), true);
        assertDataSetExistsAtAFS(sampleId.getPermId(), true);
    }

    @Test
    public void testCreateAfsDataIn1PCTransaction() throws Exception
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);

        openBIS.login(INSTANCE_ADMIN, PASSWORD);

        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
        sampleCreation.setSpaceId(new SpacePermId(DEFAULT_SPACE));
        sampleCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());

        // create sample at AS
        SamplePermId sampleId = openBIS.createSamples(List.of(sampleCreation)).get(0);

        // BEGIN
        openBIS.beginTransaction();

        assertSampleExistsAtAS(sampleId.getPermId(), true);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), false);
        assertDataSetExistsAtAFS(sampleId.getPermId(), false);

        // create data at AFS
        openBIS.getAfsServerFacade().write(sampleId.getPermId(), "test-file.txt", 0L, "test-content".getBytes());
        openBIS.getAfsServerFacade().write(sampleId.getPermId(), "test-file-2.txt", 0L, "test-content-2".getBytes());

        assertSampleExistsAtAS(sampleId.getPermId(), true);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), false);
        assertDataSetExistsAtAFS(sampleId.getPermId(), false);

        // COMMIT
        openBIS.commitTransaction();

        assertSampleExistsAtAS(sampleId.getPermId(), true);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), true);
        assertDataSetExistsAtAFS(sampleId.getPermId(), true);
    }

    @Test
    public void testCreateAfsDataIn2PCTransaction() throws Exception
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);

        openBIS.login(INSTANCE_ADMIN, PASSWORD);

        // BEGIN
        openBIS.beginTransaction();

        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
        sampleCreation.setSpaceId(new SpacePermId(DEFAULT_SPACE));
        sampleCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());

        // create sample at AS
        SamplePermId sampleId = openBIS.createSamples(List.of(sampleCreation)).get(0);

        assertSampleExistsAtAS(sampleId.getPermId(), false);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), false);
        assertDataSetExistsAtAFS(sampleId.getPermId(), false);

        // create data at AFS
        openBIS.getAfsServerFacade().write(sampleId.getPermId(), "test-file.txt", 0L, "test-content".getBytes());
        openBIS.getAfsServerFacade().write(sampleId.getPermId(), "test-file-2.txt", 0L, "test-content-2".getBytes());

        assertSampleExistsAtAS(sampleId.getPermId(), false);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), false);
        assertDataSetExistsAtAFS(sampleId.getPermId(), false);

        // COMMIT
        openBIS.commitTransaction();

        assertSampleExistsAtAS(sampleId.getPermId(), true);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), true);
        assertDataSetExistsAtAFS(sampleId.getPermId(), true);
    }

    @Test
    public void testWriteToSampleWithAccess() throws Exception
    {
        OpenBIS openBISInstanceAdmin = createOpenBIS();
        openBISInstanceAdmin.login(INSTANCE_ADMIN, PASSWORD);

        // create sample in TEST space with instance admin user
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
        sampleCreation.setSpaceId(new SpacePermId(TEST_SPACE));
        sampleCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());

        SamplePermId sampleId = openBISInstanceAdmin.createSamples(List.of(sampleCreation)).get(0);

        assertSampleExistsAtAS(sampleId.getPermId(), true);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), false);
        assertDataSetExistsAtAFS(sampleId.getPermId(), false);

        OpenBIS openBISTestSpaceAdmin = createOpenBIS();
        openBISTestSpaceAdmin.login(TEST_SPACE_ADMIN, PASSWORD);

        // write data to the sample with TEST space admin user
        openBISTestSpaceAdmin.getAfsServerFacade().write(sampleId.getPermId(), "test-file.txt", 0L, "test-content".getBytes());

        assertSampleExistsAtAS(sampleId.getPermId(), true);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), true);
        assertDataSetExistsAtAFS(sampleId.getPermId(), true);
    }

    @Test void testWriteToSampleWithoutAccess() throws Exception
    {
        OpenBIS openBISInstanceAdmin = createOpenBIS();
        openBISInstanceAdmin.login(INSTANCE_ADMIN, PASSWORD);

        // create sample in DEFAULT space with instance admin user
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
        sampleCreation.setSpaceId(new SpacePermId(DEFAULT_SPACE));
        sampleCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());

        SamplePermId sampleId = openBISInstanceAdmin.createSamples(List.of(sampleCreation)).get(0);

        assertSampleExistsAtAS(sampleId.getPermId(), true);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), false);
        assertDataSetExistsAtAFS(sampleId.getPermId(), false);

        OpenBIS openBISTestSpaceAdmin = createOpenBIS();
        openBISTestSpaceAdmin.login(TEST_SPACE_ADMIN, PASSWORD);

        try
        {
            // try to write data to the sample with TEST space admin user
            openBISTestSpaceAdmin.getAfsServerFacade().write(sampleId.getPermId(), "test-file.txt", 0L, "test-content".getBytes());
            fail();
        } catch (Exception e)
        {
            assertTrue(e.getMessage().contains("don't have rights [Write] over " + sampleId));
        }

        assertSampleExistsAtAS(sampleId.getPermId(), true);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), false);
        assertDataSetExistsAtAFS(sampleId.getPermId(), false);
    }

    @Test
    public void testWriteToExperimentWithAccess() throws Exception
    {
        OpenBIS openBISInstanceAdmin = createOpenBIS();
        openBISInstanceAdmin.login(INSTANCE_ADMIN, PASSWORD);

        // create experiment in TEST space with instance admin user
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setSpaceId(new SpacePermId(TEST_SPACE));
        projectCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());
        ProjectPermId projectId = openBISInstanceAdmin.createProjects(List.of(projectCreation)).get(0);

        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
        experimentCreation.setProjectId(projectId);
        experimentCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());
        ExperimentPermId experimentId = openBISInstanceAdmin.createExperiments(List.of(experimentCreation)).get(0);

        assertExperimentExistsAtAS(experimentId.getPermId(), true);
        assertAFSDataSetExistsAtAS(experimentId.getPermId(), false);
        assertDataSetExistsAtAFS(experimentId.getPermId(), false);

        OpenBIS openBISTestSpaceAdmin = createOpenBIS();
        openBISTestSpaceAdmin.login(TEST_SPACE_ADMIN, PASSWORD);

        // write data to the experiment with TEST space admin user
        openBISTestSpaceAdmin.getAfsServerFacade().write(experimentId.getPermId(), "test-file.txt", 0L, "test-content".getBytes());

        assertExperimentExistsAtAS(experimentId.getPermId(), true);
        assertAFSDataSetExistsAtAS(experimentId.getPermId(), true);
        assertDataSetExistsAtAFS(experimentId.getPermId(), true);
    }

    @Test
    public void testWriteToExperimentWithoutAccess() throws Exception
    {
        OpenBIS openBISInstanceAdmin = createOpenBIS();
        openBISInstanceAdmin.login(INSTANCE_ADMIN, PASSWORD);

        // create experiment in DEFAULT space with instance admin user
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setSpaceId(new SpacePermId(DEFAULT_SPACE));
        projectCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());
        ProjectPermId projectId = openBISInstanceAdmin.createProjects(List.of(projectCreation)).get(0);

        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
        experimentCreation.setProjectId(projectId);
        experimentCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());
        ExperimentPermId experimentId = openBISInstanceAdmin.createExperiments(List.of(experimentCreation)).get(0);

        assertExperimentExistsAtAS(experimentId.getPermId(), true);
        assertAFSDataSetExistsAtAS(experimentId.getPermId(), false);
        assertDataSetExistsAtAFS(experimentId.getPermId(), false);

        OpenBIS openBISTestSpaceAdmin = createOpenBIS();
        openBISTestSpaceAdmin.login(TEST_SPACE_ADMIN, PASSWORD);

        try
        {
            // try to write data to the experiment with TEST space admin user
            openBISTestSpaceAdmin.getAfsServerFacade().write(experimentId.getPermId(), "test-file.txt", 0L, "test-content".getBytes());
            fail();
        } catch (Exception e)
        {
            assertTrue(e.getMessage().contains("don't have rights [Write] over " + experimentId));
        }

        assertExperimentExistsAtAS(experimentId.getPermId(), true);
        assertAFSDataSetExistsAtAS(experimentId.getPermId(), false);
        assertDataSetExistsAtAFS(experimentId.getPermId(), false);
    }

    @Test
    public void testDeleteExperimentWithAfsDataSet() throws Exception
    {
        OpenBIS openBIS = createOpenBIS();

        openBIS.login(INSTANCE_ADMIN, PASSWORD);

        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());

        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setSpaceId(spaceId);
        projectCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());

        ProjectPermId projectId = openBIS.createProjects(List.of(projectCreation)).get(0);

        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
        experimentCreation.setProjectId(projectId);
        experimentCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());

        ExperimentPermId experimentId = openBIS.createExperiments(List.of(experimentCreation)).get(0);

        assertExperimentExistsAtAS(experimentId.getPermId(), true);
        assertAFSDataSetExistsAtAS(experimentId.getPermId(), false);
        assertDataSetExistsAtAFS(experimentId.getPermId(), false);

        openBIS.getAfsServerFacade().write(experimentId.getPermId(), "test-file.txt", 0L, "test-content".getBytes());
        openBIS.getAfsServerFacade().write(experimentId.getPermId(), "test-file-2.txt", 0L, "test-content-2".getBytes());

        ExperimentDeletionOptions options = new ExperimentDeletionOptions();
        options.setReason("It is just a test");

        assertExperimentExistsAtAS(experimentId.getPermId(), true);
        assertAFSDataSetExistsAtAS(experimentId.getPermId(), true);
        assertDataSetExistsAtAFS(experimentId.getPermId(), true);

        IDeletionId deletionId = openBIS.deleteExperiments(List.of(experimentId), options);
        openBIS.confirmDeletions(List.of(deletionId));

        assertExperimentExistsAtAS(experimentId.getPermId(), false);
        assertAFSDataSetExistsAtAS(experimentId.getPermId(), false);
        // we need to wait for both AS events-search-task and AFS serverObserver
        Thread.sleep(WAITING_TIME_FOR_ASYNC_TASKS);
        assertDataSetExistsAtAFS(experimentId.getPermId(), false);
    }

    @Test
    public void testDeleteSampleWithAfsDataSet() throws Exception
    {
        OpenBIS openBIS = createOpenBIS();

        openBIS.login(INSTANCE_ADMIN, PASSWORD);

        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());

        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
        sampleCreation.setSpaceId(spaceId);
        sampleCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());

        SamplePermId sampleId = openBIS.createSamples(List.of(sampleCreation)).get(0);

        assertSampleExistsAtAS(sampleId.getPermId(), true);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), false);
        assertDataSetExistsAtAFS(sampleId.getPermId(), false);

        openBIS.getAfsServerFacade().write(sampleId.getPermId(), "test-file.txt", 0L, "test-content".getBytes());
        openBIS.getAfsServerFacade().write(sampleId.getPermId(), "test-file-2.txt", 0L, "test-content-2".getBytes());

        SampleDeletionOptions options = new SampleDeletionOptions();
        options.setReason("It is just a test");

        assertSampleExistsAtAS(sampleId.getPermId(), true);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), true);
        assertDataSetExistsAtAFS(sampleId.getPermId(), true);

        IDeletionId deletionId = openBIS.deleteSamples(List.of(sampleId), options);
        openBIS.confirmDeletions(List.of(deletionId));

        assertSampleExistsAtAS(sampleId.getPermId(), false);
        assertAFSDataSetExistsAtAS(sampleId.getPermId(), false);
        // we need to wait for both AS events-search-task and AFS serverObserver
        Thread.sleep(WAITING_TIME_FOR_ASYNC_TASKS);
        assertDataSetExistsAtAFS(sampleId.getPermId(), false);
    }

    private void assertExperimentExistsAtAS(String experimentPermId, boolean exists) throws Exception
    {
        try (Connection connection = applicationServerSpringContext.getBean(DataSource.class).getConnection();
                Statement statement = connection.createStatement())
        {
            ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM experiments_all WHERE perm_id = '" + experimentPermId + "'");
            resultSet.next();
            assertEquals(resultSet.getInt(1), exists ? 1 : 0);
        }
    }

    private void assertSampleExistsAtAS(String samplePermId, boolean exists) throws Exception
    {
        try (Connection connection = applicationServerSpringContext.getBean(DataSource.class).getConnection();
                Statement statement = connection.createStatement())
        {
            ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM samples_all WHERE perm_id = '" + samplePermId + "'");
            resultSet.next();
            assertEquals(resultSet.getInt(1), exists ? 1 : 0);
        }
    }

    private void assertDSSDataSetExistsAtAS(String dataSetPermId, boolean exists) throws Exception
    {
        try (Connection connection = applicationServerSpringContext.getBean(DataSource.class).getConnection();
                Statement statement = connection.createStatement())
        {
            ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM data_all WHERE afs_data = 'f' AND code = '" + dataSetPermId + "'");
            resultSet.next();
            assertEquals(resultSet.getInt(1), exists ? 1 : 0);
        }
    }

    private void assertAFSDataSetExistsAtAS(String dataSetPermId, boolean exists) throws Exception
    {
        try (Connection connection = applicationServerSpringContext.getBean(DataSource.class).getConnection();
                Statement statement = connection.createStatement())
        {
            ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM data_all WHERE afs_data = 't' AND code = '" + dataSetPermId + "'");
            resultSet.next();
            assertEquals(resultSet.getInt(1), exists ? 1 : 0);
        }
    }

    private void assertDataSetExistsAtAFS(String dataSetPermId, boolean exists) throws Exception
    {
        final String storageRoot = getAfsServerConfiguration().getStringProperty(AtomicFileSystemServerParameter.storageRoot);
        final List<File> dataSetFolders = Files.find(Path.of(storageRoot), Integer.MAX_VALUE,
                        (path, basicFileAttributes) -> path.getFileName().toString().equals(dataSetPermId) && Files.isDirectory(path))
                .map(Path::toFile).collect(Collectors.toList());
        assertEquals(dataSetFolders.size(), exists ? 1 : 0);
    }

    private void deleteLastSeenDeletionFile() throws Exception
    {
        String lastSeenDeletionFile = getAfsServerConfiguration().getStringProperty(AtomicFileSystemServerParameter.openBISLastSeenDeletionFile);
        Files.deleteIfExists(Path.of(lastSeenDeletionFile));
    }

}
