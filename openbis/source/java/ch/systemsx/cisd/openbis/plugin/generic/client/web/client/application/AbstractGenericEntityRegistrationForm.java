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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.PropertyFieldFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> entity registration form.
 * 
 * @author Izabela Adamczyk
 */
abstract public class AbstractGenericEntityRegistrationForm<T extends EntityType, S extends EntityTypePropertyType<T>, P extends EntityProperty<T, S>>
        extends AbstractRegistrationForm
{

    public static final String ID_SUFFIX_CODE = "code";

    private static final String ETPT = "PROPERTY_TYPE";

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private ArrayList<Field<?>> propertyFields;

    protected TextField<String> codeField;

    private final List<S> entityTypesPropertyTypes;

    public AbstractGenericEntityRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            List<S> entityTypesPropertyTypes, EntityKind entityKind)
    {
        super(viewContext, createId(entityKind));
        this.viewContext = viewContext;
        this.entityTypesPropertyTypes = entityTypesPropertyTypes;
    }

    abstract protected List<Field<?>> getEntitySpecificFields();

    abstract protected void createEntitySpecificFields();

    abstract protected P createEntityProperty();

    protected static String createId(EntityKind entityKind)
    {
        return GenericConstants.ID_PREFIX + createSimpleId(entityKind);
    }

    protected static String createSimpleId(EntityKind entityKind)
    {
        return "generic-" + entityKind.name().toLowerCase() + "-registration_form";
    }

    private final void createFormFields()
    {
        codeField = new CodeField(viewContext, viewContext.getMessage(Dict.CODE));
        codeField.setId(getId() + ID_SUFFIX_CODE);
        createEntitySpecificFields();
        propertyFields = new ArrayList<Field<?>>();
        for (final S stpt : entityTypesPropertyTypes)
        {
            propertyFields.add(createProperty(stpt));
        }
    }

    private final void addFormFields()
    {
        formPanel.add(codeField);
        for (final Field<?> specificField : getEntitySpecificFields())
        {
            formPanel.add(specificField);
        }
        for (final Field<?> propertyField : propertyFields)
        {
            formPanel.add(propertyField);
        }
    }

    private final Field<?> createProperty(final S etpt)
    {
        final Field<?> field;
        final boolean isMandatory = etpt.isMandatory();
        final String label = etpt.getPropertyType().getLabel();
        final String propertyTypeCode = etpt.getPropertyType().getCode();
        field =
                PropertyFieldFactory.createField(etpt.getPropertyType(), isMandatory, label,
                        createFormFieldId(getId(), propertyTypeCode));
        field.setData(ETPT, etpt);
        field.setTitle(propertyTypeCode);
        return field;
    }

    private final static String createFormFieldId(String idPrefix, final String propertyTypeCode)
    {
        return idPrefix + propertyTypeCode.toLowerCase().replace(".", "-").replace("_", "-");
    }

    protected final List<P> extractProperties()
    {
        final List<P> properties = new ArrayList<P>();
        for (final Field<?> field : propertyFields)
        {
            if (field.getValue() != null)
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

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        createFormFields();
        addFormFields();
    }
}
