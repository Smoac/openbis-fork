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
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * Super class for entity (sample, material, experiment, data set) registration and edition.
 * 
 * @author Izabela Adamczyk
 */
public abstract class AbstractGenericEntityRegistrationForm<T extends EntityType, S extends EntityTypePropertyType<T>, P extends EntityProperty<T, S>>
        extends AbstractRegistrationForm implements IDatabaseModificationObserver
{
    public static final String ID_PREFIX = GenericConstants.ID_PREFIX;

    public static final String ID_SUFFIX_CODE = "code";

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    protected final EntityKind entityKind;

    protected final IViewContext<IGenericClientServiceAsync> viewContext;

    protected final IIdAndIdentifierHolder identifiableOrNull;

    protected CodeFieldWithGenerator codeField;

    protected PropertiesEditor<T, S, P> propertiesEditor;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------
    /**
     * For editing chosen entity.
     */
    protected AbstractGenericEntityRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            IIdAndIdentifierHolder identifiable, EntityKind entityKind)
    {
        super(viewContext, createId(identifiable, entityKind), DEFAULT_LABEL_WIDTH + 20,
                DEFAULT_FIELD_WIDTH);
        this.viewContext = viewContext;
        this.identifiableOrNull = identifiable;
        this.entityKind = entityKind;
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
     * Creates unique id based on {@link #createSimpleId(IIdentifierHolder, EntityKind)} and
     * application specific ID prefix.
     */
    public static final String createId(IIdentifierHolder identifier, EntityKind entityKind)
    {
        return ID_PREFIX + createSimpleId(identifier, entityKind);
    }

    /**
     * Creates unique form id for given entity.
     */
    protected static final String createSimpleId(IIdentifierHolder identifier, EntityKind entityKind)
    {
        // TODO 2009-05-11, IA: use technical id
        String editOrRegister =
                (identifier == null) ? "register" : ("edit_" + identifier.getIdentifier());
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
        compositeObserver.addObservers(getAllFormFields());
        return compositeObserver;
    }

    // ---------------------------------------------------------------------------------------------
    // Common Fields
    // ---------------------------------------------------------------------------------------------

    private final void createCommonFormFields()
    {
        propertiesEditor =
                createPropertiesEditor(createId(identifiableOrNull, entityKind), viewContext
                        .getCommonViewContext());
        codeField =
                new CodeFieldWithGenerator(viewContext, viewContext.getMessage(Dict.CODE),
                        entityKind.name().substring(0, 1));
        codeField.setId(getId() + ID_SUFFIX_CODE);
        codeField.setEnabled(identifiableOrNull == null);
        codeField.setHideTrigger(identifiableOrNull != null);
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
     * Returns all form fields.
     */
    protected final List<DatabaseModificationAwareField<?>> getAllFormFields()
    {
        List<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        fields.add(DatabaseModificationAwareField.wrapUnaware(codeField));
        for (DatabaseModificationAwareField<?> specificField : getEntitySpecificFormFields())
        {
            fields.add(specificField);
        }
        for (DatabaseModificationAwareField<?> propertyField : propertiesEditor.getPropertyFields())
        {
            fields.add(propertyField);
        }
        return fields;
    }

    /**
     * Creates, initializes and adds the fields to the form. To be used by the subclasses.
     */
    protected void initGUI()
    {
        createCommonFormFields();
        createEntitySpecificFormFields();
        initializeFormFields();
        for (DatabaseModificationAwareField<?> field : getAllFormFields())
        {
            formPanel.add(field.get());
        }
        layout();
        setLoading(false);
    }

    /**
     * @see PropertiesEditor#extractProperties()
     */
    protected final List<P> extractProperties()
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
    abstract protected PropertiesEditor<T, S, P> createPropertiesEditor(String string,
            IViewContext<ICommonClientServiceAsync> context);

}
