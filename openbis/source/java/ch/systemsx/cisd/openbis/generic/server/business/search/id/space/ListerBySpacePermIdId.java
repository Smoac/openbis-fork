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

package ch.systemsx.cisd.openbis.generic.server.business.search.id.space;

import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.search.id.IListerById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.space.SpacePermIdId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ListerBySpacePermIdId implements IListerById<SpacePermIdId, SpacePE>
{

    private ISpaceDAO spaceDAO;

    public ListerBySpacePermIdId(IDAOFactory daoFactory)
    {
        this.spaceDAO = daoFactory.getSpaceDAO();
    }

    @Override
    public Class<SpacePermIdId> getIdClass()
    {
        return SpacePermIdId.class;
    }

    @Override
    public SpacePermIdId createId(SpacePE space)
    {
        return new SpacePermIdId(space.getCode());
    }

    @Override
    public List<SpacePE> listByIds(List<SpacePermIdId> ids)
    {
        List<String> permIds = new LinkedList<String>();

        for (SpacePermIdId id : ids)
        {
            permIds.add(id.getPermId());
        }

        return spaceDAO.tryFindSpaceByCodes(permIds);
    }

}
