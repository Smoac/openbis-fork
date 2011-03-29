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

import static ch.systemsx.cisd.imagereaders.ImageReaderConstants.BIOFORMATS_LIBRARY;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * Unit test for {@link ImageReaderFactory}.
 * 
 * @author Kaloyan Enimanev
 */
public class ImageReaderFactoryTest extends AbstractImageReaderFactoryTest
{
    private static final FileFilter IGNORE_SVN = new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return (false == pathname.getName().equalsIgnoreCase(".svn"));
            }
        };

    @DataProvider(name = "libraries")
    public Object[][] librariesToTest()
    {
        return new Object[][]
            {
                { ImageReaderConstants.IMAGEIO_LIBRARY.toLowerCase() },
                { ImageReaderConstants.JAI_LIBRARY.toLowerCase() },
                { ImageReaderConstants.IMAGEJ_LIBRARY.toLowerCase() },
                { ImageReaderConstants.BIOFORMATS_LIBRARY.toLowerCase() } };
    }

    @Test(dataProvider = "libraries")
    public void testLibrary(String library) throws Exception
    {
        ImageReadersTestHelper.setUpLibrariesFromManifest(library);
        for (File file : listValidImages(library))
        {
            assertImageReadable(library, file);
        }
        for (File file : listInvalidImages(library))
        {
            assertNoReaderFor(library, file);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetInvalidLibrary()
    {
        ImageReaderFactory.tryGetImageReaderForFile("invalid_library", null);
    }

    @Test(expectedExceptions = IOExceptionUnchecked.class)
    public void testReadNonExistingFile()
    {
        final String invalidName = "invalid_file_path.jpg";
        final File invalidFile = new File(invalidName);

        IImageReader reader =
                ImageReaderFactory.tryGetImageReaderForFile(BIOFORMATS_LIBRARY, invalidName);
        reader.readImage(invalidFile, null);
    }

    private File[] listValidImages(String library)
    {
        return getValidImagesDir(library).listFiles(IGNORE_SVN);
    }

    private File[] listInvalidImages(String library)
    {
        return getInvalidImagesDir(library).listFiles(IGNORE_SVN);
    }

    private void assertImageReadable(String libraryName, File file)
    {
        IImageReader reader =
                ImageReaderFactory.tryGetImageReaderForFile(libraryName, file.getAbsolutePath());
        String error =
                String.format("Cannot find appropriate reader for file '%s' " + "in library '%s'",
                        file.getAbsolutePath(), libraryName);
        assertNotNull(error, reader);
        assertNotNull("Reader should not have NULL name", reader.getName());
        assertEquals(libraryName.toLowerCase(), reader.getLibraryName().toLowerCase());

        BufferedImage image = reader.readImage(file, null);
        assertNotNull("Read image should not be null", image);
        assertTrue("Image should have non-negative height", image.getHeight() > 0);
        assertTrue("Image should have non-negative width", image.getWidth() > 0);
    }

    private void assertNoReaderFor(String library, File file)
    {
        IImageReader reader =
                ImageReaderFactory.tryGetImageReaderForFile(library, file.getAbsolutePath());
        String error =
                String.format("Library file '%s' " + " cannot read file '%s',"
                        + " but returns a non-null image reader.", library, file.getAbsolutePath());
        assertNull(error, reader);
    }

}
