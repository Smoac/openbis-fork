/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover;

import static ch.systemsx.cisd.datamover.testhelper.FileSystemHelper.assertEmptyDir;
import static ch.systemsx.cisd.datamover.testhelper.FileSystemHelper.createDir;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.action.ITerminable;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.datamover.testhelper.FileStructEngine;
import ch.systemsx.cisd.datamover.utils.LocalBufferDirs;

/**
 * Test cases for the {@link Parameters} class.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
{ Main.class })
public final class MainTest extends AbstractFileSystemTestCase
{
    private static final FileStructEngine DEFAULT_STRUCT = new FileStructEngine("test");

    // time needed be a single test to complete. After this time we kill data mover and check if
    // results are correct. It
    // can happen, that tests running on slow machines fail because they need more time. Than this
    // constant should be
    // adjusted.
    private static final long DATA_MOVER_COMPLETION_TIME = 10000;

    private static final long DATA_MOVER_COMPLETION_TIME_LONG = 15000;

    private static final int CHECK_INTERVAL = 1;

    private static final int CHECK_INTERVAL_INTERNAL = 1;

    private static final int QUIET_PERIOD = 2;

    private static final int WAITING_TIME_OUT = 30;

    private static final File ORIGINAL_SCRIPT_FILE =
            new File(new File("dist"), ShellScriptTest.SCRIPT_FILE_NAME);

    private File scriptFile;

    @Override
    @BeforeMethod(alwaysRun = true)
    public void setUp() throws IOException
    {
        super.setUp();
        LogInitializer.init();
        scriptFile = new File(workingDirectory, ShellScriptTest.SCRIPT_FILE_NAME);
        FileOperations.getInstance().copyFile(ORIGINAL_SCRIPT_FILE, scriptFile);
    }

    // ----------------- auxiliary data structures

    private static class ExternalDirs
    {
        private static final String INCOMING = "incoming";

        private static final String BUFFER = "buffer";

        private static final String OUTGOING = "outgoing";

        private static final String EXTRA_COPY_DIR = "extra-copy";

        private static final String MANUAL_INTERV_DIR = "manual-intervention";

        private final File incoming;

        private final File outgoing;

        private final File buffer;

        private final File extraCopy;

        private final File manualIntervDir;

        public ExternalDirs(File workingDirectory) throws IOException
        {
            incoming = createDir(workingDirectory, INCOMING);
            buffer = createDir(workingDirectory, BUFFER);
            outgoing = createDir(workingDirectory, OUTGOING);
            extraCopy = createDir(workingDirectory, EXTRA_COPY_DIR);
            manualIntervDir = createDir(workingDirectory, MANUAL_INTERV_DIR);
        }
    }

    // ----------------- higher level assertions

    private static void assertSampleStructMovedWithCopy(ExternalDirs dirs,
            LocalBufferDirs bufferDirs, FileStructEngine structEngine) throws IOException
    {
        assertSampleStructMoved(dirs, bufferDirs, structEngine);
        structEngine.assertSampleStructureExists(dirs.extraCopy);
    }

    private static void assertSampleStructMoved(ExternalDirs dirs, LocalBufferDirs bufferDirs,
            FileStructEngine structEngine) throws IOException
    {
        assertEmptyIncomingAndBufferDir(dirs, bufferDirs);
        assertSampleStructInOutgoing(dirs, structEngine);
    }

    private static void assertSampleStructInOutgoing(ExternalDirs dirs,
            FileStructEngine structEngine) throws IOException
    {
        structEngine.assertSampleStructureExists(dirs.outgoing);
        structEngine.assertSampleStructFinishMarkerExists(dirs.outgoing);
    }

    private static void assertSampleStructMovedWithCopy(ExternalDirs dirs,
            LocalBufferDirs bufferDirs) throws IOException
    {
        assertEmptyIncomingAndBufferDir(dirs, bufferDirs);
        assertSampleStructInOutgoing(dirs);
        assertSampleStructureExists(dirs.extraCopy);
    }

    private static void assertSampleStructInOutgoing(ExternalDirs dirs) throws IOException
    {
        assertSampleStructureExists(dirs.outgoing);
        assertSampleStructFinishMarkerExists(dirs.outgoing);
        // we use default structure engine, so there is exactly one directory in outgoing + the
        // marker file
        assertEquals(2, dirs.outgoing.list().length);
    }

    private static void assertEmptyIncomingAndBufferDir(ExternalDirs dirs,
            LocalBufferDirs bufferDirs)
    {
        assertEmptyDir(dirs.incoming);
        assertEmptyBufferDirs(bufferDirs);
    }

    private static void assertEmptyBufferDirs(LocalBufferDirs dirs)
    {
        assertEmptyDir(dirs.getCopyCompleteDir());
        assertEmptyDir(dirs.getCopyInProgressDir());
        assertEmptyDir(dirs.getReadyToMoveDir());
        assertEmptyDir(dirs.getTempDir());
    }

    private static void createSampleStructure(File parentDir) throws IOException
    {
        DEFAULT_STRUCT.createSampleStructure(parentDir);
    }

    private static void createPartialSampleStructure(File parentDir) throws IOException
    {
        DEFAULT_STRUCT.createPartialSampleStructure(parentDir);
    }

    private static void createSampleFinishedMarkerFile(File parentDir)
    {
        DEFAULT_STRUCT.createSampleFinishedMarkerFile(parentDir);
    }

    private static void createSampleDeletionInProgressMarkerFile(File parentDir)
    {
        DEFAULT_STRUCT.createSampleDeletionInProgressMarkerFile(parentDir);
    }

    private static void assertSampleStructureExists(File parentDir) throws IOException
    {
        DEFAULT_STRUCT.assertSampleStructureExists(parentDir);
    }

    private static void assertSampleStructFinishMarkerExists(File parentDir)
    {
        DEFAULT_STRUCT.assertSampleStructFinishMarkerExists(parentDir);
    }

    // -------------------- testing engine and configuration

    private static ArrayList<String> getDefaultParameters(ExternalDirs dirs)
    {
        return createList("--incoming-target", dirs.incoming.getPath(), "--buffer-dir", dirs.buffer
                .getPath(), "--outgoing-target", dirs.outgoing.getPath(), "--check-interval",
                Integer.toString(CHECK_INTERVAL), "--check-interval-internal", Integer
                        .toString(CHECK_INTERVAL_INTERNAL), "--quiet-period", Integer
                        .toString(QUIET_PERIOD), "--treat-incoming-as-remote");
    }

    private static ArrayList<String> getManualInterventionParameters(ExternalDirs dirs,
            String filteredName)
    {
        return createList("--manual-intervention-dir", dirs.manualIntervDir.getPath(),
                "--manual-intervention-regex", filteredName);
    }

    private static ArrayList<String> getCleansingParameters(String cleansingStruct)
    {
        return createList("--cleansing-regex", cleansingStruct);
    }

    private static ArrayList<String> getExtraCopyParameters(ExternalDirs dirs)
    {
        return createList("--extra-copy-dir", dirs.extraCopy.getPath());
    }

    private static String[] asStringArray(ArrayList<String> list)
    {
        return list.toArray(new String[] {});
    }

    private static ArrayList<String> createList(String... args)
    {
        return new ArrayList<String>(Arrays.asList(args));
    }

    private static Parameters createParameters(ArrayList<String> args)
    {
        Parameters parameters = new Parameters(asStringArray(args));
        parameters.log();
        return parameters;
    }

    private static Parameters createDefaultParameters(ExternalDirs dirs)
    {
        return createParameters(getDefaultParameters(dirs));
    }

    private static Parameters createDefaultParametersWithExtraCopy(ExternalDirs dirs)
    {
        ArrayList<String> list = getDefaultParameters(dirs);
        list.addAll(getExtraCopyParameters(dirs));
        return createParameters(list);
    }

    private static Parameters createParametersWithFilter(ExternalDirs dirs,
            String manualIntervName, String cleansingStruct)
    {
        ArrayList<String> list = getDefaultParameters(dirs);
        list.addAll(getExtraCopyParameters(dirs));
        list.addAll(getManualInterventionParameters(dirs, manualIntervName));
        list.addAll(getCleansingParameters(cleansingStruct));
        return createParameters(list);
    }

    private static LocalBufferDirs createBufferDirs(Parameters parameters)
    {
        return new LocalBufferDirs(parameters.getBufferDirectoryPath(), "test-copy-in-progress",
                "test-copy-complete", "test-ready-to-move", "test-temp");
    }

    private static interface IFSPreparator
    {
        // prepares structure in the file system to mimic possible state of directories before data
        // mover is launched
        void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs) throws Exception;
    }

    private void performGenericTest(IFSPreparator preparator, long millisToWaitForPrep)
            throws Exception
    {
        ExternalDirs dirs = new ExternalDirs(workingDirectory);
        Parameters parameters = createDefaultParametersWithExtraCopy(dirs);
        LocalBufferDirs bufferDirs = createBufferDirs(parameters);

        if (millisToWaitForPrep == 0L)
        {
            preparator.prepareState(dirs, bufferDirs);
            runDataMover(parameters, bufferDirs);
        } else
        {
            runDataMover(dirs, parameters, bufferDirs, preparator, millisToWaitForPrep);
        }

        assertSampleStructMovedWithCopy(dirs, bufferDirs);
    }

    private static void runDataMover(Parameters parameters, LocalBufferDirs bufferDirs)
            throws InterruptedException
    {
        runDataMover(parameters, bufferDirs, DATA_MOVER_COMPLETION_TIME);
    }

    private static void runDataMover(Parameters parameters, LocalBufferDirs bufferDirs,
            long dataMoverCompletionTime) throws InterruptedException
    {
        ITerminable terminable = Main.startupServer(parameters, bufferDirs);
        Thread.sleep(dataMoverCompletionTime);
        assertTrue(terminable.terminate());
    }

    private static void runDataMover(ExternalDirs dirs, Parameters parameters,
            LocalBufferDirs bufferDirs, IFSPreparator preparator, long millisToWaitForPrep)
            throws Exception
    {
        ITerminable terminable = Main.startupServer(parameters, bufferDirs);
        Thread.sleep(millisToWaitForPrep);
        preparator.prepareState(dirs, bufferDirs);
        Thread.sleep(DATA_MOVER_COMPLETION_TIME / 2);
        final File recoveryFile = new File(DataMover.RECOVERY_MARKER_FIILENAME);
        recoveryFile.createNewFile();
        Thread.sleep(DATA_MOVER_COMPLETION_TIME);
        assertTrue(terminable.terminate());
        assertFalse(recoveryFile.exists());
    }

    private void waitUntilFinished() throws Exception
    {
        Thread.sleep(2 * CHECK_INTERVAL * 1000);
        for (int i = 0; i < WAITING_TIME_OUT; i++)
        {
            File[] processingMarkerFiles = getProcessingMarkerFiles();
            if (processingMarkerFiles.length == 0)
            {
                return;
            }
            Thread.sleep(1000);
        }
        final StringBuilder buf = new StringBuilder();
        for (File f : getProcessingMarkerFiles())
        {
            buf.append(f.getAbsoluteFile());
            buf.append(", ");
        }
        buf.setLength(Math.max(0, buf.length() - 2));
        fail(String.format("Not finished after %d seconds, processing files still existing: %s.",
                WAITING_TIME_OUT, buf.toString()));
    }

    private File[] getProcessingMarkerFiles()
    {
        File[] files = new File(".").listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File pathname)
                {
                    return pathname.getName().startsWith(DataMover.PROCESS_MARKER_PREFIX);
                }
            });
        return files;
    }

    @DataProvider(name = "delays")
    public Object[][] provideDelays()
    {
        return new Object[][]
        {
                { 0L },
                { 1000L } };
    }

    // --------------------- tests

    @Test(groups =
    { "slow" }, dataProvider = "delays")
    // recovery after copy from incoming has been done, but no marker exists nor the source was not
    // deleted
    public void testRecoveryIncomingCopiedNotDeleted(long delay) throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                @Override
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs)
                        throws Exception
                {
                    createSampleStructure(dirs.incoming);
                    createSampleStructure(bufferDirs.getCopyInProgressDir());
                }
            }, delay);
    }

    @Test(groups =
    { "slow" }, dataProvider = "delays")
    // recovery after failure during coping from incoming
    public void testRecoveryIncomingPartialCopy(long delay) throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                @Override
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs)
                        throws Exception
                {
                    createSampleStructure(dirs.incoming);
                    createPartialSampleStructure(bufferDirs.getCopyInProgressDir());
                }
            }, delay);
    }

    @Test(groups =
    { "slow" }, dataProvider = "delays")
    // recovery after data from incoming has been moved, but no marker was created yet
    public void testRecoveryIncomingNoMarkFile(long delay) throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                @Override
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs)
                        throws Exception
                {
                    createSampleStructure(bufferDirs.getCopyInProgressDir());
                }
            }, delay);
    }

    @Test(groups =
    { "slow" }, dataProvider = "delays")
    // recovery after failure before data are moved to 'copy-completed'
    public void testRecoveryIncomingCompleteNotMoved(long delay) throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                @Override
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs)
                        throws Exception
                {
                    createSampleStructure(bufferDirs.getCopyInProgressDir());
                    createSampleFinishedMarkerFile(bufferDirs.getCopyInProgressDir());
                }
            }, delay);
    }

    @Test(groups =
    { "slow" }, dataProvider = "delays")
    // recovery after failure when data are moved to 'copy-completed', but before the marker in
    // in-progress is deleted
    public void testRecoveryIncomingCompleteMarkerNotDeleted(long delay) throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                @Override
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs)
                        throws Exception
                {
                    createSampleStructure(bufferDirs.getCopyCompleteDir());
                    createSampleFinishedMarkerFile(bufferDirs.getCopyInProgressDir());
                }
            }, delay);
    }

    @Test(groups =
    { "slow" }, dataProvider = "delays")
    // recovery after failure when data are copied to 'copy-completed', but before deletion has been
    // finished
    public void testRecoveryIncomingCompleteDeletionInProgress(long delay) throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                @Override
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs)
                        throws Exception
                {
                    createSampleStructure(bufferDirs.getCopyInProgressDir());
                    createPartialSampleStructure(dirs.incoming);
                    createSampleDeletionInProgressMarkerFile(bufferDirs.getCopyInProgressDir());
                }
            }, delay);
    }

    @Test(groups =
    { "slow" }, dataProvider = "delays")
    // recovery after failure when incoming has finished processing and local processing has not
    // started
    public void testRecoveryLocalProcessingNotStarted(long delay) throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                @Override
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs)
                        throws Exception
                {
                    createSampleStructure(bufferDirs.getCopyCompleteDir());
                }
            }, delay);
    }

    @Test(groups =
    { "slow" }, dataProvider = "delays")
    // recovery after failure during local processing when extra copy in temp dir is partial
    public void testRecoveryLocalProcessingPartialExtraCopyInTmp(long delay) throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                @Override
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs)
                        throws Exception
                {
                    createSampleStructure(bufferDirs.getCopyCompleteDir());
                    createPartialSampleStructure(bufferDirs.getTempDir());
                }
            }, delay);
    }

    @Test(groups =
    { "slow" }, dataProvider = "delays")
    // recovery after failure during local processing, extra copy created in temp-dir, data moved to
    // read-to-move
    public void testRecoveryLocalProcessingExtraCopyInTmp(long delay) throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                @Override
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs)
                        throws Exception
                {
                    createSampleStructure(bufferDirs.getTempDir());
                    createSampleStructure(bufferDirs.getReadyToMoveDir());
                }
            }, delay);
    }

    @Test(groups =
    { "slow" }, dataProvider = "delays")
    // recovery after failure during local processing, extra copy created in temp-dir, data moved to
    // read-to-move,
    // outgoing processing has not started. It tests also outgoing process recovery.
    public void testRecoveryLocalProcessingExtraCopyInTmpAndReadyToMove(long delay)
            throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                @Override
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs)
                        throws Exception
                {
                    createSampleStructure(bufferDirs.getTempDir());
                    createSampleStructure(bufferDirs.getReadyToMoveDir());
                }
            }, delay);
    }

    @Test(groups =
    { "slow" }, dataProvider = "delays")
    // recovery after failure when partial copy has been done in outgoing
    public void testRecoveryOutgoingPartialCopy(long delay) throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                @Override
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs)
                        throws Exception
                {
                    createSampleStructure(dirs.extraCopy);
                    createSampleStructure(bufferDirs.getReadyToMoveDir());
                    createPartialSampleStructure(dirs.outgoing);
                }
            }, delay);
    }

    @Test(groups =
    { "slow" }, dataProvider = "delays")
    // some data are in incoming, test the whole pipeline
    public void testWholePipeline(long delay) throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                @Override
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs)
                        throws Exception
                {
                    createSampleStructure(dirs.incoming);
                }
            }, delay);
    }

    private FileStructEngine[] createAllThreadsPartialStruct(ExternalDirs dirs,
            LocalBufferDirs bufferDirs) throws Exception
    {
        FileStructEngine[] structs = new FileStructEngine[6];
        int c = 0;
        // incoming recovery: structure 1 partial in in-progress
        structs[c] = new FileStructEngine("test" + c);
        structs[c].createPartialSampleStructure(bufferDirs.getCopyInProgressDir());
        structs[c].createSampleStructure(dirs.incoming);
        c++;

        // local processing recovery: structure 2 in copy-complete
        structs[c] = new FileStructEngine("test" + c);
        structs[c].createSampleStructure(bufferDirs.getCopyCompleteDir());
        c++;

        // outgoing and local processing recovery: structure 3 in ready-to-move and temp
        structs[c] = new FileStructEngine("test" + c);
        structs[c].createSampleStructure(bufferDirs.getReadyToMoveDir());
        structs[c].createSampleStructure(bufferDirs.getTempDir());
        c++;

        // some normal input
        for (int i = 0; i < 3; i++)
        {
            structs[c] = new FileStructEngine("test" + c);
            structs[c].createSampleStructure(dirs.incoming);
            c++;
        }
        return structs;
    }

    @Test(groups =
    { "slow" })
    // recovery after failure when all threads need recovery
    public void testRecoveryAllThreadsPartialBis() throws Exception
    {
        ExternalDirs dirs = new ExternalDirs(workingDirectory);
        Parameters parameters = createDefaultParametersWithExtraCopy(dirs);
        LocalBufferDirs bufferDirs = createBufferDirs(parameters);

        FileStructEngine[] structs = createAllThreadsPartialStruct(dirs, bufferDirs);
        runDataMover(parameters, bufferDirs, DATA_MOVER_COMPLETION_TIME_LONG);

        for (int i = 0; i < structs.length; i++)
        {
            assertSampleStructMovedWithCopy(dirs, bufferDirs, structs[i]);
        }
        assertEquals(structs.length * 2, dirs.outgoing.list().length);
    }

    // checks recovery mode when all threads need recovery, but no restart is made
    public void testRecoveryAllThreadsPartial(Parameters parameters, ExternalDirs dirs)
            throws Exception
    {
        LocalBufferDirs bufferDirs = createBufferDirs(parameters);

        ITerminable terminable = Main.startupServer(parameters, bufferDirs);

        FileStructEngine[] structs = createAllThreadsPartialStruct(dirs, bufferDirs);
        Thread.sleep(DATA_MOVER_COMPLETION_TIME_LONG / 2);

        final File recoveryFile = new File(DataMover.RECOVERY_MARKER_FIILENAME);
        recoveryFile.createNewFile();

        Thread.sleep(DATA_MOVER_COMPLETION_TIME_LONG);

        for (int i = 0; i < structs.length; i++)
        {
            assertSampleStructMovedWithCopy(dirs, bufferDirs, structs[i]);
        }
        assertEquals(structs.length * 2, dirs.outgoing.list().length);
        assertFalse(recoveryFile.exists());
        assertTrue(terminable.terminate());
    }

    @Test(groups =
    { "slow" })
    // trigger recovery mode when all threads need recovery, but no restart is made
    public void testRecoveryRequestAllThreadsPartial() throws Exception
    {
        ExternalDirs dirs = new ExternalDirs(workingDirectory);
        Parameters parameters = createDefaultParametersWithExtraCopy(dirs);
        testRecoveryAllThreadsPartial(parameters, dirs);
    }

    @Test(groups =
    { "slow" })
    // recovery after failure when partial copy has been done in outgoing
    public void testRecoveryAllThreadsPartial() throws Exception
    {
        ExternalDirs dirs = new ExternalDirs(workingDirectory);
        Parameters parameters = createDefaultParametersWithExtraCopy(dirs);
        LocalBufferDirs bufferDirs = createBufferDirs(parameters);

        // incoming recovery: structure 1 partial in in-progress
        FileStructEngine struct1 = new FileStructEngine("test1");
        struct1.createSampleStructure(dirs.incoming);
        struct1.createPartialSampleStructure(bufferDirs.getCopyInProgressDir());

        // local processing recovery: structure 2 in copy-complete
        FileStructEngine struct2 = new FileStructEngine("test2");
        struct2.createSampleStructure(bufferDirs.getCopyCompleteDir());

        // outgoing and local processing recovery: structure 3 in ready-to-move and temp
        FileStructEngine struct3 = new FileStructEngine("test3");
        struct3.createSampleStructure(bufferDirs.getReadyToMoveDir());
        struct3.createSampleStructure(bufferDirs.getTempDir());

        runDataMover(parameters, bufferDirs);

        assertSampleStructMovedWithCopy(dirs, bufferDirs, struct1);
        assertSampleStructMovedWithCopy(dirs, bufferDirs, struct2);
        assertSampleStructMovedWithCopy(dirs, bufferDirs, struct3);
        assertEquals(6, dirs.outgoing.list().length);
    }

    @Test(groups =
    { "slow" })
    // normal work-flow tests, no extra copy is created
    public void testWholePipelineNoExtraCopy() throws Exception
    {
        ExternalDirs dirs = new ExternalDirs(workingDirectory);
        Parameters parameters = createDefaultParameters(dirs);
        LocalBufferDirs bufferDirs = createBufferDirs(parameters);

        FileStructEngine struct1 = new FileStructEngine("test1");
        FileStructEngine struct2 = new FileStructEngine("test2");
        struct1.createSampleStructure(dirs.incoming);
        struct2.createSampleStructure(dirs.incoming);

        runDataMover(parameters, bufferDirs);

        assertSampleStructMoved(dirs, bufferDirs, struct1);
        assertSampleStructMoved(dirs, bufferDirs, struct2);
        assertEquals(4, dirs.outgoing.list().length);
        assertEmptyDir(dirs.extraCopy);
    }

    @Test(groups =
    { "slow" })
    // data are coming in random moments, while data mover is running
    public void testWholePipelineDataStreaming() throws Exception
    {
        ExternalDirs dirs = new ExternalDirs(workingDirectory);
        Parameters parameters = createDefaultParametersWithExtraCopy(dirs);
        LocalBufferDirs bufferDirs = createBufferDirs(parameters);

        ITerminable terminable = Main.startupServer(parameters, bufferDirs);
        Thread.sleep(2000); // let data mover start
        // data on the input
        int size = 20;
        FileStructEngine[] structs = new FileStructEngine[size];
        for (int i = 0; i < size; i++)
        {
            int timeToWait = new Random().nextInt(200);
            Thread.sleep(timeToWait);
            structs[i] = new FileStructEngine("test" + i);
            structs[i].createSampleStructure(dirs.incoming);
        }
        waitUntilFinished();

        for (int i = 0; i < size; i++)
        {
            assertSampleStructMovedWithCopy(dirs, bufferDirs, structs[i]);
        }
        assertEquals(2 * size, dirs.outgoing.list().length);
        assertTrue(terminable.terminate());
    }

    @Test(groups =
    { "slow" })
    // some data are in incoming, test the whole pipeline taking manual intervention and cleansing
    // scenario into account
    public void testWholePipelineCleansingManualIntervention() throws Exception
    {
        ExternalDirs dirs = new ExternalDirs(workingDirectory);

        // this structure should end up in manual-intervention-directory
        FileStructEngine manualIntervStruct = new FileStructEngine("test1");
        // this structure should be deleted
        FileStructEngine cleansinStruct = new FileStructEngine("test2");

        Parameters parameters =
                createParametersWithFilter(dirs, manualIntervStruct.getMainStructName(),
                        cleansinStruct.getSampleCleansingRegExp());
        LocalBufferDirs bufferDirs = createBufferDirs(parameters);

        manualIntervStruct.createSampleStructure(dirs.incoming);
        cleansinStruct.createSampleStructure(dirs.incoming);

        runDataMover(parameters, bufferDirs);

        cleansinStruct.assertSampleStructureCleaned(dirs.outgoing);
        assertEquals(2, dirs.outgoing.list().length);

        cleansinStruct.assertSampleStructureCleaned(dirs.extraCopy);
        assertEquals(1, dirs.extraCopy.list().length);

        manualIntervStruct.assertSampleStructureExists(dirs.manualIntervDir);
        assertEquals(1, dirs.manualIntervDir.list().length);
    }
}
