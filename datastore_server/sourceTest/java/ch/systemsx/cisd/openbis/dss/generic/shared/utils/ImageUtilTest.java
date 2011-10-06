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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.ByteBufferRandomAccessFile;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.io.FileBasedContentNode;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.ImageReadersTestHelper;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = ImageUtil.class)
public class ImageUtilTest extends AssertJUnit
{
    private static class MockIHierarchicalContentNode implements IHierarchicalContentNode
    {
        final MockRandomAccessFile is = new MockRandomAccessFile();

        public boolean exists()
        {
            throw new UnsupportedOperationException();
        }

        public InputStream getInputStream()
        {
            throw new UnsupportedOperationException();
        }

        public String getName()
        {
            throw new UnsupportedOperationException();
        }

        public String getRelativePath()
        {
            throw new UnsupportedOperationException();
        }

        public String getParentRelativePath()
        {
            throw new UnsupportedOperationException();
        }

        public boolean isDirectory()
        {
            throw new UnsupportedOperationException();
        }

        public long getLastModified()
        {
            throw new UnsupportedOperationException();
        }

        public List<IHierarchicalContentNode> getChildNodes() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }

        public File getFile() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }

        public long getFileLength() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }

        public IRandomAccessFile getFileContent() throws UnsupportedOperationException,
                IOExceptionUnchecked
        {
            return is;
        }

    }

    private static class MockRandomAccessFile extends ByteBufferRandomAccessFile
    {
        public MockRandomAccessFile()
        {
            super(1);
        }

        boolean closeInvoked;

        @Override
        public void close()
        {
            closeInvoked = true;
        }
    }

    private File dir;

    @BeforeMethod
    public void setUp() throws Exception
    {
        dir = new File("../datastore_server/resource/test-data/ImageUtilTest");
        ImageReadersTestHelper.setUpLibraries(ImageReaderConstants.IMAGEIO_LIBRARY,
                ImageReaderConstants.JAI_LIBRARY, ImageReaderConstants.IMAGEJ_LIBRARY);

    }

    @Test
    public void testGifImage()
    {
        assertImageSize(805, 1023, loadImageByFile("gif-example.gif"));
        assertImageSize(805, 1023, loadImageByInputStream("gif-example.gif"));
    }

    @Test
    public void testJpegImage()
    {
        assertImageSize(805, 1023, loadImageByFile("jpeg-example.jpg"));
        assertImageSize(805, 1023, loadImageByInputStream("jpeg-example.jpg"));
    }

    @Test
    public void testPngImage()
    {
        assertImageSize(805, 1023, loadImageByFile("png-example.png"));
        assertImageSize(805, 1023, loadImageByInputStream("png-example.png"));
    }

    @Test(groups = "slow")
    public void testTiffImage()
    {
        assertImageSize(805, 1023, loadImageByFile("tiff-example.tiff"));
        assertImageSize(805, 1023, loadImageByInputStream("tiff-example.tiff"));
    }

    @Test
    public void testCreateThumbnail()
    {
        BufferedImage image = loadImageByFile("gif-example.gif");
        assertImageSize(79, 100, ImageUtil.createThumbnailForDisplay(image, 200, 100));
        assertImageSize(100, 127, ImageUtil.createThumbnailForDisplay(image, 100, 200));
    }

    @Test
    public void testInputStreamAutomaticallyClosed()
    {
        MockIHierarchicalContentNode contentNode = new MockIHierarchicalContentNode();
        try
        {
            ImageUtil.loadImage(contentNode);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("File type of an image input stream couldn't be determined.",
                    ex.getMessage());
        }

        assertEquals(true, contentNode.is.closeInvoked);
    }

    private BufferedImage loadImageByFile(String fileName)
    {
        File file = new File(dir, fileName);
        BufferedImage image = ImageUtil.loadImage(file);
        return image;
    }

    private BufferedImage loadImageByInputStream(String fileName)
    {
        File file = new File(dir, fileName);
        return ImageUtil.loadImage(new FileBasedContentNode(file));
    }

    private void assertImageSize(int expectedWith, int expectedHeight, BufferedImage image)
    {
        assertEquals(expectedWith, image.getWidth());
        assertEquals(expectedHeight, image.getHeight());
    }

    public static BufferedImage loadImage(File imageFile)
    {
        return loadImage(new FileBasedContentNode(imageFile));
    }

    public static BufferedImage loadImage(IHierarchicalContentNode content)
    {
        return ImageUtil.loadImage(content);
    }

}
