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

package ch.ethz.sis.openbis.generic.server.api.v3.helper.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Franz-Josef Elmer
 */
// TODO: neds unit tests
public class ListSampleByIdentifier implements IListObjectById<SampleIdentifier, SamplePE>
{

    private ISpaceDAO spaceDAO;

    private ISampleDAO sampleDAO;

    private SpacePE homeSpaceOrNull;

    public ListSampleByIdentifier(ISpaceDAO spaceDAO, ISampleDAO sampleDAO, SpacePE homeSpaceOrNull)
    {
        this.spaceDAO = spaceDAO;
        this.sampleDAO = sampleDAO;
        this.homeSpaceOrNull = homeSpaceOrNull;
    }

    @Override
    public Class<SampleIdentifier> getIdClass()
    {
        return SampleIdentifier.class;
    }

    @Override
    public SampleIdentifier createId(SamplePE sample)
    {
        return new SampleIdentifier(sample.getIdentifier());
    }

    @Override
    public List<SamplePE> listByIds(List<SampleIdentifier> ids)
    {
        Map<SampleIdentifier, ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier> idMap = getIdMap(ids);
        Map<String, SpacePE> spaceMap = getSpaceMap(idMap.values());

        Map<Key, List<String>> sampleCodesBySpaceAndContainer = new HashMap<ListSampleByIdentifier.Key, List<String>>();

        for (SampleIdentifier id : ids)
        {
            ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier sid = idMap.get(id);

            if (sid.isDatabaseInstanceLevel())
            {
                addToMap(sampleCodesBySpaceAndContainer, null, sid);
            } else if (sid.isSpaceLevel())
            {
                SpaceIdentifier spaceIdentifier = sid.getSpaceLevel();
                if (sid.isInsideHomeSpace() == false)
                {
                    String spaceCode = spaceIdentifier.getSpaceCode();
                    SpacePE space = spaceMap.get(spaceCode);
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

    private Map<SampleIdentifier, ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier> getIdMap(List<SampleIdentifier> ids)
    {
        Map<SampleIdentifier, ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier> sidMap =
                new HashMap<SampleIdentifier, ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier>();

        for (SampleIdentifier id : ids)
        {
            ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier sid = SampleIdentifierFactory.parse(id.getIdentifier());
            sidMap.put(id, sid);
        }

        return sidMap;
    }

    private Map<String, SpacePE> getSpaceMap(Collection<ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier> ids)
    {
        Set<String> spaceCodes = new HashSet<String>();

        for (ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier id : ids)
        {
            if (id.isSpaceLevel())
            {
                spaceCodes.add(id.getSpaceLevel().getSpaceCode());
            }
        }

        if (false == spaceCodes.isEmpty())
        {
            List<SpacePE> spaces = spaceDAO.tryFindSpaceByCodes(new ArrayList<String>(spaceCodes));
            Map<String, SpacePE> spaceMap = new HashMap<String, SpacePE>();

            for (SpacePE space : spaces)
            {
                spaceMap.put(space.getCode(), space);
            }

            return spaceMap;
        } else
        {
            return Collections.emptyMap();
        }
    }

    private void addToMap(Map<Key, List<String>> sampleCodesBySpaceAndContainer, SpacePE space,
            ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier sampleIdentifier)
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
