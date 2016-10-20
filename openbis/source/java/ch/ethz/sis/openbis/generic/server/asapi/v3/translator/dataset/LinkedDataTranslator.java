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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.LinkedData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.LinkedDataFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class LinkedDataTranslator extends AbstractCachingTranslator<Long, LinkedData, LinkedDataFetchOptions> implements
        ILinkedDataTranslator
{

    @Autowired
    private ILinkedDataBaseTranslator baseTranslator;

    @Autowired
    private ILinkedDataExternalDmsTranslator externalDmsTranslator;

    @Override
    protected LinkedData createObject(TranslationContext context, Long dataSetId, LinkedDataFetchOptions fetchOptions)
    {
        LinkedData linkedData = new LinkedData();
        linkedData.setFetchOptions(new LinkedDataFetchOptions());
        return linkedData;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> dataSetIds, LinkedDataFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(ILinkedDataBaseTranslator.class, baseTranslator.translate(context, dataSetIds, null));

        if (fetchOptions.hasExternalDms())
        {
            relations.put(ILinkedDataExternalDmsTranslator.class,
                    externalDmsTranslator.translate(context, dataSetIds, fetchOptions.withExternalDms()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long dataSetId, LinkedData result, Object objectRelations,
            LinkedDataFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        LinkedDataBaseRecord baseRecord = relations.get(ILinkedDataBaseTranslator.class, dataSetId);

        result.setExternalCode(baseRecord.externalCode);

        if (fetchOptions.hasExternalDms())
        {
            result.setExternalDms(relations.get(ILinkedDataExternalDmsTranslator.class, dataSetId));
            result.getFetchOptions().withExternalDmsUsing(fetchOptions.withExternalDms());
        }
    }

}
