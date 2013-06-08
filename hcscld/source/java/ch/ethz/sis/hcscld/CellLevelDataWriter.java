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

package ch.ethz.sis.hcscld;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

/**
 * A writer for HCS cell level data.
 * 
 * @author Bernd Rinn
 */
class CellLevelDataWriter extends CellLevelDataReader implements ICellLevelDataWriter
{
    private final IHDF5Writer writer;

    private final Set<IDatasetVerifyer> datasetsWritten;

    private final boolean manageWriter;

    CellLevelDataWriter(File file)
    {
        this(HDF5Factory.open(file), true);
    }

    CellLevelDataWriter(IHDF5Writer writer)
    {
        this(writer, false);
    }

    CellLevelDataWriter(IHDF5Writer writer, boolean manageWriter)
    {
        super(writer, manageWriter, false);
        this.writer = writer;
        this.manageWriter = manageWriter;
        this.datasetsWritten = new HashSet<IDatasetVerifyer>();
        if (writer.object().hasAttribute("/", getCLDFormatTagAttributeName()) == false)
        {
            if (writer.getGroupMembers("/").isEmpty() == false)
            {
                throw new UnsupportedFileFormatException(
                        "File is HDF5, but doesn't have a proper CLD tag or version.");
            }
            writer.compound().setAttr("/", getCLDFormatTagAttributeName(),
                    CURRENT_FORMAT_DESCRIPTOR);
        }
    }

    @Override
    public ICellLevelFeatureWritableDataset addFeatureDataset(String datasetCode,
            ImageQuantityStructure quantityStructure)
    {
        final CellLevelFeatureWritableDataset dataset =
                new CellLevelFeatureWritableDataset(writer, datasetCode, quantityStructure,
                        getHints(), getHdf5DatasetTypeEnum());
        datasetsWritten.add(dataset.getVerifyer());
        return dataset;
    }

    @Override
    public ICellLevelClassificationWritableDataset addClassificationDataset(String datasetCode,
            ImageQuantityStructure quantityStructure, Class<? extends Enum<?>> enumType)
    {
        final CellLevelClassificationWritableDataset dataset =
                new CellLevelClassificationWritableDataset(writer, datasetCode, quantityStructure,
                        getHdf5DatasetTypeEnum(), enumType);
        datasetsWritten.add(dataset.getVerifyer());
        return dataset;
    }

    @Override
    public ICellLevelClassificationWritableDataset addClassificationDataset(String datasetCode,
            ImageQuantityStructure quantityStructure, List<String> options)
    {
        final CellLevelClassificationWritableDataset dataset =
                new CellLevelClassificationWritableDataset(writer, datasetCode, quantityStructure,
                        getHdf5DatasetTypeEnum(), options);
        datasetsWritten.add(dataset.getVerifyer());
        return dataset;
    }

    @Override
    public ICellLevelSegmentationWritableDataset addSegmentationDataset(String datasetCode,
            ImageQuantityStructure quantityStructure, ImageGeometry imageGeometry,
            boolean storeEdgeMasks)
    {
        final CellLevelSegmentationWritableDataset dataset =
                new CellLevelSegmentationWritableDataset(writer, datasetCode, quantityStructure,
                        imageGeometry, getHdf5DatasetTypeEnum(), storeEdgeMasks);
        datasetsWritten.add(dataset.getVerifyer());
        return dataset;
    }

    @Override
    public ICellLevelTrackingWritableDataset addTrackingDataset(String datasetCode,
            ImageQuantityStructure quantityStructure)
    {
        final CellLevelTrackingWritableDataset dataset =
                new CellLevelTrackingWritableDataset(writer, datasetCode, quantityStructure,
                        getHdf5DatasetTypeEnum());
        datasetsWritten.add(dataset.getVerifyer());
        return dataset;
    }

    @Override
    public void close() throws IOExceptionUnchecked
    {
        StringBuilder builder = null;
        for (IDatasetVerifyer verifyer : datasetsWritten)
        {
            final String errorMsg = verifyer.verify();
            if (errorMsg != null)
            {
                if (builder == null)
                {
                    builder = new StringBuilder();
                }
                builder.append("Dataset ");
                builder.append(verifyer.getDatasetCode());
                builder.append(": ");
                builder.append(errorMsg);
                builder.append('\n');
            }
        }

        if (manageWriter)
        {
            writer.close();
        } else
        {
            writer.file().flush();
        }
        if (builder != null)
        {
            builder.setLength(builder.length() - 1);
            throw new IOExceptionUnchecked(new IOException(builder.toString()));
        }
    }

}
