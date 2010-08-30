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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
            DataTypeUtil.tryToFigureOutFileTypeOf(new InputStream()
            {
                @Override
                public int read() throws IOException
                {
                    return 0;
                }
            });
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Input stream does not support marking. "
                    + "Wrap input stream with a BufferedInputStream to solve the problem.", ex
                    .getMessage());
        }
    }
    
    @Test
    public void testInputStreamAtTheBeginning() throws Exception
    {
        ByteArrayInputStream is = new ByteArrayInputStream("hello world".getBytes());
        DataTypeUtil.tryToFigureOutFileTypeOf(is);
        assertEquals("[hello world]", IOUtils.readLines(is).toString());
    }
    
    private void assertFileType(String expectedFileType, String fileName) throws Exception
    {
        FileInputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(new File(dir, fileName));
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            String type = DataTypeUtil.tryToFigureOutFileTypeOf(bis);
            
            assertEquals(expectedFileType, type);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
        
    }
}
