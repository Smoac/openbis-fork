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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.search;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.CodeSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ProjectSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SpaceSearchCriterion;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentAttributeSearchFieldKind;

/**
 * @author pkupczyk
 */
public class ProjectSearchCriterionTranslator extends AbstractFieldFromCompositeSearchCriterionTranslator
{

    public ProjectSearchCriterionTranslator(IDAOFactory daoFactory, IEntityAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriterion criterion)
    {
        return criterion instanceof ProjectSearchCriterion;
    }

    @Override
    protected SearchCriterionTranslationResult doTranslate(SearchTranslationContext context, ISearchCriterion criterion)
    {
        if (false == EntityKind.EXPERIMENT.equals(context.peekEntityKind()))
        {
            throw new IllegalArgumentException("A project criterion can be used only in an experiment criterion, but was used in: "
                    + context.peekEntityKind() + " context.");
        }

        return super.doTranslate(context, criterion);
    }

    @Override
    protected DetailedSearchField doTranslateSearchField(SearchTranslationContext context, ISearchCriterion criterion, ISearchCriterion subCriterion)
    {
        if (subCriterion instanceof CodeSearchCriterion)
        {
            return DetailedSearchField.createAttributeField(ExperimentAttributeSearchFieldKind.PROJECT);
        } else if (subCriterion instanceof SpaceSearchCriterion)
        {
            return DetailedSearchField.createAttributeField(ExperimentAttributeSearchFieldKind.PROJECT_SPACE);
        } else
        {
            throw new IllegalArgumentException("Unknown criterion: " + subCriterion);
        }
    }

}