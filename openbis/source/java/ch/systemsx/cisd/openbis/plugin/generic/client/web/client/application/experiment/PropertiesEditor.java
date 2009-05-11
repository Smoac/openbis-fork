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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
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

    private List<DatabaseModificationAwareField<?>> propertyFields;

    private final String id;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    abstract protected P createEntityProperty();

    /**
     * Requires initial values of properties.
     */
    protected PropertiesEditor(String id, IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.id = id;
        this.viewContext = viewContext;
    }

    public void initWithProperties(final List<S> entityTypesPropertyTypes, final List<P> properties)
    {
        assert properties != null : "Undefined properties.";
        assert propertyFields == null : "Already initialized.";
        this.propertyFields =
                createPropertyFields(entityTypesPropertyTypes, createInitialProperties(properties));

    }

    public void initWithoutProperties(final List<S> entityTypesPropertyTypes)
    {
        assert propertyFields == null : "Already initialized.";
        this.propertyFields =
                createPropertyFields(entityTypesPropertyTypes,
                        createInitialProperties(new ArrayList<P>()));

    }

    private List<DatabaseModificationAwareField<?>> createPropertyFields(
            List<S> entityTypesPropertyTypes, Map<String, String> initialProperties)
    {
        List<DatabaseModificationAwareField<?>> result =
                new ArrayList<DatabaseModificationAwareField<?>>();
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

    private final DatabaseModificationAwareField<?> createPropertyField(final S etpt, String value)
    {
        assert viewContext != null;
        final DatabaseModificationAwareField<?> field;
        final boolean isMandatory = etpt.isMandatory();
        final String label = etpt.getPropertyType().getLabel();
        final String propertyTypeCode = etpt.getPropertyType().getCode();
        field =
                PropertyFieldFactory.createField(etpt.getPropertyType(), isMandatory, label,
                        createFormFieldId(getId(), propertyTypeCode), value, viewContext);
        field.get().setData(ETPT, etpt);
        field.get().setTitle(propertyTypeCode);
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
        assert propertyFields != null : "Not initialized.";
        final List<P> properties = new ArrayList<P>();
        for (final DatabaseModificationAwareField<?> field : propertyFields)
        {
            Object value = field.get().getValue();
            if (value != null && PropertyFieldFactory.valueToString(value) != null)
            {
                final S stpt = field.get().getData(ETPT);
                final P sampleProperty = createEntityProperty();
                sampleProperty.setValue(PropertyFieldFactory.valueToString(value));
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
    public final List<DatabaseModificationAwareField<?>> getPropertyFields()
    {
        assert propertyFields != null : "Not initialized.";
        return propertyFields;
    }

}
