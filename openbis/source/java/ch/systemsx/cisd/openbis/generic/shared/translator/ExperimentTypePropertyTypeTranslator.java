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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePropertyTypePE;

/**
 * A {@link ExperimentTypePropertyType} &lt;---&gt; {@link ExperimentTypePropertyTypePE} translator.
 * 
 * @author Franz-Josef Elmer
 */
public final class ExperimentTypePropertyTypeTranslator
{
    private ExperimentTypePropertyTypeTranslator()
    {
        // Can not be instantiated.
    }

    static private class ExperimentTypePropertyTypeTranslatorHelper
            extends
            AbstractEntityTypePropertyTypeTranslator<ExperimentType, ExperimentTypePropertyType, ExperimentTypePropertyTypePE>
    {
        @Override
        void setSpecificFields(ExperimentTypePropertyType result,
                ExperimentTypePropertyTypePE etptPE)
        {
        }

        @Override
        ExperimentType translate(EntityTypePE entityTypePE)
        {
            return ExperimentTranslator.translate((ExperimentTypePE) entityTypePE);
        }

        @Override
        ExperimentTypePropertyType create()
        {
            return new ExperimentTypePropertyType();
        }
    }

    public static List<ExperimentTypePropertyType> translate(
            Set<ExperimentTypePropertyTypePE> experimentTypePropertyTypes, ExperimentType result)
    {
        return new ExperimentTypePropertyTypeTranslatorHelper().translate(
                experimentTypePropertyTypes, result);
    }

    public static List<ExperimentTypePropertyType> translate(
            Set<ExperimentTypePropertyTypePE> experimentTypePropertyTypes, PropertyType result)
    {
        return new ExperimentTypePropertyTypeTranslatorHelper().translate(
                experimentTypePropertyTypes, result);
    }

    public static ExperimentTypePropertyType translate(
            ExperimentTypePropertyTypePE entityTypePropertyType)
    {
        return new ExperimentTypePropertyTypeTranslatorHelper().translate(entityTypePropertyType);
    }
}
