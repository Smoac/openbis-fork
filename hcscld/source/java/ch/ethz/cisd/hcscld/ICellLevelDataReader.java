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

import java.io.Closeable;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * An interface for reading cell-level data.
 *
 * @author Bernd Rinn
 */
public interface ICellLevelDataReader extends Closeable
{
    /**
     * Returns all data sets in this cell-level data file. 
     */
    public List<ICellLevelDataset> getDataSets();
    
    /**
     * Returns the data set with code <var>datasetCode</var>.
     */
    public ICellLevelDataset getDataSet(String datasetCode);

    // New signature: we only throw IOExceptionUnchecked.
    public void close() throws IOExceptionUnchecked;
}
