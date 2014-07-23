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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.collection.ListTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.id.experiment.IExperimentIdTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.server.business.search.id.IListerById;
import ch.systemsx.cisd.openbis.generic.server.business.search.id.ListerById;
import ch.systemsx.cisd.openbis.generic.server.business.search.id.experiment.ListerByExperimentPermIdId;
import ch.systemsx.cisd.openbis.generic.server.business.search.id.experiment.ListerByExperimentTechIdId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
@Component
public class ListExperimentByIdExecutor implements IListExperimentByIdExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @SuppressWarnings("unused")
    private ListExperimentByIdExecutor()
    {
    }

    public ListExperimentByIdExecutor(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Override
    public List<ExperimentPE> list(IOperationContext context, Collection<? extends IExperimentId> experimentIds)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.IExperimentId> experimentIdsCore =
                new ListTranslator().translate(experimentIds, new IExperimentIdTranslator());

        @SuppressWarnings("rawtypes")
        List<IListerById> listers = new LinkedList<IListerById>();
        listers.add(new ListerByExperimentTechIdId(daoFactory));
        listers.add(new ListerByExperimentPermIdId(daoFactory));
        List<ExperimentPE> experiments = new ListerById(listers).list(experimentIdsCore);

        return experiments;
    }

}
