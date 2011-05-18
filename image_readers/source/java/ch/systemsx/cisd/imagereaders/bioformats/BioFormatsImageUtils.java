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

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import loci.common.IRandomAccess;
import loci.common.Location;
import loci.formats.FormatException;
import loci.formats.IFormatHandler;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;
import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;
import loci.formats.in.MetadataOptions;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * A utility class to use bio-formats to read images.
 * 
 * @author Bernd Rinn
 */
final class BioFormatsImageUtils
{

    private final static List<IFormatReader> readers;

    static
    {
        IFormatReader[] formatReaders = new ImageReader().getReaders();
        readers = Arrays.asList(formatReaders);
    }

    /**
     * Tries to find a suitable reader for the file specified with <var>fileName</var>. May return
     * <code>null</code> if no suitable reader is found.
     * <p>
     * Note that the suffix of <var>fileName</var> is used to find the right reader.
     */
    public static IFormatReader tryFindReaderForFile(String fileName)
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

    /**
     * Return an {@link IFormatReader} for a specified name. May return <code>null</code> if no
     * corresponding reader is found.
     */
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

    /**
     * Return a list with the names of all known readers.
     */
    public static List<String> getReaderNames()
    {
        final List<String> readerNames = new ArrayList<String>(readers.size());
        for (IFormatReader reader : readers)
        {
            String readerName = getReaderName(reader);
            readerNames.add(readerName);
        }
        return readerNames;
    }

    /**
     * Return the name of a {@link IFormatReader}.
     */
    public static String getReaderName(IFormatReader reader)
    {
        return reader.getClass().getSimpleName();
    }

    /**
     * Returns the image <var>page</var> of the image file represented by <var>handle</var> as
     * {@link BufferedImage}.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     */
    static BufferedImage readImage(IFormatReader reader,
            IRandomAccess handle, int page) throws IOExceptionUnchecked, IllegalArgumentException
    {
        String handleId = generateHandleId(reader);
        // Add to static map.
        Location.mapFile(handleId, handle);
        try
        {
            // This does the actual parsing.
            reader.setId(handleId);
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
            Location.mapFile(handleId, null);
        }
    }

    /**
     * Returns the metadata of the image file represented by <var>handle</var>.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     */
    public static Map<String, Object> readMetadata(IFormatReader reader, IRandomAccess handle)
    {
        // Add to static map.
        String handleId = generateHandleId(reader);
        Location.mapFile(handleId, handle);

        try
        {
            HashMap<String, Object> result = new HashMap<String, Object>();
            MetadataOptions metaOptions = new DefaultMetadataOptions(MetadataLevel.ALL);
            reader.setMetadataOptions(metaOptions);

            reader.setId(handleId);
            nullSafeAddAll(result, reader.getGlobalMetadata());
            nullSafeAddAll(result, reader.getSeriesMetadata());
            reader.close();

            return result;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            // Remove from static map.
            Location.mapFile(handleId, null);
        }
    }


    /**
     * An utility method that uses bio-formats reader to read an image and ImageJ to do a basic
     * intensity rescaling.
     * <p>
     * Returns the image <var>filename</var> represented by <var>handle</var> as
     * {@link BufferedImage}.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     * @throws IllegalArgumentException If no suitable reader can be found.
     */
    static BufferedImage readImageWithIntensityRescaling(IFormatReader reader,
            IRandomAccess handle, int page, int channel) throws IOExceptionUnchecked,
            IllegalArgumentException
    {
        // Add to static map.
        String handleId = generateHandleId(reader);
        Location.mapFile(handleId, handle);
        try
        {
            // This does the actual parsing.
            reader.setId(handleId);
            int width = reader.getSizeX();
            int height = reader.getSizeY();
            final ImageStack stack = new ImageStack(width, height);
            final ImageProcessor ip = BioFormatsImageProcessor.openProcessor(reader, page, channel);
            stack.addSlice("", ip);
            final ImagePlus imp = new ImagePlus(handleId, stack);

            final BufferedImage image = imp.getBufferedImage();
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
            Location.mapFile(handleId, null);
        }
    }

    public static String generateHandleId(IFormatHandler formatHandler)
    {
        return UUID.randomUUID().toString() + "." + formatHandler.getSuffixes()[0];
    }

    private static void nullSafeAddAll(HashMap<String, Object> accumulator,
            Hashtable<String, Object> toAdd)
    {
        if (toAdd != null)
        {
            accumulator.putAll(toAdd);
        }

    }

    private BioFormatsImageUtils()
    {
        // Not to be instantiated.
    }
}
