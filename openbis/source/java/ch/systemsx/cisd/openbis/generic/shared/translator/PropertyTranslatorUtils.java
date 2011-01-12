/*
 * Copyright 2009 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * Some utility methods for entity property translations.
 * 
 * @author Bernd Rinn
 */
final class PropertyTranslatorUtils
{

    private PropertyTranslatorUtils()
    {
        // cannot be instantiated.
    }

    private static DataTypeCode translateDataTypeCode(EntityTypePropertyTypePE etpt)
    {
        return DataTypeCode.valueOf(etpt.getPropertyType().getType().getCode().name());
    }

    /**
     * Returns the {@link DataTypeCode} for the given <var>property</var>.
     */
    static DataTypeCode getDataTypeCode(EntityPropertyPE property)
    {
        return translateDataTypeCode(property.getEntityTypePropertyType());
    }

    /**
     * Creates an appropriate {@link IEntityProperty} for the given <var>propertyPE</var> based on
     * its type.
     */
    static IEntityProperty createEntityProperty(EntityPropertyPE propertyPE)
    {
        final DataTypeCode typeCode = PropertyTranslatorUtils.getDataTypeCode(propertyPE);
        final IEntityProperty basicProperty = createEntityProperty(typeCode);
        if (propertyPE.getEntityTypePropertyType().isManaged())
        {
            return createManagedEntityProperty(propertyPE, basicProperty);
        } else
        {
            return basicProperty;
        }
    }

    /**
     * Creates a managed {@link IEntityProperty} wrapping given <var>basicProperty</var>.
     */
    private static IEntityProperty createManagedEntityProperty(EntityPropertyPE property,
            IEntityProperty basicProperty)
    {
        final ScriptPE script = property.getEntityTypePropertyType().getScript();
        assert script != null && script.getScriptType() == ScriptType.MANAGED_PROPERTY;
        final ManagedValueEntityProperty result = new ManagedValueEntityProperty(basicProperty);
        // TODO 2010-01-12, Piotr Buczek: fill managed property
        return result;
    }

    /**
     * Creates an appropriate {@link IEntityProperty} for the given <var>dataTypeCode</var>.
     */
    private static IEntityProperty createEntityProperty(DataTypeCode dataTypeCode)
    {
        switch (dataTypeCode)
        {
            case CONTROLLEDVOCABULARY:
                return new VocabularyTermValueEntityProperty();
            case MATERIAL:
                return new MaterialValueEntityProperty();
            default:
                return new GenericValueEntityProperty();
        }
    }

}
