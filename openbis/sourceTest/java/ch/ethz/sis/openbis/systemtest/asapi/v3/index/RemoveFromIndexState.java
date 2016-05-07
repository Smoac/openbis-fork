/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.systemtest.asapi.v3.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.FullTextIndexUpdater;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexUpdateOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexUpdateOperation.IndexUpdateOperationKind;

/**
 * @author pkupczyk
 */
public class RemoveFromIndexState extends IndexState
{

    public RemoveFromIndexState(IDAOFactory daoFactory)
    {
        super(createOperations(daoFactory));
    }

    private static Collection<RemoveFromIndexOperation> createOperations(IDAOFactory daoFactory)
    {
        FullTextIndexUpdater indexUpdater = (FullTextIndexUpdater) daoFactory.getPersistencyResources().getIndexUpdateScheduler();
        Collection<IndexUpdateOperation> updateOperations = indexUpdater.getQueue();
        List<RemoveFromIndexOperation> removeOperations = new ArrayList<RemoveFromIndexOperation>();

        for (IndexUpdateOperation updateOperation : updateOperations)
        {
            if (IndexUpdateOperationKind.REMOVE.equals(updateOperation.getOperationKind()))
            {
                removeOperations.add(new RemoveFromIndexOperation(updateOperation));
            }
        }

        return removeOperations;
    }

}
