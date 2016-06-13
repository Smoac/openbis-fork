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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.StorageFormatSearchCriteria;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

/**
 * @author pkupczyk
 */
public class StorageFormatSearchCriteriaTranslator extends AbstractCompositeSearchCriteriaTranslator
{

    public StorageFormatSearchCriteriaTranslator(IDAOFactory daoFactory, IObjectAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof StorageFormatSearchCriteria;
    }

    @Override
    protected SearchCriteriaTranslationResult doTranslate(SearchTranslationContext context, ISearchCriteria criteria)
    {
        if (false == SearchObjectKind.PHYSICAL_DATA.equals(context.peekObjectKind()))
        {
            throw new IllegalArgumentException("Storage format criteria can be used only in physical data criteria, "
                    + "but was used in: " + context.peekObjectKind() + " context.");
        }

        context.pushObjectKind(SearchObjectKind.STORAGE_FORMAT);
        SearchCriteriaTranslationResult translationResult = super.doTranslate(context, criteria);
        context.popObjectKind();

        return translationResult;
    }

}
