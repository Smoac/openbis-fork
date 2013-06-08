/*
 * Copyright 2011-2013 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.hcscld;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.hdf5.CompoundElement;
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
        @CompoundElement(dimensions =
            { 20 })
        private String formatTag;

        private int majorVersion;

        private int minorVersion;

        // Used when reading from the HDF5 file.
        FormatDescriptor()
        {
        }

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
                    && CURRENT_FORMAT_DESCRIPTOR.getFormatTag().equals(descOrNull.getFormatTag())
                    && CURRENT_FORMAT_DESCRIPTOR.getMajorVersion() == descOrNull.getMajorVersion();
        }

        @Override
        public String toString()
        {
            return "FormatDescriptor [formatTag=" + formatTag + ", majorVersion=" + majorVersion
                    + ", minorVersion=" + minorVersion + "]";
        }
    }

    static final FormatDescriptor CURRENT_FORMAT_DESCRIPTOR = new FormatDescriptor("SIS-HCSCLD",
            1, 0);

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
                reader.enumeration().getType("datasetTypes",
                        new String[]
                            { CellLevelDatasetType.SEGMENTATION.name(),
                                    CellLevelDatasetType.FEATURES.name(),
                                    CellLevelDatasetType.CLASSIFICATION.name() });
        this.manageReader = manageReader;
        this.hints = new HDF5CompoundMappingHints().enumReturnType(EnumReturnType.STRING);
    }

    private CellLevelDatasetTypeDescriptor getDatasetTypeDesc(String datasetCode)
    {
        return reader.compound().getAttr(CellLevelDataset.getDatasetPath(datasetCode),
                CellLevelDataset.getDatasetTypeAttributeName(),
                CellLevelDatasetTypeDescriptor.class);
    }

    @Override
    public List<ICellLevelDataset> getDataSets()
    {
        final List<String> codes = reader.getGroupMembers("/");
        final List<ICellLevelDataset> result = new ArrayList<ICellLevelDataset>(codes.size());
        for (String code : codes)
        {
            final CellLevelDatasetTypeDescriptor desc = getDatasetTypeDesc(code);
            switch (desc.getDatasetType())
            {
                case CLASSIFICATION:
                    result.add(new CellLevelClassificationDataset(reader, code, reader
                            .readCompound(
                                    CellLevelDataset.getImageQuantityStructureObjectPath(code),
                                    ImageQuantityStructure.class), desc.getFormatType(), desc
                            .getFormatVersionNumber()));
                    break;
                case FEATURES:
                    result.add(new CellLevelFeatureDataset(reader, code, reader.readCompound(
                            CellLevelDataset.getImageQuantityStructureObjectPath(code),
                            ImageQuantityStructure.class), hints, desc.getFormatType(), desc
                            .getFormatVersionNumber()));
                    break;
                case SEGMENTATION:
                    result.add(new CellLevelSegmentationDataset(reader, code, reader.readCompound(
                            CellLevelDataset.getImageQuantityStructureObjectPath(code),
                            ImageQuantityStructure.class), reader.readCompound(
                            CellLevelSegmentationDataset.getImageGeometryObjectPath(code),
                            ImageGeometry.class), desc.getFormatType(), desc
                            .getFormatVersionNumber()));
                    break;
                default:
                    throw new Error("Unknown enum type.");
            }
        }
        return result;
    }

    @Override
    public ICellLevelDataset getDataSet(String datasetCode)
    {
        final CellLevelDatasetTypeDescriptor desc = getDatasetTypeDesc(datasetCode);
        switch (desc.getDatasetType())
        {
            case CLASSIFICATION:
                return new CellLevelClassificationDataset(reader, datasetCode, reader.readCompound(
                        CellLevelDataset.getImageQuantityStructureObjectPath(datasetCode),
                        ImageQuantityStructure.class), desc.getFormatType(),
                        desc.getFormatVersionNumber());
            case FEATURES:
                return new CellLevelFeatureDataset(reader, datasetCode, reader.readCompound(
                        CellLevelDataset.getImageQuantityStructureObjectPath(datasetCode),
                        ImageQuantityStructure.class), hints, desc.getFormatType(),
                        desc.getFormatVersionNumber());
            case SEGMENTATION:
                return new CellLevelSegmentationDataset(reader, datasetCode, reader.readCompound(
                        CellLevelDataset.getImageQuantityStructureObjectPath(datasetCode),
                        ImageQuantityStructure.class), reader.readCompound(
                        CellLevelSegmentationDataset.getImageGeometryObjectPath(datasetCode),
                        ImageGeometry.class), desc.getFormatType(), desc.getFormatVersionNumber());
            case TRACKING:
                return new CellLevelTrackingDataset(reader, datasetCode, reader.readCompound(
                        CellLevelDataset.getImageQuantityStructureObjectPath(datasetCode),
                        ImageQuantityStructure.class), desc.getFormatType(),
                        desc.getFormatVersionNumber());
        }
        throw new Error("Unknown enum type.");
    }

    HDF5EnumerationType getHdf5DatasetTypeEnum()
    {
        return hdf5DatasetTypeEnum;
    }

    HDF5CompoundMappingHints getHints()
    {
        return hints;
    }

    @Override
    public ICellLevelDataReader enumAsString()
    {
        hints.setEnumReturnType(EnumReturnType.STRING);
        return this;
    }

    @Override
    public ICellLevelDataReader enumAsOrdinal()
    {
        hints.setEnumReturnType(EnumReturnType.ORDINAL);
        return this;
    }

    @Override
    public ICellLevelDataReader enumAsHDF5Enum()
    {
        hints.setEnumReturnType(EnumReturnType.HDF5ENUMERATIONVALUE);
        return this;
    }

    @Override
    public void close()
    {
        if (manageReader)
        {
            reader.close();
        }
    }

    FormatDescriptor tryGetCLDFormat()
    {
        if (reader.object().hasAttribute("/", getCLDFormatTagAttributeName()))
        {
            return reader.compound().getAttr("/", getCLDFormatTagAttributeName(),
                    FormatDescriptor.class);
        } else
        {
            return null;
        }
    }

    static String getCLDFormatTagAttributeName()
    {
        return "formatTag";
    }

}
