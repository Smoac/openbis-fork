package ch.ethz.sis.transaction;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

public class TransactionLogTest
{

    public static final UUID TEST_TRANSACTION_ID = UUID.randomUUID();

    public static final UUID TEST_TRANSACTION_ID_2 = UUID.randomUUID();

    private File testWorkspace;

    @BeforeTest
    protected void beforeTest() throws IOException
    {
        testWorkspace = Files.createTempDirectory(TransactionLogTest.class.getSimpleName()).toFile();
    }

    @AfterTest
    protected void afterTest()
    {
        if (testWorkspace != null && testWorkspace.exists())
        {
            FileUtilities.deleteRecursively(testWorkspace);
        }
    }

    @Test
    public void testCreateWithNonExistentFolder()
    {
        File nonExistentFolder = new File(testWorkspace, UUID.randomUUID().toString());
        assertFalse(nonExistentFolder.exists());

        new TransactionLog(nonExistentFolder);

        assertTrue(nonExistentFolder.exists());
        assertTrue(nonExistentFolder.isDirectory());
    }

    @Test
    public void testCreateWithExistingFolderAndStatuses() throws IOException
    {
        File existingLogFolder = new File(testWorkspace, UUID.randomUUID().toString());
        Files.createDirectory(existingLogFolder.toPath());
        assertTrue(existingLogFolder.exists());

        File transaction1Folder = createFolder(new File(existingLogFolder, TEST_TRANSACTION_ID.toString()));
        createFile(new File(transaction1Folder, TransactionStatus.BEGIN_STARTED.name()));
        createFile(new File(transaction1Folder, TransactionStatus.BEGIN_FINISHED.name()));
        createFile(new File(transaction1Folder, TransactionStatus.PREPARE_STARTED.name()));
        createFolder(new File(transaction1Folder, "some_folder"));
        createFile(new File(transaction1Folder, "some_file_with_name_which_is_not_status"));

        File transaction2Folder = createFolder(new File(existingLogFolder, TEST_TRANSACTION_ID_2.toString()));
        createFile(new File(transaction2Folder, TransactionStatus.BEGIN_STARTED.name()));
        createFile(new File(transaction2Folder, TransactionStatus.BEGIN_FINISHED.name()));
        createFile(new File(transaction2Folder, TransactionStatus.PREPARE_STARTED.name()));
        createFile(new File(transaction2Folder, TransactionStatus.ROLLBACK_STARTED.name()));
        createFile(new File(transaction2Folder, TransactionStatus.ROLLBACK_FINISHED.name()));

        createFolder(new File(existingLogFolder, "some_folder"));
        createFile(new File(existingLogFolder, "some_file"));

        ITransactionLog transactionLog = new TransactionLog(existingLogFolder);

        Map<UUID, TransactionStatus> expectedLastStatuses = new HashMap<>();
        expectedLastStatuses.put(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED);
        expectedLastStatuses.put(TEST_TRANSACTION_ID_2, TransactionStatus.ROLLBACK_FINISHED);

        assertEquals(transactionLog.getLastStatuses(), expectedLastStatuses);
    }

    @Test
    public void testCreateWithNotAFolder() throws IOException
    {
        File existingFile = new File(testWorkspace, UUID.randomUUID().toString());
        Files.createFile(existingFile.toPath());
        assertTrue(existingFile.exists());

        try
        {
            new TransactionLog(existingFile);
        } catch (Exception e)
        {
            assertEquals(e.getCause().getMessage(), "Folder '" + existingFile.getAbsolutePath() + "' is not a directory");
        }
    }

    @Test
    public void testLogStatus() throws IOException
    {
        File logFolder = new File(testWorkspace, UUID.randomUUID().toString());
        ITransactionLog transactionLog = new TransactionLog(logFolder);

        assertTransactionFolders(logFolder);

        transactionLog.logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
        transactionLog.logStatus(TEST_TRANSACTION_ID_2, TransactionStatus.PREPARE_STARTED);
        transactionLog.logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

        assertTransactionFolders(logFolder, TEST_TRANSACTION_ID.toString(), TEST_TRANSACTION_ID_2.toString());
        assertTransactionStatusFiles(logFolder, TEST_TRANSACTION_ID.toString(), TransactionStatus.BEGIN_STARTED, TransactionStatus.BEGIN_FINISHED);
        assertTransactionStatusFiles(logFolder, TEST_TRANSACTION_ID_2.toString(), TransactionStatus.PREPARE_STARTED);
    }

    private void assertTransactionFolders(File logFolder, String... expectedFolderNames) throws IOException
    {
        Set<String> expectedFolderNamesSet = new HashSet<>(Arrays.asList(expectedFolderNames));
        Set<String> actualFolderNamesSet = new HashSet<>();

        List<File> transactionFolders = list(logFolder);
        for (File transactionFolder : transactionFolders)
        {
            assertTrue(transactionFolder.isDirectory());
            actualFolderNamesSet.add(transactionFolder.getName());
        }

        assertEquals(expectedFolderNamesSet, actualFolderNamesSet);
    }

    private void assertTransactionStatusFiles(File logFolder, String transactionId, TransactionStatus... expectedStatuses) throws IOException
    {
        Set<String> expectedFileNamesSet = Arrays.stream(expectedStatuses).map(Enum::name).collect(Collectors.toSet());
        Set<String> actualFileNamesSet = new HashSet<>();

        List<File> statusFiles = list(new File(logFolder, transactionId));
        for (File statusFile : statusFiles)
        {
            assertTrue(statusFile.isFile());
            actualFileNamesSet.add(statusFile.getName());
        }

        assertEquals(expectedFileNamesSet, actualFileNamesSet);
    }

    private static File createFolder(File folder) throws IOException
    {
        return Files.createDirectory(folder.toPath()).toFile();
    }

    private static File createFile(File file) throws IOException
    {
        return Files.createFile(file.toPath()).toFile();
    }

    private static List<File> list(File folder) throws IOException
    {
        return Files.list(folder.toPath()).map(Path::toFile).collect(Collectors.toList());
    }

}
