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

import java.util.List;

import ch.systemsx.cisd.hdf5.HDF5EnumerationType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationValue;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

/**
 * @author Bernd Rinn
 */
class CellLevelBaseWritableDataset extends CellLevelDataset
{
    final IHDF5Writer writer;

    private final CellLevelDatasetType datasetType;

    CellLevelBaseWritableDataset(final IHDF5Writer writer, final String datasetCode,
            final WellFieldGeometry geometry, final HDF5EnumerationType hdf5KindEnum,
            final CellLevelDatasetType datasetType)
    {
        super(writer, datasetCode, geometry);
        this.writer = writer;
        this.datasetType = datasetType;
        run(new IWellFieldRunnable()
            {
                public void run(WellFieldId id, Object state)
                {
                    writer.createGroup(getObjectPath(id));
                }
            }, null);
        writer.writeCompound(getDatasetCode() + "/geometry", geometry);
        writer.setEnumAttribute(getDatasetCode(), getDatasetTypeAttributeName(),
                new HDF5EnumerationValue(hdf5KindEnum, datasetType.ordinal()));
    }

    void run(IWellFieldRunnable runnable, Object stateOrNull)
    {
        WellFieldRunner.run(geometry, runnable, stateOrNull);
    }

    public CellLevelDatasetType getType()
    {
        return datasetType;
    }

    HDF5EnumerationType addEnum(String name, List<String> values)
    {
        return addEnum(writer, datasetCode, name, values);
    }

    HDF5EnumerationType addEnum(Class<? extends Enum<?>> enumClass)
    {
        return addEnum(writer, datasetCode, enumClass);
    }

    static HDF5EnumerationType addEnum(IHDF5Writer writer, String datasetCode, String name,
            List<String> values)
    {
        return writer.getEnumType(datasetCode + "_" + name,
                values.toArray(new String[values.size()]));
    }

    static HDF5EnumerationType addEnum(IHDF5Writer writer, String datasetCode,
            Class<? extends Enum<?>> enumClass)
    {
        final Enum<?>[] constants = enumClass.getEnumConstants();
        final String[] options = new String[constants.length];
        for (int i = 0; i < options.length; ++i)
        {
            options[i] = constants[i].name();
        }
        return writer.getEnumType(datasetCode + "_" + enumClass.getSimpleName(), options);
    }

}
