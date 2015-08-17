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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.material.ISearchMaterialIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.sql.IMaterialSqlTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.MaterialSearchCriterion;

/**
 * @author pkupczyk
 */
@Component
public class SearchMaterialSqlMethodExecutor extends AbstractMethodExecutor implements ISearchMaterialMethodExecutor
{

    @Autowired
    private ISearchMaterialIdExecutor searchExecutor;

    @Autowired
    private IMaterialSqlTranslator translator;

    @Override
    public List<Material> search(final String sessionToken, final MaterialSearchCriterion criterion, final MaterialFetchOptions fetchOptions)
    {
        return executeInContext(sessionToken, new IMethodAction<List<Material>>()
            {
                @Override
                public List<Material> execute(IOperationContext context)
                {
                    List<Long> results = search(context, criterion, fetchOptions);
                    return translate(context, results, fetchOptions);
                }
            });
    }

    @SuppressWarnings("unchecked")
    private List<Long> search(IOperationContext context, MaterialSearchCriterion criterion, MaterialFetchOptions fetchOptions)
    {
        if (fetchOptions.getCacheMode() != null)
        {
            List<Long> ids = (List<Long>) context.getSession().getAttributes().get(getClass().getName() + "_ids");

            if (ids == null)
            {
                ids = searchExecutor.search(context, criterion);
                context.getSession().getAttributes().put(getClass().getName() + "_ids", ids);
            }

            return ids;
        } else
        {
            return searchExecutor.search(context, criterion);
        }
    }

    private List<Material> translate(IOperationContext context, List<Long> peList, MaterialFetchOptions fetchOptions)
    {
        if (peList == null || peList.isEmpty())
        {
            return Collections.emptyList();
        }

        TranslationContext translationContext = null;

        if (fetchOptions.getCacheMode() != null)
        {
            translationContext = (TranslationContext) context.getSession().getAttributes().get(getClass().getName() + "_context");
            if (translationContext == null)
            {
                translationContext = new TranslationContext(context.getSession());
                context.getSession().getAttributes().put(getClass().getName() + "_context", translationContext);
            }
        } else
        {
            translationContext = new TranslationContext(context.getSession());
        }

        Map<Long, Material> peToObjectMap = translator.translate(translationContext, peList, fetchOptions);
        return new ArrayList<Material>(peToObjectMap.values());
    }
}
