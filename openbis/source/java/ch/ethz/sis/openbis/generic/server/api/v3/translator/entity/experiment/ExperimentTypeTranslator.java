/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;

/**
 * @author pkupczyk
 */
@Component
public class ExperimentTypeTranslator extends AbstractCachingTranslator<ExperimentTypePE, ExperimentType, ExperimentTypeFetchOptions> implements
        IExperimentTypeTranslator
{

    @Override
    protected ExperimentType createObject(TranslationContext context, ExperimentTypePE type, ExperimentTypeFetchOptions fetchOptions)
    {
        ExperimentType result = new ExperimentType();
        result.setPermId(new EntityTypePermId(type.getCode()));
        result.setCode(type.getCode());
        result.setDescription(type.getDescription());
        result.setModificationDate(type.getModificationDate());
        result.setFetchOptions(new ExperimentTypeFetchOptions());
        return result;
    }

    @Override
    protected void updateObject(TranslationContext context, ExperimentTypePE input, ExperimentType output, Relations relations,
            ExperimentTypeFetchOptions fetchOptions)
    {
    }

}
