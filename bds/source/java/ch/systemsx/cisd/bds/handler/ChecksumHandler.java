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

package ch.systemsx.cisd.bds.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.bds.Constants;
import ch.systemsx.cisd.bds.DataStructureException;
import ch.systemsx.cisd.bds.DataStructureV1_0;
import ch.systemsx.cisd.bds.IDataStructureHandler;
import ch.systemsx.cisd.bds.Utilities;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.CollectionIO;
import ch.systemsx.cisd.common.utilities.IFromStringConverter;
import ch.systemsx.cisd.common.utilities.IToStringConverter;

/**
 * A <code>IDataStructureHandler</code> implementation for the <code>md5sum</code> directory.
 * 
 * @author Christian Ribeaud
 */
public final class ChecksumHandler implements IDataStructureHandler
{
    private static final ChecksumConverter CHECKSUM_CONVERTER = new ChecksumConverter();

    public static final String CHECKSUM_DIRECTORY = "md5sum";

    private final IChecksumCalculator checksumCalculator;

    private final IDirectory checksumDirectory;

    private final IDirectory originalDataDirectory;

    /** Whether we should allow call to {@link #assertValid()}. */
    private final boolean doValidation;

    public ChecksumHandler(final IDirectory checksumDirectory, final IDirectory originalDataDirectory,
            final boolean doValidation)
    {
        this.checksumDirectory = checksumDirectory;
        this.originalDataDirectory = originalDataDirectory;
        this.checksumCalculator = new MD5ChecksumCalculator();
        this.doValidation = doValidation;
    }

    private final List<Checksum> loadChecksumsForAllFilesIn(final IDirectory directory)
    {
        assert directory != null : "Unspecified directory";
        final List<Checksum> checksums = new ArrayList<Checksum>();
        addChecksums(checksums, null, directory);
        return checksums;
    }

    private void addChecksums(final List<Checksum> checksums, final String nodePath, final IDirectory directory)
    {
        final List<INode> children = new ArrayList<INode>();
        for (final INode child : directory)
        {
            children.add(child);
        }
        Collections.sort(children, NodeComparator.BY_NAME_IGNORE_CASE);
        for (final INode child : children)
        {
            addChecksum(checksums, nodePath, child);
        }
    }

    private final void addChecksum(final List<Checksum> checksums, final String path, final INode node)
    {
        final String nodePath = (path == null ? "" : path + Constants.PATH_SEPARATOR) + node.getName();
        if (node instanceof IFile)
        {
            IFile file = (IFile) node;
            final InputStream inputStream = file.getInputStream();
            try
            {
                checksums.add(new Checksum(checksumCalculator.calculateChecksum(inputStream), nodePath));
            } catch (IOException ex)
            {
                throw new EnvironmentFailureException("Couldn't calculate checksum for file '" + nodePath + "'");
            } finally
            {
                IOUtils.closeQuietly(inputStream);
            }
        } else if (node instanceof IDirectory)
        {
            addChecksums(checksums, nodePath, (IDirectory) node);
        }
    }

    //
    // IDataStructureHandler
    //

    public final void assertValid() throws DataStructureException
    {
        if (doValidation == false)
        {
            return;
        }
        final List<String> expected = Utilities.getStringList(checksumDirectory, DataStructureV1_0.DIR_ORIGINAL);
        final List<Checksum> actual = loadChecksumsForAllFilesIn(originalDataDirectory);
        for (final String value : expected)
        {
            final Checksum checkum = CHECKSUM_CONVERTER.fromString(value);
            if (actual.remove(checkum) == false)
            {
                throw new DataStructureException(String.format("Given checksum '%s' not found in directory '%s'",
                        checkum, originalDataDirectory));
            }
        }
    }

    public final void performClosing()
    {
        final StringWriter writer = new StringWriter();
        final List<Checksum> checksums = loadChecksumsForAllFilesIn(originalDataDirectory);
        CollectionIO.writeIterable(writer, checksums, CHECKSUM_CONVERTER);
        checksumDirectory.addKeyValuePair(DataStructureV1_0.DIR_ORIGINAL, writer.toString());
    }

    public final void performOpening()
    {
    }

    //
    // Helper Classes
    //

    private final static class Checksum
    {

        private final String checksum;

        private final String path;

        Checksum(final String checksum, final String path)
        {
            assert checksum != null : "Given checksum can not be null.";
            assert path != null : "Given path can not be null.";
            this.checksum = checksum;
            this.path = path;
        }

        //
        // Checksum
        //

        @Override
        public final boolean equals(final Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (obj instanceof Checksum == false)
            {
                return false;
            }
            final Checksum that = (Checksum) obj;
            return that.checksum.equals(checksum) && that.path.equals(path);
        }

        @Override
        public final int hashCode()
        {
            int hashCode = 17;
            hashCode = hashCode * 37 + path.hashCode();
            hashCode = hashCode * 37 + checksum.hashCode();
            return hashCode;
        }

        @Override
        public final String toString()
        {
            return "[checksum=" + checksum + ",path=" + path + "]";
        }
    }

    final static class ChecksumConverter implements IToStringConverter<Checksum>, IFromStringConverter<Checksum>
    {
        private static final String SEPARATOR = "  ";

        ChecksumConverter()
        {

        }

        //
        // IToStringConverter
        //

        public final String toString(final Checksum checksum)
        {
            return checksum.checksum + SEPARATOR + checksum.path;
        }

        //
        // IFromStringConverter
        //

        public final Checksum fromString(final String value)
        {
            final int index = value.indexOf(SEPARATOR);
            final String checksum = value.substring(0, index);
            final String path = value.substring(index + SEPARATOR.length());
            return new Checksum(checksum, path);
        }
    }
}