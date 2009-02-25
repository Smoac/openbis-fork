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

package ch.systemsx.cisd.etlserver;

import java.util.Properties;

import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * This role is supposed to be implemented by classes that can extract HCS image files from an
 * incoming data set directory. Implementations of this interface need to have a constructor that
 * takes {@link Properties} to initialize itself.
 * 
 * @author Christian Ribeaud
 */
public interface IHCSImageFileExtractor
{

    public final static String FILE_EXTRACTOR = "file-extractor";

    /**
     * Extracts <code>StandardCoordinates</code> in the given <var>incomingDataSetDirectory</var>
     * and for the given <var>dataSetInfo</var> and hands the image files that it finds over to the
     * specified <var>accepter</var>.
     * 
     * @return the extraction result. Must not be <code>null</code>.
     */
    public HCSImageFileExtractionResult process(final IDirectory incomingDataSetDirectory,
            DataSetInformation dataSetInformation, final IHCSImageFileAccepter accepter);
}