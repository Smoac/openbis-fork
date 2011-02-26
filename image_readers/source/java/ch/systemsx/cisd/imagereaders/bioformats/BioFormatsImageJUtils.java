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
import java.io.File;
import java.io.IOException;

import loci.common.ByteArrayHandle;
import loci.common.IRandomAccess;
import loci.common.Location;
import loci.common.NIOFileHandle;
import loci.formats.FormatException;
import loci.formats.IFormatReader;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;

/**
 * A utility class to use bio-formats readers to read images and ImageJ to do a basic intensity
 * rescaling.
 * 
 * @author Bernd Rinn
 */
public final class BioFormatsImageJUtils
{

    /**
     * Returns the image <var>filename</var> represented by <var>bytes</var> as
     * {@link BufferedImage}.
     * <p>
     * Note that the suffix of <var>filename</var> is used to find the right reader.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     * @throws IllegalArgumentException If no suitable reader can be found.
     */
    public static BufferedImage readAndTransformImage(String filename, byte[] bytes, int page,
            int channel) throws IOExceptionUnchecked, IllegalArgumentException
    {
        return readAndTransformImage(filename, new ByteArrayHandle(bytes), page, channel);
    }

    /**
     * Returns the image <var>file</var> represented by <var>handle</var> as {@link BufferedImage}.
     * <p>
     * Note that the suffix of <var>file</var> is used to find the right reader.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     * @throws IllegalArgumentException If no suitable reader can be found.
     */
    public static BufferedImage readAndTransformImage(File file, int page, int channel)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        try
        {
            return readAndTransformImage(file.getPath(), new NIOFileHandle(file, "r"), page,
                    channel);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /**
     * Returns the image <var>filename</var> represented by <var>handle</var> as
     * {@link BufferedImage}.
     * <p>
     * Note that the suffix of <var>filename</var> is used to find the right reader.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     * @throws IllegalArgumentException If no suitable reader can be found.
     */
    public static BufferedImage readAndTransformImage(String filename, IRandomAccessFile handle,
            int page, int channel) throws IOExceptionUnchecked, IllegalArgumentException
    {
        return readAndTransformImage(filename, new BioFormatsRandomAccessAdapter(handle), page,
                channel);
    }

    /**
     * Returns the image <var>filename</var> represented by <var>handle</var> as
     * {@link BufferedImage}.
     * <p>
     * Note that the suffix of <var>filename</var> is used to find the right reader.
     * 
     * @throws IOExceptionUnchecked If access to <var>handle</var> fails.
     * @throws IllegalArgumentException If no suitable reader can be found.
     */
    private static BufferedImage readAndTransformImage(String filename, IRandomAccess handle,
            int page, int channel) throws IOExceptionUnchecked, IllegalArgumentException
    {
        // Add to static map.
        Location.mapFile(filename, handle);
        try
        {
            final IFormatReader reader = BioFormatsImageUtils.findReader(filename);
            // This does the actual parsing.
            reader.setId(filename);
            int width = reader.getSizeX();
            int height = reader.getSizeY();
            final ImageStack stack = new ImageStack(width, height);
            final ImageProcessor ip = BioFormatsImageProcessor.openProcessor(reader, page, channel);
            stack.addSlice("", ip);
            final ImagePlus imp = new ImagePlus(filename, stack);

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
            Location.mapFile(filename, null);
        }
    }

    private BioFormatsImageJUtils()
    {
        // Not to be instantiated.
    }

}
