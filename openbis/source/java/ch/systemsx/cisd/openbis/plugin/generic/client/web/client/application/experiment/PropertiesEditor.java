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
import java.util.Map.Entry;

import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PropertyTypeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.PropertyFieldFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;

/**
 * @author Izabela Adamczyk
 */
abstract public class PropertiesEditor<T extends EntityType, S extends EntityTypePropertyType<T>>

{
    private static final String ETPT = "PROPERTY_TYPE";

    private List<DatabaseModificationAwareField<?>> propertyFields;

    private final String id;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions;

    protected IEntityProperty createEntityProperty()
    {
        return new EntityProperty();
    }

    /**
     * Requires initial values of properties.
     */
    protected PropertiesEditor(String id,
            Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
            IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.id = id;
        this.inputWidgetDescriptions = inputWidgetDescriptions;
        this.viewContext = viewContext;
    }

    public void initWithProperties(final List<S> entityTypesPropertyTypes,
            final List<IEntityProperty> properties)
    {
        assert properties != null : "Undefined properties.";
        assert propertyFields == null : "Already initialized.";
        List<S> shownEtpts = getEtptsShownInEditView(entityTypesPropertyTypes);
        this.propertyFields = createPropertyFields(shownEtpts, createInitialProperties(properties));

    }

    public void initWithoutProperties(final List<S> entityTypesPropertyTypes)
    {
        assert propertyFields == null : "Already initialized.";
        List<S> shownEtpts = getEtptsShownInEditView(entityTypesPropertyTypes);
        this.propertyFields =
                createPropertyFields(shownEtpts,
                        createInitialProperties(new ArrayList<IEntityProperty>()));

    }

    private List<DatabaseModificationAwareField<?>> createPropertyFields(
            List<S> entityTypesPropertyTypes, Map<String, String> initialProperties)
    {
        List<DatabaseModificationAwareField<?>> result =
                new ArrayList<DatabaseModificationAwareField<?>>();
        List<PropertyType> propertyTypes = getPropertyTypes(entityTypesPropertyTypes);
        for (final S stpt : entityTypesPropertyTypes)
        {
            String value = initialProperties.get(stpt.getPropertyType().getCode());
            result.add(createPropertyField(stpt, value, propertyTypes));
        }
        return result;
    }

    private List<PropertyType> getPropertyTypes(List<S> entityTypesPropertyTypes)
    {
        List<PropertyType> propertyTypes = new ArrayList<PropertyType>();
        for (final S stpt : entityTypesPropertyTypes)
        {
            propertyTypes.add(stpt.getPropertyType());
        }
        return propertyTypes;
    }

    private Map<String, String> createInitialProperties(final List<IEntityProperty> properties)
    {
        Map<String, String> result = new HashMap<String, String>();
        for (IEntityProperty p : properties)
        {
            result.put(p.getPropertyType().getCode(),
                    StringEscapeUtils.unescapeHtml(p.tryGetOriginalValue()));
        }
        return result;
    }

    private final DatabaseModificationAwareField<?> createPropertyField(final S etpt, String value,
            List<PropertyType> propertyTypes)
    {
        assert viewContext != null;
        final DatabaseModificationAwareField<?> field;
        final boolean isMandatory = etpt.isMandatory();
        PropertyType propertyType = etpt.getPropertyType();
        final String label = PropertyTypeRenderer.getDisplayName(propertyType, propertyTypes);
        final String propertyTypeCode = propertyType.getCode();
        List<IManagedInputWidgetDescription> widgetDescriptions =
                inputWidgetDescriptions.get(propertyTypeCode);
        if (widgetDescriptions != null && widgetDescriptions.isEmpty() == false)
        {
            field = createManagedPropertySection(label, isMandatory, widgetDescriptions);
        } else
        {
            field =
                    PropertyFieldFactory.createField(propertyType, isMandatory, label,
                            createFormFieldId(getId(), propertyTypeCode), value, viewContext);
        }
        field.get().setData(ETPT, etpt);
        GWTUtils.setToolTip(field.get(), propertyTypeCode);
        // Hide any properties that are not to be shown in edit/update views (unless in debugging
        // mode)
        if (etpt.isShownInEditView() == false && isDebuggingModeEnabled() == false)
        {
            FieldUtil.setVisibility(false, field.get());
        }
        return field;
    }

    private DatabaseModificationAwareField<?> createManagedPropertySection(String label,
            boolean isMandatory, List<IManagedInputWidgetDescription> widgetDescriptions)
    {
        return DatabaseModificationAwareField.wrapUnaware(new ManagedPropertyField(viewContext,
                label, isMandatory, widgetDescriptions));
    }

    private boolean isDebuggingModeEnabled()
    {
        return viewContext.getDisplaySettingsManager().isDebuggingModeEnabled();
    }

    private String getId()
    {
        return id;
    }

    private final static String createFormFieldId(String idPrefix, final String propertyTypeCode)
    {
        return idPrefix + GWTUtils.escapeToFormId(propertyTypeCode);
    }

    /**
     * Returns a list of {@link IEntityProperty} for property types with selected values.
     */
    public final List<IEntityProperty> extractProperties()
    {
        assert propertyFields != null : "Not initialized.";
        final List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        for (final DatabaseModificationAwareField<?> field : propertyFields)
        {
            Object value = field.get().getValue();
            final S etpt = field.get().getData(ETPT); // null for section labels

            if (etpt != null)
            {
                final IEntityProperty entityProperty = createEntityProperty();
                PropertyType propertyType = etpt.getPropertyType();
                String valueAsString = PropertyFieldFactory.valueToString(value);
                if (inputWidgetDescriptions.get(propertyType.getCode()) != null)
                {
                    JSONArray jsonArray = new JSONArray();
                    @SuppressWarnings("unchecked")
                    List<Map<String, String>> rows = (List<Map<String, String>>) value;
                    for (int i = 0; i < rows.size(); i++)
                    {
                        Map<String, String> row = rows.get(i);
                        JSONObject jsonObject = new JSONObject();
                        for (Entry<String, String> entry : row.entrySet())
                        {
                            jsonObject.put(entry.getKey(), new JSONString(entry.getValue()));
                        }
                        jsonArray.set(i, jsonObject);
                    }
                    valueAsString =
                            BasicConstant.MANAGED_PROPERTY_JSON_PREFIX + jsonArray.toString();
                }
                entityProperty.setValue(valueAsString);
                entityProperty.setPropertyType(propertyType);
                properties.add(entityProperty);
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

    public void addPropertyFieldsWithFieldsetToPanel(FormPanel form)
    {
        String previousSection = null;
        FieldSet currentSectionFieldSet = null;
        for (final DatabaseModificationAwareField<?> field : propertyFields)
        {
            final S etpt = field.get().getData(ETPT);
            final String currentSection = etpt.getSection();

            // if there was a section before and current field doesn't fit to it
            // add previous section field set to the form
            if (previousSection != null && previousSection.equals(currentSection) == false)
            {
                form.add(currentSectionFieldSet);
            }
            // update current section (create new one if needed)
            if (currentSection != null)
            {
                if (currentSection.equals(previousSection) == false)
                {
                    currentSectionFieldSet = createSectionFieldSet(currentSection);
                }
            } else
            {
                currentSectionFieldSet = null;
            }
            // add current field to current field set if it exists or directly to form otherwise
            if (currentSectionFieldSet != null)
            {
                currentSectionFieldSet.add(field.get());
            } else
            {
                if (field.get() instanceof ManagedPropertyField)
                {
                    PropertyType propertyType = etpt.getPropertyType();
                    String label = propertyType.getLabel();
                    if (etpt.isMandatory())
                    {
                        label += " *";
                    }
                    FieldSet fieldSet = createSectionFieldSet(label);
                    fieldSet.add(((ManagedPropertyField) field.get()).getWidget());
                    form.add(fieldSet);
                } else
                {
                    form.add(field.get());
                }
            }

            previousSection = currentSection;
        }
        // add last section
        if (currentSectionFieldSet != null)
        {
            form.add(currentSectionFieldSet);
        }
    }

    private FieldSet createSectionFieldSet(String sectionName)
    {
        return new PropertiesSectionFieldSet(sectionName);
    }

    private static final class PropertiesSectionFieldSet extends FieldSet
    {

        public PropertiesSectionFieldSet(final String sectionName)
        {
            createForm(sectionName);
        }

        private void createForm(final String sectionName)
        {
            setHeading(sectionName);
            setLayout(createFormLayout());
            setWidth(AbstractRegistrationForm.SECTION_WIDTH);
        }

        private final FormLayout createFormLayout()
        {
            final FormLayout formLayout = new FormLayout();
            formLayout.setLabelWidth(AbstractRegistrationForm.SECTION_LABEL_WIDTH);
            formLayout.setDefaultWidth(AbstractRegistrationForm.SECTION_DEFAULT_FIELD_WIDTH);
            return formLayout;
        }
    }

    private List<S> getEtptsShownInEditView(List<S> allEntityTypesPropertyTypes)
    {
        ArrayList<S> result = new ArrayList<S>();
        for (S etpt : allEntityTypesPropertyTypes)
        {
            if (etpt.isShownInEditView())
            {
                result.add(etpt);
            }
        }
        return result;
    }

}
