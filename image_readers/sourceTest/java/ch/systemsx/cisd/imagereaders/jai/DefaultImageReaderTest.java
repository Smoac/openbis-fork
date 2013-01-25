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

package ch.systemsx.cisd.imagereaders.jai;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.io.RandomAccessFileImpl;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.imagereaders.ImageReaderTestCase;

/**
 * @author Franz-Josef Elmer
 */
public class DefaultImageReaderTest extends ImageReaderTestCase
{
    @Test
    public void testMultiPageTIFF() throws FileNotFoundException
    {
        JAIReaderLibrary library = new JAIReaderLibrary();
        String libraryName = library.getName();
        File imageFile = getImageFileForLibrary(libraryName, "multi-page.tif");
        IImageReader reader = library.tryGetReaderForFile(imageFile.getPath());

        List<ImageID> imageIDs = reader.getImageIDs(imageFile);
        assertEquals("[0-0-0-0, 0-1-0-0]", imageIDs.toString());

        Map<String, Object> metaData = reader.readMetaData(imageFile, imageIDs.get(0), null);
        assertEquals(0, metaData.size());

        BufferedImage image0 = reader.readImage(imageFile, imageIDs.get(0), null);
        assertEquals(459, image0.getWidth());
        assertEquals(435, image0.getHeight());

        BufferedImage image1 = reader.readImage(imageFile, imageIDs.get(1), null);
        assertEquals(460, image1.getWidth());
        assertEquals(437, image1.getHeight());

        int depth =
                reader.readColorDepth(
                        new RandomAccessFileImpl(new RandomAccessFile(imageFile, "r")),
                        imageIDs.get(0));

        assertEquals(24, depth);
    }

    @DataProvider(name = "image-files")
    public Object[][] librariesToTest()
    {
        return new Object[][]
            {
                { "pond.bmp", 24 },
                { "pond.gif", 8 },
                { "pond.jpg", 24 },
                { "pond.png", 24 },
                { "pond.pgm", 8 },
                { "pond.tif", 24 },
                { "pond.ppm", 24 } };
    }

    @Test(dataProvider = "image-files")
    public void testSingleImageExamples(String imageFileName, int expectedDepth)
            throws FileNotFoundException
    {
        JAIReaderLibrary library = new JAIReaderLibrary();
        String libraryName = library.getName();
        File imageFile = getImageFileForLibrary(libraryName, imageFileName);
        IImageReader reader = library.tryGetReaderForFile(imageFile.getPath());

        List<ImageID> imageIDs = reader.getImageIDs(imageFile);
        assertEquals("[0-0-0-0]", imageIDs.toString());

        Map<String, Object> metaData = reader.readMetaData(imageFile, imageIDs.get(0), null);
        assertEquals(0, metaData.size());

        BufferedImage image = reader.readImage(imageFile, imageIDs.get(0), null);
        assertEquals(512, image.getWidth());
        assertEquals(384, image.getHeight());

        int depth =
                reader.readColorDepth(
                        new RandomAccessFileImpl(new RandomAccessFile(imageFile, "r")),
                        imageIDs.get(0));

        assertEquals(expectedDepth, depth);
    }

}
