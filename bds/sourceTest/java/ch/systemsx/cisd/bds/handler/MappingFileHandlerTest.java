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

package ch.systemsx.cisd.bds.handler;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.DataStructureException;
import ch.systemsx.cisd.bds.Reference;
import ch.systemsx.cisd.bds.ReferenceType;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;

/**
 * Test cases for corresponding {@link MappingFileHandler} class.
 * 
 * @author Christian Ribeaud
 */
public final class MappingFileHandlerTest extends AbstractFileSystemTestCase
{

    private final static String MAPPING_FILE_CONTENT =
            "row1/row2_column1.tiff\tI\toriginal1.tiff\n" + "row2/row3_column2.tiff\tI\toriginal2.tiff\n";

    private MappingFileHandler handler;

    private final void prepareMappingFileHandler()
    {
        final IDirectory mappingDirectory = NodeFactory.createDirectoryNode(workingDirectory);
        final IDirectory originalDirectory = mappingDirectory.makeDirectory("original");
        final IDirectory standardDirectory = mappingDirectory.makeDirectory("standard");
        handler = new MappingFileHandler(mappingDirectory, standardDirectory, originalDirectory, true);
    }

    private final void writeMappingFile() throws IOException
    {
        final File mappingFile = new File(workingDirectory, MappingFileHandler.MAPPING_FILE);
        FileUtils.writeStringToFile(mappingFile, MAPPING_FILE_CONTENT);
    }

    private final void createMappedFiles() throws IOException
    {
        final File original = new File(workingDirectory, "original");
        final File standard = new File(workingDirectory, "standard");
        final File original1 = new File(original, "original1.tiff");
        FileUtils.touch(original1);
        assertTrue(original1.exists());
        final File original2 = new File(original, "original2.tiff");
        FileUtils.touch(original2);
        assertTrue(original2.exists());
        final File row1 = new File(standard, "row1");
        row1.mkdir();
        final File row2 = new File(standard, "row2");
        row2.mkdir();
        final File standard1 = new File(row1, "row2_column1.tiff");
        FileUtils.touch(standard1);
        assertTrue(standard1.exists());
        final File standard2 = new File(row2, "row3_column2.tiff");
        FileUtils.touch(standard2);
        assertTrue(standard2.exists());
    }

    @Override
    @BeforeMethod
    public void setup() throws IOException
    {
        super.setup();
        prepareMappingFileHandler();
    }

    @Test
    public final void testAddReference()
    {
        try
        {
            handler.addReference(null);
            fail("Null value not allowed.");
        } catch (AssertionError ex)
        {
            // Nothing to do here.
        }
        handler.addReference(new Reference("1", "2", ReferenceType.IDENTICAL));
        try
        {
            handler.addReference(new Reference("1", "2", ReferenceType.IDENTICAL));
            fail("Can not added the same reference twice.");
        } catch (DataStructureException ex)
        {
            // Nothing to do here.
        }
    }

    @Test
    public final void testPerformOpening() throws IOException
    {
        writeMappingFile();
        handler.performOpening();
        assertEquals(2, handler.getReferences().size());
    }

    @Test
    public final void testPerformClosing() throws IOException
    {
        handler.addReference(new Reference("row1/row2_column1.tiff", "original1.tiff", ReferenceType.IDENTICAL));
        handler.addReference(new Reference("row2/row3_column2.tiff", "original2.tiff", ReferenceType.IDENTICAL));
        handler.performClosing();
        final File mappingFile = new File(workingDirectory, MappingFileHandler.MAPPING_FILE);
        assertTrue(mappingFile.exists());
        final String mapping = FileUtils.readFileToString(mappingFile);
        mapping.equals(MAPPING_FILE_CONTENT);
    }

    @Test
    public final void testAssertValid() throws IOException
    {
        writeMappingFile();
        handler.performOpening();
        createMappedFiles();
        handler.assertValid();
    }

}