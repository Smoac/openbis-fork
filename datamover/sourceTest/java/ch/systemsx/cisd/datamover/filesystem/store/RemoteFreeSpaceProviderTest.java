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

package ch.systemsx.cisd.datamover.filesystem.store;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandBuilder;

/**
 * Test cases for {@link RemoteFreeSpaceProvider}.
 * 
 * @author Christian Ribeaud
 */
public final class RemoteFreeSpaceProviderTest
{

    @Test
    public final void testFreeSpaceKb() throws IOException
    {
        ISshCommandBuilder sshCmdBuilder = FileStoreRemoteTest.createFakeSshComandBuilder();
        final RemoteFreeSpaceProvider freeSpaceProvider =
                new RemoteFreeSpaceProvider(sshCmdBuilder);
        System.out.println(freeSpaceProvider.freeSpaceKb(new HostAwareFile("fake-host-name",
                new File("/"), null)));
    }
}
