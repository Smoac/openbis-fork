/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeGridColumnIDs.DATA_SET_TYPES;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeGridColumnIDs.DATA_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeGridColumnIDs.DATA_TYPE_CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeGridColumnIDs.EXPERIMENT_TYPES;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeGridColumnIDs.LABEL;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeGridColumnIDs.MATERIAL_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeGridColumnIDs.MATERIAL_TYPES;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeGridColumnIDs.SAMPLE_TYPES;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeGridColumnIDs.VOCABULARY;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeGridColumnIDs.XML_SCHEMA;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeGridColumnIDs.XSLT;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class PropertyTypeProvider extends AbstractCommonTableModelProvider<PropertyType>
{

    public PropertyTypeProvider(ICommonServer commonServer, String sessionToken)
    {
        super(commonServer, sessionToken);
    }

    @Override
    protected TypedTableModel<PropertyType> createTableModel()
    {
        List<PropertyType> propertyTypes = commonServer.listPropertyTypes(sessionToken, true);
        TypedTableModelBuilder<PropertyType> builder = new TypedTableModelBuilder<PropertyType>();
        builder.addColumn(LABEL);
        builder.addColumn(CODE);
        builder.addColumn(DATA_TYPE).withDefaultWidth(200);
        builder.addColumn(DATA_TYPE_CODE).hideByDefault();
        builder.addColumn(VOCABULARY).hideByDefault();
        builder.addColumn(MATERIAL_TYPE).hideByDefault();
        builder.addColumn(XML_SCHEMA).hideByDefault();
        builder.addColumn(XSLT).hideByDefault();
        builder.addColumn(DESCRIPTION);
        builder.addColumn(SAMPLE_TYPES);
        builder.addColumn(EXPERIMENT_TYPES);
        builder.addColumn(MATERIAL_TYPES);
        builder.addColumn(DATA_SET_TYPES);
        for (PropertyType propertyType : propertyTypes)
        {
            builder.addRow(propertyType);
            builder.column(LABEL).addString(propertyType.getLabel());
            builder.column(CODE).addString(propertyType.getCode());
            builder.column(DATA_TYPE).addString(renderDataType(propertyType));
            builder.column(DATA_TYPE_CODE).addString(propertyType.getDataType().getCode().name());
            Vocabulary vocabulary = propertyType.getVocabulary();
            builder.column(VOCABULARY).addString(vocabulary != null ? vocabulary.getCode() : null);
            MaterialType materialType = propertyType.getMaterialType();
            builder.column(MATERIAL_TYPE).addString(materialType != null ? materialType.getCode() : null);
            builder.column(XML_SCHEMA).addString(propertyType.getSchema());
            builder.column(XSLT).addString(propertyType.getTransformation());
            builder.column(DESCRIPTION).addString(propertyType.getDescription());
            builder.column(SAMPLE_TYPES).addString(render(propertyType.getSampleTypePropertyTypes()));
            builder.column(EXPERIMENT_TYPES).addString(render(propertyType.getExperimentTypePropertyTypes()));
            builder.column(MATERIAL_TYPES).addString(render(propertyType.getMaterialTypePropertyTypes()));
            builder.column(DATA_SET_TYPES).addString(render(propertyType.getDataSetTypePropertyTypes()));
        }
        return builder.getModel();
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

}
