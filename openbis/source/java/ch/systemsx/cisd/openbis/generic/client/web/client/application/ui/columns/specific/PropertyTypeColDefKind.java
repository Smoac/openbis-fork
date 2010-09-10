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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * @author Tomasz Pylak
 */
public enum PropertyTypeColDefKind implements IColumnDefinitionKind<PropertyType>
{
    LABEL(new AbstractColumnDefinitionKind<PropertyType>(Dict.LABEL)
        {
            @Override
            public String tryGetValue(PropertyType entity)
            {
                return entity.getLabel();
            }
        }),

    CODE(new AbstractColumnDefinitionKind<PropertyType>(Dict.CODE)
        {
            @Override
            public String tryGetValue(PropertyType entity)
            {
                return entity.getCode();
            }
        }),

    DATA_TYPE(new AbstractColumnDefinitionKind<PropertyType>(Dict.DATA_TYPE, 200)
        {
            @Override
            public String tryGetValue(PropertyType entity)
            {
                return renderDataType(entity);
            }
        }),

    DATA_TYPE_CODE(new AbstractColumnDefinitionKind<PropertyType>(Dict.DATA_TYPE_CODE, true)
        {
            @Override
            public String tryGetValue(PropertyType entity)
            {
                return entity.getDataType().getCode().name();
            }
        }),

    VOCABULARY(new AbstractColumnDefinitionKind<PropertyType>(Dict.VOCABULARY, true)
        {
            @Override
            public String tryGetValue(PropertyType entity)
            {
                Vocabulary vocabulary = entity.getVocabulary();
                return vocabulary != null ? vocabulary.getCode() : null;
            }
        }),

    MATERIAL_TYPE(new AbstractColumnDefinitionKind<PropertyType>(Dict.MATERIAL_TYPE, true)
        {
            @Override
            public String tryGetValue(PropertyType entity)
            {
                MaterialType materialType = entity.getMaterialType();
                return materialType != null ? materialType.getCode() : null;
            }
        }),

    SCHEMA(new AbstractColumnDefinitionKind<PropertyType>(Dict.XML_SCHEMA, true)
        {
            @Override
            public String tryGetValue(PropertyType entity)
            {
                return entity.getSchema();
            }
        }),

    TRANSFORMATION(new AbstractColumnDefinitionKind<PropertyType>(Dict.XSLT, true)
        {
            @Override
            public String tryGetValue(PropertyType entity)
            {
                return entity.getTransformation();
            }
        }),

    DESCRIPTION(new AbstractColumnDefinitionKind<PropertyType>(Dict.DESCRIPTION)
        {
            @Override
            public String tryGetValue(PropertyType entity)
            {
                return entity.getDescription();
            }
        }),

    SAMPLE_TYPES(new AbstractColumnDefinitionKind<PropertyType>(Dict.SAMPLE_TYPES)
        {
            @Override
            public String tryGetValue(PropertyType entity)
            {
                return render(entity.getSampleTypePropertyTypes());
            }
        }),

    EXPERIMENT_TYPES(new AbstractColumnDefinitionKind<PropertyType>(Dict.EXPERIMENT_TYPES)
        {
            @Override
            public String tryGetValue(PropertyType entity)
            {
                return render(entity.getExperimentTypePropertyTypes());
            }
        }),

    MATERIAL_TYPES(new AbstractColumnDefinitionKind<PropertyType>(Dict.MATERIAL_TYPES)
        {
            @Override
            public String tryGetValue(PropertyType entity)
            {
                return render(entity.getMaterialTypePropertyTypes());
            }
        }),

    DATA_SET_TYPES(new AbstractColumnDefinitionKind<PropertyType>(Dict.DATA_SET_TYPES)
        {
            @Override
            public String tryGetValue(PropertyType entity)
            {
                return render(entity.getDataSetTypePropertyTypes());
            }
        });

    private final AbstractColumnDefinitionKind<PropertyType> columnDefinitionKind;

    private PropertyTypeColDefKind(AbstractColumnDefinitionKind<PropertyType> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<PropertyType> getDescriptor()
    {
        return columnDefinitionKind;
    }

    private static String render(List<? extends EntityTypePropertyType<?>> list)
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (EntityTypePropertyType<?> etpt : list)
        {
            if (first == false)
            {
                sb.append(", ");
            } else
            {
                first = false;
            }
            // TODO 2009-01-01, Tomasz Pylak: how a list should be exported as one column?
            sb.append(render(etpt));
        }
        return sb.toString();
    }

    private static String render(EntityTypePropertyType<?> etpt)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(etpt.getEntityType().getCode());
        if (etpt.isMandatory())
        {
            sb.append(" *");
        }
        return sb.toString();
    }

    private static String tryGetVocabularyCode(PropertyType entity)
    {
        Vocabulary vocabulary = entity.getVocabulary();
        return vocabulary != null ? vocabulary.getCode() : null;
    }

    private static String tryGetMaterialTypeCode(PropertyType entity)
    {
        MaterialType materialType = entity.getMaterialType();
        return materialType != null ? materialType.getCode() : null;
    }

    static String renderDataType(PropertyType entity)
    {
        DataTypeCode dataType = entity.getDataType().getCode();
        switch (dataType)
        {
            case BOOLEAN:
                return "True / False";
            case CONTROLLEDVOCABULARY:
                return "Vocabulary: " + tryGetVocabularyCode(entity);
            case INTEGER:
                return "Integer Number";
            case MATERIAL:
                String materialTypeCode = tryGetMaterialTypeCode(entity);
                if (materialTypeCode == null)
                {
                    return "Material of Any Type";
                } else
                {
                    return "Material of Type: " + materialTypeCode;
                }
            case REAL:
                return "Float Number";
            case TIMESTAMP:
                return "Date and Time";
            case VARCHAR:
                return "Text";
            default:
                return dataType.name();
        }
    }

}
