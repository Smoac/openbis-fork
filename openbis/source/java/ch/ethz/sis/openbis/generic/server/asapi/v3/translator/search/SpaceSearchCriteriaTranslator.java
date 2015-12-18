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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.search;

import java.util.EnumSet;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;

/**
 * @author pkupczyk
 */
public class SpaceSearchCriteriaTranslator extends AbstractFieldFromCompositeSearchCriteriaTranslator
{

    private static final EnumSet<EntityKind> ENTITY_KINDS_WITH_SPACE 
            = EnumSet.of(EntityKind.EXPERIMENT,  EntityKind.SAMPLE);

    public SpaceSearchCriteriaTranslator(IDAOFactory daoFactory, IEntityAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof SpaceSearchCriteria;
    }

    @Override
    protected SearchCriteriaTranslationResult doTranslate(SearchTranslationContext context, ISearchCriteria criteria)
    {
        if (ENTITY_KINDS_WITH_SPACE.contains(context.peekEntityKind()) == false)
        {
            throw new IllegalArgumentException("Space criteria can be used only in experiment and sample criteria, "
                    + "but was used in: " + context.peekEntityKind() + " context.");
        }
        return super.doTranslate(context, criteria);
    }

    @Override
    protected DetailedSearchField doTranslateSearchField(SearchTranslationContext context, ISearchCriteria criteria, ISearchCriteria subCriteria)
    {
        EntityKind entityKind = context.peekEntityKind();

        if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            return DetailedSearchField.createAttributeField(ExperimentAttributeSearchFieldKind.PROJECT_SPACE);
        } else if (EntityKind.SAMPLE.equals(entityKind))
        {
            return DetailedSearchField.createAttributeField(SampleAttributeSearchFieldKind.SPACE);
        } else
        {
            throw new IllegalArgumentException("Unknown entity kind: " + entityKind);
        }
    }

}
