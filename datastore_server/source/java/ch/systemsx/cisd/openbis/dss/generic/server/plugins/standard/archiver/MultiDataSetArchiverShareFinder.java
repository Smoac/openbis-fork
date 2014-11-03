/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.IShareFinder;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Jakub Straszewski
 */
public class MultiDataSetArchiverShareFinder implements IShareFinder
{
    private Share share;

    public MultiDataSetArchiverShareFinder()
    {
    }

    @Override
    public Share tryToFindShare(SimpleDataSetInformationDTO dataSet, List<Share> shares)
    {
        if (share == null)
        {
            share = tryToFindShare(shares);
        }
        return share;
    }

    public Share tryToFindShare(List<Share> shares)
    {
        for (Share sh : shares)
        {
            if (sh.isUnarchivingScratchShare())
            {
                return sh;
            }
        }
        throw new IllegalArgumentException("Could not find proper share");
    }
}
