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

package ch.systemsx.cisd.imagereaders;

import static ch.systemsx.cisd.imagereaders.Constants.BIOFORMATS_LIBRARY;

import java.awt.image.BufferedImage;
import java.io.File;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * Unit test for {@link ImageReaderFactory}.
 * 
 * @author Kaloyan Enimanev
 */
public class ImageReaderFactoryTest extends AssertJUnit
{
    private static final String IMAGES_DIR = "./sourceTest/resources/images/";

    @Test
    public void testBioFormatReaders()
    {
        assertImageReadable(BIOFORMATS_LIBRARY, getResourceFile("demo.tif"));
        assertImageReadable(BIOFORMATS_LIBRARY, getResourceFile("annapolis.jpg"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetInvalidLibrary()
    {
        assertImageReadable("Invalid_library", getResourceFile("demo.tif"));
    }

    @Test(expectedExceptions = IOExceptionUnchecked.class)
    public void testReadNonExistingFile()
    {
        final String invalidName = "invalid_file_path.jpg";
        final File invalidFile = new File(invalidName);

        IImageReader reader =
                ImageReaderFactory.tryGetImageReaderForFile(BIOFORMATS_LIBRARY, invalidName);
        reader.readImage(invalidFile, 0);
    }

    private void assertImageReadable(String libraryName, File file)
    {
        IImageReader reader =
                ImageReaderFactory.tryGetImageReaderForFile(libraryName, file.getAbsolutePath());
        assertNotNull("Reader should not have NULL name", reader.getName());
        assertEquals(libraryName, reader.getLibraryName());

        String error =
                String.format("Cannot find appropriate reader for file '%s' " + "in library '%s'",
                        libraryName, file.getAbsolutePath());
        assertNotNull(error, reader);

        BufferedImage image = reader.readImage(file, 0);
        assertNotNull("Read image should not be null", image);
        assertTrue("Image should have non-negative height", image.getHeight() > 0);
        assertTrue("Image should have non-negative width", image.getWidth() > 0);
    }

    private File getResourceFile(String name)
    {
        return new File(IMAGES_DIR, name);
    }

}
