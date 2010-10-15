/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.calculator.property;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;

/**
 * {@link IEntityAdaptor} implementation for {@link SamplePE}.
 * 
 * @author Piotr Buczek
 */
public class SampleAdaptor implements IEntityAdaptor
{
    private final Map<String, IEntityPropertyAdaptor> propertiesByCode =
            new HashMap<String, IEntityPropertyAdaptor>();

    private final String code;

    private final SamplePE samplePE;

    SampleAdaptor(SamplePE samplePE)
    {
        this.samplePE = samplePE;
        this.code = samplePE.getCode();
        for (SamplePropertyPE property : samplePE.getProperties())
        {
            final String propertyTypeCode =
                    property.getEntityTypePropertyType().getPropertyType().getCode();
            final String value;
            if (property.getMaterialValue() != null)
            {
                value = property.getMaterialValue().getCode();
            } else if (property.getVocabularyTerm() != null)
            {
                value = property.getVocabularyTerm().getCode();
            } else
            {
                value = property.getValue();
            }
            propertiesByCode.put(propertyTypeCode, new BasicPropertyAdaptor(propertyTypeCode,
                    value, property));
        }
    }

    public SamplePE getSamplePE()
    {
        return samplePE;
    }

    public String getCode()
    {
        return code;
    }

    public IEntityPropertyAdaptor getPropertyByCode(String propertyTypeCode)
    {
        return propertiesByCode.get(propertyTypeCode);
    }

    public String getPropertyValueByCode(String propertyTypeCode)
    {
        return propertiesByCode.get(propertyTypeCode).getValueAsString();
    }

    public Collection<IEntityPropertyAdaptor> getProperties()
    {
        return propertiesByCode.values();
    }

}
