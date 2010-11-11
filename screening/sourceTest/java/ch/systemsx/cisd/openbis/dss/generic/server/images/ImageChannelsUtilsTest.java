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

package ch.systemsx.cisd.openbis.dss.generic.server.images;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.io.FileBasedContent;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.IHCSImageDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ImageChannelsUtilsTest extends AssertJUnit
{
    private static final String SESSION_ID = "session-42";
    private static final String DATASET_CODE = "dataset-123";

    private static final String CHANNEL = "GFP";

    private static final IImageTransformer TRANSFORMER = new IImageTransformer()
        {
            public BufferedImage transform(BufferedImage image)
            {
                int width = image.getWidth();
                int height = image.getHeight();
                BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < width; x++)
                {
                    for (int y = 0; y < height; y++)
                    {
                        int rgb = image.getRGB(x, y);
                        output.setRGB(x, y, (rgb & 0xff) << 16);
                    }
                }
                return output;
            }
        };

    private Mockery context;
    private IHCSImageDatasetLoader loader;
    private File imageFolder;
    private IImageTransformerFactory transformerFactory;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        loader = context.mock(IHCSImageDatasetLoader.class);
        transformerFactory = context.mock(IImageTransformerFactory.class);
        
        imageFolder = new File("../screening/sourceTest/java/" + getClass().getPackage().getName().replace('.', '/'));
    }
    
    @AfterMethod
    public void tearDown()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetUnkownImage()
    {
        final TileImageReference imageRef = new TileImageReference();
        imageRef.setChannel(CHANNEL);
        imageRef.setDatasetCode(DATASET_CODE);
        imageRef.setSessionId(SESSION_ID);
        imageRef.setChannelStack(ImageChannelStackReference.createFromId(4711));
        context.checking(new Expectations()
            {
                {
                    one(loader)
                            .tryGetImage(imageRef.getChannel(), imageRef.getChannelStack(), null);
                    will(returnValue(null));
                }
            });

        try
        {
            ImageChannelsUtils.getImage(loader, imageRef);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("No image found for channel stack "
                    + "ImageChannelStackReference{locationRefOrNull=<null>,idRefOrNull=4711} "
                    + "and channel GFP", ex.getMessage());
        }

        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetTiffImage()
    {
        final TileImageReference imageRef = new TileImageReference();
        imageRef.setChannel(CHANNEL);
        imageRef.setDatasetCode(DATASET_CODE);
        imageRef.setSessionId(SESSION_ID);
        imageRef.setChannelStack(ImageChannelStackReference.createFromId(4711));
        context.checking(new Expectations()
            {
                {
                    one(loader)
                            .tryGetImage(imageRef.getChannel(), imageRef.getChannelStack(), null);
                    will(returnValue(new AbsoluteImageReference(
                            image("img1.tiff"), "id42", null, null, null)));
                }
            });
        
        IContent image = ImageChannelsUtils.getImage(loader, imageRef);
        assertEquals(getImageContentDescription(image("img1.png")), getImageContentDescription(image));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetJpegImageAsPngThumbnail()
    {
        final TileImageReference imageRef = new TileImageReference();
        imageRef.setChannel(CHANNEL);
        imageRef.setDatasetCode(DATASET_CODE);
        imageRef.setSessionId(SESSION_ID);
        imageRef.setChannelStack(ImageChannelStackReference.createFromId(4711));
        context.checking(new Expectations()
            {
                {
                    one(loader)
                            .tryGetImage(imageRef.getChannel(), imageRef.getChannelStack(), null);
                    will(returnValue(new AbsoluteImageReference(image("img1.jpg"), "id42", null,
                            null, new Size(4, 2))));
                }
            });

        IContent image = ImageChannelsUtils.getImage(loader, imageRef);
        assertPNG(image);
        assertEquals("c4dce6 c5dce4\nc3dbe5 c4dbe3\n", getImageContentDescription(image));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetTransformedImage()
    {
        final TileImageReference imageRef = new TileImageReference();
        imageRef.setChannel(CHANNEL);
        imageRef.setDatasetCode(DATASET_CODE);
        imageRef.setSessionId(SESSION_ID);
        imageRef.setChannelStack(ImageChannelStackReference.createFromId(4711));
        context.checking(new Expectations()
            {
                {
                    one(loader)
                            .tryGetImage(imageRef.getChannel(), imageRef.getChannelStack(), null);
                    AbsoluteImageReference imgRef =
                            new AbsoluteImageReference(image("img1.gif"), "id42", null, null,
                                    null);
                    imgRef.setTransformerFactory(transformerFactory);
                    will(returnValue(imgRef));
                    
                    one(transformerFactory).createTransformer();
                    will(returnValue(TRANSFORMER));
                }
            });
        
        IContent image = ImageChannelsUtils.getImage(loader, imageRef);
        assertPNG(image);
        assertEquals("e50000 f00000 f00000 e40000\n" + "f20000 cf0000 cf0000 f00000\n"
                + "f00000 cf0000 cf0000 f00000\n" + "e50000 f00000 f00000 e50000\n",
                getImageContentDescription(image));
        
        context.assertIsSatisfied();
    }

    public void assertPNG(IContent image)
    {
        InputStream inputStream = image.getInputStream();
        try
        {
            byte[] signature = new byte[3];
            inputStream.read(); // skip first byte
            inputStream.read(signature);
            inputStream.close();
            assertEquals("PNG", new String(signature));
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }
    
    private IContent image(String fileName)
    {
        return new FileBasedContent(new File(imageFolder, fileName));
    }
    
    private String getImageContentDescription(IContent image)
    {
        BufferedImage bufferedImage = ImageUtil.loadImage(image.getInputStream());
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                int pixel = bufferedImage.getRGB(i, j);
                if (i > 0)
                {
                    builder.append(' ');
                }
                builder.append(Integer.toHexString(pixel).substring(2));
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
