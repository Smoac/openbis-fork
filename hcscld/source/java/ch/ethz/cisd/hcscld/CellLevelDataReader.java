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

package ch.ethz.cisd.hcscld;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.hdf5.HDF5CompoundMappingHints;
import ch.systemsx.cisd.hdf5.HDF5CompoundMappingHints.EnumReturnType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationType;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * A reader for HCS cell level data.
 * 
 * @author Bernd Rinn
 */
class CellLevelDataReader implements ICellLevelDataReader
{
    /**
     * A class to describe the format of this file.
     */
    static class FormatDescriptor
    {
        private final String formatTag;

        private final int majorVersion;

        private final int minorVersion;

        public FormatDescriptor(String formatTag, int majorVersion, int minorVersion)
        {
            this.formatTag = formatTag;
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
        }

        public String getFormatTag()
        {
            return formatTag;
        }

        public int getMajorVersion()
        {
            return majorVersion;
        }

        public int getMinorVersion()
        {
            return minorVersion;
        }

        public static boolean canRead(FormatDescriptor descOrNull)
        {
            return descOrNull != null
                    && EXPECTED_DESCRIPTOR.getFormatTag().equals(descOrNull.getFormatTag())
                    && EXPECTED_DESCRIPTOR.getMajorVersion() == descOrNull.getMajorVersion();
        }

        @Override
        public String toString()
        {
            return "FormatDescriptor [formatTag=" + formatTag + ", majorVersion=" + majorVersion
                    + ", minorVersion=" + minorVersion + "]";
        }
    }

    static final FormatDescriptor EXPECTED_DESCRIPTOR = new FormatDescriptor("CISD-HCSCLD", 1, 0);

    private final IHDF5Reader reader;

    private final HDF5EnumerationType hdf5DatasetTypeEnum;

    private final boolean manageReader;

    private final HDF5CompoundMappingHints hints;

    CellLevelDataReader(File file)
    {
        this(HDF5Factory.open(file), true, true);
    }

    CellLevelDataReader(IHDF5Reader reader)
    {
        this(reader, false, true);
    }

    CellLevelDataReader(IHDF5Reader reader, boolean manageReader, boolean doFormatCheck)
    {
        this.reader = reader;
        final FormatDescriptor descOrNull = tryGetCLDFormat();
        if (doFormatCheck && FormatDescriptor.canRead(descOrNull) == false)
        {
            throw new UnsupportedFileFormatException(
                    String.format(
                            "File is HDF5, but doesn't have a proper CLD tag or version. [FormatDescriptor: %s]",
                            (descOrNull == null) ? "NO DESCRIPTOR" : descOrNull));
        }
        this.hdf5DatasetTypeEnum =
                reader.getEnumType("datasetTypes",
                        new String[]
                            { CellLevelDatasetType.SEGMENTATION.name(),
                                    CellLevelDatasetType.FEATURES.name(),
                                    CellLevelDatasetType.CLASSIFICATION.name() });
        this.manageReader = manageReader;
        this.hints = new HDF5CompoundMappingHints().enumReturnType(EnumReturnType.STRING);
    }

    private CellLevelDatasetType getDatasetType(String datasetCode)
    {
        return CellLevelDatasetType.valueOf(reader.getEnumAttributeAsString(
                CellLevelDataset.getDatasetPath(datasetCode), "datasetType"));
    }

    public List<ICellLevelDataset> getDataSets()
    {
        final List<String> codes = reader.getGroupMembers("/");
        final List<ICellLevelDataset> result = new ArrayList<ICellLevelDataset>(codes.size());
        for (String code : codes)
        {
            switch (getDatasetType(code))
            {
                case CLASSIFICATION:
                    result.add(new CellLevelClassificationDataset(reader, code, reader
                            .readCompound(
                                    CellLevelDataset.getImageQuantityStructureObjectPath(code),
                                    ImageQuantityStructure.class)));
                    break;
                case FEATURES:
                    result.add(new CellLevelFeatureDataset(reader, code, reader.readCompound(
                            CellLevelDataset.getImageQuantityStructureObjectPath(code),
                            ImageQuantityStructure.class), hints));
                    break;
                case SEGMENTATION:
                    result.add(new CellLevelSegmentationDataset(reader, code, reader.readCompound(
                            CellLevelDataset.getImageQuantityStructureObjectPath(code),
                            ImageQuantityStructure.class), reader.readCompound(
                            CellLevelSegmentationDataset.getImageGeometryObjectPath(code),
                            ImageGeometry.class)));
                    break;
                default:
                    throw new Error("Unknown enum type.");
            }
        }
        return result;
    }

    public ICellLevelDataset getDataSet(String datasetCode)
    {
        switch (getDatasetType(datasetCode))
        {
            case CLASSIFICATION:
                return new CellLevelClassificationDataset(reader, datasetCode, reader.readCompound(
                        CellLevelDataset.getImageQuantityStructureObjectPath(datasetCode),
                        ImageQuantityStructure.class));
            case FEATURES:
                return new CellLevelFeatureDataset(reader, datasetCode, reader.readCompound(
                        CellLevelDataset.getImageQuantityStructureObjectPath(datasetCode),
                        ImageQuantityStructure.class), hints);
            case SEGMENTATION:
                return new CellLevelSegmentationDataset(reader, datasetCode, reader.readCompound(
                        CellLevelDataset.getImageQuantityStructureObjectPath(datasetCode),
                        ImageQuantityStructure.class), reader.readCompound(
                        CellLevelSegmentationDataset.getImageGeometryObjectPath(datasetCode),
                        ImageGeometry.class));
            default:
                throw new Error("Unknown enum type.");
        }
    }

    HDF5EnumerationType getHdf5DatasetTypeEnum()
    {
        return hdf5DatasetTypeEnum;
    }

    HDF5CompoundMappingHints getHints()
    {
        return hints;
    }

    public ICellLevelDataReader enumAsString()
    {
        hints.setEnumReturnType(EnumReturnType.STRING);
        return this;
    }

    public ICellLevelDataReader enumAsOrdinal()
    {
        hints.setEnumReturnType(EnumReturnType.ORDINAL);
        return this;
    }

    public ICellLevelDataReader enumAsHDF5Enum()
    {
        hints.setEnumReturnType(EnumReturnType.HDF5ENUMERATIONVALUE);
        return this;
    }

    public void close()
    {
        if (manageReader)
        {
            reader.close();
        }
    }

    FormatDescriptor tryGetCLDFormat()
    {
        if (reader.hasAttribute("/", getCLDFormatTagAttributeName())
                && reader.hasAttribute("/", getCLDMajorVersionObjectPath())
                && reader.hasAttribute("/", getCLDMinorVersionObjectPath()))
        {
            final String formatTag = reader.getStringAttribute("/", getCLDFormatTagAttributeName());
            final int majorVersion = reader.getIntAttribute("/", getCLDMajorVersionObjectPath());
            final int minorVersion = reader.getIntAttribute("/", getCLDMinorVersionObjectPath());
            return new FormatDescriptor(formatTag, majorVersion, minorVersion);
        } else
        {
            return null;
        }
    }

    static String getCLDFormatTagAttributeName()
    {
        return "formatTag";
    }

    static String getCLDMajorVersionObjectPath()
    {
        return "formatMajorVersion";
    }

    static String getCLDMinorVersionObjectPath()
    {
        return "formatMinorVersion";
    }

}
