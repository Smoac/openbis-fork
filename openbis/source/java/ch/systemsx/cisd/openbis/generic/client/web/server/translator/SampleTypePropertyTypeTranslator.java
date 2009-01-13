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

import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.shared.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.client.shared.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;

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

    static private class SampleTypePropertyTypeTranslatorHelper
            extends
            AbstractEntityTypePropertyTypeTranslator<SampleType, SampleTypePropertyType, SampleTypePropertyTypePE>
    {
        @Override
        void setSpecificFields(SampleTypePropertyType result, SampleTypePropertyTypePE etptPE)
        {
            result.setDisplayed(etptPE.isDisplayed());
        }

        @Override
        SampleType translate(EntityTypePE entityTypePE)
        {
            return SampleTypeTranslator.translate((SampleTypePE) entityTypePE);
        }

        @Override
        SampleTypePropertyType create()
        {
            return new SampleTypePropertyType();
        }

    }

    public static List<SampleTypePropertyType> translate(
            Set<SampleTypePropertyTypePE> sampleTypePropertyTypes, SampleType result)
    {
        return new SampleTypePropertyTypeTranslatorHelper().translate(sampleTypePropertyTypes,
                result);
    }

    public static SampleTypePropertyType translate(SampleTypePropertyTypePE entityTypePropertyType)
    {
        return new SampleTypePropertyTypeTranslatorHelper().translate(entityTypePropertyType);
    }

    public static List<SampleTypePropertyType> translate(
            Set<SampleTypePropertyTypePE> sampleTypePropertyTypes, PropertyType result)
    {
        return new SampleTypePropertyTypeTranslatorHelper().translate(sampleTypePropertyTypes,
                result);
    }

}
