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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.imagereaders.imageio.ImageIOReaderLibrary;

/**
 * A factory for image readers.
 * <p>
 * Uses java.util.ServiceLoader-s underneath to find out about the available libraries and readers.
 * 
 * @author Bernd Rinn
 * @author Kaloyan Enimanev
 */
public class ImageReaderFactory
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ImageReaderFactory.class);

    private static List<IImageReaderLibrary> libraries;

    static
    {
        Iterator<IImageReaderLibrary> librariesIterator;
        try
        {
            librariesIterator = java.util.ServiceLoader.load(IImageReaderLibrary.class).iterator();
        } catch (NoClassDefFoundError ex)
        {
            operationLog.warn("Image reader plugins not available (JRE < 1.6), "
                    + "fallback to built-in readers.", ex);
            librariesIterator =
                    Arrays.asList((IImageReaderLibrary) new ImageIOReaderLibrary()).iterator();
        }
        libraries = CollectionUtils.asList(librariesIterator);
    }

    /**
     * Returns an {@link IImageReader} for specified library name and reader name. Can return
     * <code>null</code> if no matching reader is found.
     * 
     * @throws IllegalArgumentException if libraryName does not specify a valid library name.
     */
    public static IImageReader tryGetReader(String libraryName, String readerName)
    {
        IImageReaderLibrary library = findLibrary(libraryName);
        return (library == null) ? null : library.tryGetReader(readerName);
    }


    /**
     * Tries to find a suitable reader in a library for a specified <var>fileName</var>. May return
     * <code>null</code> if no suitable reader is found.
     * <p>
     * The behavior of this method may vary across libraries. For example, some image libraries can
     * use the suffix of <var>fileName</var> to find the right reader, while others might attempt to
     * open the file and apply heuristics on its content to determine the appropriate reader.
     * 
     * @throws IllegalArgumentException if libraryName does not specify a valid library name.
     */
    public static IImageReader tryGetReaderForFile(String libraryName, String fileName)
    {
        IImageReaderLibrary library = findLibrary(libraryName);
        return (library == null) ? null : library.tryGetReaderForFile(fileName);
    }

    /**
     * Iterates over all available reader libraries and tries to find a suitable reader for a
     * specified <var>fileName</var>. May return <code>null</code> if no suitable reader is found.
     * <p>
     * The method produces non-deterministic results as it relies upon an arbitrary ordering of the
     * known image libraries, where the first library to return a valid image reader "wins".
     * <p>
     * The behavior of this method may vary across libraries. For example, some image libraries can
     * use the suffix of <var>fileName</var> to find the right reader, while others might attempt to
     * open the file and apply heuristics on its content to determine the appropriate reader.
     * 
     * @throws IllegalArgumentException if libraryName does not specify a valid library name.
     */
    public static IImageReader tryGetReaderForFile(String fileName)
    {
        for (IImageReaderLibrary library : libraries)
        {
            IImageReader imageReader = library.tryGetReaderForFile(fileName);
            if (imageReader != null)
            {
                return imageReader;
            }
        }
        return null;
    }

    private static IImageReaderLibrary findLibrary(String libraryName)
            throws IllegalArgumentException
    {
        for (IImageReaderLibrary library : libraries)
        {
            if (library.getName().equalsIgnoreCase(libraryName))
            {
                return library;
            }
        }
        return null;
    }

    /**
     * used only for testing purposes.
     */
    static void setLibraries(List<IImageReaderLibrary> newLibraries)
    {
        libraries = newLibraries;
    }
}
