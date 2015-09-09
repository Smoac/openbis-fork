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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.sql;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;

/**
 * @author pkupczyk
 */
@Component
public class SampleSqlTranslator extends AbstractCachingTranslator<Long, Sample, SampleFetchOptions> implements ISampleSqlTranslator
{

    @Autowired
    private ISampleBaseSqlTranslator baseTranslator;

    @Override
    protected Sample createObject(TranslationContext context, Long sampleId, SampleFetchOptions fetchOptions)
    {
        final Sample sample = new Sample();
        sample.setFetchOptions(new SampleFetchOptions());
        return sample;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> sampleIds, SampleFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(ISampleBaseSqlTranslator.class, baseTranslator.translate(context, sampleIds, null));

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long sampleId, Sample result, Object objectRelations, SampleFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        SampleBaseRecord baseRecord = relations.get(ISampleBaseSqlTranslator.class, sampleId);

        result.setPermId(new SamplePermId(baseRecord.permId));
        result.setCode(baseRecord.code);
        result.setIdentifier(new SampleIdentifier(baseRecord.spaceCode, baseRecord.code));
        result.setModificationDate(baseRecord.modificationDate);
        result.setRegistrationDate(baseRecord.registrationDate);
    }

}
