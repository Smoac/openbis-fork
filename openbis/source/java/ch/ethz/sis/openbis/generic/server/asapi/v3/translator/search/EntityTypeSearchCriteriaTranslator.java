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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.EntityTypeSearchCriteria;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;

/**
 * @author pkupczyk
 */
public class EntityTypeSearchCriteriaTranslator extends AbstractFieldFromCompositeSearchCriteriaTranslator
{

    public EntityTypeSearchCriteriaTranslator(IDAOFactory daoFactory, IEntityAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof EntityTypeSearchCriteria;
    }

    @Override
    protected DetailedSearchField doTranslateSearchField(SearchTranslationContext context, ISearchCriteria criteria, ISearchCriteria subCriteria)
    {
        EntityKind entityKind = context.peekEntityKind();

        if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            return DetailedSearchField.createAttributeField(ExperimentAttributeSearchFieldKind.EXPERIMENT_TYPE);

        } else if (EntityKind.SAMPLE.equals(entityKind))
        {
            return DetailedSearchField.createAttributeField(SampleAttributeSearchFieldKind.SAMPLE_TYPE);
        } else if (EntityKind.DATA_SET.equals(entityKind))
        {
            return DetailedSearchField.createAttributeField(DataSetAttributeSearchFieldKind.DATA_SET_TYPE);
        } else if (EntityKind.MATERIAL.equals(entityKind))
        {
            return DetailedSearchField.createAttributeField(MaterialAttributeSearchFieldKind.MATERIAL_TYPE);
        } else
        {
            throw new IllegalArgumentException("Unknown entity kind: " + entityKind);
        }
    }

}
