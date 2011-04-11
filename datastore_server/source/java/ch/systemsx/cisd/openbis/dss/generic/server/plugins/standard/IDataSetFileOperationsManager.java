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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IDataSetFileOperationsManager
{

    /**
     * Copies specified dataset's data to destination specified in constructor. The path at the
     * destination is defined by the original location of the data set.
     */
    public abstract Status copyToDestination(File originalData, DatasetDescription dataset);

    /**
     * Retrieves specified datases's data from the destination specified in constructor. The path at
     * the destination is defined by original location of the data set.
     */
    public abstract Status retrieveFromDestination(File originalData, DatasetDescription dataset);

    /**
     * Deletes specified datases's data from the destination specified in constructor. The path at
     * the destination is defined by original location of the data set.
     */
    public abstract Status deleteFromDestination(DeletedDataSet dataset);

    /**
     * Checks if specified dataset's data are present in the destination specified in constructor.
     * The path at the destination is defined by original location of the data set.
     */
    public abstract BooleanStatus isPresentInDestination(File originalData,
            DatasetDescription dataset);

}