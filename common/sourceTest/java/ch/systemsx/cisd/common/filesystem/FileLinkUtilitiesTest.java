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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;


import static org.testng.AssertJUnit.*;

/**
 * Test cases for the {@link FileLinkUtilities}.
 * 
 * @author Bernd Rinn
 */
public class FileLinkUtilitiesTest extends AbstractFileSystemTestCase
{

    @Test(groups =
        { "requires_unix" })
    public void testGetLinkInfoRegularFile() throws IOException
    {
        final short accessMode = (short) 0777;
        final String content = "someText\n";
        final File f = new File(workingDirectory, "someFile");
        FileUtilities.writeToFile(f, content);
        FileLinkUtilities.setAccessMode(f.getAbsolutePath(), accessMode);
        final FileLinkInfo info = FileLinkUtilities.getLinkInfo(f.getAbsolutePath());
        FileLinkUtilities.setOwner(f.getAbsolutePath(), info.getUid(), info.getGid());
        assertEquals(content.length(), info.getSize());
        assertEquals(accessMode, info.getPermissions());
        assertEquals("root", FileLinkUtilities.getUserNameForUid(0));
        final String rootGroup = FileLinkUtilities.getGroupNameForGid(0);
        assertTrue("root".equals(rootGroup) || "wheel".equals(rootGroup));
        assertEquals(FileLinkType.REGULAR_FILE, info.getLinkType());
        assertFalse(info.isSymbolicLink());
        assertEquals(0, FileLinkUtilities.getUidForUserName("root"));
        assertEquals(0, FileLinkUtilities.getGidForGroupName(rootGroup));
        assertEquals(f.lastModified(), 1000 * info.getLastModified());
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetLinkInfoDirectory() throws IOException
    {
        final File d = new File(workingDirectory, "someDir");
        d.mkdir();
        final FileLinkInfo info = FileLinkUtilities.getLinkInfo(d.getAbsolutePath());
        assertEquals(FileLinkType.DIRECTORY, info.getLinkType());
        assertFalse(info.isSymbolicLink());
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetLinkInfoSymLink() throws IOException
    {
        final File f = new File(workingDirectory, "someOtherFile");
        f.createNewFile();
        final File s = new File(workingDirectory, "someLink");
        FileLinkUtilities.createSymbolicLink(f.getAbsolutePath(), s.getAbsolutePath());
        final FileLinkInfo info = FileLinkUtilities.getLinkInfo(s.getAbsolutePath());
        assertEquals(FileLinkType.SYMLINK, info.getLinkType());
        assertTrue(info.isSymbolicLink());
        assertEquals(f.getAbsolutePath(), info.tryGetSymbolicLink());
        assertEquals(f.getAbsolutePath(), FileLinkUtilities
                .tryReadSymbolicLink(s.getAbsolutePath()));
        assertNull(FileLinkUtilities.getLinkInfo(s.getAbsolutePath(), false).tryGetSymbolicLink());
    }

    public static void main(String[] args) throws IOException
    {
        FileLinkUtilitiesTest test = new FileLinkUtilitiesTest();
        test.testGetLinkInfoRegularFile();
        test.testGetLinkInfoDirectory();
        test.testGetLinkInfoSymLink();
    }

}
