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

import ch.systemsx.cisd.hdf5.HDF5DataBlock;

/**
 * Block of cell level features.
 * 
 * @author Bernd Rinn
 */
public class CellLevelFeatureBlock extends CellLevelFeatures
{
    private final long offset;

    private final long index;

    CellLevelFeatureBlock(IFeatureGroup featureGroup, WellFieldId id,
            HDF5DataBlock<Object[][]> block)
    {
        super(featureGroup, id, block.getData());
        this.index = block.getIndex();
        this.offset = block.getOffset();
    }

    /**
     * Returns the offset of this block in the feature data set.
     * <p>
     * When translating between object id and the index to provide to {@link #getData()}, this
     * offset needs to be subtracted.
     */
    public long getOffset()
    {
        return offset;
    }

    /**
     * Returns the iteration index of this block, starting with 0.
     */
    public long getIndex()
    {
        return index;
    }

}
