package ch.ethz.sis.openbis.systemtests;

import static ch.ethz.sis.transaction.TransactionTestUtil.TestTransaction;
import static ch.ethz.sis.transaction.TransactionTestUtil.assertTransactions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.openbis.generic.asapi.v3.ITransactionCoordinatorApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.TransactionCoordinatorApi;
import ch.ethz.sis.openbis.systemtests.common.AbstractIntegrationTest;
import ch.ethz.sis.transaction.TransactionStatus;
import ch.systemsx.cisd.common.test.AssertionUtil;

public class Integration2PCTest extends AbstractIntegrationTest
{

    private static final String ENTITY_CODE_PREFIX = "TRANSACTION_TEST_";

    private static final long WAITING_TIME_FOR_FINISHING_TRANSACTIONS = 2000L;

    @AfterMethod
    public void afterMethod(Method method) throws Exception
    {
        rollbackPreparedDatabaseTransactions();
        deleteCreatedSpacesProjectsAndExperiments();
        super.afterMethod(method);
    }

    @Test
    public void testTransactionCommit()
    {
        testTransaction(false);
    }

    @Test
    public void testTransactionRollback()
    {
        testTransaction(true);
    }

    private void testTransaction(boolean rollback)
    {
        OpenBIS openBISWithTr = createOpenBIS();
        OpenBIS openBISWithNoTr = createOpenBIS();

        openBISWithTr.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);

        openBISWithTr.login(USER, PASSWORD);
        openBISWithNoTr.login(USER, PASSWORD);

        UUID transactionId = openBISWithTr.beginTransaction();

        WriteData writeData = createWriteData();
        openBISWithTr.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes, writeData.md5);

        SpaceCreation spaceCreation = createSpaceCreation();
        SpacePermId spaceId = openBISWithTr.createSpaces(List.of(spaceCreation)).get(0);

        ProjectCreation projectCreation = createProjectCreation(spaceCreation.getCode());
        ProjectPermId projectId = openBISWithTr.createProjects(List.of(projectCreation)).get(0);

        ExperimentCreation experimentCreation = createExperimentCreation(spaceCreation.getCode(), projectCreation.getCode(), null);

        try
        {
            openBISWithTr.createExperiments(List.of(experimentCreation));
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Operation 'createExperiments' failed.");
            AssertionUtil.assertContains("Type id cannot be null", e.getCause().getMessage());
        }

        ExperimentCreation experimentCreation2 = createExperimentCreation(spaceCreation.getCode(), projectCreation.getCode(), "UNKNOWN");

        ExperimentPermId experimentId = openBISWithTr.createExperiments(List.of(experimentCreation2)).get(0);

        // the transaction session sees created entities before they are committed (except for afs changes with are not visible until commit)
        Space trSpaceBefore = openBISWithTr.getSpaces(Collections.singletonList(spaceId), new SpaceFetchOptions()).get(spaceId);
        Project trProjectBefore = openBISWithTr.getProjects(Collections.singletonList(projectId), new ProjectFetchOptions()).get(projectId);
        Experiment trExperimentBefore =
                openBISWithTr.getExperiments(Collections.singletonList(experimentId), new ExperimentFetchOptions()).get(experimentId);

        assertNotNull(trSpaceBefore);
        assertNotNull(trProjectBefore);
        assertNotNull(trExperimentBefore);

        // the non-transaction session does not see created entities before they are committed
        Space noTrSpaceBefore = openBISWithNoTr.getSpaces(Collections.singletonList(spaceId), new SpaceFetchOptions()).get(spaceId);
        Project noTrProjectBefore = openBISWithNoTr.getProjects(Collections.singletonList(projectId), new ProjectFetchOptions()).get(projectId);
        Experiment noTrExperimentBefore =
                openBISWithNoTr.getExperiments(Collections.singletonList(experimentId), new ExperimentFetchOptions()).get(experimentId);

        assertNull(noTrSpaceBefore);
        assertNull(noTrProjectBefore);
        assertNull(noTrExperimentBefore);

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

        if (rollback)
        {
            openBISWithTr.rollbackTransaction();
        } else
        {
            openBISWithTr.commitTransaction();
        }

        assertTransactions(getTransactionCoordinator().getTransactionMap());

        Space trSpaceAfter = openBISWithTr.getSpaces(Collections.singletonList(spaceId), new SpaceFetchOptions()).get(spaceId);
        Project trProjectAfter = openBISWithTr.getProjects(Collections.singletonList(projectId), new ProjectFetchOptions()).get(projectId);
        Experiment trExperimentAfter =
                openBISWithTr.getExperiments(Collections.singletonList(experimentId), new ExperimentFetchOptions()).get(experimentId);

        Space noTrSpaceAfter = openBISWithNoTr.getSpaces(Collections.singletonList(spaceId), new SpaceFetchOptions()).get(spaceId);
        Project noTrProjectAfter = openBISWithNoTr.getProjects(Collections.singletonList(projectId), new ProjectFetchOptions()).get(projectId);
        Experiment noTrExperimentAfter =
                openBISWithNoTr.getExperiments(Collections.singletonList(experimentId), new ExperimentFetchOptions()).get(experimentId);

        if (rollback)
        {
            // neither the transaction session nor the non-transaction session see the created entities after the rollback
            assertNull(trSpaceAfter);
            assertNull(trProjectAfter);
            assertNull(trExperimentAfter);

            assertNull(noTrSpaceAfter);
            assertNull(noTrProjectAfter);
            assertNull(noTrExperimentAfter);
        } else
        {
            // both the transaction session and the non-transaction session see the created entities after the commit
            assertNotNull(trSpaceAfter);
            assertNotNull(trProjectAfter);
            assertNotNull(trExperimentAfter);

            assertNotNull(noTrSpaceAfter);
            assertNotNull(noTrProjectAfter);
            assertNotNull(noTrExperimentAfter);

            byte[] trBytesRead = openBISWithTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
            byte[] noTrBytesRead = openBISWithNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);

            assertEquals(new String(trBytesRead, StandardCharsets.UTF_8), writeData.content);
            assertEquals(new String(noTrBytesRead, StandardCharsets.UTF_8), writeData.content);
        }
    }

    @Test
    public void testBeginFailsAtAS()
    {
        // make begin fail at AS
        setApplicationServerProxyInterceptor((method, defaultAction) ->
        {
            if (method != null && method.equals("beginTransaction"))
            {
                throw new RuntimeException("Test begin exception");
            } else
            {
                defaultAction.call();
            }
        });

        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBIS.login(USER, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

        WriteData writeData = createWriteData();
        openBIS.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes, writeData.md5);

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

        SpaceCreation spaceCreation = createSpaceCreation();

        try
        {
            // first attempt
            openBIS.createSpaces(List.of(spaceCreation));
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(),
                    "Transaction '" + transactionId + "' execute operation 'createSpaces' for participant 'application-server' failed.");
            assertEquals(e.getCause().getMessage(),
                    "Begin transaction '" + transactionId + "' failed for participant 'application-server'.");
        }

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

        // make begin succeed at AS
        setApplicationServerProxyInterceptor((method, defaultAction) -> defaultAction.call());

        // second attempt
        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        openBIS.commitTransaction();

        assertTransactions(getTransactionCoordinator().getTransactionMap());

        // check committed data
        OpenBIS openBISNoTr = createOpenBIS();
        openBISNoTr.login(USER, PASSWORD);

        Space createdSpace = openBISNoTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        assertNotNull(createdSpace);

        byte[] bytesRead = openBISNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
        assertEquals(new String(bytesRead, StandardCharsets.UTF_8), writeData.content);
    }

    @Test
    public void testBeginFailsAtAFS()
    {
        // make begin fail at AFS
        setAfsServerProxyInterceptor((method, defaultAction) ->
        {
            if (method != null && method.equals("begin"))
            {
                throw new RuntimeException("Test begin exception");
            } else
            {
                defaultAction.call();
            }
        });

        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBIS.login(USER, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

        SpaceCreation spaceCreation = createSpaceCreation();
        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        WriteData writeData = createWriteData();
        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

        try
        {
            // first attempt
            openBIS.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes, writeData.md5);
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(),
                    "Transaction '" + transactionId + "' execute operation 'write' for participant 'afs-server' failed.");
            assertEquals(e.getCause().getMessage(), "Begin transaction '" + transactionId + "' failed for participant 'afs-server'.");
        }

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

        // make begin succeed at AFS
        setAfsServerProxyInterceptor((method, defaultAction) -> defaultAction.call());

        // second attempt
        openBIS.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes, writeData.md5);

        openBIS.commitTransaction();

        assertTransactions(getTransactionCoordinator().getTransactionMap());

        // check committed data
        OpenBIS openBISNoTr = createOpenBIS();
        openBISNoTr.login(USER, PASSWORD);

        Space createdSpace = openBISNoTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        assertNotNull(createdSpace);

        byte[] bytesRead = openBISNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
        assertEquals(new String(bytesRead, StandardCharsets.UTF_8), writeData.content);
    }

    @Test
    public void testPrepareFailsAtAS()
    {
        // make prepare fail at AS
        setApplicationServerProxyInterceptor((method, defaultAction) ->
        {
            if (method != null && method.equals("prepareTransaction"))
            {
                throw new RuntimeException("Test prepare exception");
            } else
            {
                defaultAction.call();
            }
        });

        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBIS.login(USER, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

        WriteData writeData = createWriteData();
        openBIS.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes, writeData.md5);

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

        SpaceCreation spaceCreation = createSpaceCreation();
        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        try
        {
            openBIS.commitTransaction();
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(),
                    "Commit transaction '" + transactionId + "' failed.");
            assertEquals(e.getCause().getMessage(),
                    "Prepare transaction '" + transactionId
                            + "' failed for participant 'application-server'. The transaction was rolled back.");
        }

        assertTransactions(getTransactionCoordinator().getTransactionMap());

        // check data hasn't been committed
        OpenBIS openBISNoTr = createOpenBIS();
        openBISNoTr.login(USER, PASSWORD);

        Space createdSpace = openBISNoTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        assertNull(createdSpace);

        try
        {
            openBISNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
            fail();
        } catch (Exception expected)
        {
        }
    }

    @Test
    public void testPrepareFailsAtAFS()
    {
        // make prepare fail at AFS
        setAfsServerProxyInterceptor((method, defaultAction) ->
        {
            if (method != null && method.equals("prepare"))
            {
                throw new RuntimeException("Test prepare exception");
            } else
            {
                defaultAction.call();
            }
        });

        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBIS.login(USER, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

        WriteData writeData = createWriteData();
        openBIS.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes, writeData.md5);

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

        SpaceCreation spaceCreation = createSpaceCreation();
        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        try
        {
            openBIS.commitTransaction();
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(),
                    "Commit transaction '" + transactionId + "' failed.");
            assertEquals(e.getCause().getMessage(),
                    "Prepare transaction '" + transactionId
                            + "' failed for participant 'afs-server'. The transaction was rolled back.");
        }

        assertTransactions(getTransactionCoordinator().getTransactionMap());

        // check data hasn't been committed
        OpenBIS openBISNoTr = createOpenBIS();
        openBISNoTr.login(USER, PASSWORD);

        Space createdSpace = openBISNoTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        assertNull(createdSpace);

        try
        {
            openBISNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
            fail();
        } catch (Exception expected)
        {
        }
    }

    @Test
    public void testCommitFailsAtAS() throws Exception
    {
        // make commit fail at AS
        setApplicationServerProxyInterceptor((method, defaultAction) ->
        {
            if (method != null && method.equals("commitTransaction"))
            {
                throw new RuntimeException("Test commit exception");
            } else
            {
                defaultAction.call();
            }
        });

        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBIS.login(USER, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

        WriteData writeData = createWriteData();
        openBIS.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes, writeData.md5);

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

        SpaceCreation spaceCreation = createSpaceCreation();
        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        // commit (no exception is thrown because prepare succeeded at both AS and AFS, the failed commit at AS will be internally retried)
        openBIS.commitTransaction();

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.COMMIT_STARTED));

        // make commit succeed at AS
        setApplicationServerProxyInterceptor((method, defaultAction) -> defaultAction.call());

        // let's wait for the task that tries to finish failed or abandoned transactions runs
        Thread.sleep(WAITING_TIME_FOR_FINISHING_TRANSACTIONS);

        assertTransactions(getTransactionCoordinator().getTransactionMap());

        // check committed data
        OpenBIS openBISNoTr = createOpenBIS();
        openBISNoTr.login(USER, PASSWORD);

        Space createdSpace = openBISNoTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        assertNotNull(createdSpace);

        byte[] bytesRead = openBISNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
        assertEquals(new String(bytesRead, StandardCharsets.UTF_8), writeData.content);
    }

    @Test
    public void testCommitFailsAtAFS() throws Exception
    {
        // make commit fail at AFS
        setAfsServerProxyInterceptor((method, defaultAction) ->
        {
            if (method != null && method.equals("commit"))
            {
                throw new RuntimeException("Test commit exception");
            } else
            {
                defaultAction.call();
            }
        });

        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBIS.login(USER, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

        WriteData writeData = createWriteData();
        openBIS.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes, writeData.md5);

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

        SpaceCreation spaceCreation = createSpaceCreation();
        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        // commit (no exception is thrown because prepare succeeded at both AS and AFS, the failed commit at AFS will be internally retried)
        openBIS.commitTransaction();

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.COMMIT_STARTED));

        // make commit succeed at AFS
        setAfsServerProxyInterceptor((method, defaultAction) -> defaultAction.call());

        // let's wait for the task that tries to finish failed or abandoned transactions runs
        Thread.sleep(WAITING_TIME_FOR_FINISHING_TRANSACTIONS);

        assertTransactions(getTransactionCoordinator().getTransactionMap());

        // check committed data
        OpenBIS openBISNoTr = createOpenBIS();
        openBISNoTr.login(USER, PASSWORD);

        Space createdSpace = openBISNoTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        assertNotNull(createdSpace);

        byte[] bytesRead = openBISNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
        assertEquals(new String(bytesRead, StandardCharsets.UTF_8), writeData.content);
    }

    @Test
    public void testRecovery() throws Exception
    {
        // make commit fail at both AS and AFS (prepare will succeed)
        setApplicationServerProxyInterceptor((method, defaultAction) ->
        {
            if (method != null && method.equals("commitTransaction"))
            {
                throw new RuntimeException("Test commit exception");
            } else
            {
                defaultAction.call();
            }
        });
        setAfsServerProxyInterceptor((method, defaultAction) ->
        {
            if (method != null && method.equals("commit"))
            {
                throw new RuntimeException("Test commit exception");
            } else
            {
                defaultAction.call();
            }
        });

        // transaction 1 is committed before the crash (prepare is successful but commit fails at both AS and AFS)
        OpenBIS openBISWithCommittedTransaction = createOpenBIS();
        openBISWithCommittedTransaction.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBISWithCommittedTransaction.login(USER, PASSWORD);

        UUID committedTransactionId = openBISWithCommittedTransaction.beginTransaction();

        SpaceCreation spaceCreationCommitted = createSpaceCreation();
        SpacePermId spaceIdCommitted = openBISWithCommittedTransaction.createSpaces(List.of(spaceCreationCommitted)).get(0);

        WriteData writeDataCommitted = createWriteData();
        openBISWithCommittedTransaction.getAfsServerFacade()
                .write(writeDataCommitted.owner, writeDataCommitted.source, 0L, writeDataCommitted.bytes, writeDataCommitted.md5);

        openBISWithCommittedTransaction.commitTransaction();

        // transaction 2 is not committed before the crash
        OpenBIS openBISWithNotCommittedTransaction = createOpenBIS();
        openBISWithNotCommittedTransaction.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBISWithNotCommittedTransaction.login(USER, PASSWORD);

        UUID notCommittedTransactionId = openBISWithNotCommittedTransaction.beginTransaction();

        SpaceCreation spaceCreationNotCommitted = createSpaceCreation();
        SpacePermId spaceIdNotCommitted = openBISWithNotCommittedTransaction.createSpaces(List.of(spaceCreationNotCommitted)).get(0);

        WriteData writeDataNotCommitted = createWriteData();
        openBISWithNotCommittedTransaction.getAfsServerFacade()
                .write(writeDataNotCommitted.owner, writeDataNotCommitted.source, 0L, writeDataNotCommitted.bytes, writeDataNotCommitted.md5);

        // check transactions state before the crash
        assertTransactions(getTransactionCoordinator().getTransactionMap(),
                new TestTransaction(committedTransactionId, TransactionStatus.COMMIT_STARTED),
                new TestTransaction(notCommittedTransactionId, TransactionStatus.BEGIN_FINISHED));

        OpenBIS openBISNoTr = createOpenBIS();
        openBISNoTr.login(USER, PASSWORD);

        Space committedSpace = openBISNoTr.getSpaces(List.of(spaceIdCommitted), new SpaceFetchOptions()).get(spaceIdCommitted);
        assertNull(committedSpace);

        try
        {
            openBISNoTr.getAfsServerFacade().read(writeDataCommitted.owner, writeDataCommitted.source, 0L, writeDataCommitted.bytes.length);
            fail();
        } catch (Exception ignore)
        {
        }

        Space notCommittedSpace = openBISNoTr.getSpaces(List.of(spaceIdNotCommitted), new SpaceFetchOptions()).get(spaceIdNotCommitted);
        assertNull(notCommittedSpace);

        try
        {
            openBISNoTr.getAfsServerFacade().read(writeDataNotCommitted.owner, writeDataNotCommitted.source, 0L, writeDataNotCommitted.bytes.length);
            fail();
        } catch (Exception ignore)
        {
        }

        // simulate servers crash
        restartApplicationServer();
        restartAfsServer();

        // make commit succeed at both AS and AFS
        setApplicationServerProxyInterceptor((method, defaultAction) -> defaultAction.call());
        setAfsServerProxyInterceptor((method, defaultAction) -> defaultAction.call());

        // let's wait for the task that tries to finish failed or abandoned transactions runs
        Thread.sleep(WAITING_TIME_FOR_FINISHING_TRANSACTIONS);

        // check transactions state after the crash
        openBISNoTr.login(USER, PASSWORD);

        assertTransactions(getTransactionCoordinator().getTransactionMap());

        committedSpace = openBISNoTr.getSpaces(List.of(spaceIdCommitted), new SpaceFetchOptions()).get(spaceIdCommitted);
        assertNotNull(committedSpace);

        byte[] committedBytesRead =
                openBISNoTr.getAfsServerFacade().read(writeDataCommitted.owner, writeDataCommitted.source, 0L, writeDataCommitted.bytes.length);
        assertEquals(new String(committedBytesRead, StandardCharsets.UTF_8), writeDataCommitted.content);

        notCommittedSpace = openBISNoTr.getSpaces(List.of(spaceIdNotCommitted), new SpaceFetchOptions()).get(spaceIdNotCommitted);
        assertNull(notCommittedSpace);

        try
        {
            openBISNoTr.getAfsServerFacade().read(writeDataNotCommitted.owner, writeDataNotCommitted.source, 0L, writeDataNotCommitted.bytes.length);
            fail();
        } catch (Exception ignore)
        {
        }
    }

    private TransactionCoordinatorApi getTransactionCoordinator()
    {
        return (TransactionCoordinatorApi) applicationServerSpringContext.getBean(ITransactionCoordinatorApi.class);
    }

    private void rollbackPreparedDatabaseTransactions() throws Exception
    {
        try (Connection connection = applicationServerSpringContext.getBean(DataSource.class).getConnection();
                Statement statement = connection.createStatement())
        {
            List<String> preparedTransactionIds = new ArrayList<>();

            ResultSet preparedTransactions = statement.executeQuery("SELECT gid FROM pg_prepared_xacts");
            while (preparedTransactions.next())
            {
                preparedTransactionIds.add(preparedTransactions.getString(1));
            }

            for (String preparedTransactionId : preparedTransactionIds)
            {
                statement.execute("ROLLBACK PREPARED '" + preparedTransactionId + "'");
            }
        }
    }

    private void deleteCreatedSpacesProjectsAndExperiments() throws Exception
    {
        try (Connection connection = applicationServerSpringContext.getBean(DataSource.class).getConnection();
                Statement statement = connection.createStatement())
        {
            statement.execute("DELETE FROM experiments WHERE code LIKE '" + ENTITY_CODE_PREFIX + "%'");
            statement.execute("DELETE FROM projects WHERE code LIKE '" + ENTITY_CODE_PREFIX + "%'");
            statement.execute("DELETE FROM spaces WHERE code LIKE '" + ENTITY_CODE_PREFIX + "%'");
        }
    }

    private static SpaceCreation createSpaceCreation(){
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());
        return spaceCreation;
    }

    private static ProjectCreation createProjectCreation(String spaceCode){
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setSpaceId(new SpacePermId(spaceCode));
        projectCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());
        return projectCreation;
    }

    private static ExperimentCreation createExperimentCreation(String spaceCode, String projectCode, String experimentTypeCode){
        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setProjectId(new ProjectIdentifier(spaceCode, projectCode));
        experimentCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());
        experimentCreation.setTypeId(experimentTypeCode != null ? new EntityTypePermId(experimentTypeCode) : null);
        return experimentCreation;
    }

    private static WriteData createWriteData()
    {
        WriteData writeData = new WriteData();
        writeData.owner = "test-owner-" + UUID.randomUUID();
        writeData.source = "test-source-" + UUID.randomUUID();
        writeData.content = "test-content";
        writeData.bytes = writeData.content.getBytes(StandardCharsets.UTF_8);
        writeData.md5 = calculateMD5(writeData.bytes);
        return writeData;
    }

    private static byte[] calculateMD5(byte[] data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            return md.digest();
        } catch (Exception e)
        {
            throw new RuntimeException("Checksum calculation failed", e);
        }
    }

    private static class WriteData
    {
        public String owner;

        public String source;

        public String content;

        public byte[] bytes;

        public byte[] md5;
    }

}
