/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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
import java.util.List;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.AbstractSearchObjectExecutor;
import ch.systemsx.cisd.openbis.generic.server.business.search.ExperimentSearchManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchExperimentExecutor extends AbstractSearchObjectExecutor<ExperimentSearchCriteria, ExperimentPE> implements
        ISearchExperimentExecutor
{

    @Override
    protected List<ExperimentPE> doSearch(IOperationContext context, DetailedSearchCriteria criteria)
    {
        ExperimentSearchManager searchManager =
                new ExperimentSearchManager(daoFactory.getHibernateSearchDAO(),
                        businessObjectFactory.createExperimentTable(context.getSession()));

        Collection<Long> experimentIds =
                searchManager.searchForExperimentIDs(context.getSession().getUserName(), criteria);

        return daoFactory.getExperimentDAO().listByIDs(experimentIds);
    }

}
