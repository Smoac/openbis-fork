/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.io.hierarchical_content;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipOutputStream;

/**
 * @author Franz-Josef Elmer
 */
public class ZipBasedHierarchicalContentTest extends AbstractFileSystemTestCase
{
    private static final File TEST_HDF5_CONTAINER = new File(
            "../openbis-common/resource/test-data/HDF5ContainerBasedHierarchicalContentNodeTest/thumbnails.h5");

    public static File[] getUnzippedFiles()
    {
        File[] tempFiles = ZipBasedHierarchicalContent.TEMP_FOLDER.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File file, String name)
                {
                    return name.startsWith(ZipBasedHierarchicalContent.TEMP_FILE_PREFIX);
                }
            });
        return tempFiles;
    }

    public static void removeUnzippedFiles()
    {
        File[] unzippedFiles = getUnzippedFiles();
        for (File unzippedFile : unzippedFiles)
        {
            FileUtilities.delete(unzippedFile);
        }
    }

    private IHierarchicalContent content;

    @BeforeMethod
    public void setUpContent() throws Exception
    {
        removeUnzippedFiles();
    }

    @AfterMethod
    public void closeContent()
    {
        if (content == null)
        {
            content.close();
        }
    }

    @Test
    public void test() throws Exception
    {
        File dataRoot = new File(workingDirectory, "data");
        File alpha = new File(dataRoot, "alpha");
        File beta = new File(alpha, "beta");
        beta.mkdirs();
        new File(alpha, "empty-dir").mkdir();
        FileUtilities.writeToFile(new File(beta, "hello.txt"), "hello world!");
        FileUtilities.writeToFile(new File(alpha, "read.me"), "don't read me!");
        FileUtilities.writeToFile(new File(dataRoot, "change-log.txt"), "nothing really changed.");
        File originalHdf5ContainerFile = new File(dataRoot, "my-container.h5");
        FileUtils.copyFile(TEST_HDF5_CONTAINER, originalHdf5ContainerFile, false);
        File zipFile = new File(workingDirectory, "data.zip");
        zip(zipFile, dataRoot);

        content = new ZipBasedHierarchicalContent(zipFile);

        IHierarchicalContentNode rootNode = content.getRootNode();
        assertDirectoryNode("", "", rootNode);
        List<IHierarchicalContentNode> childNodes = rootNode.getChildNodes();
        assertDirectoryNode("alpha", "alpha", childNodes.get(0));
        List<IHierarchicalContentNode> grandChildNodes = childNodes.get(0).getChildNodes();
        assertDirectoryNode("alpha/beta", "beta", grandChildNodes.get(0));
        assertDirectoryNode("alpha/empty-dir", "empty-dir", grandChildNodes.get(1));
        List<IHierarchicalContentNode> grandGrandChildNodes = grandChildNodes.get(0).getChildNodes();
        assertFileNode("alpha/beta/hello.txt", "hello.txt", "hello world!", grandGrandChildNodes.get(0));
        assertEquals(1, grandGrandChildNodes.size());
        assertFileNode("alpha/read.me", "read.me", "don't read me!", grandChildNodes.get(2));
        assertEquals(3, grandChildNodes.size());
        assertFileNode("change-log.txt", "change-log.txt", "nothing really changed.", childNodes.get(1));
        IHierarchicalContentNode hdf5Node = childNodes.get(2);
        assertEquals(true, hdf5Node.isDirectory());
        assertEquals(-2098219814, hdf5Node.getChecksumCRC32());
        assertEquals(537641, hdf5Node.getFileLength());

        // allow 1 second difference as de.schlichtherle.util.zip.ZipEntry.setTime()
        // method may cause such a side effect due to some OS compatibility conversion
        assertTrue(Math.abs(originalHdf5ContainerFile.lastModified() - hdf5Node.getLastModified()) <= 1000);

        assertEquals(3, childNodes.size());
        File[] tempFiles = getUnzippedFiles();
        for (File tempFile : tempFiles)
        {
            assertEquals(0, tempFile.length());
        }
        assertEquals(1, tempFiles.length);

        // now the file is lazy created
        assertEquals(hdf5Node.getFileLength(), hdf5Node.getFile().length());
        for (File tempFile : tempFiles)
        {
            assertEquals(hdf5Node.getFile(), tempFile);
            assertEquals(537641, tempFile.length());
        }

        HDF5ContainerBasedHierarchicalContentNodeTest.assertH5ExampleContent(hdf5Node);

        content.close();
        assertEquals("[]", Arrays.asList(getUnzippedFiles()).toString());
    }

    private void assertDirectoryNode(String expectedPath, String expectedName, IHierarchicalContentNode node)
    {
        assertPathAndName(expectedPath, expectedName, node);
        assertEquals(true, node.isDirectory());
    }

    private void assertFileNode(String expectedPath, String expectedName, String expectedContent,
            IHierarchicalContentNode node) throws Exception
    {
        assertPathAndName(expectedPath, expectedName, node);
        assertEquals(false, node.isDirectory());
        assertEquals(expectedContent.length(), node.getFileLength());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        IOUtils.copy(node.getInputStream(), output);
        assertEquals(expectedContent, output.toString());
        CRC32 checksum = new CRC32();
        CheckedInputStream in = new CheckedInputStream(new ByteArrayInputStream(expectedContent.getBytes()), checksum);
        IOUtils.copy(in, new NullOutputStream());
        assertEquals(checksum.getValue(), node.getChecksumCRC32());

    }

    private void assertPathAndName(String expectedPath, String expectedName, IHierarchicalContentNode node)
    {
        assertEquals(expectedPath, node.getRelativePath());
        assertEquals(expectedName, node.getName());
        String parentRelativePath = node.getParentRelativePath();
        if (expectedPath.equals(""))
        {
            assertEquals(null, parentRelativePath);
        } else
        {
            assertEquals(expectedPath,
                    (StringUtils.isBlank(parentRelativePath) ? "" : parentRelativePath + "/") + node.getName());
        }
    }

    private void zip(File zipFile, File folder)
    {
        OutputStream outputStream = null;
        ZipOutputStream zipOutputStream = null;
        try
        {
            outputStream = new FileOutputStream(zipFile);
            zipOutputStream = new ZipOutputStream(outputStream);
            zip(zipOutputStream, folder, folder);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            if (zipOutputStream != null)
            {
                try
                {
                    zipOutputStream.close();
                } catch (Exception ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            } else
            {
                IOUtils.closeQuietly(outputStream);
            }
        }
    }

    private void zip(ZipOutputStream zipOutputStream, File rootFile, File file)
    {
        if (file.isFile())
        {
            zipTo(zipOutputStream, rootFile, file);
        } else if (file.isDirectory())
        {
            File[] files = file.listFiles();
            if (files.length == 0)
            {
                try
                {
                    String path = FileUtilities.getRelativeFilePath(rootFile, file).replace('\\', '/');
                    if (path.endsWith("/") == false)
                    {
                        path += "/";
                    }
                    ZipEntry entry = new ZipEntry(path);
                    entry.setTime(file.lastModified());
                    zipOutputStream.putNextEntry(entry);
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                } finally
                {
                    try
                    {
                        zipOutputStream.closeEntry();
                    } catch (IOException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }
            } else
            {
                Arrays.sort(files);
                for (File childFile : files)
                {
                    zip(zipOutputStream, rootFile, childFile);
                }
            }
        }
    }

    private void zipTo(ZipOutputStream zipOutputStream, File rootFile, File file)
    {
        long lastModified = file.lastModified();
        FileInputStream in = null;
        try
        {
            in = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(FileUtilities.getRelativeFilePath(rootFile, file).replace('\\', '/'));
            zipEntry.setTime(lastModified);
            zipEntry.setMethod(ZipEntry.DEFLATED);
            zipOutputStream.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) > 0)
            {
                zipOutputStream.write(buffer, 0, len);
            }
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(in);
            try
            {
                zipOutputStream.closeEntry();
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

}
