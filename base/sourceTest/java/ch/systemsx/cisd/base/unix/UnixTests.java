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

package ch.systemsx.cisd.base.unix;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.base.unix.Unix.Group;
import ch.systemsx.cisd.base.unix.Unix.Password;
import ch.systemsx.cisd.base.unix.Unix.Stat;

/**
 * Test cases for the {@link Unix} system calls.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses = Unix.class)
public class UnixTests extends AbstractFileSystemTestCase
{

    @Test(groups =
        { "requires_unix" })
    public void testGetLinkInfoRegularFile() throws IOException
    {
        final short accessMode = (short) 0777;
        final String content = "someText\n";
        final File f = new File(workingDirectory, "someFile");
        FileUtils.writeStringToFile(f, content);
        Unix.setAccessMode(f.getAbsolutePath(), accessMode);
        final Stat info = Unix.getLinkInfo(f.getAbsolutePath());
        Unix.setOwner(f.getAbsolutePath(), info.getUid(), info.getGid());
        assertEquals(1, info.getNumberOfHardLinks());
        assertEquals(content.length(), info.getSize());
        assertEquals(accessMode, info.getPermissions());
        assertEquals("root", Unix.tryGetUserNameForUid(0));
        assertEquals(FileLinkType.REGULAR_FILE, info.getLinkType());
        assertFalse(info.isSymbolicLink());
        assertEquals(f.lastModified(), 1000 * info.getLastModified());
    }

    @Test(groups =
        { "requires_unix" }, expectedExceptions = NullPointerException.class)
    public void testGetLinkNull() throws IOException
    {
        Unix.getLinkInfo(null);
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetLinkInfoDirectory() throws IOException
    {
        final File d = new File(workingDirectory, "someDir");
        d.mkdir();
        final Stat info = Unix.getLinkInfo(d.getAbsolutePath());
        assertEquals(2, info.getNumberOfHardLinks());
        assertEquals(FileLinkType.DIRECTORY, info.getLinkType());
        assertFalse(info.isSymbolicLink());
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetLinkInfoSymLink() throws IOException
    {
        final File f = new File(workingDirectory, "someOtherFile");
        final String content = "someMoreText\n";
        FileUtils.writeStringToFile(f, content);
        final File s = new File(workingDirectory, "someLink");
        Unix.createSymbolicLink(f.getAbsolutePath(), s.getAbsolutePath());
        final Stat info = Unix.getLinkInfo(s.getAbsolutePath());
        assertEquals(1, info.getNumberOfHardLinks());
        assertEquals(FileLinkType.SYMLINK, info.getLinkType());
        assertTrue(info.isSymbolicLink());
        assertEquals(f.getAbsolutePath(), info.tryGetSymbolicLink());
        assertEquals(f.getAbsolutePath(), Unix.tryReadSymbolicLink(s.getAbsolutePath()));
        assertNull(Unix.getLinkInfo(s.getAbsolutePath(), false).tryGetSymbolicLink());

        final Stat info2 = Unix.getFileInfo(s.getAbsolutePath());
        assertEquals(1, info2.getNumberOfHardLinks());
        assertEquals(content.length(), info2.getSize());
        assertEquals(FileLinkType.REGULAR_FILE, info2.getLinkType());
        assertFalse(info2.isSymbolicLink());
        assertNull(info2.tryGetSymbolicLink());
    }

    @Test(groups =
        { "requires_unix" }, expectedExceptions = NullPointerException.class)
    public void testCreateSymbolicLinkNull() throws IOException
    {
        Unix.createSymbolicLink(null, null);
    }

    @Test(groups =
        { "requires_unix" }, expectedExceptions = NullPointerException.class)
    public void testCreateHardLinkNull() throws IOException
    {
        Unix.createHardLink(null, null);
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetLinkInfoHardLink() throws IOException
    {
        final File f = new File(workingDirectory, "someOtherFile");
        f.createNewFile();
        final File s = new File(workingDirectory, "someLink");
        Unix.createHardLink(f.getAbsolutePath(), s.getAbsolutePath());
        final Stat info = Unix.getLinkInfo(s.getAbsolutePath());
        assertEquals(2, info.getNumberOfHardLinks());
        assertEquals(FileLinkType.REGULAR_FILE, info.getLinkType());
        assertFalse(info.isSymbolicLink());
        assertNull(info.tryGetSymbolicLink());
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetUid()
    {
        assertTrue(Unix.getUid() > 0);
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetEuid()
    {
        assertTrue(Unix.getEuid() > 0);
        assertEquals(Unix.getUid(), Unix.getEuid());
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetGid()
    {
        assertTrue(Unix.getGid() > 0);
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetEgid()
    {
        assertTrue(Unix.getEgid() > 0);
        assertEquals(Unix.getGid(), Unix.getEgid());
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetUidForUserName()
    {
        assertEquals(0, Unix.getUidForUserName("root"));
    }

    @Test(groups =
        { "requires_unix" }, expectedExceptions = NullPointerException.class)
    public void testGetUidForUserNameNull() throws IOException
    {
        Unix.getUidForUserName(null);
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetGidForGroupName()
    {
        final String rootGroup = Unix.tryGetGroupNameForGid(0);
        assertTrue(rootGroup, "root".equals(rootGroup) || "wheel".equals(rootGroup));
        assertEquals(0, Unix.getGidForGroupName(rootGroup));
    }

    @Test(groups =
        { "requires_unix" }, expectedExceptions = NullPointerException.class)
    public void testGetGidForGroupNameNull() throws IOException
    {
        Unix.getGidForGroupName(null);
    }

    @Test(groups =
        { "requires_unix" })
    public void testTryGetGroupByName()
    {
        final String rootGroup = Unix.tryGetGroupNameForGid(0);
        final Group group = Unix.tryGetGroupByName(rootGroup);
        assertNotNull(group);
        assertEquals(rootGroup, group.getGroupName());
        assertEquals(0, group.getGid());
        assertNotNull(group.getGroupMembers());
    }

    @Test(groups =
        { "requires_unix" }, expectedExceptions = NullPointerException.class)
    public void testTryGetGroupByNameNull() throws IOException
    {
        Unix.tryGetGroupByName(null);
    }

    @Test(groups =
        { "requires_unix" })
    public void testTryGetGroupByGid()
    {
        final Group group = Unix.tryGetGroupByGid(0);
        assertNotNull(group);
        final String rootGroup = group.getGroupName();
        assertTrue(rootGroup, "root".equals(rootGroup) || "wheel".equals(rootGroup));
        assertEquals(0, group.getGid());
        assertNotNull(group.getGroupMembers());
    }

    @Test(groups =
        { "requires_unix" })
    public void testTryGetUserByName()
    {
        final Password user = Unix.tryGetUserByName("root");
        assertNotNull(user);
        assertEquals("root", user.getUserName());
        assertEquals(0, user.getUid());
        assertEquals(0, user.getGid());
        assertNotNull(user.getUserFullName());
        assertNotNull(user.getHomeDirectory());
        assertNotNull(user.getShell());
        assertTrue(user.getShell().startsWith("/"));
    }

    @Test(groups =
        { "requires_unix" }, expectedExceptions = NullPointerException.class)
    public void testTryGetUserByNameNull() throws IOException
    {
        Unix.tryGetUserByName(null);
    }

    @Test(groups =
        { "requires_unix" })
    public void testTryGetUserByUid()
    {
        final Password user = Unix.tryGetUserByUid(0);
        assertNotNull(user);
        assertEquals("root", user.getUserName());
        assertEquals(0, user.getUid());
        assertEquals(0, user.getGid());
        assertNotNull(user.getUserFullName());
        assertNotNull(user.getHomeDirectory());
        assertNotNull(user.getShell());
        assertTrue(user.getShell().startsWith("/"));
    }

    @Test(groups =
        { "requires_unix" })
    public void testDetectProcess()
    {
        assertTrue(Unix.canDetectProcesses());
        assertTrue(Unix.isProcessRunningPS(Unix.getPid()));
    }
    
    public static void main(String[] args) throws Throwable
    {
        System.out.println(BuildAndEnvironmentInfo.INSTANCE);
        System.out.println();
        if (Unix.isOperational() == false)
        {
            System.err.println("No unix library found.");
            System.exit(1);
        }
        final UnixTests test = new UnixTests();
        try
        {
            for (Method m : UnixTests.class.getMethods())
            {
                final Test testAnnotation = m.getAnnotation(Test.class);
                if (testAnnotation == null)
                {
                    continue;
                }
                System.out.println("Running " + m.getName());
                test.setUp();
                try
                {
                    m.invoke(test);
                } catch (InvocationTargetException wrapperThrowable)
                {
                    final Throwable th = wrapperThrowable.getCause();
                    boolean exceptionFound = false;
                    for (Class<?> expectedExClazz : testAnnotation.expectedExceptions())
                    {
                        if (expectedExClazz == th.getClass())
                        {
                            exceptionFound = true;
                            break;
                        }
                    }
                    if (exceptionFound == false)
                    {
                        throw th;
                    }
                }
            }
            System.out.println("Tests OK!");
        } finally
        {
            test.afterClass();
        }
    }

}
