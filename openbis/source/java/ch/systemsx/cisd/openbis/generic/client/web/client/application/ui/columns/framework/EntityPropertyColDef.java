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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PropertyTypeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Column definition for a one entity property.
 * 
 * @author Tomasz Pylak
 */
public class EntityPropertyColDef<T extends IEntityPropertiesHolder> extends
        AbstractColumnDefinition<T> implements IsSerializable
{
    private static final int PROPERTY_COLUMN_WIDTH = 120;

    public static final String PROPERTY_PREFIX = "property-";

    private String identifierPrefix;

    private boolean isInternalNamespace;

    private String simpleCode;

    private PropertyType propertyType;

    // GWT only
    public EntityPropertyColDef()
    {
    }

    /**
     * @param propertyType the property type for which this column definition is created.
     * @param propertyTypesOrNull list of all properties which are displayed with this property. It
     *            is used to set a unique display name for the specified property.
     */
    public EntityPropertyColDef(PropertyType propertyType, boolean isDisplayedByDefault,
            List<PropertyType> propertyTypesOrNull)
    {
        this(propertyType, isDisplayedByDefault, PROPERTY_COLUMN_WIDTH, getDisplayName(
                propertyType, propertyTypesOrNull));
    }

    private static String getDisplayName(PropertyType propertyType,
            List<PropertyType> propertyTypesOrNull)
    {
        if (propertyTypesOrNull == null)
        {
            return null;
        } else
        {
            return PropertyTypeRenderer.getDisplayName(propertyType, propertyTypesOrNull);
        }
    }

    public EntityPropertyColDef(PropertyType propertyType, boolean isDisplayedByDefault, int width,
            String displayName)
    {
        this(propertyType.getSimpleCode(), isDisplayedByDefault, width, propertyType
                .isInternalNamespace(), displayName, PROPERTY_PREFIX, propertyType);
    }

    private EntityPropertyColDef(String propertyTypeCode, boolean isDisplayedByDefault, int width,
            boolean isInternalNamespace, String propertyTypeLabel, String identifierPrefix,
            PropertyType propertyType)
    {
        super(propertyTypeLabel, width, isDisplayedByDefault, false);
        this.isInternalNamespace = isInternalNamespace;
        this.simpleCode = propertyTypeCode;
        this.identifierPrefix = identifierPrefix;
        this.propertyType = propertyType;
    }

    @Override
    protected final String tryGetValue(T entity)
    {
        IEntityProperty property = tryGetProperty(entity);
        String valueAsString = property == null ? null : property.tryGetAsString();

        // remove error prefix for export
        if (StringUtils.isBlank(valueAsString) == false
                && StringEscapeUtils.unescapeHtml(valueAsString).startsWith(
                        BasicConstant.ERROR_PROPERTY_PREFIX))
        {
            valueAsString =
                    StringEscapeUtils.unescapeHtml(valueAsString).substring(
                            BasicConstant.ERROR_PROPERTY_PREFIX.length());
        }
        return valueAsString;
    }

    @Override
    public Comparable<?> tryGetComparableValue(GridRowModel<T> rowModel)
    {
        IEntityProperty property = tryGetProperty(rowModel.getOriginalObject());
        String valueAsString = property == null ? null : property.tryGetOriginalValue();
        // treat error message as null
        if (StringUtils.isBlank(valueAsString) == false)
        {
            if (StringEscapeUtils.unescapeHtml(valueAsString).startsWith(
                    BasicConstant.ERROR_PROPERTY_PREFIX))
            {
                valueAsString = null;
            }
        }
        DataTypeCode dataType = getDataTypeCode();
        switch (dataType)
        {
            case INTEGER:
                return valueAsString == null ? null : new Integer(valueAsString);
            case REAL:
                return valueAsString == null ? null : new Double(valueAsString);
            case XML:
                return valueAsString;
            default:
                return super.tryGetComparableValue(rowModel);
        }
    }

    @Override
    public boolean isNumeric()
    {
        DataTypeCode dataType = getDataTypeCode();
        switch (dataType)
        {
            case INTEGER:
            case REAL:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean isEditable()
    {
        // TODO better criteria: should be based on data type and whether it is dynamic/managed property
        return true;
    }

    public IEntityProperty tryGetProperty(T entity)
    {
        for (IEntityProperty prop : getProperties(entity))
        {
            if (isMatching(prop))
            {
                return prop;
            }
        }
        return null;
    }

    protected List<? extends IEntityProperty> getProperties(T entity)
    {
        return entity.getProperties();
    }

    private boolean isMatching(IEntityProperty prop)
    {
        final PropertyType propType = prop.getPropertyType();
        return propType.isInternalNamespace() == isInternalNamespace
                && propType.getSimpleCode().equals(simpleCode);
    }

    public String getIdentifier()
    {
        return identifierPrefix + (isInternalNamespace ? "INTERN" : "USER") + "-" + simpleCode;
    }

    public final DataTypeCode getDataTypeCode()
    {
        return propertyType.getDataType().getCode();
    }

}
