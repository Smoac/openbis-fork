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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.collection.ListTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.id.sample.ISampleIdTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.server.business.search.id.IListerById;
import ch.systemsx.cisd.openbis.generic.server.business.search.id.ListerById;
import ch.systemsx.cisd.openbis.generic.server.business.search.id.sample.ListerBySampleIdentifierId;
import ch.systemsx.cisd.openbis.generic.server.business.search.id.sample.ListerBySamplePermIdId;
import ch.systemsx.cisd.openbis.generic.server.business.search.id.sample.ListerBySampleTechIdId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class ListSampleByIdExecutor implements IListSampleByIdExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @SuppressWarnings("unused")
    private ListSampleByIdExecutor()
    {
    }

    public ListSampleByIdExecutor(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Override
    public List<SamplePE> list(IOperationContext context, Collection<? extends ISampleId> sampleIds)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.sample.ISampleId> sampleIdsCore =
                new ListTranslator().translate(sampleIds, new ISampleIdTranslator());

        @SuppressWarnings("rawtypes")
        List<IListerById> listers = new ArrayList<IListerById>();
        listers.add(new ListerBySamplePermIdId(daoFactory));
        listers.add(new ListerBySampleTechIdId(daoFactory));
        listers.add(new ListerBySampleIdentifierId(daoFactory, context.getSession().tryGetPerson().getHomeSpace()));

        return new ListerById(listers).list(sampleIdsCore);
    }

}
