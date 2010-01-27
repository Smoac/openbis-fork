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

package ch.systemsx.cisd.bds.hcs;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.bds.AbstractFormattedData;
import ch.systemsx.cisd.bds.Constants;
import ch.systemsx.cisd.bds.Format;
import ch.systemsx.cisd.bds.FormattedDataContext;
import ch.systemsx.cisd.bds.IFormatParameters;
import ch.systemsx.cisd.bds.IFormattedData;
import ch.systemsx.cisd.bds.Utilities;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.exception.StorageException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.bds.v1_0.DataStructureV1_0;

/**
 * {@link IFormattedData} implementation for <i>HCS (High-Content Screening) with Images</i>. It is
 * associated with {@link HCSImageFormatV1_0}.
 * 
 * @author Christian Ribeaud
 */
public final class HCSImageFormattedData extends AbstractFormattedData implements
        IHCSImageFormattedData
{
    /** The <i>column</i> (or <i>x</i>) coordinate. */
    public static final String COLUMN = "column";

    /** The <i>row</i> (or <i>y</i>) coordinate. */
    public static final String ROW = "row";

    private final DirectoryContentCache standardDataDirectoryCache;

    private final IDirectory originalDataDirectoryOrNull; // set if containsOriginalData is true

    private final boolean containsOriginalData;

    /** see {@link HCSImageFormatV1_0#IS_INCOMING_SYMBOLIC_LINK} */
    private final boolean isIncomingSymbolicLink;

    private final Geometry wellGeometry;

    private final Geometry plateGeometry;

    private final int channelCount;

    // It is a kludge to keep this around but IDirectory.addFile() requires a java.io.File and
    // INodes can not be checked for equal(), so we need it.
    private File currentImageRootDirectory;

    /** The source directory node from where to copy/link all images. */
    private IDirectory currentImageRootDirectoryNode;

    public HCSImageFormattedData(final FormattedDataContext context)
    {
        super(context);
        IFormatParameters formatParameters = getFormatParameters();
        this.containsOriginalData = figureContainsOriginalData(formatParameters);
        if (containsOriginalData)
        {
            this.originalDataDirectoryOrNull =
                    Utilities.getSubDirectory(dataDirectory, DataStructureV1_0.DIR_ORIGINAL);
        } else
        {
            this.originalDataDirectoryOrNull = null;
        }
        this.isIncomingSymbolicLink = figureIsIncomingSymbolicLink(formatParameters);

        IDirectory standardDataDirectory =
                Utilities.getSubDirectory(dataDirectory, DataStructureV1_0.DIR_STANDARD);
        this.standardDataDirectoryCache =
                new DirectoryContentCache(standardDataDirectory, createChannelDirNameProvider());

        this.wellGeometry = figureWellGeometry(formatParameters);
        this.plateGeometry = figurePlateGeometry(formatParameters);
        this.channelCount = figureChannelCount(formatParameters);
    }

    private static int figureChannelCount(IFormatParameters params)
    {
        return ((Integer) params.getValue(HCSImageFormatV1_0.NUMBER_OF_CHANNELS)).intValue();
    }

    private static Geometry figurePlateGeometry(IFormatParameters params)
    {
        return (Geometry) params.getValue(PlateGeometry.PLATE_GEOMETRY);
    }

    private static Geometry figureWellGeometry(IFormatParameters params)
    {
        return (Geometry) params.getValue(WellGeometry.WELL_GEOMETRY);
    }

    private static boolean figureContainsOriginalData(IFormatParameters params)
    {
        return ((Utilities.Boolean) params.getValue(HCSImageFormatV1_0.CONTAINS_ORIGINAL_DATA))
                .toBoolean();
    }

    private static boolean figureIsIncomingSymbolicLink(IFormatParameters params)
    {
        String paramName = HCSImageFormatV1_0.IS_INCOMING_SYMBOLIC_LINK;
        if (params.containsParameter(paramName))
        {
            return ((Utilities.Boolean) params.getValue(paramName)).toBoolean();
        } else
        {
            return false; // default value
        }
    }

    public final boolean containsOriginalData()
    {
        return containsOriginalData;
    }

    /** see {@link HCSImageFormatV1_0#IS_INCOMING_SYMBOLIC_LINK} */
    public final boolean isIncomingSymbolicLink()
    {
        return isIncomingSymbolicLink;
    }

    public final Geometry getWellGeometry()
    {
        return wellGeometry;
    }

    public final Geometry getPlateGeometry()
    {
        return plateGeometry;
    }

    public final int getChannelCount()
    {
        return channelCount;
    }

    private final static void checkLocation(final Geometry geometry, final Location location)
    {
        if (geometry.contains(location) == false)
        {
            throw new IllegalArgumentException(String.format(
                    "Given geometry '%s' does not contain location '%s'", geometry, location));
        }

    }

    private final void checkChannel(final int channel)
    {
        if (channel < 1)
        {
            throw new IndexOutOfBoundsException(String.format(
                    "Channel index must start at 1 (given value is %d).", channel));
        }
        if (channel > channelCount)
        {
            throw new IndexOutOfBoundsException(String.format(
                    "Channel index %d exceeds the number of channels %d", channel, channelCount));
        }
    }

    /**
     * From given <var>wellLocation</var> creates the leaf file name that is found in
     * <code>data/standard</code>.
     */
    final static String createWellFileName(final Location wellLocation)
    {
        assert wellLocation != null : "Well location can not be null.";
        return ROW + wellLocation.getY() + "_" + COLUMN + wellLocation.getX() + ".tiff";
    }

    private final static String getPlateColumnDirName(final int colNumber)
    {
        return COLUMN + colNumber;
    }

    private final static String getPlateRowDirName(final int rowNumber)
    {
        return ROW + rowNumber;
    }

    private final static String getChannelName(final int channel)
    {
        return Channel.CHANNEL + channel;
    }

    private void checkCoordinates(final int channel, final Location plateLocation,
            final Location wellLocation)
    {
        checkChannel(channel);
        assert plateLocation != null : "Plate location can not be null.";
        assert wellLocation != null : "Well location can not be null.";
        checkLocation(getPlateGeometry(), plateLocation);
        checkLocation(getWellGeometry(), wellLocation);
    }

    private final IDirectory getImageRootDirectoryNode(final File imageRootDirectory)
    {
        assert originalDataDirectoryOrNull != null : "original data directrory not set";
        final String imageRootDirName = imageRootDirectory.getName();
        // If not already present, move the 'imageRootDirectory' to 'original' data directory.
        if (originalDataDirectoryOrNull.tryGetNode(imageRootDirName) == null)
        {
            originalDataDirectoryOrNull.addFile(imageRootDirectory, null, true);
        }
        final INode imageRootNode = originalDataDirectoryOrNull.tryGetNode(imageRootDirName);
        if (imageRootNode == null)
        {
            throw new DataStructureException(String.format(
                    "No image root directory named '%s' could be found in the original directory.",
                    imageRootDirName));
        }
        assert imageRootNode instanceof IDirectory : "Image root node must be a directory.";
        return (IDirectory) imageRootNode;
    }

    //
    // IHCSFormattedData
    //

    public final INode tryGetStandardNodeAt(final int channel, final Location wellLocation,
            final Location tileLocation)
    {
        checkCoordinates(channel, wellLocation, tileLocation);
        try
        {
            final IDirectory plateColumnDir = getWellImagesParentDir(channel, wellLocation);
            return plateColumnDir.tryGetNode(createWellFileName(tileLocation));
        } catch (final DataStructureException e)
        {
            return null;
        }
    }

    private IDirectory getWellImagesParentDir(final int channel, final Location wellLocation)
    {
        DirectoryContentCache channelDirCache = standardDataDirectoryCache.getSubdirectory(channel);
        DirectoryContentCache rowDirCache = channelDirCache.getSubdirectory(wellLocation.getY());
        DirectoryContentCache colDirCache = rowDirCache.getSubdirectory(wellLocation.getX());
        return colDirCache.getDirectory();
    }

    private final void updateImageRootDirectory(final File imageRootDirectory)
    {
        assert imageRootDirectory != null : "Given image root directory can not be null.";

        if (imageRootDirectory == currentImageRootDirectory)
        {
            return; // small performance optimization
        }
        if (imageRootDirectory.equals(currentImageRootDirectory) == false)
        {
            this.currentImageRootDirectory = imageRootDirectory;
            this.currentImageRootDirectoryNode = getImageRootDirectoryNode(imageRootDirectory);
        }
    }

    // Allows for easy cached access to sub-directories which are identified by integer numbers.
    // Allows to access items in sub-directories using the same cached mechanism.
    // Useful when getting access to an existing directory is an expensive operation.
    private static class DirectoryContentCache
    {
        private final IDirectory currentDir;

        private final Map<Integer, DirectoryContentCache> subdirsCache;

        private final INumberedDirNameProvider nameProviderOrNull;

        public DirectoryContentCache(IDirectory currentDir,
                INumberedDirNameProvider nameProviderOrNull)
        {
            this.currentDir = currentDir;
            this.nameProviderOrNull = nameProviderOrNull;
            this.subdirsCache = new HashMap<Integer, DirectoryContentCache>();
        }

        public DirectoryContentCache getSubdirectory(int directoryNumber)
        {
            assert nameProviderOrNull != null : "this directory is no expected to have numbered subdirectories";
            DirectoryContentCache subdirCache = subdirsCache.get(directoryNumber);
            if (subdirCache == null)
            {
                String subdirName = nameProviderOrNull.getName(directoryNumber);
                // this operation is cached as it can be expensive
                IDirectory subdir = currentDir.makeDirectory(subdirName);
                INumberedDirNameProvider subdirNameProviderOrNull =
                        nameProviderOrNull.tryGetSubdirectoryProvider();
                subdirCache = new DirectoryContentCache(subdir, subdirNameProviderOrNull);
                subdirsCache.put(directoryNumber, subdirCache);
            }
            return subdirCache;
        }

        public IDirectory getDirectory()
        {
            return currentDir;
        }
    }

    private static interface INumberedDirNameProvider
    {
        // name of a sub-directory with a given number
        String getName(int number);

        // naming schema for directories inside sub-directory
        INumberedDirNameProvider tryGetSubdirectoryProvider();
    }

    private static INumberedDirNameProvider createChannelDirNameProvider()
    {
        return new INumberedDirNameProvider()
            {

                public String getName(int channel)
                {
                    return getChannelName(channel);
                }

                public INumberedDirNameProvider tryGetSubdirectoryProvider()
                {
                    return createRowDirNameProvider();
                }
            };
    }

    private static INumberedDirNameProvider createRowDirNameProvider()
    {
        return new INumberedDirNameProvider()
            {

                public String getName(int row)
                {
                    return getPlateRowDirName(row);
                }

                public INumberedDirNameProvider tryGetSubdirectoryProvider()
                {
                    return createColumnDirNameProvider();
                }
            };
    }

    private static INumberedDirNameProvider createColumnDirNameProvider()
    {
        return new INumberedDirNameProvider()
            {

                public String getName(int column)
                {
                    return getPlateColumnDirName(column);
                }

                public INumberedDirNameProvider tryGetSubdirectoryProvider()
                {
                    return null;
                }
            };
    }

    public final NodePath addStandardNode(final File imageRootDirectory,
            final String imageRelativePath, final int channel, final Location wellLocation,
            final Location tileLocation) throws DataStructureException
    {
        assert imageRelativePath != null : "Given image relative path can not be null.";

        check(channel, wellLocation, tileLocation);
        DirectoryContentCache channelDirCache = standardDataDirectoryCache.getSubdirectory(channel);
        DirectoryContentCache rowDirCache = channelDirCache.getSubdirectory(wellLocation.getY());
        DirectoryContentCache colDirCache = rowDirCache.getSubdirectory(wellLocation.getX());
        final IDirectory plateColumnDir = colDirCache.getDirectory();

        final String wellFileName = createWellFileName(tileLocation);
        final INode node;
        if (containsOriginalData)
        {
            updateImageRootDirectory(imageRootDirectory);
            // NOTE: existence of the node is checked, it can be slow for remote file systems
            final INode imageNode = currentImageRootDirectoryNode.tryGetNode(imageRelativePath);
            if (imageNode == null)
            {
                throw new DataStructureException(
                        String
                                .format(
                                        "No image node with path '%s' could be found in the 'original data' directory.",
                                        imageRelativePath));
            }
            node = plateColumnDir.tryAddLink(wellFileName, imageNode);
        } else
        {
            // Copies the file. So we are able to undo the operation.
            node =
                    plateColumnDir.addFile(new File(imageRootDirectory, imageRelativePath),
                            wellFileName, false);
        }
        if (node == null)
        {
            throw new StorageException(
                    String
                            .format(
                                    "Original file name '%s' could not be added at channel %d, plate location '%s' and well location '%s'.",
                                    imageRelativePath, channel, wellLocation, tileLocation));
        }
        final char sep = Constants.PATH_SEPARATOR;
        final String standardNodePath =
                channelDirCache.getDirectory().getName() + sep
                        + rowDirCache.getDirectory().getName() + sep + plateColumnDir.getName()
                        + sep + wellFileName;
        return new NodePath(node, standardNodePath);
    }

    /** Am key class for HCS image files. */
    private static class ImageFileKey
    {
        final private int channel;

        final private Location wellLocation;

        final private Location tileLocation;

        ImageFileKey(final int channel, final Location wellLocation, final Location tileLocation)
        {
            assert channel >= 0;
            assert wellLocation != null;
            assert tileLocation != null;

            this.channel = channel;
            this.wellLocation = wellLocation;
            this.tileLocation = tileLocation;
        }

        //
        // Object
        //

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null || obj instanceof ImageFileKey == false)
            {
                return false;
            }
            final ImageFileKey that = (ImageFileKey) obj;
            return this.channel == that.channel && this.wellLocation.equals(that.wellLocation)
                    && this.tileLocation.equals(that.tileLocation);
        }

        @Override
        public int hashCode()
        {
            int hashCode = 17;
            hashCode = hashCode * 37 + channel;
            hashCode = hashCode * 37 + wellLocation.hashCode();
            hashCode = hashCode * 37 + tileLocation.hashCode();
            return hashCode;
        }
    }

    private final HashSet<ImageFileKey> imageFilesStored = new HashSet<ImageFileKey>();

    private void check(final int channel, final Location wellLocation, final Location tileLocation)
    {
        checkCoordinates(channel, wellLocation, tileLocation);
        final ImageFileKey key = new ImageFileKey(channel, wellLocation, tileLocation);
        final boolean alreadyStored = imageFilesStored.contains(key);
        if (alreadyStored == false)
        {
            imageFilesStored.add(key);
        }
        if (alreadyStored)
        {
            throw new DataStructureException(
                    String
                            .format(
                                    "A node already exists at channel %d, plate location '%s' and well location '%s'.",
                                    channel, wellLocation, tileLocation));
        }
    }

    //
    // AbstractFormattedData
    //

    public final Format getFormat()
    {
        return HCSImageFormatV1_0.HCS_IMAGE_1_0;
    }

    @Override
    protected final void assertValidFormatAndFormatParameters()
    {
        super.assertValidFormatAndFormatParameters();
        final IFormatParameters formatParameters = getFormatParameters();
        final Set<String> notPresent = new HashSet<String>();
        for (final String formatParameterName : format.getMandatoryParameterNames())
        {
            if (formatParameters.containsParameter(formatParameterName) == false)
            {
                notPresent.add(formatParameterName);
            }
        }
        if (notPresent.isEmpty() == false)
        {
            throw new DataStructureException(String.format(
                    "Following format parameters '%s' could not be found in the structure.",
                    notPresent));
        }

    }
}
