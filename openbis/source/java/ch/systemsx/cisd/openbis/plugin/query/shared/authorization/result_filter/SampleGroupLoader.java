/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.shared.authorization.result_filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * {@link IGroupLoader} for samples.
 * 
 * @author Izabela Adamczyk
 */
class SampleGroupLoader implements IGroupLoader
{

    private final ISampleDAO dao;

    public SampleGroupLoader(ISampleDAO dao)
    {
        this.dao = dao;
    }

    public Map<String, SpacePE> loadGroups(Set<String> keys)
    {
        Map<String, SpacePE> map = new HashMap<String, SpacePE>();
        List<SamplePE> samples = dao.listByPermID(keys);
        for (SamplePE s : samples)
        {
            map.put(s.getPermId(), s.getSpace());
        }
        return map;
    }
}