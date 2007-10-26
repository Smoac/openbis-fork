/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;

import com.twmacinta.util.MD5;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.CompositeComparator;
import ch.systemsx.cisd.common.utilities.FileComparator;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * A default <code>IChecksumWriter</code> implementation.
 * 
 * @author Christian Ribeaud
 */
public final class ChecksumWriter implements IChecksumWriter
{

    private final static Comparator<File> BY_TYPE_AND_NAME = createFileComparator();

    private static final String LINE_SEPARATOR = "\n";

    private static final String COLUMN_SEPARATOR = "  ";

    private final IChecksum checksumImpl;

    /** We use only one instance of <code>StringBuilder</code> and recycle it. */
    private final StringBuilder builder;

    protected File root;

    public ChecksumWriter()
    {
        this.checksumImpl = new MD5Checksum();
        builder = new StringBuilder();
    }

    @SuppressWarnings("unchecked")
    private final static Comparator<File> createFileComparator()
    {
        return new CompositeComparator<File>(FileComparator.BY_TYPE, FileComparator.BY_NAME);
    }

    private final static void checkFile(final File file)
    {
        assert file != null && file.exists();
    }

    private final void appendToWriter(final File file, final Writer writer)
    {
        try
        {
            writer.append(createChecksumLine(file, checksumImpl.getChecksum(file)));
        } catch (IOException ex)
        {
            throw EnvironmentFailureException.fromTemplate(ex, "An I/O exception has been thrown regarding file '%s'.",
                    file.getAbsolutePath());
        }
    }

    private final void internalWriteChecksum(final File file, final Writer writer)
    {
        if (file.isFile())
        {
            appendToWriter(file, writer);
        } else if (file.isDirectory())
        {
            final File[] files = FileUtilities.listFiles(file);
            Arrays.sort(files, BY_TYPE_AND_NAME);
            for (int i = 0; i < files.length; i++)
            {
                internalWriteChecksum(files[i], writer);
            }
        }
    }

    protected String getColumnSeparator()
    {
        return COLUMN_SEPARATOR;
    }

    protected String getLineSeparator()
    {
        return LINE_SEPARATOR;
    }

    protected String createChecksumLine(final File file, final String checksum)
    {
        builder.setLength(0);
        builder.append(checksum);
        builder.append(getColumnSeparator());
        builder.append(FileUtilities.getRelativeFile(root.getAbsolutePath(), file.getAbsolutePath()));
        builder.append(getLineSeparator());
        return builder.toString();
    }

    //
    // IChecksumHandler
    //

    public final void writeChecksum(final File file, final Writer writer)
    {
        checkFile(file);
        root = file;
        internalWriteChecksum(file, writer);
    }

    //
    // Helper classes
    //

    /**
     * A <code>IChecksum</code> implementation based on <i>MD5</i>.
     * 
     * @author Christian Ribeaud
     */
    private final static class MD5Checksum implements IChecksum
    {

        //
        // IChecksum
        //

        public final String getChecksum(final File file) throws EnvironmentFailureException
        {
            checkFile(file);
            try
            {
                return MD5.asHex(MD5.getHash(file));
            } catch (IOException ex)
            {
                throw EnvironmentFailureException.fromTemplate(ex,
                        "An I/O exception has been thrown regarding file '%s'.", file.getAbsolutePath());
            }
        }

    }
}
