package ch.ethz.sis.openbis.systemtests;

import static ch.ethz.sis.transaction.TransactionTestUtil.TestTransaction;
import static ch.ethz.sis.transaction.TransactionTestUtil.assertTransactions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.TransactionConfiguration;
import ch.ethz.sis.openbis.generic.server.asapi.v3.TransactionCoordinatorApi;
import ch.ethz.sis.openbis.systemtests.common.AbstractIntegrationTest;
import ch.ethz.sis.transaction.TransactionStatus;

public class Integration2PCTest extends AbstractIntegrationTest
{

    private static final String ENTITY_CODE_PREFIX = "2PC_TEST_";

    private static final long WAITING_TIME_FOR_FINISHING_TRANSACTIONS = 2000L;

    private static final long WAITING_TIME_FOR_TIMEOUT = 6000L;

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

        openBISWithTr.login(ADMIN, PASSWORD);
        openBISWithNoTr.login(ADMIN, PASSWORD);

        UUID transactionId = openBISWithTr.beginTransaction();

        // create incorrect space (test we can still work with the transaction after the failure)
        try
        {
            openBISWithTr.createSpaces(List.of(new SpaceCreation()));
            fail();
        } catch (Exception e)
        {
            assertTrue(e.getMessage()
                    .startsWith("Transaction '" + transactionId
                            + "' execute operation 'createSpaces' for participant 'application-server' failed with error: Code cannot be empty"));
        }

        // create space
        SpaceCreation spaceCreation = createSpaceCreation();
        SpacePermId spaceId = openBISWithTr.createSpaces(List.of(spaceCreation)).get(0);

        // create sample at AS
        SampleCreation sampleCreation = createSampleCreation(spaceCreation.getCode());
        SamplePermId sampleId = openBISWithTr.createSamples(List.of(sampleCreation)).get(0);

        // write data to AFS
        WriteData writeData = createWriteData(sampleId.getPermId());
        openBISWithTr.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes);

        // create sample2 at AS
        SampleCreation sampleCreation2 = createSampleCreation(spaceCreation.getCode());
        SamplePermId sampleId2 = openBISWithTr.createSamples(List.of(sampleCreation2)).get(0);

        // write data2 to AFS
        WriteData writeData2 = createWriteData(sampleId2.getPermId());
        openBISWithTr.getAfsServerFacade().write(writeData2.owner, writeData2.source, 0L, writeData2.bytes);

        // the transaction session sees created entities before they are committed (except for afs changes with are not visible until commit)
        Space trSpaceBefore = openBISWithTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        Sample trSampleBefore = openBISWithTr.getSamples(List.of(sampleId), new SampleFetchOptions()).get(sampleId);
        Sample trSample2Before = openBISWithTr.getSamples(List.of(sampleId2), new SampleFetchOptions()).get(sampleId2);

        assertNotNull(trSpaceBefore);
        assertNotNull(trSampleBefore);
        assertNotNull(trSample2Before);

        // the non-transaction session does not see created entities before they are committed
        Space noTrSpaceBefore = openBISWithNoTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        Sample noTrSampleBefore = openBISWithNoTr.getSamples(List.of(sampleId), new SampleFetchOptions()).get(sampleId);
        Sample noTrSample2Before = openBISWithNoTr.getSamples(List.of(sampleId2), new SampleFetchOptions()).get(sampleId2);

        assertNull(noTrSpaceBefore);
        assertNull(noTrSampleBefore);
        assertNull(noTrSample2Before);

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

        if (rollback)
        {
            openBISWithTr.rollbackTransaction();
        } else
        {
            openBISWithTr.commitTransaction();
        }

        assertTransactions(getTransactionCoordinator().getTransactionMap());

        Space trSpaceAfter = openBISWithTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        Sample trSampleAfter = openBISWithTr.getSamples(List.of(sampleId), new SampleFetchOptions()).get(sampleId);
        Sample trSample2After = openBISWithTr.getSamples(List.of(sampleId2), new SampleFetchOptions()).get(sampleId2);

        Space noTrSpaceAfter = openBISWithNoTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        Sample noTrSampleAfter = openBISWithNoTr.getSamples(List.of(sampleId), new SampleFetchOptions()).get(sampleId);
        Sample noTrSample2After = openBISWithNoTr.getSamples(List.of(sampleId2), new SampleFetchOptions()).get(sampleId2);

        if (rollback)
        {
            // neither the transaction session nor the non-transaction session see the created entities after the rollback
            assertNull(trSpaceAfter);
            assertNull(trSampleAfter);
            assertNull(trSample2After);

            assertNull(noTrSpaceAfter);
            assertNull(noTrSampleAfter);
            assertNull(noTrSample2After);

            try
            {
                openBISWithTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
                fail();
            } catch (Exception expected)
            {
            }

            try
            {
                openBISWithTr.getAfsServerFacade().read(writeData2.owner, writeData2.source, 0L, writeData2.bytes.length);
                fail();
            } catch (Exception expected)
            {
            }

            try
            {
                openBISWithNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
                fail();
            } catch (Exception expected)
            {
            }

            try
            {
                openBISWithNoTr.getAfsServerFacade().read(writeData2.owner, writeData2.source, 0L, writeData2.bytes.length);
                fail();
            } catch (Exception expected)
            {
            }
        } else
        {
            // both the transaction session and the non-transaction session see the created entities after the commit
            assertNotNull(trSpaceAfter);
            assertNotNull(trSampleAfter);
            assertNotNull(trSample2After);

            assertNotNull(noTrSpaceAfter);
            assertNotNull(noTrSampleAfter);
            assertNotNull(noTrSample2After);

            byte[] trBytesRead = openBISWithTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
            byte[] noTrBytesRead = openBISWithNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);

            assertEquals(trBytesRead, writeData.bytes);
            assertEquals(noTrBytesRead, writeData.bytes);

            byte[] trBytesRead2 = openBISWithTr.getAfsServerFacade().read(writeData2.owner, writeData2.source, 0L, writeData2.bytes.length);
            byte[] noTrBytesRead2 = openBISWithNoTr.getAfsServerFacade().read(writeData2.owner, writeData2.source, 0L, writeData2.bytes.length);

            assertEquals(trBytesRead2, writeData2.bytes);
            assertEquals(noTrBytesRead2, writeData2.bytes);
        }
    }

    @Test
    public void testBeginWithoutSessionToken()
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            openBIS.beginTransaction();
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Session token hasn't been set");
        }
    }

    @Test
    public void testBeginWithoutInteractiveSessionKey()
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.login(ADMIN, PASSWORD);

        try
        {
            openBIS.beginTransaction();
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Interactive session token hasn't been set");
        }
    }

    @Test
    public void testBeginWithIncorrectInteractiveSessionKey()
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey("this is incorrect");
        openBIS.login(ADMIN, PASSWORD);

        try
        {
            openBIS.beginTransaction();
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Invalid interactive session key");
        }
    }

    @Test
    public void testBeginWithAlreadyStartedTransaction()
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBIS.login(ADMIN, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

        try
        {
            openBIS.beginTransaction();
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(),
                    "Operation cannot be executed. Expected no active transactions, but found transaction '" + transactionId + "'.");
        } finally
        {
            openBIS.rollbackTransaction();
        }
    }

    @Test
    public void testBeginMoreThanOneTransactionPerSessionToken()
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);

        OpenBIS openBIS2 = createOpenBIS();
        openBIS2.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);

        String sessionToken = openBIS.login(ADMIN, PASSWORD);
        openBIS2.setSessionToken(sessionToken);

        UUID transactionId = openBIS.beginTransaction();

        try
        {
            openBIS2.beginTransaction();
            fail();
        } catch (Exception e)
        {
            assertTrue(e.getMessage().matches(
                    "Cannot create more than one transaction for the same session token. Transaction that could not be created: '.*'. The already existing and still active transaction: '"
                            + transactionId + "'."));
        } finally
        {
            openBIS.rollbackTransaction();
        }
    }

    @Test
    public void testBeginTooManyTransactions()
    {
        List<OpenBIS> openBISes = new ArrayList<>();

        try
        {
            for (int i = 0; i < TransactionConfiguration.TRANSACTION_COUNT_LIMIT_DEFAULT + 1; i++)
            {
                OpenBIS openBIS = createOpenBIS();
                openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
                openBIS.login(ADMIN, PASSWORD);
                openBIS.beginTransaction();
                openBISes.add(openBIS);
            }
            fail();
        } catch (Exception e)
        {
            assertTrue(e.getMessage().matches(
                    "Cannot create transaction '.*' because the transaction count limit has been reached. Number of existing transactions: "
                            + TransactionConfiguration.TRANSACTION_COUNT_LIMIT_DEFAULT));
        } finally
        {
            for (OpenBIS openBIS : openBISes)
            {
                openBIS.rollbackTransaction();
            }
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
        openBIS.login(ADMIN, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

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

        SampleCreation sampleCreation = createSampleCreation(spaceCreation.getCode());
        SamplePermId sampleId = openBIS.createSamples(List.of(sampleCreation)).get(0);

        WriteData writeData = createWriteData(sampleId.getPermId());
        openBIS.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes);

        openBIS.commitTransaction();

        assertTransactions(getTransactionCoordinator().getTransactionMap());

        // check committed data
        OpenBIS openBISNoTr = createOpenBIS();
        openBISNoTr.login(ADMIN, PASSWORD);

        Space createdSpace = openBISNoTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        assertNotNull(createdSpace);

        Sample createdSample = openBISNoTr.getSamples(List.of(sampleId), new SampleFetchOptions()).get(sampleId);
        assertNotNull(createdSample);

        byte[] bytesRead = openBISNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
        assertEquals(bytesRead, writeData.bytes);
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
        openBIS.login(ADMIN, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

        SpaceCreation spaceCreation = createSpaceCreation();
        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        SampleCreation sampleCreation = createSampleCreation(spaceCreation.getCode());
        SamplePermId sampleId = openBIS.createSamples(List.of(sampleCreation)).get(0);

        WriteData writeData = createWriteData(sampleId.getPermId());
        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

        try
        {
            // first attempt
            openBIS.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes);
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
        openBIS.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes);

        openBIS.commitTransaction();

        assertTransactions(getTransactionCoordinator().getTransactionMap());

        // check committed data
        OpenBIS openBISNoTr = createOpenBIS();
        openBISNoTr.login(ADMIN, PASSWORD);

        Space createdSpace = openBISNoTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        assertNotNull(createdSpace);

        Sample createdSample = openBISNoTr.getSamples(List.of(sampleId), new SampleFetchOptions()).get(sampleId);
        assertNotNull(createdSample);

        byte[] bytesRead = openBISNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
        assertEquals(bytesRead, writeData.bytes);
    }

    @Test
    public void testExecuteOperationFailsAtAS()
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBIS.login(ADMIN, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

        try
        {
            openBIS.createSpaces(Collections.singletonList(new SpaceCreation()));
            fail();
        } catch (Exception e)
        {
            assertTrue(e.getMessage().startsWith("Transaction '" + transactionId
                    + "' execute operation 'createSpaces' for participant 'application-server' failed with error: Code cannot be empty"));
        } finally
        {
            openBIS.rollbackTransaction();
        }
    }

    @Test
    public void testExecuteOperationFailsAtAFS()
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBIS.login(ADMIN, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

        try
        {
            // fails in AfsClient before calling AFS
            openBIS.getAfsServerFacade().create(null, null, false);
            fail();
        } catch (Exception e)
        {
            assertTrue(e.getMessage().startsWith("Transaction '" + transactionId
                    + "' execute operation 'create' for participant 'afs-server' failed with error: owner is marked non-null but is null"));
        } finally
        {
            openBIS.rollbackTransaction();
        }

        UUID transactionId2 = openBIS.beginTransaction();

        try
        {
            // fails in AFS
            openBIS.getAfsServerFacade().read("i-dont-exist", "me-neither", 0L, 0);
            fail();
        } catch (Exception e)
        {
            assertTrue(e.getMessage()
                    .startsWith("Transaction '" + transactionId2 + "' execute operation 'read' for participant 'afs-server' failed with error:"));
            assertTrue(e.getMessage().contains("don't have rights [Read] over i-dont-exist me-neither to perform the operation Read"));
        } finally
        {
            openBIS.rollbackTransaction();
        }
    }

    @Test
    public void testExecuteOldDSSOperation()
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBIS.login(ADMIN, PASSWORD);

        openBIS.beginTransaction();

        try
        {
            openBIS.getDataStoreFacade().searchFiles(new DataSetFileSearchCriteria(), new DataSetFileFetchOptions());
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Transactions are not supported for data store methods.");
        } finally
        {
            openBIS.rollbackTransaction();
        }
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
        openBIS.login(ADMIN, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

        SpaceCreation spaceCreation = createSpaceCreation();
        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        SampleCreation sampleCreation = createSampleCreation(spaceCreation.getCode());
        SamplePermId sampleId = openBIS.createSamples(List.of(sampleCreation)).get(0);

        WriteData writeData = createWriteData(sampleId.getPermId());
        openBIS.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes);

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

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
        openBISNoTr.login(ADMIN, PASSWORD);

        Space createdSpace = openBISNoTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        assertNull(createdSpace);

        Sample createdSample = openBISNoTr.getSamples(List.of(sampleId), new SampleFetchOptions()).get(sampleId);
        assertNull(createdSample);

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
        openBIS.login(ADMIN, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

        SpaceCreation spaceCreation = createSpaceCreation();
        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        SampleCreation sampleCreation = createSampleCreation(spaceCreation.getCode());
        SamplePermId sampleId = openBIS.createSamples(List.of(sampleCreation)).get(0);

        WriteData writeData = createWriteData(sampleId.getPermId());
        openBIS.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes);

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

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
        openBISNoTr.login(ADMIN, PASSWORD);

        Space createdSpace = openBISNoTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        assertNull(createdSpace);

        Sample createdSample = openBISNoTr.getSamples(List.of(sampleId), new SampleFetchOptions()).get(sampleId);
        assertNull(createdSample);

        try
        {
            openBISNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
            fail();
        } catch (Exception expected)
        {
        }
    }

    @Test
    public void testCommitWithoutTransaction()
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBIS.login(ADMIN, PASSWORD);

        try
        {
            openBIS.commitTransaction();
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Operation cannot be executed. No active transaction found.");
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
        openBIS.login(ADMIN, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

        SpaceCreation spaceCreation = createSpaceCreation();
        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        SampleCreation sampleCreation = createSampleCreation(spaceCreation.getCode());
        SamplePermId sampleId = openBIS.createSamples(List.of(sampleCreation)).get(0);

        WriteData writeData = createWriteData(sampleId.getPermId());
        openBIS.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes);

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

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
        openBISNoTr.login(ADMIN, PASSWORD);

        Space createdSpace = openBISNoTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        assertNotNull(createdSpace);

        Sample createdSample = openBISNoTr.getSamples(List.of(sampleId), new SampleFetchOptions()).get(sampleId);
        assertNotNull(createdSample);

        byte[] bytesRead = openBISNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
        assertEquals(bytesRead, writeData.bytes);
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
        openBIS.login(ADMIN, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

        SpaceCreation spaceCreation = createSpaceCreation();
        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        SampleCreation sampleCreation = createSampleCreation(spaceCreation.getCode());
        SamplePermId sampleId = openBIS.createSamples(List.of(sampleCreation)).get(0);

        WriteData writeData = createWriteData(sampleId.getPermId());
        openBIS.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes);

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

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
        openBISNoTr.login(ADMIN, PASSWORD);

        Space createdSpace = openBISNoTr.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
        assertNotNull(createdSpace);

        Sample createdSample = openBISNoTr.getSamples(List.of(sampleId), new SampleFetchOptions()).get(sampleId);
        assertNotNull(createdSample);

        byte[] bytesRead = openBISNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
        assertEquals(bytesRead, writeData.bytes);
    }

    @Test
    public void testASOnlyTransactionCommit()
    {
        testASOnlyTransaction(false);
    }

    @Test
    public void testASOnlyTransactionRollback()
    {
        testASOnlyTransaction(true);
    }

    private void testASOnlyTransaction(boolean rollback)
    {
        // make AFS always fail to make sure it does not interrupt AS only transaction
        setAfsServerProxyInterceptor((method, defaultAction) ->
        {
            throw new RuntimeException("Test AFS exception");
        });

        OpenBIS openBISWithTr = createOpenBIS();
        OpenBIS openBISWithNoTr = createOpenBIS();

        openBISWithTr.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);

        openBISWithTr.login(ADMIN, PASSWORD);
        openBISWithNoTr.login(ADMIN, PASSWORD);

        UUID transactionId = openBISWithTr.beginTransaction();

        SpaceCreation spaceCreation = createSpaceCreation();
        SpacePermId spaceId = openBISWithTr.createSpaces(List.of(spaceCreation)).get(0);

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

        Space trSpaceBefore = openBISWithTr.getSpaces(Collections.singletonList(spaceId), new SpaceFetchOptions()).get(spaceId);
        Space noTrSpaceBefore = openBISWithNoTr.getSpaces(Collections.singletonList(spaceId), new SpaceFetchOptions()).get(spaceId);

        assertNotNull(trSpaceBefore);
        assertNull(noTrSpaceBefore);

        if (rollback)
        {
            openBISWithTr.rollbackTransaction();
        } else
        {
            openBISWithTr.commitTransaction();
        }

        assertTransactions(getTransactionCoordinator().getTransactionMap());

        Space trSpaceAfter = openBISWithTr.getSpaces(Collections.singletonList(spaceId), new SpaceFetchOptions()).get(spaceId);
        Space noTrSpaceAfter = openBISWithNoTr.getSpaces(Collections.singletonList(spaceId), new SpaceFetchOptions()).get(spaceId);

        if (rollback)
        {
            // neither the transaction session nor the non-transaction session see the created entities after the rollback
            assertNull(trSpaceAfter);
            assertNull(noTrSpaceAfter);
        } else
        {
            // both the transaction session and the non-transaction session see the created entities after the commit
            assertNotNull(trSpaceAfter);
            assertNotNull(noTrSpaceAfter);
        }
    }

    @Test
    public void testAFSOnlyTransactionCommit()
    {
        testAFSOnlyTransaction(false);
    }

    @Test
    public void testAFSOnlyTransactionRollback()
    {
        testAFSOnlyTransaction(true);
    }

    private void testAFSOnlyTransaction(boolean rollback)
    {
        OpenBIS openBISWithTr = createOpenBIS();
        OpenBIS openBISWithNoTr = createOpenBIS();

        openBISWithTr.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);

        openBISWithTr.login(ADMIN, PASSWORD);
        openBISWithNoTr.login(ADMIN, PASSWORD);

        SpaceCreation spaceCreation = createSpaceCreation();
        openBISWithNoTr.createSpaces(List.of(spaceCreation));

        SampleCreation sampleCreation = createSampleCreation(spaceCreation.getCode());
        SamplePermId sampleId = openBISWithNoTr.createSamples(List.of(sampleCreation)).get(0);

        UUID transactionId = openBISWithTr.beginTransaction();

        WriteData writeData = createWriteData(sampleId.getPermId());
        openBISWithTr.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes);

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

        if (rollback)
        {
            openBISWithTr.rollbackTransaction();
        } else
        {
            openBISWithTr.commitTransaction();
        }

        assertTransactions(getTransactionCoordinator().getTransactionMap());

        if (rollback)
        {
            // neither the transaction session nor the non-transaction session see the created entities after the rollback
            try
            {
                openBISWithTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
                fail();
            } catch (Exception expected)
            {
            }

            try
            {
                openBISWithNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
                fail();
            } catch (Exception expected)
            {
            }
        } else
        {
            // both the transaction session and the non-transaction session see the created entities after the commit
            byte[] trBytesRead = openBISWithTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
            byte[] noTrBytesRead = openBISWithNoTr.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);

            assertEquals(trBytesRead, writeData.bytes);
            assertEquals(noTrBytesRead, writeData.bytes);
        }
    }

    @Test
    public void testRollbackWithoutTransaction()
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBIS.login(ADMIN, PASSWORD);

        try
        {
            openBIS.rollbackTransaction();
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Operation cannot be executed. No active transaction found.");
        }
    }

    @Test
    public void testTimeout() throws Exception
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBIS.login(ADMIN, PASSWORD);

        UUID transactionId = openBIS.beginTransaction();

        SpaceCreation spaceCreation = createSpaceCreation();
        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        SampleCreation sampleCreation = createSampleCreation(spaceCreation.getCode());
        SamplePermId sampleId = openBIS.createSamples(List.of(sampleCreation)).get(0);

        WriteData writeData = createWriteData(sampleId.getPermId());
        openBIS.getAfsServerFacade().write(writeData.owner, writeData.source, 0L, writeData.bytes);

        assertTransactions(getTransactionCoordinator().getTransactionMap(), new TestTransaction(transactionId, TransactionStatus.BEGIN_FINISHED));

        Thread.sleep(WAITING_TIME_FOR_TIMEOUT);

        assertTransactions(getTransactionCoordinator().getTransactionMap());

        try
        {
            openBIS.getSpaces(Collections.singletonList(spaceId), new SpaceFetchOptions()).get(spaceId);
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Transaction '" + transactionId + "' does not exist.");
        }

        OpenBIS openBIS2 = createOpenBIS();
        openBIS2.login(ADMIN, PASSWORD);

        Space space = openBIS2.getSpaces(Collections.singletonList(spaceId), new SpaceFetchOptions()).get(spaceId);
        assertNull(space);

        Sample sample = openBIS2.getSamples(List.of(sampleId), new SampleFetchOptions()).get(sampleId);
        assertNull(sample);

        try
        {
            openBIS2.getAfsServerFacade().read(writeData.owner, writeData.source, 0L, writeData.bytes.length);
            fail();
        } catch (Exception expected)
        {
        }
    }

    @Test
    public void testRecovery() throws Exception
    {
        // make commit fail at both AS and AFS (prepare will succeed)
        setApplicationServerProxyInterceptor((method, defaultAction) ->
        {
            if (method != null && (method.equals("commitTransaction") || method.equals("commitRecoveredTransaction")))
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
        openBISWithCommittedTransaction.login(ADMIN, PASSWORD);

        UUID committedTransactionId = openBISWithCommittedTransaction.beginTransaction();

        SpaceCreation spaceCreationCommitted = createSpaceCreation();
        SpacePermId spaceIdCommitted = openBISWithCommittedTransaction.createSpaces(List.of(spaceCreationCommitted)).get(0);

        SampleCreation sampleCreationCommitted = createSampleCreation(spaceCreationCommitted.getCode());
        SamplePermId sampleIdCommitted = openBISWithCommittedTransaction.createSamples(List.of(sampleCreationCommitted)).get(0);

        WriteData writeDataCommitted = createWriteData(sampleIdCommitted.getPermId());
        openBISWithCommittedTransaction.getAfsServerFacade()
                .write(writeDataCommitted.owner, writeDataCommitted.source, 0L, writeDataCommitted.bytes);

        openBISWithCommittedTransaction.commitTransaction();

        // transaction 2 is not committed before the crash
        OpenBIS openBISWithNotCommittedTransaction = createOpenBIS();
        openBISWithNotCommittedTransaction.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);
        openBISWithNotCommittedTransaction.login(ADMIN, PASSWORD);

        UUID notCommittedTransactionId = openBISWithNotCommittedTransaction.beginTransaction();

        SpaceCreation spaceCreationNotCommitted = createSpaceCreation();
        SpacePermId spaceIdNotCommitted = openBISWithNotCommittedTransaction.createSpaces(List.of(spaceCreationNotCommitted)).get(0);

        SampleCreation sampleCreationNotCommitted = createSampleCreation(spaceCreationNotCommitted.getCode());
        SamplePermId sampleIdNotCommitted = openBISWithNotCommittedTransaction.createSamples(List.of(sampleCreationNotCommitted)).get(0);

        WriteData writeDataNotCommitted = createWriteData(sampleIdNotCommitted.getPermId());
        openBISWithNotCommittedTransaction.getAfsServerFacade()
                .write(writeDataNotCommitted.owner, writeDataNotCommitted.source, 0L, writeDataNotCommitted.bytes);

        // let's wait for the task that tries to finish failed or abandoned transactions runs
        Thread.sleep(WAITING_TIME_FOR_FINISHING_TRANSACTIONS);

        // check transactions state before the crash
        assertTransactions(getTransactionCoordinator().getTransactionMap(),
                new TestTransaction(committedTransactionId, TransactionStatus.COMMIT_STARTED),
                new TestTransaction(notCommittedTransactionId, TransactionStatus.BEGIN_FINISHED));

        OpenBIS openBISNoTr = createOpenBIS();
        openBISNoTr.login(ADMIN, PASSWORD);

        Space committedSpace = openBISNoTr.getSpaces(List.of(spaceIdCommitted), new SpaceFetchOptions()).get(spaceIdCommitted);
        assertNull(committedSpace);

        Sample committedSample = openBISNoTr.getSamples(List.of(sampleIdCommitted), new SampleFetchOptions()).get(sampleIdCommitted);
        assertNull(committedSample);

        try
        {
            openBISNoTr.getAfsServerFacade().read(writeDataCommitted.owner, writeDataCommitted.source, 0L, writeDataCommitted.bytes.length);
            fail();
        } catch (Exception ignore)
        {
        }

        Space notCommittedSpace = openBISNoTr.getSpaces(List.of(spaceIdNotCommitted), new SpaceFetchOptions()).get(spaceIdNotCommitted);
        assertNull(notCommittedSpace);

        Sample notCommittedSample = openBISNoTr.getSamples(List.of(sampleIdNotCommitted), new SampleFetchOptions()).get(sampleIdNotCommitted);
        assertNull(notCommittedSample);

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
        openBISNoTr.login(ADMIN, PASSWORD);

        assertTransactions(getTransactionCoordinator().getTransactionMap());

        committedSpace = openBISNoTr.getSpaces(List.of(spaceIdCommitted), new SpaceFetchOptions()).get(spaceIdCommitted);
        assertNotNull(committedSpace);

        committedSample = openBISNoTr.getSamples(List.of(sampleIdCommitted), new SampleFetchOptions()).get(sampleIdCommitted);
        assertNotNull(committedSample);

        byte[] committedBytesRead =
                openBISNoTr.getAfsServerFacade().read(writeDataCommitted.owner, writeDataCommitted.source, 0L, writeDataCommitted.bytes.length);
        assertEquals(committedBytesRead, writeDataCommitted.bytes);

        notCommittedSpace = openBISNoTr.getSpaces(List.of(spaceIdNotCommitted), new SpaceFetchOptions()).get(spaceIdNotCommitted);
        assertNull(notCommittedSpace);

        notCommittedSample = openBISNoTr.getSamples(List.of(sampleIdNotCommitted), new SampleFetchOptions()).get(sampleIdNotCommitted);
        assertNull(notCommittedSample);

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
            statement.execute(
                    "DELETE FROM external_data WHERE id in (SELECT id FROM data_all WHERE samp_id in (select id from samples WHERE code LIKE '"
                            + ENTITY_CODE_PREFIX + "%'))");
            statement.execute("DELETE FROM data_all WHERE samp_id in (select id from samples WHERE code LIKE '" + ENTITY_CODE_PREFIX + "%')");
            statement.execute("DELETE FROM samples WHERE code LIKE '" + ENTITY_CODE_PREFIX + "%'");
            statement.execute("DELETE FROM experiments WHERE code LIKE '" + ENTITY_CODE_PREFIX + "%'");
            statement.execute("DELETE FROM projects WHERE code LIKE '" + ENTITY_CODE_PREFIX + "%'");
            statement.execute("DELETE FROM spaces WHERE code LIKE '" + ENTITY_CODE_PREFIX + "%'");
        }
    }

    private static SpaceCreation createSpaceCreation()
    {
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());
        return spaceCreation;
    }

    private static SampleCreation createSampleCreation(String spaceCode)
    {
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode(ENTITY_CODE_PREFIX + UUID.randomUUID());
        sampleCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
        sampleCreation.setSpaceId(new SpacePermId(spaceCode));
        return sampleCreation;
    }

    private static WriteData createWriteData(String owner)
    {
        WriteData writeData = new WriteData();
        writeData.owner = owner;
        writeData.source = "test-source-" + UUID.randomUUID();
        writeData.content = "test-content-" + UUID.randomUUID();
        writeData.bytes = writeData.content.getBytes(StandardCharsets.UTF_8);
        return writeData;
    }

    private static class WriteData
    {
        public String owner;

        public String source;

        public String content;

        public byte[] bytes;

    }

}
