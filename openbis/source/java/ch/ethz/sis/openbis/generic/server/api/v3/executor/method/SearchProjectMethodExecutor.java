/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.project.ISearchProjectExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.project.IProjectTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ProjectSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchProjectMethodExecutor extends AbstractSearchMethodExecutor<Project, ProjectPE, ProjectSearchCriterion, ProjectFetchOptions>
        implements ISearchProjectMethodExecutor
{

    @Autowired
    private ISearchProjectExecutor searchExecutor;

    @Autowired
    private IProjectTranslator translator;

    @Override
    protected ISearchObjectExecutor<ProjectSearchCriterion, ProjectPE> getSearchExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<ProjectPE, Project, ProjectFetchOptions> getTranslator()
    {
        return translator;
    }

}
