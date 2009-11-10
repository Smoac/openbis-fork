/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.ProcedureTypeCode;

/**
 * Test cases for corresponding {@link DefaultStorageProcessor} class.
 * 
 * @author Christian Ribeaud
 */
public final class DefaultStorageProcessorTest extends AbstractFileSystemTestCase
{

    private final static ITypeExtractor TYPE_EXTRACTOR = new TestProcedureAndDataTypeExtractor();

    private final DefaultStorageProcessor createStorageProcessor()
    {
        final Properties properties = new Properties();
        final DefaultStorageProcessor storageProcessor = new DefaultStorageProcessor(properties);
        storageProcessor.setStoreRootDirectory(workingDirectory);
        return storageProcessor;
    }

    private File createDirectory(final String directoryName)
    {
        final File file = new File(workingDirectory, directoryName);
        file.mkdir();
        assertEquals(true, file.isDirectory());
        return file;
    }

    @Test
    public final void testStoreData()
    {
        final DefaultStorageProcessor storageProcessor = createStorageProcessor();
        try
        {
            storageProcessor.storeData(null, null, null, null, null);
            fail("Null values not accepted");
        } catch (final AssertionError e)
        {
            // Nothing to do here.
        }
        final File incomingDataSetDirectory = createDirectory("incoming");
        FileUtilities.writeToFile(new File(incomingDataSetDirectory, "read.me"), "hello world");
        final File rootDir = createDirectory("root");
        final File storeData =
                storageProcessor.storeData(null, TYPE_EXTRACTOR, null, incomingDataSetDirectory,
                        rootDir);
        assertEquals(false, incomingDataSetDirectory.exists());
        assertEquals(true, storeData.isDirectory());
        assertEquals(rootDir.getAbsolutePath(), storeData.getAbsolutePath());
        assertEquals("hello world", FileUtilities.loadToString(
                new File(storeData, DefaultStorageProcessor.ORIGINAL_DIR + "/incoming/read.me"))
                .trim());
    }

    @Test
    public final void testGetStoreRootDirectory()
    {
        DefaultStorageProcessor storageProcessor = createStorageProcessor();
        File storeRootDirectory = storageProcessor.getStoreRootDirectory();
        assertEquals(workingDirectory.getAbsolutePath(), storeRootDirectory.getAbsolutePath());
    }

    @Test
    public final void testUnstoreData()
    {
        final DefaultStorageProcessor storageProcessor = createStorageProcessor();
        try
        {
            storageProcessor.rollback(null, null, null);
            fail("Null values not accepted");
        } catch (final AssertionError e)
        {
            // Nothing to do here.
        }
        final File root = createDirectory("root");
        final File incomingDataSetDirectory = createDirectory("incoming");
        File readMeFile = new File(incomingDataSetDirectory, "read.me");
        FileUtilities.writeToFile(readMeFile, "hi");
        final File storeData =
                storageProcessor.storeData(null, TYPE_EXTRACTOR, null, incomingDataSetDirectory,
                        root);
        assertEquals(true, storeData.exists());
        assertEquals(false, incomingDataSetDirectory.exists());
        storageProcessor.rollback(incomingDataSetDirectory, root, null);
        assertEquals(true, incomingDataSetDirectory.exists());
        assertEquals("hi", FileUtilities.loadToString(readMeFile).trim());
    }

    //
    // Helper classes
    //

    final static class TestProcedureAndDataTypeExtractor implements ITypeExtractor
    {

        static final String PROCEDURE_TYPE = ProcedureTypeCode.DATA_ACQUISITION.getCode();

        static final String DATA_SET_TYPE = "dataSetType";

        static final String LOCATOR_TYPE = "locatorType";

        static final String FILE_FORMAT_TYPE = "fileFormatType";

        public static final String DATA_SET_PROPERTIES_FILE_KEY = "dataSetProperties";

        //
        // IProcedureAndDataTypeExtractor
        //

        public final FileFormatType getFileFormatType(final File incomingDataSetPath)
        {
            return new FileFormatType(FILE_FORMAT_TYPE);
        }

        public final LocatorType getLocatorType(final File incomingDataSetPath)
        {
            return new LocatorType(LOCATOR_TYPE);
        }

        public final DataSetType getDataSetType(final File incomingDataSetPath)
        {
            return new DataSetType(DATA_SET_TYPE);
        }

        public String getProcessorType(File incomingDataSetPath)
        {
            return "da";
        }

        public boolean isMeasuredData(File incomingDataSetPath)
        {
            return true;
        }

        public List<NewProperty> getDataSetProperties()
        {
            return new ArrayList<NewProperty>();
        }
    }
}
