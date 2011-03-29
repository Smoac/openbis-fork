/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import java.io.File;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.io.ByteBufferRandomAccessFile;
import ch.systemsx.cisd.base.io.RandomAccessFileImpl;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataTypeUtilTest extends AssertJUnit
{
    private File dir;

    @BeforeMethod
    public void setUp()
    {
        dir = new File("resource/test-data/DataTypeUtilTest");
    }
    
    @Test
    public void testGif() throws Exception
    {
        assertFileType("gif", "gif-example.gif");
    }
    
    @Test
    public void testJpg() throws Exception
    {
        assertFileType("jpg", "jpeg-example.jpg");
    }
    
    @Test
    public void testPng() throws Exception
    {
        assertFileType("png", "png-example.png");
    }
    
    @Test
    public void testTiff() throws Exception
    {
        assertFileType("tif", "tiff-example.tiff");
    }
    
    @Test
    public void testFileContainingOnlyOneUmlaut() throws Exception
    {
        assertFileType(null, "one-umlaut.txt");
    }
    
    @Test
    public void testMarkUnsupportedInputStream()
    {
        try
        {
            DataTypeUtil.tryToFigureOutFileTypeOf(new ByteBufferRandomAccessFile(1)
                {
                    @Override
                    public boolean markSupported()
                    {
                        return false;
                    }
                });
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Input stream does not support marking. "
                    + "Wrap input stream with a BufferedInputStream to solve the problem.",
                    ex.getMessage());
        }
    }
    
    @Test
    public void testInputStreamAtTheBeginning() throws Exception
    {
        byte[] bytes = "hello world".getBytes();
        ByteBufferRandomAccessFile buffer = new ByteBufferRandomAccessFile(bytes);

        DataTypeUtil.tryToFigureOutFileTypeOf(buffer);

        assertEquals(0, buffer.getFilePointer());
    }
    
    private void assertFileType(String expectedFileType, String fileName) throws Exception
    {
        RandomAccessFileImpl handle = null;
        try
        {
            handle = new RandomAccessFileImpl(new File(dir, fileName), "r");
            String type = DataTypeUtil.tryToFigureOutFileTypeOf(handle);
            
            assertEquals(expectedFileType, type);
        } finally
        {
            closeQuetly(handle);
        }

    }

    private void closeQuetly(RandomAccessFileImpl handle)
    {
        try {
            handle.close();
        } catch (Exception ex)
        {
            // keep quiet
        }
        
    }
}
