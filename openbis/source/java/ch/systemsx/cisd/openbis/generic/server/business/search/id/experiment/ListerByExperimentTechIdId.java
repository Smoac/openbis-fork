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

package ch.systemsx.cisd.openbis.generic.server.business.search.id.experiment;

import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.search.id.IListerById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.ExperimentTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
public class ListerByExperimentTechIdId implements IListerById<ExperimentTechIdId, ExperimentPE>
{

    private IExperimentDAO experimentDAO;

    public ListerByExperimentTechIdId(IDAOFactory daoFactory)
    {
        this.experimentDAO = daoFactory.getExperimentDAO();
    }

    @Override
    public Class<ExperimentTechIdId> getIdClass()
    {
        return ExperimentTechIdId.class;
    }

    @Override
    public ExperimentTechIdId createId(ExperimentPE experiment)
    {
        return new ExperimentTechIdId(experiment.getId());
    }

    @Override
    public List<ExperimentPE> listByIds(List<ExperimentTechIdId> ids)
    {
        List<Long> techIds = new LinkedList<Long>();

        for (ExperimentTechIdId id : ids)
        {
            techIds.add(id.getTechId());
        }

        return experimentDAO.listByIDs(techIds);
    }

}
