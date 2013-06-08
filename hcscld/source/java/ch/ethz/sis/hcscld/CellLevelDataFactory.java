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

import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;

import ch.systemsx.cisd.hdf5.IHDF5Reader;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

/**
 * A factory for cell-level data readers and writers.
 * 
 * @author Bernd Rinn
 */
public class CellLevelDataFactory
{
    /**
     * Opens a cell-level data file for writing, creating it if it does not yet exist.
     * 
     * @param file The file of the cell-leve data file.
     */
    public static ICellLevelDataWriter open(File file)
    {
        return new CellLevelDataWriter(file);
    }

    /**
     * Opens a cell-level data file for writing.
     * 
     * @param writer The HDF5 writer of the cell-leve data file.
     */
    public static ICellLevelDataWriter open(IHDF5Writer writer)
    {
        return new CellLevelDataWriter(writer);
    }

    /**
     * Opens a cell-level data file for reading. It is an error if the specified <var>file</var does
     * not exist.
     * 
     * @param file The file of the cell-level data file.
     */
    public static ICellLevelDataReader openForReading(File file)
    {
        try
        {
            return new CellLevelDataReader(file);
        } catch (HDF5Exception ex)
        {
            throw new UnsupportedFileFormatException(ex.getClass().getSimpleName() + ":" + ex.getMessage(),
                    ex);
        }
    }

    /**
     * Opens a cell-level data file for reading.
     * 
     * @param reader The HDF5 reader of the cell-level data file.
     */
    public static ICellLevelDataReader openForReading(IHDF5Reader reader)
    {
        try
        {
            return new CellLevelDataReader(reader);
        } catch (HDF5Exception ex)
        {
            throw new UnsupportedFileFormatException(ex.getClass().getSimpleName() + ":" + ex.getMessage(),
                    ex);
        }
    }

}
