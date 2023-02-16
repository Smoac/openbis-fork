/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.business.bo.exception;

import java.util.List;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author pkupczyk
 */
public class DataSetDeletionUnknownLocationsException extends UserFailureException
{

    private static final long serialVersionUID = 1L;

    public DataSetDeletionUnknownLocationsException(List<String> codesOfDataSetsWithUnknownLocations)
    {
        super(
                "Deletion failed because the following data sets are unknown by Data Store Servers they were registered in. "
                        + "May be the responsible Data Store Servers are not running.\n\n"
                        + "Data sets: "
                        + CollectionUtils.abbreviate(codesOfDataSetsWithUnknownLocations, 10)
                        + "\n\n"
                        + "To delete these data sets please choose 'Force > Force not existing locations' option.");
    }

}
