/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link SampleTypePropertyType} &lt;---&gt; {@link SampleTypePropertyTypePE} translator.
 * 
 * @author Franz-Josef Elmer
 */
public final class SampleTypePropertyTypeTranslator
{
    private SampleTypePropertyTypeTranslator()
    {
        // Can not be instantiated.
    }

    public final static SampleTypePropertyType translate(
            final SampleTypePropertyTypePE sampleTypePropertyType)
    {
        final SampleTypePropertyType result = new SampleTypePropertyType();
        result.setDisplayed(sampleTypePropertyType.isDisplayed());
        result.setManagedInternally(sampleTypePropertyType.isManagedInternally());
        result.setMandatory(sampleTypePropertyType.isMandatory());
        result.setPropertyType(PropertyTypeTranslator.translate(sampleTypePropertyType
                .getPropertyType()));
        return result;
    }

    public final static List<SampleTypePropertyType> translate(
            final Set<SampleTypePropertyTypePE> sampleTypePropertyTypes, final SampleType sampleType)
    {
        if (HibernateUtils.isInitialized(sampleTypePropertyTypes) == false)
        {
            return DtoConverters.createUnmodifiableEmptyList();
        }
        final List<SampleTypePropertyType> result = new ArrayList<SampleTypePropertyType>();
        for (final SampleTypePropertyTypePE sampleTypePropertyType : sampleTypePropertyTypes)
        {
            final SampleTypePropertyType stpt = translate(sampleTypePropertyType);
            stpt.setEntityType(sampleType);
            result.add(stpt);
        }
        Collections.sort(result);
        return result;
    }

}
