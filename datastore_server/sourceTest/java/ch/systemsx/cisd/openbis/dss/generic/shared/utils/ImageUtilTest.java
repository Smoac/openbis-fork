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

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.io.ByteBufferRandomAccessFile;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.io.FileBasedContent;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.ImageReadersTestHelper;

/**
 * @author Franz-Josef Elmer
 */
public class ImageUtilTest extends AssertJUnit
{
    private static class MockIContent implements IContent
    {
        final MockRandomAccessFile is = new MockRandomAccessFile();
        
        public String tryGetName()
        {
            throw new UnsupportedOperationException();
        }

        public long getSize()
        {
            throw new UnsupportedOperationException();
        }

        public boolean exists()
        {
            throw new UnsupportedOperationException();
        }

        public IRandomAccessFile getReadOnlyRandomAccessFile()
        {
            return is;
        }

        public InputStream getInputStream()
        {
            throw new UnsupportedOperationException();
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
        assertImageSize(79, 100, ImageUtil.createThumbnail(image, 200, 100));
        assertImageSize(100, 127, ImageUtil.createThumbnail(image, 100, 200));
    }
    
    @Test
    public void testInputStreamAutomaticallyClosed()
    {
        MockIContent content = new MockIContent();
        try
        {
            ImageUtil.loadImage(content);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("File type of an image input stream couldn't be determined.", ex.getMessage());
        }
        
        assertEquals(true, content.is.closeInvoked);
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
        return ImageUtil.loadImage(new FileBasedContent(file));
    }
    
    private void assertImageSize(int expectedWith, int expectedHeight, BufferedImage image)
    {
        assertEquals(expectedWith, image.getWidth());
        assertEquals(expectedHeight, image.getHeight());
    }
}
