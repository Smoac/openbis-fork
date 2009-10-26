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
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * Translates {@link MaterialTypePropertyTypePE} to {@link MaterialTypePropertyType}.
 * 
 * @author Izabela Adamczyk
 */
public class MaterialTypePropertyTypeTranslator
{

    static private class MaterialTypePropertyTypeTranslatorHelper
            extends
            AbstractEntityTypePropertyTypeTranslator<MaterialType, MaterialTypePropertyType, MaterialTypePropertyTypePE>
    {
        @Override
        MaterialType translate(EntityTypePE entityTypePE,
                Map<PropertyTypePE, PropertyType> cacheOrNull)
        {
            return MaterialTypeTranslator.translate((MaterialTypePE) entityTypePE, cacheOrNull);
        }

        @Override
        MaterialTypePropertyType create()
        {
            return new MaterialTypePropertyType();
        }
    }

    public static List<MaterialTypePropertyType> translate(
            Set<MaterialTypePropertyTypePE> materialTypePropertyTypes, PropertyType result,
            Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        return new MaterialTypePropertyTypeTranslatorHelper().translate(materialTypePropertyTypes,
                result, cacheOrNull);
    }

    public static MaterialTypePropertyType translate(
            MaterialTypePropertyTypePE entityTypePropertyType,
            Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        return new MaterialTypePropertyTypeTranslatorHelper().translate(entityTypePropertyType,
                cacheOrNull);
    }

    public static List<MaterialTypePropertyType> translate(
            Set<MaterialTypePropertyTypePE> materialTypePropertyTypes, MaterialType result,
            Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        return new MaterialTypePropertyTypeTranslatorHelper().translate(materialTypePropertyTypes,
                result, cacheOrNull);
    }

}
