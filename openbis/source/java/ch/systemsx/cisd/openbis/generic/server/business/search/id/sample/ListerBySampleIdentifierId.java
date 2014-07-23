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

package ch.systemsx.cisd.openbis.generic.server.business.search.id.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.business.search.id.IListerById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Franz-Josef Elmer
 */
// TODO: neds unit tests
public class ListerBySampleIdentifierId implements IListerById<SampleIdentifierId, SamplePE>
{

    private ISampleDAO sampleDAO;

    private ISpaceDAO spaceDAO;

    private SpacePE homeSpaceOrNull;

    public ListerBySampleIdentifierId(IDAOFactory daoFactory, SpacePE homeSpaceOrNull)
    {
        this.spaceDAO = daoFactory.getSpaceDAO();
        this.sampleDAO = daoFactory.getSampleDAO();
        this.homeSpaceOrNull = homeSpaceOrNull;
    }

    @Override
    public Class<SampleIdentifierId> getIdClass()
    {
        return SampleIdentifierId.class;
    }

    @Override
    public SampleIdentifierId createId(SamplePE sample)
    {
        return new SampleIdentifierId(sample.getIdentifier());
    }

    @Override
    public List<SamplePE> listByIds(List<SampleIdentifierId> ids)
    {
        Map<Key, List<String>> sampleCodesBySpaceAndContainer = new HashMap<ListerBySampleIdentifierId.Key, List<String>>();
        for (SampleIdentifierId id : ids)
        {
            SampleIdentifier sid = SampleIdentifierFactory.parse(id.getIdentifier());
            if (sid.isDatabaseInstanceLevel())
            {
                addToMap(sampleCodesBySpaceAndContainer, null, sid);
            } else if (sid.isSpaceLevel())
            {
                SpaceIdentifier spaceIdentifier = sid.getSpaceLevel();
                if (sid.isInsideHomeSpace() == false)
                {
                    String spaceCode = spaceIdentifier.getSpaceCode();
                    SpacePE space = spaceDAO.tryFindSpaceByCode(spaceCode);
                    if (space != null)
                    {
                        addToMap(sampleCodesBySpaceAndContainer, space, sid);
                    }
                } else if (homeSpaceOrNull != null)
                {
                    addToMap(sampleCodesBySpaceAndContainer, homeSpaceOrNull, sid);
                }
            } else
            {
                assert false;
            }
        }
        List<SamplePE> result = new ArrayList<SamplePE>();
        Set<Entry<Key, List<String>>> entrySet = sampleCodesBySpaceAndContainer.entrySet();

        for (Entry<Key, List<String>> entry : entrySet)
        {
            Key key = entry.getKey();
            List<String> sampleCodes = entry.getValue();
            SpacePE space = key.getSpace();
            String containerId = key.getContainerId();
            if (space == null)
            {
                result.addAll(sampleDAO.listByCodesAndDatabaseInstance(sampleCodes, containerId));
            } else
            {
                result.addAll(sampleDAO.listByCodesAndSpace(sampleCodes, containerId, space));
            }
        }
        return result;
    }

    private void addToMap(Map<Key, List<String>> sampleCodesBySpaceAndContainer, SpacePE space, SampleIdentifier sampleIdentifier)
    {
        Key key = new Key(space, sampleIdentifier.tryGetContainerCode());
        List<String> list = sampleCodesBySpaceAndContainer.get(key);
        if (list == null)
        {
            list = new ArrayList<String>();
            sampleCodesBySpaceAndContainer.put(key, list);
        }
        list.add(sampleIdentifier.getSampleSubCode());
    }

    private static final class Key
    {
        final SpacePE space;

        final String containerId;

        public Key(SpacePE space, String containerId)
        {
            super();
            this.space = space;
            this.containerId = containerId;
        }

        public SpacePE getSpace()
        {
            return space;
        }

        public String getContainerId()
        {
            return containerId;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((containerId == null) ? 0 : containerId.hashCode());
            result = prime * result + ((space == null) ? 0 : space.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            if (containerId == null)
            {
                if (other.containerId != null)
                    return false;
            } else if (!containerId.equals(other.containerId))
                return false;
            if (space == null)
            {
                if (other.space != null)
                    return false;
            } else if (!space.equals(other.space))
                return false;
            return true;
        }

    }

}
