/*
 * Copyright 2010 ETH Zuerich, CISD
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

package eu.basysbio.cisd.dss;

import java.io.Reader;

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Interface for database feeders.
 *
 * @author Franz-Josef Elmer
 */
interface IDatabaseFeeder
{

    public void resetValueGroupIDGenerator();

    public void feedDatabase(DataSetInformation dataSetInformation, Reader reader,
            String nameOfReaderSource);
    
    /**
     * For LCA Microscopy data types who need to allow uploading of many data sets with the same header
     * information but different biIds. <code>biId==null</code> stands for a "regular" time seris data set.  
     */
    public void feedDatabase(DataSetInformation dataSetInformation, Reader reader,
            String nameOfReaderSource, String biIdOrNull);

}