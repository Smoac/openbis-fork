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

package ch.systemsx.cisd.imagereaders.bioformats;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import loci.common.ByteArrayHandle;
import loci.common.IRandomAccess;
import loci.common.Location;
import loci.common.NIOFileHandle;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;

/**
 * A utility class to use bio-formats to read images.
 * 
 * @author Bernd Rinn
 */
public final class BioFormatsImageUtils
{

    private final static List<IFormatReader> readers = new ArrayList<IFormatReader>();

    static
    {
        final Class<? extends IFormatReader>[] classes =
                ImageReader.getDefaultReaderClasses().getClasses();
        for (int i = 0; i < classes.length; i++)
        {
            IFormatReader reader = null;
            try
            {
                reader = classes[i].newInstance();
            } catch (IllegalAccessException exc)
            {
            } catch (InstantiationException exc)
            {
            }
            if (reader == null)
            {
                continue;
            }
            readers.add(reader);
        }
    }

    public static IFormatReader tryFindReaderForFile(String fileName) throws IOException,
            IllegalArgumentException
    {
        for (IFormatReader r : readers)
        {
            if (r.isThisType(fileName))
            {
                return r;
            }
        }
        return null;
    }

    public static IFormatReader findReaderForFile(String fileName) throws IOException,
            IllegalArgumentException
    {
        final IFormatReader readerOrNull = tryFindReaderForFile(fileName);
        if (readerOrNull == null)
        {
            throw new IllegalArgumentException("Cannot find reader.");
        } else
        {
            return readerOrNull;
        }
    }

    public static IFormatReader tryFindReaderByName(String readerName)
            throws IllegalArgumentException
    {
        for (IFormatReader r : readers)
        {
            if (r.getClass().getSimpleName().equals(readerName))
            {
                return r;
            }
        }
        return null;

    }

    public static IFormatReader findReaderByName(String readerName) throws IllegalArgumentException
    {
        final IFormatReader readerOrNull = tryFindReaderByName(readerName);
        if (readerOrNull == null)
        {
            throw new IllegalArgumentException("Cannot find reader.");
        } else
        {
            return readerOrNull;
        }

    }

    public static List<String> getReaderNames()
    {
        final List<String> readerNames = new ArrayList<String>(readers.size());
        for (IFormatReader reader : readers)
        {
            readerNames.add(reader.getClass().getSimpleName());
        }
        return readerNames;
    }

    /**
     * Returns all images of the image <var>filename</var> represented by <var>bytes</var> as
     * {@link BufferedImage}.
     * <p>
     * Note that the suffix of <var>filename</var> is used to find the right reader.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     * @throws IllegalArgumentException If no suitable reader can be found.
     */
    public static BufferedImage[] readImages(String filename, byte[] bytes)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return readImages(filename, new ByteArrayHandle(bytes));
    }

    /**
     * Returns the image <var>filename</var> represented by <var>bytes</var> as
     * {@link BufferedImage}.
     * <p>
     * Note that the suffix of <var>filename</var> is used to find the right reader.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     * @throws IllegalArgumentException If no suitable reader can be found.
     */
    public static BufferedImage readImage(String filename, byte[] bytes, int page)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return readImage(filename, new ByteArrayHandle(bytes), page);
    }

    /**
     * Returns all images of the image file given by <var>file</var> as {@link BufferedImage}.
     * <p>
     * Note that the suffix of <var>file</var> is used to find the right reader.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     * @throws IllegalArgumentException If no suitable reader can be found.
     */
    public static BufferedImage[] readImages(File file) throws IOExceptionUnchecked,
            IllegalArgumentException
    {
        NIOFileHandle handle = null;
        try
        {
            handle = new NIOFileHandle(file, "r");
            return readImages(file.getPath(), handle);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            if (handle != null)
            {
                try
                {
                    handle.close();
                } catch (IOException ex)
                {
                    // Silence.
                }
            }
        }
    }

    /**
     * Returns the image <var>file</var> represented by <var>handle</var> as {@link BufferedImage}.
     * <p>
     * Note that the suffix of <var>file</var> is used to find the right reader.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     * @throws IllegalArgumentException If no suitable reader can be found.
     */
    public static BufferedImage readImage(File file, int page) throws IOExceptionUnchecked,
            IllegalArgumentException
    {
        try
        {
            return readImage(file.getPath(), new NIOFileHandle(file, "r"), page);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /**
     * Returns the image <var>page</var> of the image file given by <var>filename</var> represented
     * by <var>handle</var> as {@link BufferedImage}.
     * <p>
     * Note that the suffix of <var>filename</var> is used to find the right reader.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     * @throws IllegalArgumentException If no suitable reader can be found.
     */
    public static BufferedImage readImage(String filename, IRandomAccessFile handle, int page)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return readImage(filename, new BioFormatsRandomAccessAdapter(handle), page);
    }

    /**
     * Returns the image <var>page</var> of the image file given by <var>filename</var> represented
     * by <var>handle</var> as {@link BufferedImage}.
     * <p>
     * Note that the suffix of <var>filename</var> is used to find the right reader.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     * @throws IllegalArgumentException If no suitable reader can be found.
     */
    private static BufferedImage readImage(String filename, IRandomAccess handle, int page)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        // Add to static map.
        Location.mapFile(filename, handle);
        try
        {
            final IFormatReader reader = findReaderForFile(filename);
            // This does the actual parsing.
            reader.setId(filename);
            final BufferedImageReader biReader =
                    BufferedImageReader.makeBufferedImageReader(reader);
            final BufferedImage image = biReader.openImage(page);
            reader.close();
            return image;
        } catch (FormatException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            // Remove from static map.
            Location.mapFile(filename, null);
        }
    }

    /**
     * Returns the image <var>page</var> of the image file given by <var>filename</var> represented
     * by <var>handle</var> as {@link BufferedImage}.
     * <p>
     * Note that the suffix of <var>filename</var> is used to find the right reader.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     * @throws IllegalArgumentException If no suitable reader can be found.
     */
    static BufferedImage readImage(IFormatReader formatReader, String filename,
            IRandomAccess handle, int page) throws IOExceptionUnchecked, IllegalArgumentException
    {
        // Add to static map.
        Location.mapFile(filename, handle);
        try
        {
            final IFormatReader reader = findReaderForFile(filename);
            // This does the actual parsing.
            reader.setId(filename);
            final BufferedImageReader biReader =
                    BufferedImageReader.makeBufferedImageReader(reader);
            final BufferedImage image = biReader.openImage(page);
            reader.close();
            return image;
        } catch (FormatException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            // Remove from static map.
            Location.mapFile(filename, null);
        }
    }

    /**
     * Returns all images of the image file given by <var>filename</var> represented by
     * <var>handle</var> as {@link BufferedImage}.
     * <p>
     * Note that the suffix of <var>filename</var> is used to find the right reader.
     * 
     * @param filename The name of the image, suffix used to determine the reader
     * @param handle The handle of the content of the file, may be used to determine the reader as
     *            well. Will <i>not</i> be closed!
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     * @throws IllegalArgumentException If no suitable reader can be found.
     */
    public static BufferedImage[] readImages(String filename, IRandomAccessFile handle)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return readImages(filename, new BioFormatsRandomAccessAdapter(handle));
    }

    /**
     * Returns all images of the image file given by <var>filename</var> represented by
     * <var>handle</var> as {@link BufferedImage}.
     * <p>
     * Note that the suffix of <var>filename</var> is used to find the right reader.
     * 
     * @param filename The name of the image, suffix used to determine the reader
     * @param handle The handle of the content of the file, may be used to determine the reader as
     *            well. Will <i>not</i> be closed!
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     * @throws IllegalArgumentException If no suitable reader can be found.
     */
    private static BufferedImage[] readImages(String filename, IRandomAccess handle)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        BufferedImage[] images = null;
        // Add to static map.
        Location.mapFile(filename, handle);
        try
        {
            final IFormatReader reader = findReaderForFile(filename);
            // This does the actual parsing.
            reader.setId(filename);
            final BufferedImageReader biReader =
                    BufferedImageReader.makeBufferedImageReader(reader);
            images = new BufferedImage[biReader.getImageCount()];
            for (int i = 0; i < images.length; ++i)
            {
                images[i] = biReader.openImage(i);
            }
            reader.close();
            return images;
        } catch (FormatException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            // Remove from static map.
            Location.mapFile(filename, null);
        }
    }

    private BioFormatsImageUtils()
    {
        // Not to be instantiated.
    }

}
