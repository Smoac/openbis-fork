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

package ch.systemsx.cisd.etlserver.postregistration;

import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Strategy of finding a share to move a data set. Classes implementing this interface should
 * have a public constructor with one argument which is of type {@link Properties}.
 *
 * @author Franz-Josef Elmer
 */
public interface IShareFinder
{
    /**
     * Tries to find a share from the specified shares to whom the specified data set can be moved.
     * 
     * @param dataSet with known size and old share ID.
     * @param shares All shares. Share instances know whether they are incoming or external.
     * @return <code>null</code> if no share could be found.
     */
    public Share tryToFindShare(SimpleDataSetInformationDTO dataSet, List<Share> shares);
}
