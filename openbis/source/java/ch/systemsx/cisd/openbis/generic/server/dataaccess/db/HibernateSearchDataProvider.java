/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.translator.GroupTranslator;

/**
 * Encapsulates data provided to {@link HibernateSearchDAO} by other DAOs.
 * 
 * @author Piotr Buczek
 */
public class HibernateSearchDataProvider
{
    private Map<String, Group> groupsById;

    public HibernateSearchDataProvider(IDAOFactory factory)
    {
        List<GroupPE> groups = factory.getGroupDAO().listGroups();
        groupsById = new HashMap<String, Group>();
        for (GroupPE group : groups)
        {
            groupsById.put(group.getId().toString(), GroupTranslator.translate(group));
        }
    }

    public Map<String, Group> getGroupsById()
    {
        return groupsById;
    }
}
