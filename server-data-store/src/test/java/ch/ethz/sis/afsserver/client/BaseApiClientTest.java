/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.afsserver.client;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.sis.afs.manager.TransactionConnection;
import ch.ethz.sis.afsapi.dto.File;
import ch.ethz.sis.afsapi.dto.FreeSpace;
import ch.ethz.sis.afsclient.client.AfsClient;
import ch.ethz.sis.afsserver.server.Server;
import ch.ethz.sis.shared.io.IOUtils;

public abstract class BaseApiClientTest
{
    protected static Server<TransactionConnection, ?> afsServer;

    protected static AfsClient afsClient;

    protected static int httpServerPort;

    protected static String httpServerPath;

    protected static String storageRoot;

    protected static final String FILE_A = "A.txt";

    protected static final byte[] DATA = "ABCD".getBytes();

    protected static final String FILE_B = "B.txt";

    protected static final String FILE_BINARY = "test.png";

    protected static String owner = UUID.randomUUID().toString();

    protected int binarySize = -1;

    protected byte[] binaryData = null;

    protected String testDataRoot;

    @AfterClass
    public static void classTearDown() throws Exception
    {
        afsServer.shutdown(true);
    }

    @Before
    public void setUp() throws Exception
    {
        testDataRoot = IOUtils.getPath(storageRoot, owner);
        IOUtils.createDirectories(testDataRoot);
        String testDataFile = IOUtils.getPath(testDataRoot, FILE_A);
        IOUtils.createFile(testDataFile);
        IOUtils.write(testDataFile, 0, DATA);

        final String binaryTestDataFile = IOUtils.getPath(testDataRoot, FILE_BINARY);
        final URL resource = getClass().getClassLoader().getResource("ch/ethz/sis/afsserver/client/test.png");
        final java.io.File file = new java.io.File(resource.toURI());
        this.binarySize = (int) file.length();
        IOUtils.copy(resource.getPath(), binaryTestDataFile);

        try (final FileInputStream fis = new FileInputStream(file)) {
            binaryData = fis.readAllBytes();
        }

        afsClient = new AfsClient(
                new URI("http", null, "localhost", httpServerPort, httpServerPath, null, null));
    }

    @After
    public void deleteTestData() throws IOException
    {
        IOUtils.delete(storageRoot);
    }

    @Test
    public void login_sessionTokenIsNotNull() throws Exception
    {
        final String token = login();
        assertNotNull(token);
    }

    @Test
    public void isSessionValid_throwsException() throws Exception
    {
        try
        {
            afsClient.isSessionValid();
            fail();
        } catch (IllegalStateException e)
        {
            assertThat(e.getMessage(), containsString("No session information detected!"));
        }
    }

    @Test
    public void isSessionValid_returnsTrue() throws Exception
    {
        login();

        final Boolean isValid = afsClient.isSessionValid();
        assertTrue(isValid);
    }

    @Test
    public void logout_withoutLogin_throwsException() throws Exception
    {
        try
        {
            afsClient.logout();
            fail();
        } catch (IllegalStateException e)
        {
            assertThat(e.getMessage(), containsString("No session information detected!"));
        }
    }

    @Test
    public void logout_withLogin_returnsTrue() throws Exception
    {
        login();

        final Boolean result = afsClient.logout();

        assertTrue(result);
    }

    @Test
    public void list_getsDataListFromTemporaryFolder() throws Exception
    {
        login();

        List<File> list = afsClient.list(owner, "", Boolean.TRUE);
        assertEquals(2, list.size());
        assertEquals(FILE_A, list.get(0).getName());
    }

    @Test
    public void free_returnsValue() throws Exception
    {
        login();

        final FreeSpace space = afsClient.free(owner, "");
        assertTrue(space.getFree() >= 0);
        assertTrue(space.getTotal() > 0);
        assertTrue(space.getFree() <= space.getTotal());
    }

    @Test
    public void read_getsDataFromTemporaryFile() throws Exception
    {
        login();

        byte[] bytes = afsClient.read(owner, FILE_A, 0L, DATA.length);
        assertArrayEquals(DATA, bytes);
    }

    @Test
    public void read_binaryFile() throws Exception
    {
        login();

        byte[] bytes = afsClient.read(owner, FILE_BINARY, 0L, binarySize);
        assertArrayEquals(binaryData, bytes);
    }

    @Test
    public void resumeRead_getsDataFromTemporaryFile() throws Exception
    {
        login();

        afsClient.resumeRead(owner, FILE_A, Path.of(FILE_B), 0L);

        assertFilesEqual(IOUtils.getPath(testDataRoot, FILE_A), FILE_B);
    }

    private void assertFilesEqual(final String expectedFile, final String actualFile) throws IOException
    {
        byte[] expectedData = IOUtils.readFully(expectedFile);
        byte[] actualData = IOUtils.readFully(actualFile);
        assertArrayEquals(expectedData, actualData);
    }

    @Test
    public void write_zeroOffset_createsFile() throws Exception
    {
        login();

        Boolean result = afsClient.write(owner, FILE_B, 0L, DATA, IOUtils.getMD5(DATA));
        assertTrue(result);

        byte[] testDataFile = IOUtils.readFully(IOUtils.getPath(testDataRoot, FILE_B));
        assertArrayEquals(DATA, testDataFile);
    }

    @Test
    public void write_nonZeroOffset_createsFile() throws Exception
    {
        login();

        Long offset = 65L;
        Boolean result = afsClient.write(owner, FILE_B, offset, DATA, IOUtils.getMD5(DATA));
        assertTrue(result);

        byte[] testDataFile = IOUtils.readFully(IOUtils.getPath(testDataRoot, FILE_A));
        assertArrayEquals(DATA, testDataFile);
    }

    @Test
    public void resumeWrite_zeroOffset_createsFile() throws Exception
    {
        login();

        final Boolean result = afsClient.resumeWrite(owner, FILE_B, Path.of(IOUtils.getPath(testDataRoot, FILE_A)), 0L);
        assertTrue(result);

        assertFilesEqual(IOUtils.getPath(testDataRoot, FILE_A), FILE_B);

        Path.of(FILE_B).toFile().delete();
    }

    @Test
    public void resumeWrite_nonZeroOffset_doesNotCreateFile() throws Exception
    {
        login();

        final Long offset = 65L;
        final Boolean result = afsClient.resumeWrite(owner, FILE_B, Path.of(IOUtils.getPath(testDataRoot, FILE_A)), offset);
        assertTrue(result);

        assertFalse(Path.of(FILE_B).toFile().exists());
    }

    @Test
    public void delete_fileIsGone() throws Exception
    {
        login();

        Boolean deleted = afsClient.delete(owner, FILE_A);
        assertTrue(deleted);

        List<ch.ethz.sis.afs.api.dto.File> list = IOUtils.list(testDataRoot, true);
        assertEquals(1, list.size());
    }

    @Test
    public void copy_newFileIsCreated() throws Exception
    {
        login();

        Boolean result = afsClient.copy(owner, FILE_A, owner, FILE_B);
        assertTrue(result);

        byte[] testDataFile = IOUtils.readFully(IOUtils.getPath(testDataRoot, FILE_B));
        assertArrayEquals(DATA, testDataFile);
    }

    @Test
    public void move_fileIsRenamed() throws Exception
    {
        login();

        Boolean result = afsClient.move(owner, FILE_A, owner, FILE_B);
        assertTrue(result);

        List<ch.ethz.sis.afs.api.dto.File> list = IOUtils.list(testDataRoot, true);
        assertEquals(2, list.size());
        assertEquals(Set.of(FILE_B, FILE_BINARY), Set.of(list.get(0).getName(), list.get(1).getName()));

        byte[] testDataFile = IOUtils.readFully(IOUtils.getPath(testDataRoot, FILE_B));
        assertArrayEquals(DATA, testDataFile);
    }

    protected String login() throws Exception
    {
        return afsClient.login("test", "test");
    }

}
