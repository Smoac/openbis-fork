/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.ISearchExperimentTypeExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.experiment.IExperimentTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class SearchExperimentTypeSqlMethodExecutor extends
        AbstractIdSearchMethodExecutor<ExperimentType, ExperimentTypePE, ExperimentTypeSearchCriteria, ExperimentTypeFetchOptions>
        implements ISearchExperimentTypeMethodExecutor
{
    @Autowired
    private ISearchExperimentTypeExecutor searchExecutor;

    @Autowired
    private IExperimentTypeTranslator translator;

    @Override
    protected List<ExperimentTypePE> searchPEs(IOperationContext context, ExperimentTypeSearchCriteria criteria)
    {
        return searchExecutor.search(context, criteria);
    }

    @Override
    protected ITranslator<Long, ExperimentType, ExperimentTypeFetchOptions> getTranslator()
    {
        return translator;
    }

}
