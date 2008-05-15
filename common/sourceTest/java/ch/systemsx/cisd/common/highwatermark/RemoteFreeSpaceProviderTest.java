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

package ch.systemsx.cisd.common.highwatermark;

import static org.testng.AssertJUnit.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.highwatermark.RemoteFreeSpaceProvider;
import ch.systemsx.cisd.common.utilities.OSUtilities;

/**
 * Test cases for {@link RemoteFreeSpaceProvider}.
 * 
 * @author Christian Ribeaud
 */
public final class RemoteFreeSpaceProviderTest
{

    @Test(groups = "broken")
    public final void testFreeSpaceKb() throws IOException
    {
        final File sshExecutable = OSUtilities.findExecutable("ssh");
        assertNotNull(sshExecutable);
        final RemoteFreeSpaceProvider freeSpaceProvider =
                new RemoteFreeSpaceProvider("sprint-ob", sshExecutable);
        System.out.println(freeSpaceProvider.freeSpaceKb(new File("/")));
    }
}
