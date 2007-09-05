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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.datamover.testhelper.FileStructEngine;

/**
 * @author Tomasz Pylak on Aug 29, 2007
 */
public class MainTest
{
    private static final FileStructEngine DEFAULT_STRUCT = new FileStructEngine("test");

    // time needed be a single test to complete. After this time we kill data mover and check if results are correct. It
    // can happen, that tests running on slow machines fail because they need more time. Than this constant should be
    // adjusted.
    private static final long DATA_MOVER_COMPLETION_TIME = 4000;

    private static final int CHECK_INTERVAL = 1;

    private static final int QUIET_PERIOD = 2;

    private static final File unitTestRootDirectory = new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, MainTest.class.getSimpleName());

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
        unitTestRootDirectory.mkdirs();
        assert unitTestRootDirectory.isDirectory();
    }

    @BeforeMethod(alwaysRun=true)
    public void setUp()
    {
        FileUtilities.deleteRecursively(workingDirectory);
        workingDirectory.mkdirs();
    }

    @AfterClass
    public void clean()
    {
        // FileUtilities.deleteRecursively(unitTestRootDirectory);
    }

    // ----------------- auxiliary data structures

    private static class ExternalDirs
    {
        private static final String INCOMING = "incoming";

        private static final String BUFFER = "buffer";

        private static final String OUTGOING = "outgoing";

        private static final String EXTRA_COPY_DIR = "extra-copy";

        private static final String MANUAL_INTERV_DIR = "manual-intervention";

        public File incoming;

        public File outgoing;

        public File buffer;

        public File extraCopy;

        public File manualIntervDir;

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

    private static void assertSampleStructMovedWithCopy(ExternalDirs dirs, LocalBufferDirs bufferDirs,
            FileStructEngine structEngine) throws IOException
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

    private static void assertSampleStructInOutgoing(ExternalDirs dirs, FileStructEngine structEngine)
            throws IOException
    {
        structEngine.assertSampleStructureExists(dirs.outgoing);
        structEngine.assertSampleStructFinishMarkerExists(dirs.outgoing);
    }

    private static void assertSampleStructMovedWithCopy(ExternalDirs dirs, LocalBufferDirs bufferDirs)
            throws IOException
    {
        assertEmptyIncomingAndBufferDir(dirs, bufferDirs);
        assertSampleStructInOutgoing(dirs);
        assertSampleStructureExists(dirs.extraCopy);
    }

    private static void assertSampleStructInOutgoing(ExternalDirs dirs) throws IOException
    {
        assertSampleStructureExists(dirs.outgoing);
        assertSampleStructFinishMarkerExists(dirs.outgoing);
        // we use default structure engine, so there is exactly one directory in outgoing + the marker file
        assert dirs.outgoing.list().length == 2;
    }

    private static void assertEmptyIncomingAndBufferDir(ExternalDirs dirs, LocalBufferDirs bufferDirs)
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

    private static void assertNumberOfResources(File file, int size)
    {
        assert file.list().length == size;
    }

    private static ArrayList<String> getDefaultParameters(ExternalDirs dirs)
    {
        return createList("--incoming-dir", dirs.incoming.getPath(), "--buffer-dir", dirs.buffer.getPath(),
                "--outgoing-dir", dirs.outgoing.getPath(), "--check-interval", Integer.toString(CHECK_INTERVAL),
                "--quiet-period", Integer.toString(QUIET_PERIOD), "--treat-incoming-as-remote");
    }

    private static ArrayList<String> getManualInterventionParameters(ExternalDirs dirs, String filteredName)
    {
        return createList("--manual-intervention-dir", dirs.manualIntervDir.getPath(), "--manual-intervention-regex",
                filteredName);
    }

    private static ArrayList<String> getCleansingParameters(ExternalDirs dirs, String cleansingStruct)
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

    private static Parameters createParametersWithFilter(ExternalDirs dirs, String manualIntervName,
            String cleansingStruct)
    {
        ArrayList<String> list = getDefaultParameters(dirs);
        list.addAll(getExtraCopyParameters(dirs));
        list.addAll(getManualInterventionParameters(dirs, manualIntervName));
        list.addAll(getCleansingParameters(dirs, cleansingStruct));
        return createParameters(list);
    }

    private static LocalBufferDirs createBufferDirs(Parameters parameters)
    {
        return new LocalBufferDirs(parameters, "test-copy-in-progress", "test-copy-complete", "test-ready-to-move",
                "test-temp");
    }

    private static interface IFSPreparator
    {
        // prepares structure in the file system to mimic possible state of directories before data mover is launched
        void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs) throws Exception;
    }

    private static void performGenericTest(IFSPreparator preparator) throws Exception
    {
        ExternalDirs dirs = new ExternalDirs(workingDirectory);
        Parameters parameters = createDefaultParametersWithExtraCopy(dirs);
        LocalBufferDirs bufferDirs = createBufferDirs(parameters);

        preparator.prepareState(dirs, bufferDirs);

        runDataMover(parameters, bufferDirs);

        assertSampleStructMovedWithCopy(dirs, bufferDirs);
    }

    private static void runDataMover(Parameters parameters, LocalBufferDirs bufferDirs) throws InterruptedException
    {
        ITerminable terminable = Main.startupServer(parameters, bufferDirs);
        Thread.sleep(DATA_MOVER_COMPLETION_TIME);
        assert terminable.terminate();
    }

    // --------------------- tests

    @Test(groups =
        { "slow" })
    // recovery after copy from incoming has been done, but no marker exists nor the source was not deleted
    public void testRecoveryIncomingCopiedNotDeleted() throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs) throws Exception
                {
                    createSampleStructure(dirs.incoming);
                    createSampleStructure(bufferDirs.getCopyInProgressDir());
                }
            });
    }

    @Test(groups =
        { "slow" })
    // recovery after failure during coping from incoming
    public void testRecoveryIncomingPartialCopy() throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs) throws Exception
                {
                    createSampleStructure(dirs.incoming);
                    createPartialSampleStructure(bufferDirs.getCopyInProgressDir());
                }
            });
    }

    @Test(groups =
        { "slow" })
    // recovery after data from incoming has been moved, but no marker was created yet
    public void testRecoveryIncomingNoMarkFile() throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs) throws Exception
                {
                    createSampleStructure(bufferDirs.getCopyInProgressDir());
                }
            });
    }

    @Test(groups =
        { "slow" })
    // recovery after failure before data are moved to 'copy-completed'
    public void testRecoveryIncomingCompleteNotMoved() throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs) throws Exception
                {
                    createSampleStructure(bufferDirs.getCopyInProgressDir());
                    createSampleFinishedMarkerFile(bufferDirs.getCopyInProgressDir());
                }
            });
    }

    @Test(groups =
        { "slow" })
    // recovery after failure when data are moved to 'copy-completed', but before the marker in in-progress is deleted
    public void testRecoveryIncomingCompleteMarkerNotDeleted() throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs) throws Exception
                {
                    createSampleStructure(bufferDirs.getCopyCompleteDir());
                    createSampleFinishedMarkerFile(bufferDirs.getCopyInProgressDir());
                }
            });
    }

    @Test(groups =
        { "slow" })
    // recovery after failure when data are copied to 'copy-completed', but before deletion has been finished
    public void testRecoveryIncomingCompleteDeletionInProgress() throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs) throws Exception
                {
                    createSampleStructure(bufferDirs.getCopyInProgressDir());
                    createPartialSampleStructure(dirs.incoming);
                    createSampleDeletionInProgressMarkerFile(bufferDirs.getCopyInProgressDir());
                }
            });
    }

    @Test(groups =
        { "slow" })
    // recovery after failure when incoming has finished processing and local processing has not started
    public void testRecoveryLocalProcessingNotStarted() throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs) throws Exception
                {
                    createSampleStructure(bufferDirs.getCopyCompleteDir());
                }
            });
    }

    @Test(groups =
        { "slow" })
    // recovery after failure during local processing when extra copy in temp dir is partial
    public void testRecoveryLocalProcessingPartialExtraCopyInTmp() throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs) throws Exception
                {
                    createSampleStructure(bufferDirs.getCopyCompleteDir());
                    createPartialSampleStructure(bufferDirs.getTempDir());
                }
            });
    }

    @Test(groups =
        { "slow" })
    // recovery after failure during local processing, extra copy created in temp-dir, data moved to read-to-move
    public void testRecoveryLocalProcessingExtraCopyInTmp() throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs) throws Exception
                {
                    createSampleStructure(bufferDirs.getTempDir());
                    createSampleStructure(bufferDirs.getReadyToMoveDir());
                }
            });
    }

    @Test(groups =
        { "slow" })
    // recovery after failure during local processing, extra copy created in temp-dir, data moved to read-to-move,
    // outgoing processing has not started. It tests also outgoing process recovery.
    public void testRecoveryLocalProcessingExtraCopyInTmpAndReadyToMove() throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs) throws Exception
                {
                    createSampleStructure(bufferDirs.getTempDir());
                    createSampleStructure(bufferDirs.getReadyToMoveDir());
                }
            });
    }

    @Test(groups =
        { "slow" })
    // recovery after failure when partial copy has been done in outgoing
    public void testRecoveryOutgoingPartialCopy() throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs) throws Exception
                {
                    createSampleStructure(dirs.extraCopy);
                    createSampleStructure(bufferDirs.getReadyToMoveDir());
                    createPartialSampleStructure(dirs.outgoing);
                }
            });
    }

    @Test(groups =
        { "slow" })
    // some data are in incoming, test the whole pipeline
    public void testWholePipeline() throws Exception
    {
        performGenericTest(new IFSPreparator()
            {
                public void prepareState(ExternalDirs dirs, LocalBufferDirs bufferDirs) throws Exception
                {
                    createSampleStructure(dirs.incoming);
                }
            });
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
        assertNumberOfResources(dirs.outgoing, 6);
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
        assertNumberOfResources(dirs.outgoing, 4);
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
        Thread.sleep(DATA_MOVER_COMPLETION_TIME);

        for (int i = 0; i < size; i++)
        {
            assertSampleStructMovedWithCopy(dirs, bufferDirs, structs[i]);
        }
        assertNumberOfResources(dirs.outgoing, 2 * size);
        assert terminable.terminate();
    }

    @Test(groups =
        { "slow" })
    // some data are in incoming, test the whole pipeline taking manual intervention and cleansing scenario into account
    public void testWholePipelineCleansingManualIntervention() throws Exception
    {
        ExternalDirs dirs = new ExternalDirs(workingDirectory);

        // this structure should end up in manual-intervention-directory
        FileStructEngine manualIntervStruct = new FileStructEngine("test1");
        // this structure should be deleted
        FileStructEngine cleansinStruct = new FileStructEngine("test2");

        Parameters parameters =
                createParametersWithFilter(dirs, manualIntervStruct.getMainStructName(), cleansinStruct
                        .getSampleCleansingRegExp());
        LocalBufferDirs bufferDirs = createBufferDirs(parameters);

        manualIntervStruct.createSampleStructure(dirs.incoming);
        cleansinStruct.createSampleStructure(dirs.incoming);

        runDataMover(parameters, bufferDirs);

        cleansinStruct.assertSampleStructureCleaned(dirs.outgoing);
        assertNumberOfResources(dirs.outgoing, 2);

        cleansinStruct.assertSampleStructureCleaned(dirs.extraCopy);
        assertNumberOfResources(dirs.extraCopy, 1);

        manualIntervStruct.assertSampleStructureExists(dirs.manualIntervDir);
        assertNumberOfResources(dirs.manualIntervDir, 1);
    }
}
