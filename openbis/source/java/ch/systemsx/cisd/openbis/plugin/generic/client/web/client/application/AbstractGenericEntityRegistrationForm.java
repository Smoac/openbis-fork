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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeFieldWithGenerator;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * Super class for entity (sample, material, experiment, data set) registration and edition.
 * 
 * @author Izabela Adamczyk
 */
public abstract class AbstractGenericEntityRegistrationForm<T extends EntityType, S extends EntityTypePropertyType<T>>
        extends AbstractRegistrationForm implements IDatabaseModificationObserver
{
    public static final String ID_PREFIX = GenericConstants.ID_PREFIX;

    public static final String ID_SUFFIX_CODE = "code";

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    protected final EntityKind entityKind;

    protected final IViewContext<IGenericClientServiceAsync> viewContext;

    protected final TechId techIdOrNull;

    protected CodeFieldWithGenerator codeField;

    protected PropertiesEditor<T, S> propertiesEditor;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------
    /**
     * For editing chosen entity.
     */
    protected AbstractGenericEntityRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            IIdentifiable identifiableOrNull, EntityKind entityKind)
    {
        super(viewContext, createId(identifiableOrNull, entityKind), DEFAULT_LABEL_WIDTH + 20,
                DEFAULT_FIELD_WIDTH);
        this.viewContext = viewContext;
        this.entityKind = entityKind;
        this.techIdOrNull = TechId.create(identifiableOrNull);
    }

    /**
     * For registering new entity.
     */
    protected AbstractGenericEntityRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext, EntityKind entityKind)
    {
        this(viewContext, null, entityKind);
    }

    // ---------------------------------------------------------------------------------------------
    // Main Part
    // ---------------------------------------------------------------------------------------------
    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        setLoading(true);
        loadForm();
    }

    // ---------------------------------------------------------------------------------------------
    // ID generation
    // ---------------------------------------------------------------------------------------------
    /**
     * Creates unique id based on {@link #createSimpleId(IIdentifiable, EntityKind)} and application
     * specific ID prefix.
     */
    public static final String createId(IIdentifiable identifiable, EntityKind entityKind)
    {
        return ID_PREFIX + createSimpleId(identifiable, entityKind);
    }

    /**
     * Creates unique id based on {@link #createSimpleId(TechId, EntityKind)} and application
     * specific ID prefix.
     */
    public static final String createId(TechId techId, EntityKind entityKind)
    {
        return ID_PREFIX + createSimpleId(techId, entityKind);
    }

    /**
     * Creates unique form id for given entity.
     */
    protected static final String createSimpleId(IIdentifiable identifiable, EntityKind entityKind)
    {
        return createSimpleId(TechId.create(identifiable), entityKind);
    }

    /**
     * Creates unique form id for given entity.
     */
    protected static final String createSimpleId(TechId techId, EntityKind entityKind)
    {
        String editOrRegister = (techId == null) ? "register" : ("edit_" + techId);
        return "generic-" + entityKind.name().toLowerCase() + "-" + editOrRegister + "_form";
    }

    // ---------------------------------------------------------------------------------------------
    // IDatabaseModificationObserver
    // ---------------------------------------------------------------------------------------------
    public final void update(Set<DatabaseModificationKind> observedModifications)
    {
        createDatabaseModificationObserver().update(observedModifications);
    }

    public final DatabaseModificationKind[] getRelevantModifications()
    {
        return createDatabaseModificationObserver().getRelevantModifications();
    }

    // the db modification observer is composed from all the fields' observers
    private IDatabaseModificationObserver createDatabaseModificationObserver()
    {
        CompositeDatabaseModificationObserver compositeObserver =
                new CompositeDatabaseModificationObserver();
        compositeObserver.addObservers(getFormFieldsWithoutPropertyFields());
        compositeObserver.addObservers(getFormPropertyFields());
        return compositeObserver;
    }

    // ---------------------------------------------------------------------------------------------
    // Common Fields
    // ---------------------------------------------------------------------------------------------

    private final void createCommonFormFields()
    {
        propertiesEditor =
                createPropertiesEditor(createId(techIdOrNull, entityKind), viewContext
                        .getCommonViewContext());
        codeField =
                new CodeFieldWithGenerator(viewContext, viewContext.getMessage(Dict.CODE),
                        entityKind.name().substring(0, 1));
        codeField.setId(getId() + ID_SUFFIX_CODE);
        codeField.setEnabled(techIdOrNull == null);
        codeField.setHideTrigger(techIdOrNull != null);
    }

    protected void updatePropertyFieldsOriginalValues()
    {
        for (DatabaseModificationAwareField<?> f : propertiesEditor.getPropertyFields())
        {
            updateFieldOriginalValue(f.get());
        }
    }

    protected <D> void updateFieldOriginalValue(Field<D> field)
    {
        field.updateOriginalValue(field.getValue());
    }

    /**
     * Returns form fields without fields for properties.
     */
    private final List<DatabaseModificationAwareField<?>> getFormFieldsWithoutPropertyFields()
    {
        List<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        fields.add(DatabaseModificationAwareField.wrapUnaware(codeField));
        for (DatabaseModificationAwareField<?> specificField : getEntitySpecificFormFields())
        {
            fields.add(specificField);
        }
        return fields;
    }

    /**
     * Returns all form fields for properties.
     */
    private final List<DatabaseModificationAwareField<?>> getFormPropertyFields()
    {
        return propertiesEditor.getPropertyFields();
    }

    /**
     * Creates, initializes and adds the fields to the form. To be used by the subclasses.
     */
    protected void initGUI()
    {
        createCommonFormFields();
        createEntitySpecificFormFields();
        initializeFormFields();
        addFormFieldsToPanel(formPanel);
        layout();
        setLoading(false);
    }

    /**
     * Adds previously created and initialized fields to the form.
     */
    protected void addFormFieldsToPanel(FormPanel panel)
    {
        for (DatabaseModificationAwareField<?> fieldHolder : getFormFieldsWithoutPropertyFields())
        {
            panel.add(fieldHolder.get());
        }
        propertiesEditor.addPropertyFieldsWithFieldsetToPanel(panel);
    }

    /**
     * @see PropertiesEditor#extractProperties()
     */
    protected final List<IEntityProperty> extractProperties()
    {
        return propertiesEditor.extractProperties();
    }

    // ---------------------------------------------------------------------------------------------
    // Abstract methods
    // ---------------------------------------------------------------------------------------------

    /**
     * Creates fields specific to given entity kind.
     */
    abstract protected void createEntitySpecificFormFields();

    /**
     * Returns previously created fields specific to given entity kind, to be added to the form.
     */
    abstract protected List<DatabaseModificationAwareField<?>> getEntitySpecificFormFields();

    /**
     * Initializes previously created form fields, before adding them to the form.
     */
    abstract protected void initializeFormFields();

    /**
     * Loads necessary data from the server and creates the form.
     */
    abstract protected void loadForm();

    /**
     * Returns the {@link PropertiesEditor} to be used for .
     */
    abstract protected PropertiesEditor<T, S> createPropertiesEditor(String string,
            IViewContext<ICommonClientServiceAsync> context);

}
