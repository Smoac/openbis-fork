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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEditableEntity;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.EntityPropertyGrid;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * The <i>generic</i> entity edit form. Two modes are available: edit and check.
 * 
 * @author Izabela Adamczyk
 */
abstract public class AbstractGenericEntityEditForm<T extends EntityType, S extends EntityTypePropertyType<T>, P extends EntityProperty<T, S>, V extends IEditableEntity<T, S, P>>
        extends AbstractRegistrationForm
{

    private final PropertiesEditor<T, S, P> editor;

    private final EntityPropertyGrid<T, S, P> grid;

    private boolean editMode;

    protected final V entity;

    private final List<Widget> checkComponents;

    abstract protected List<Widget> getEntitySpecificDisplayComponents();

    abstract protected List<Field<?>> getEntitySpecificFormFields();

    abstract protected PropertiesEditor<T, S, P> createPropertiesEditor(
            List<S> entityTypesPropertyTypes, List<P> properties, String string);

    public AbstractGenericEntityEditForm(final IViewContext<?> viewContext, V entity,
            boolean editMode)
    {
        super(viewContext, createId(entity.getEntityKind(), entity.getIdentifier()));
        checkComponents = new ArrayList<Widget>();
        this.entity = entity;
        this.editMode = editMode;
        editor =
                createPropertiesEditor(entity.getEntityTypePropertyTypes(), entity.getProperties(),
                        createId(entity.getEntityKind(), entity.getIdentifier()));
        grid = new EntityPropertyGrid<T, S, P>(viewContext, entity.getProperties());
        for (Widget w : getEntitySpecificDisplayComponents())
        {
            checkComponents.add(w);
        }
        checkComponents.add(grid.getWidget());
        checkComponents.add(new Button(viewContext.getMessage(Dict.BUTTON_EDIT),
                new SelectionListener<ComponentEvent>()
                    {
                        @Override
                        public void componentSelected(ComponentEvent ce)
                        {
                            showEditor();
                        }
                    }));
        for (Widget w : checkComponents)
        {
            add(w);
        }
    }

    protected static String createId(EntityKind entityKind, String identifier)
    {
        return GenericConstants.ID_PREFIX + createSimpleId(entityKind, identifier);
    }

    protected static String createSimpleId(EntityKind entityKind, String identifier)
    {
        return "generic-" + entityKind.name().toLowerCase() + "-edit_form_" + identifier;
    }

    private final void addFormFields()
    {
        for (final Field<?> specificField : getEntitySpecificFormFields())
        {
            formPanel.add(specificField);
        }
        for (final Field<?> propertyField : editor.getPropertyFields())
        {
            formPanel.add(propertyField);
        }
    }

    protected List<P> extractProperties()
    {
        return editor.extractProperties();
    }

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        setEditMode(editMode);
        addFormFields();
    }

    private void setEditMode(boolean edit)
    {
        this.editMode = edit;
        formPanel.setVisible(edit);
        for (Widget w : checkComponents)
        {
            w.setVisible(edit == false);
        }
    }

    protected void showEditor()
    {
        setEditMode(true);
        infoBox.reset();
    }

    protected void showCheckPage()
    {
        updateState();
        setEditMode(false);
    }

    @SuppressWarnings("unchecked")
    protected void updateState()
    {
        for (Field f : editor.getPropertyFields())
        {
            f.updateOriginalValue(f.getValue());
        }
        entity.setProperties(editor.extractProperties());
        grid.setProperties(entity.getProperties());
    }
}
