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

package ch.systemsx.cisd.common.parser;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.parser.ParserUtilities.Line;

/**
 * Test cases for corresponding {@link ParserUtilities} class.
 * 
 * @author Christian Ribeaud
 */
public final class ParserUtilitiesTest
{

    private static final File unitTestRootDirectory = new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "ParserUtilitiesTest");

    @BeforeClass
    public final void setUp()
    {
        LogInitializer.init();
        workingDirectory.mkdirs();
        assert workingDirectory.isDirectory();
    }

    @AfterClass
    public void tearDown() throws IOException
    {
        FileUtils.deleteDirectory(workingDirectory);
        workingDirectory.deleteOnExit();
    }

    @Test
    public final void testGetFirstAcceptedLineWithProblematicValues()
    {
        File file = new File(workingDirectory, "doesNotExist");
        try
        {
            ParserUtilities.getFirstAcceptedLine(null, null);
            fail("Given file can not be null.");
        } catch (AssertionError e)
        {
            // Nothing to do here.
        }
        assert file.exists() == false;
        try
        {
            ParserUtilities.getFirstAcceptedLine(file, null);
            fail("Given file must exist.");
        } catch (AssertionError e)
        {
            // Nothing to do here.
        }
    }

    @Test
    public final void testGetFirstAcceptedLineWithNullILineFilter() throws IOException
    {
        String[] lines = new String[]
            { StringUtils.EMPTY, "non-empty line", StringUtils.EMPTY, "hello" };
        File file = new File(workingDirectory, "test.txt");
        FileUtils.writeLines(file, Arrays.asList(lines));
        Line line = ParserUtilities.getFirstAcceptedLine(file, null);
        assertEquals(StringUtils.EMPTY, line.text);
        assertEquals(0, line.number);
        assert file.delete();
    }

    @Test
    public final void testGetFirstAcceptedLine() throws IOException
    {
        String[] lines = new String[]
            { StringUtils.EMPTY, "# comment line", StringUtils.EMPTY, "hello" };
        File file = new File(workingDirectory, "test.txt");
        FileUtils.writeLines(file, Arrays.asList(lines));
        Line line = ParserUtilities.getFirstAcceptedLine(file, ExcludeEmptyAndCommentLineFilter.INSTANCE);
        assertEquals("hello", line.text);
        assertEquals(3, line.number);
        assert file.delete();
    }
}
