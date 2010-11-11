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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link Material} &lt;---&gt; {@link MaterialPE} translator.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialTranslator
{

    private MaterialTranslator()
    {
        // Can not be instantiated.
    }

    public final static List<Material> translate(final List<MaterialPE> materials)
    {
        final List<Material> result = new ArrayList<Material>();
        for (final MaterialPE material : materials)
        {
            result.add(MaterialTranslator.translate(material));
        }
        return result;
    }

    public final static Material translate(final MaterialPE materialPE)
    {
        return translate(materialPE, true);
    }

    public final static Material translate(final MaterialPE materialPE, final boolean withProperties)
    {
        if (materialPE == null)
        {
            return null;
        }
        final Material result = new Material();
        result.setCode(materialPE.getCode());
        result.setId(HibernateUtils.getId(materialPE));
        result.setModificationDate(materialPE.getModificationDate());
        result.setMaterialType(MaterialTypeTranslator.translate(materialPE.getMaterialType(),
                new HashMap<PropertyTypePE, PropertyType>()));
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(materialPE
                .getDatabaseInstance()));
        result.setRegistrator(PersonTranslator.translate(materialPE.getRegistrator()));
        result.setRegistrationDate(materialPE.getRegistrationDate());
        if (withProperties)
        {
            setProperties(materialPE, result);
        }

        return result;
    }

    public final static Material translateWithoutEscaping(final MaterialPE materialPE,
            final boolean withProperties)
    {
        if (materialPE == null)
        {
            return null;
        }
        final Material result = new Material();
        result.setCode(materialPE.getCode());
        result.setId(HibernateUtils.getId(materialPE));
        result.setModificationDate(materialPE.getModificationDate());
        result.setMaterialType(MaterialTypeTranslator.translate(materialPE.getMaterialType(),
                new HashMap<PropertyTypePE, PropertyType>()));
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(materialPE
                .getDatabaseInstance()));
        result.setRegistrator(PersonTranslator.translate(materialPE.getRegistrator()));
        result.setRegistrationDate(materialPE.getRegistrationDate());
        if (withProperties)
        {
            setProperties(materialPE, result);
        }
        return result;
    }

    private static void setProperties(final MaterialPE materialPE, final Material result)
    {
        if (materialPE.isPropertiesInitialized())
        {
            result.setProperties(EntityPropertyTranslator.translate(materialPE.getProperties(),
                    new HashMap<PropertyTypePE, PropertyType>()));
        } else
        {
            result.setProperties(new ArrayList<IEntityProperty>());
        }
    }

}
