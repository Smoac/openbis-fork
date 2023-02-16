/*
 * Copyright ETH 2020 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectHolder;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleTranslator;

public abstract class SamplePropertyTranslator extends
        AbstractCachingTranslator<Long, ObjectHolder<Map<String, Sample>>, SampleFetchOptions> implements ISamplePropertyTranslator
{

    @Autowired
    private ISampleTranslator sampleTranslator;

    @Override
    protected ObjectHolder<Map<String, Sample>> createObject(TranslationContext context, Long objectId, SampleFetchOptions fetchOptions)
    {
        return new ObjectHolder<Map<String, Sample>>();
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> objectIds, SampleFetchOptions fetchOptions)
    {
        List<SamplePropertyRecord> records = loadSampleProperties(objectIds);

        Collection<Long> propertyValues = new HashSet<Long>();

        for (SamplePropertyRecord record : records)
        {
            propertyValues.add(record.propertyValue);
        }

        Map<Long, Sample> samples = sampleTranslator.translate(context, propertyValues, fetchOptions);
        Map<Long, Map<String, Sample>> sampleProperties = new HashMap<Long, Map<String, Sample>>();

        for (SamplePropertyRecord record : records)
        {
            Map<String, Sample> properties = sampleProperties.get(record.objectId);
            if (properties == null)
            {
                properties = new HashMap<String, Sample>();
                sampleProperties.put(record.objectId, properties);
            }
            Sample sample = samples.get(record.propertyValue);
            if (sample != null)
            {
                properties.put(record.propertyCode, sample);
            }
        }

        return sampleProperties;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void updateObject(TranslationContext context, Long objectId, ObjectHolder<Map<String, Sample>> result, Object relations,
            SampleFetchOptions fetchOptions)
    {
        Map<Long, Map<String, Sample>> sampleProperties = (Map<Long, Map<String, Sample>>) relations;
        Map<String, Sample> objectProperties = sampleProperties.get(objectId);

        if (objectProperties == null)
        {
            objectProperties = new HashMap<String, Sample>();
        }

        result.setObject(objectProperties);
    }

    protected abstract List<SamplePropertyRecord> loadSampleProperties(Collection<Long> objectIds);

}
