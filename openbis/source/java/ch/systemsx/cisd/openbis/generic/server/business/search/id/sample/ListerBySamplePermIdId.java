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

import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.search.id.IListerById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.sample.SamplePermIdId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
public class ListerBySamplePermIdId implements IListerById<SamplePermIdId, SamplePE>
{

    private ISampleDAO sampleDAO;

    public ListerBySamplePermIdId(IDAOFactory daoFactory)
    {
        this.sampleDAO = daoFactory.getSampleDAO();
    }

    @Override
    public Class<SamplePermIdId> getIdClass()
    {
        return SamplePermIdId.class;
    }

    @Override
    public SamplePermIdId createId(SamplePE sample)
    {
        return new SamplePermIdId(sample.getPermId());
    }

    @Override
    public List<SamplePE> listByIds(List<SamplePermIdId> ids)
    {
        List<String> permIds = new LinkedList<String>();

        for (SamplePermIdId id : ids)
        {
            permIds.add(id.getPermId());
        }

        return sampleDAO.listByPermID(permIds);
    }

}
