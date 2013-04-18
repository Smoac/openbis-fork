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

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs.MODIFICATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.ASSIGNED_TO;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.DATA_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.IS_DYNAMIC;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.IS_MANAGED;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.IS_MANDATORY;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.IS_SHOWN_IN_EDITOR_VIEW;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.LABEL;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.ORDINAL;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.PROPERTY_TYPE_CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.SCRIPT;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.SECTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.SHOW_RAW_VALUE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.TYPE_OF;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Provider of instances of {@link EntityTypePropertyType}.
 * 
 * @author Franz-Josef Elmer
 */
public class EntityTypePropertyTypeProvider extends
        AbstractCommonTableModelProvider<EntityTypePropertyType<?>>
{

    public EntityTypePropertyTypeProvider(ICommonServer commonServer, String sessionToken)
    {
        super(commonServer, sessionToken);
    }

    @Override
    protected TypedTableModel<EntityTypePropertyType<?>> createTableModel()
    {
        List<EntityTypePropertyType<?>> entityTypePropertyTypes =
                commonServer.listEntityTypePropertyTypes(sessionToken);
        TypedTableModelBuilder<EntityTypePropertyType<?>> builder =
                new TypedTableModelBuilder<EntityTypePropertyType<?>>();
        builder.addColumn(PROPERTY_TYPE_CODE).withDefaultWidth(200);
        builder.addColumn(LABEL).hideByDefault();
        builder.addColumn(DESCRIPTION).hideByDefault();
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(300).hideByDefault();
        builder.addColumn(ASSIGNED_TO).withDefaultWidth(200);
        builder.addColumn(TYPE_OF);
        builder.addColumn(IS_MANDATORY);
        builder.addColumn(DATA_TYPE).withDefaultWidth(200);
        builder.addColumn(ORDINAL).withDefaultWidth(100).hideByDefault();
        builder.addColumn(SECTION);
        builder.addColumn(IS_DYNAMIC);
        builder.addColumn(IS_MANAGED);
        builder.addColumn(IS_SHOWN_IN_EDITOR_VIEW);
        builder.addColumn(SHOW_RAW_VALUE);
        builder.addColumn(SCRIPT);
        for (EntityTypePropertyType<?> etpt : entityTypePropertyTypes)
        {
            builder.addRow(etpt);
            PropertyType propertyType = etpt.getPropertyType();
            builder.column(PROPERTY_TYPE_CODE).addString(propertyType.getCode());
            builder.column(LABEL).addString(propertyType.getLabel());
            builder.column(DESCRIPTION).addString(propertyType.getDescription());
            builder.column(MODIFICATION_DATE).addDate(propertyType.getModificationDate());
            builder.column(ASSIGNED_TO).addString(etpt.getEntityType().getCode());
            builder.column(TYPE_OF).addString(etpt.getEntityKind().getDescription());
            builder.column(IS_MANDATORY).addString(SimpleYesNoRenderer.render(etpt.isMandatory()));
            builder.column(DATA_TYPE).addString(renderDataType(propertyType));
            builder.column(ORDINAL).addInteger(etpt.getOrdinal());
            builder.column(SECTION).addString(etpt.getSection());
            builder.column(IS_DYNAMIC).addString(SimpleYesNoRenderer.render(etpt.isDynamic()));
            builder.column(IS_MANAGED).addString(SimpleYesNoRenderer.render(etpt.isManaged()));
            builder.column(IS_SHOWN_IN_EDITOR_VIEW).addString(
                    SimpleYesNoRenderer.render(etpt.isShownInEditView()));
            builder.column(SHOW_RAW_VALUE).addString(
                    SimpleYesNoRenderer.render(etpt.getShowRawValue()));
            Script script = etpt.getScript();
            if (script != null)
            {
                builder.column(SCRIPT).addString(script.getName());
            }
        }
        return builder.getModel();
    }

    private static String renderDataType(PropertyType entity)
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
