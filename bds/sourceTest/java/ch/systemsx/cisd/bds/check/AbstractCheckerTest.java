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

package ch.systemsx.cisd.bds.check;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.common.test.Retry10;
import ch.systemsx.cisd.common.utilities.OSUtilities;

/**
 * An abstract test class for the checkers.
 * 
 * @author Izabela Adamczyk
 */
public abstract class AbstractCheckerTest
{

    protected final String errorFoundNotTrimmed(final String path, final String file,
            final String dir)
    {
        return String.format("ERROR: Found not trimmed value in file '%s' (directory '%s%s')."
                + OSUtilities.LINE_SEPARATOR, file, path, dir);
    }

    @Test(retryAnalyzer = Retry10.class)
    public final void testCheckTrimmedOk()
    {
        final File dir = new File("testdata/trimming");
        final IDirectory containerNode = NodeFactory.createDirectoryNode(dir);
        final ProblemReport problemReport = new ProblemReport();
        AbstractChecker.checkTrimmed(problemReport, containerNode, "ok");
        Assert.assertEquals(problemReport.toString(), "");
    }

    @Test
    public final void testCheckTrimmedNewLineBeginning()
    {
        final File dir = new File("testdata/trimming");
        final IDirectory containerNode = NodeFactory.createDirectoryNode(dir);
        final ProblemReport problemReport = new ProblemReport();
        final String fileName = "new_line_beginning";
        AbstractChecker.checkTrimmed(problemReport, containerNode, fileName);
        Assert.assertEquals(problemReport.toString(), errorFoundNotTrimmed(dir.getAbsolutePath(),
                fileName, ""));
    }

    @Test
    public final void testCheckTrimmedNewLineEnd()
    {
        final File dir = new File("testdata/trimming");
        final IDirectory containerNode = NodeFactory.createDirectoryNode(dir);
        final ProblemReport problemReport = new ProblemReport();
        final String fileName = "new_line_end";
        AbstractChecker.checkTrimmed(problemReport, containerNode, fileName);
        Assert.assertEquals(problemReport.toString(), errorFoundNotTrimmed(dir.getAbsolutePath(),
                fileName, ""));
    }

    @Test
    public final void testCheckTrimmedSpaceBeginning()
    {
        final File dir = new File("testdata/trimming");
        final IDirectory containerNode = NodeFactory.createDirectoryNode(dir);
        final ProblemReport problemReport = new ProblemReport();
        final String fileName = "space_beginning";
        AbstractChecker.checkTrimmed(problemReport, containerNode, fileName);
        Assert.assertEquals(problemReport.toString(), errorFoundNotTrimmed(dir.getAbsolutePath(),
                fileName, ""));
    }

    @Test(retryAnalyzer = Retry10.class)
    public final void testCheckTrimmedSpaceEnd()
    {
        final File dir = new File("testdata/trimming");
        final IDirectory containerNode = NodeFactory.createDirectoryNode(dir);
        final ProblemReport problemReport = new ProblemReport();
        final String fileName = "space_end";
        AbstractChecker.checkTrimmed(problemReport, containerNode, fileName);
        Assert.assertEquals(problemReport.toString(), errorFoundNotTrimmed(dir.getAbsolutePath(),
                fileName, ""));
    }
}
