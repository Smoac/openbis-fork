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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;

/**
 * @author Jakub Straszewski
 */
@Component
public class DataSetTypeTranslator extends AbstractCachingTranslator<DataSetTypePE, DataSetType, DataSetTypeFetchOptions> implements
        IDataSetTypeTranslator
{

    @Override
    protected DataSetType createObject(TranslationContext context, DataSetTypePE input, DataSetTypeFetchOptions fetchOptions)
    {
        final DataSetType dataSetType = new DataSetType();
        dataSetType.setCode(input.getCode());
        dataSetType.setPermId(new EntityTypePermId(input.getCode()));
        dataSetType.setKind(DataSetKind.valueOf(input.getDataSetKind()));
        dataSetType.setDescription(input.getDescription());
        dataSetType.setModificationDate(input.getModificationDate());

        return dataSetType;
    }

    @Override
    protected void updateObject(TranslationContext context, DataSetTypePE input, DataSetType output, Object relations,
            DataSetTypeFetchOptions fetchOptions)
    {
    }

}
