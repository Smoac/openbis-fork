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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;

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
    static DataTypeCode getDataTypeCode(SamplePropertyPE property)
    {
        return translateDataTypeCode(property.getEntityTypePropertyType());
    }
    
    /**
     * Returns the {@link DataTypeCode} for the given <var>property</var>.
     */
    static DataTypeCode getDataTypeCode(MaterialPropertyPE property)
    {
        return translateDataTypeCode(property.getEntityTypePropertyType());
    }
    
    /**
     * Returns the {@link DataTypeCode} for the given <var>property</var>.
     */
    static DataTypeCode getDataTypeCode(ExperimentPropertyPE property)
    {
        return translateDataTypeCode(property.getEntityTypePropertyType());
    }
    
    /**
     * Returns the {@link DataTypeCode} for the given <var>property</var>.
     */
    static DataTypeCode getDataTypeCode(DataSetPropertyPE property)
    {
        return translateDataTypeCode(property.getEntityTypePropertyType());
    }
    
    /**
     * Creates an appropriate {@link IEntityProperty} for the given <var>dataTypeCode</var>.
     */
    static IEntityProperty createEntityProperty(DataTypeCode dataTypeCode)
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
