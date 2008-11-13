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

package ch.systemsx.cisd.openbis.generic.shared.dto.properties;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.collections.ListSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.ISimpleEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;

/**
 * A static helper class which contains helpful methods around {@link PropertyTypePE},
 * {@link ISimpleEntityPropertiesHolder}.
 * 
 * @author Tomasz Pylak
 */
public final class PropertiesHelper
{

    private PropertiesHelper()
    {
        // Can not be instantiated.
    }

    /**
     * Returns a description of given <var>propertyType</var>.
     */
    public static final String getTypeDescription(final PropertyTypePE propertyType)
    {
        assert propertyType != null : "Unspecified property type";
        final DataTypePE dataType = propertyType.getType();
        String type = dataType != null ? dataType.getCode().name() : getSimpleType(propertyType);
        final String vocabularyCode = getVocabularyCode(propertyType);
        if (vocabularyCode != null)
        {
            type = type + "(" + vocabularyCode + ")";
        }
        return type;
    }

    public static final String getSimpleType(final PropertyTypePE propertyType)
    {
        return propertyType.getType() == null ? null : propertyType.getType().getCode().name();
    }

    public static String getVocabularyCode(final PropertyTypePE propertyType)
    {
        final VocabularyPE vocabulary = propertyType.getVocabulary();
        if (vocabulary == null || StringUtils.isEmpty(vocabulary.getCode()))
        {
            return null;
        } else
        {
            return vocabulary.getCode();
        }
    }

    public final static <T extends EntityPropertyPE> ListSet createPropertiesListSet(
            final IEntityPropertiesHolder<T>[] entities)
    {
        final ListSet table = new ListSet();
        for (int rowIndex = 0; rowIndex < entities.length; rowIndex++)
        {
            final Set<T> properties = entities[rowIndex].getProperties();
            assert properties != null : "Unspecified properties";
            for (final EntityPropertyPE property : properties)
            {
                final PropertyTypePE propertyType =
                        property.getEntityTypePropertyType().getPropertyType();
                table.addToList(propertyType.getCode(), propertyType.getLabel(), property
                        .tryGetUntypedValue(), rowIndex);
            }
        }
        return table;
    }
}
