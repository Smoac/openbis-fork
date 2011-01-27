/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class MoveFileCommandTest extends AbstractTestWithRollbackStack
{
    private File srcFile;

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();

        srcFile = new File(workingDirectory, "read.me");
        fillContentsOfSource();
    }

    @Test
    public void testMoveToDir()
    {
        File dstDir = new File(workingDirectory, "bar-dir/");
        MkdirsCommand mkdirsCmd = new MkdirsCommand(dstDir.getAbsolutePath());

        File dstFile = new File(dstDir, srcFile.getName());
        MoveFileCommand cmd =
                new MoveFileCommand(workingDirectory.getAbsolutePath(), srcFile.getName(),
                        dstDir.getAbsolutePath(), srcFile.getName());

        rollbackStack.pushAndExecuteCommand(mkdirsCmd);
        rollbackStack.pushAndExecuteCommand(cmd);

        checkContentsOfFile(dstFile);
    }

    @Test
    public void testUndo()
    {
        File dstDir = new File(workingDirectory, "bar-dir/");
        MkdirsCommand mkdirsCmd = new MkdirsCommand(dstDir.getAbsolutePath());

        File dstFile = new File(dstDir, srcFile.getName());
        MoveFileCommand cmd =
                new MoveFileCommand(workingDirectory.getAbsolutePath(), srcFile.getName(),
                        dstDir.getAbsolutePath(), srcFile.getName());

        rollbackStack.pushAndExecuteCommand(mkdirsCmd);
        rollbackStack.pushAndExecuteCommand(cmd);

        checkContentsOfFile(dstFile);

        rollbackStack.rollbackAll();

        assertTrue("The file should have been deleted", false == dstFile.exists());
        assertTrue("The directory should have been deleted", false == dstDir.exists());
        checkContentsOfFile(srcFile);
    }

    private void checkContentsOfFile(File dst)
    {
        assertTrue("The file should exist", dst.exists());
        assertEquals("hello world\n", FileUtilities.loadToString(dst));
    }

    private void fillContentsOfSource()
    {
        FileUtilities.writeToFile(srcFile, "hello world");
    }
}
