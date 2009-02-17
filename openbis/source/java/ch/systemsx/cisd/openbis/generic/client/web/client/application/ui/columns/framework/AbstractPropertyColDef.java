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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author Tomasz Pylak
 */
public abstract class AbstractPropertyColDef<T> extends AbstractColumnDefinition<T> implements
        IsSerializable
{
    protected abstract List<? extends EntityProperty<?, ?>> getProperties(T entity);

    private static final int PROPERTY_COLUMN_WIDTH = 120;

    private static final String PROPERTY_PREFIX = "property";

    private String identifierPrefix;

    private boolean isInternalNamespace;

    private String simpleCode;

    // GWT only
    public AbstractPropertyColDef()
    {
    }

    public AbstractPropertyColDef(PropertyType propertyType, boolean isDisplayedByDefault)
    {
        this(propertyType, isDisplayedByDefault, PROPERTY_COLUMN_WIDTH, propertyType.getLabel(), "");
    }

    public AbstractPropertyColDef(PropertyType propertyType, boolean isDisplayedByDefault,
            int width, String propertyTypeLabel, String identifierPrefix)
    {
        this(propertyType.getSimpleCode(), isDisplayedByDefault, width, propertyType
                .isInternalNamespace(), propertyTypeLabel, identifierPrefix + PROPERTY_PREFIX);
    }

    protected AbstractPropertyColDef(String propertyTypeCode, boolean isDisplayedByDefault,
            int width, boolean isInternalNamespace, String propertyTypeLabel,
            String identifierPrefix)
    {
        super(propertyTypeLabel, width, isDisplayedByDefault);
        this.isInternalNamespace = isInternalNamespace;
        this.simpleCode = propertyTypeCode;
        this.identifierPrefix = identifierPrefix;
    }

    @Override
    protected String tryGetValue(T entity)
    {
        return tryGetValue(getProperties(entity));
    }

    private String tryGetValue(List<? extends EntityProperty<?, ?>> properties)
    {
        for (EntityProperty<?, ?> prop : properties)
        {
            if (isMatching(prop))
            {
                return prop.getValue();
            }
        }
        return null;
    }

    private boolean isMatching(EntityProperty<?, ?> prop)
    {
        PropertyType propertyType = prop.getEntityTypePropertyType().getPropertyType();
        return propertyType.isInternalNamespace() == isInternalNamespace
                && propertyType.getSimpleCode().equals(simpleCode);
    }

    public String getIdentifier()
    {
        return identifierPrefix + isInternalNamespace + simpleCode;
    }
}