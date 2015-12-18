/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.dataset;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.LocatorTypePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ILocatorTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;

/**
 * @author pkupczyk
 */
public class ListLocatorTypeByPermId extends AbstractListObjectById<LocatorTypePermId, LocatorTypePE>
{

    private ILocatorTypeDAO typeDAO;

    public ListLocatorTypeByPermId(ILocatorTypeDAO typeDAO)
    {
        this.typeDAO = typeDAO;
    }

    @Override
    public Class<LocatorTypePermId> getIdClass()
    {
        return LocatorTypePermId.class;
    }

    @Override
    public LocatorTypePermId createId(LocatorTypePE type)
    {
        return new LocatorTypePermId(type.getCode());
    }

    @Override
    public List<LocatorTypePE> listByIds(List<LocatorTypePermId> ids)
    {
        List<LocatorTypePE> types = new ArrayList<LocatorTypePE>();

        for (LocatorTypePermId id : ids)
        {
            LocatorTypePE type = typeDAO.tryToFindLocatorTypeByCode(id.getPermId());
            if (type != null)
            {
                types.add(type);
            }
        }
        return types;
    }

}
