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

package ch.systemsx.cisd.common.utilities;

import ch.systemsx.cisd.base.io.IRandomAccessFile;

/**
 * Utility methods about the type of (binary) data.
 * 
 * @author Franz-Josef Elmer
 */
public class DataTypeUtil
{
    public static final String TIFF_FILE = "tif";

    public static final String PNG_FILE = "png";

    public static final String JPEG_FILE = "jpg";

    public static final String GIF_FILE = "gif";

    private static final class MagicNumber
    {
        private final String fileType;

        private final String[] magicHexNumbers;

        private final int maxLength;

        MagicNumber(String fileType, String... magicHexNumbers)
        {
            this.fileType = fileType;
            this.magicHexNumbers = magicHexNumbers;
            int length = 0;
            for (String magicNumber : magicHexNumbers)
            {
                length = Math.max(length, magicNumber.length());
            }
            maxLength = length / 2;
        }

        public String getFileType()
        {
            return fileType;
        }

        int getMaxLength()
        {
            return maxLength;
        }

        public boolean matches(byte[] bytes)
        {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < maxLength; i++)
            {
                byte b = bytes[i];
                builder.append(Integer.toHexString((b >> 4) & 0xf));
                builder.append(Integer.toHexString(b & 0xf));
            }
            String initialBytes = builder.toString().toLowerCase();
            for (String magicNumber : magicHexNumbers)
            {
                if (initialBytes.startsWith(magicNumber))
                {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class MagicNumbersManager
    {
        private final MagicNumber[] magicNumbers;

        MagicNumbersManager(MagicNumber... magicNumbers)
        {
            this.magicNumbers = magicNumbers;
        }

        int getMaxLength()
        {
            int max = 0;
            for (MagicNumber magicNumber : magicNumbers)
            {
                max = Math.max(max, magicNumber.getMaxLength());
            }
            return max;
        }

        String tryToFigureOutFileTypeOf(byte[] initialBytes)
        {
            for (MagicNumber magicNumber : magicNumbers)
            {
                if (magicNumber.matches(initialBytes))
                {
                    return magicNumber.getFileType();
                }
            }
            return null;
        }
    }

    private static final MagicNumbersManager MAGIC_NUMBERS_MANAGER =
            new MagicNumbersManager(new MagicNumber(GIF_FILE, "474946383961", "474946383761"),
                    new MagicNumber(JPEG_FILE, "ffd8ff"), new MagicNumber(PNG_FILE,
                            "89504e470d0a1a0a"), new MagicNumber(TIFF_FILE, "49492a00", "4d4d002a"));

    /**
     * Tries to figure out the file type of the specified binary content. It uses the first few
     * bytes as a finger print (so-called 'magic numbers') as a heuristic to get the type of
     * content. Currently only the following types are recognized: <code>gif, jpg, png, tif</code>.
     * 
     * @param handle {@link IRandomAccessFile} which supports marking.
     * @return <code>null</code> if file type couldn't be figured out.
     */
    public static String tryToFigureOutFileTypeOf(IRandomAccessFile handle)
    {
        if (handle.markSupported() == false)
        {
            throw new IllegalArgumentException("Input stream does not support marking. "
                    + "Wrap input stream with a BufferedInputStream to solve the problem.");
        }
        int maxLength = MAGIC_NUMBERS_MANAGER.getMaxLength();
        handle.mark(maxLength);
        byte[] initialBytes = new byte[maxLength];
        handle.read(initialBytes);
        handle.reset();
        return MAGIC_NUMBERS_MANAGER.tryToFigureOutFileTypeOf(initialBytes);
    }

    /**
     * Returns <code>true</code> if the <var>fileTypeOrNull</var> is a tiff file.
     */
    public static boolean isTiff(String fileTypeOrNull)
    {
        return TIFF_FILE.equals(fileTypeOrNull);
    }

    /**
     * Returns <code>true</code> if the <var>fileTypeOrNull</var> is a jpeg file.
     */
    public static boolean isJpeg(String fileTypeOrNull)
    {
        return JPEG_FILE.equals(fileTypeOrNull);
    }

    /**
     * Returns <code>true</code> if the <var>fileTypeOrNull</var> is a png file.
     */
    public static boolean isPng(String fileTypeOrNull)
    {
        return PNG_FILE.equals(fileTypeOrNull);
    }

    /**
     * Returns <code>true</code> if the <var>fileTypeOrNull</var> is a gif file.
     */
    public static boolean isGif(String fileTypeOrNull)
    {
        return GIF_FILE.equals(fileTypeOrNull);
    }

    private DataTypeUtil()
    {
    }
}
