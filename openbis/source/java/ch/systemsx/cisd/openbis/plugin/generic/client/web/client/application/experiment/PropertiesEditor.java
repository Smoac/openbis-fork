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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.form.Field;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.PropertyFieldFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;

/**
 * @author Izabela Adamczyk
 */
abstract public class PropertiesEditor<T extends EntityType, S extends EntityTypePropertyType<T>, P extends EntityProperty<T, S>>

{
    private static final String ETPT = "PROPERTY_TYPE";

    private final List<S> entityTypesPropertyTypes;

    private final List<Field<?>> propertyFields;

    private final String id;

    private final Map<String, String> initialProperties;

    abstract protected P createEntityProperty();

    /**
     * Requires initial values of properties.
     */
    protected PropertiesEditor(String id, final List<S> entityTypesPropertyTypes,
            final List<P> properties)
    {
        assert properties != null : "Undefined properties.";
        this.id = id;
        this.entityTypesPropertyTypes = entityTypesPropertyTypes;
        this.initialProperties = createInitialProperties(properties);
        this.propertyFields = createPropertyFields();
    }

    private List<Field<?>> createPropertyFields()
    {
        List<Field<?>> result = new ArrayList<Field<?>>();
        for (final S stpt : entityTypesPropertyTypes)
        {
            result.add(createPropertyField(stpt, initialProperties.get(stpt.getPropertyType()
                    .getCode())));
        }
        return result;
    }

    private Map<String, String> createInitialProperties(final List<P> properties)
    {
        Map<String, String> result = new HashMap<String, String>();
        for (P p : properties)
        {
            result.put(p.getEntityTypePropertyType().getPropertyType().getCode(), p.getValue());
        }
        return result;
    }

    /**
     * Does not require initial values of properties.
     */
    public PropertiesEditor(String id, final List<S> entityTypesPropertyTypes)
    {
        this(id, entityTypesPropertyTypes, new ArrayList<P>());
    }

    private final Field<?> createPropertyField(final S etpt, String value)
    {
        final Field<?> field;
        final boolean isMandatory = etpt.isMandatory();
        final String label = etpt.getPropertyType().getLabel();
        final String propertyTypeCode = etpt.getPropertyType().getCode();
        field =
                PropertyFieldFactory.createField(etpt.getPropertyType(), isMandatory, label,
                        createFormFieldId(getId(), propertyTypeCode), value, null);
        field.setData(ETPT, etpt);
        field.setTitle(propertyTypeCode);
        return field;
    }

    private String getId()
    {
        return id;
    }

    private final static String createFormFieldId(String idPrefix, final String propertyTypeCode)
    {
        return idPrefix + propertyTypeCode.toLowerCase().replace(".", "-").replace("_", "-");
    }

    /**
     * Returns a list of {@link EntityProperty} for property types with selected values.
     */
    public final List<P> extractProperties()
    {
        final List<P> properties = new ArrayList<P>();
        for (final Field<?> field : propertyFields)
        {
            if (field.getValue() != null
                    && PropertyFieldFactory.valueToString(field.getValue()) != null)
            {
                final S stpt = field.getData(ETPT);
                final P sampleProperty = createEntityProperty();
                sampleProperty.setValue(PropertyFieldFactory.valueToString(field.getValue()));
                sampleProperty.setEntityTypePropertyType(stpt);
                properties.add(sampleProperty);
            }
        }
        return properties;
    }

    /**
     * Returns a list of fields appropriate for entity type - property type assignments specific to
     * given {@link PropertiesEditor}.
     */
    public final List<Field<?>> getPropertyFields()
    {
        return propertyFields;
    }

}
