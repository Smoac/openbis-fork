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
import java.util.Set;

import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * The <i>generic</i> entity registration form.
 * 
 * @author Izabela Adamczyk
 */
abstract public class AbstractGenericEntityRegistrationForm<T extends EntityType, S extends EntityTypePropertyType<T>, P extends EntityProperty<T, S>>
        extends AbstractRegistrationForm implements IDatabaseModificationObserver
{

    public static final String ID_SUFFIX_CODE = "code";

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    protected TextField<String> codeField;

    private PropertiesEditor<T, S, P> propertiesEditor;

    public AbstractGenericEntityRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            List<S> entityTypesPropertyTypes, EntityKind entityKind)
    {
        super(viewContext, createId(entityKind));
        this.viewContext = viewContext;
        propertiesEditor =
                createPropertiesEditor(entityTypesPropertyTypes, createId(entityKind), viewContext
                        .getCommonViewContext());
    }

    abstract protected List<DatabaseModificationAwareField<?>> getEntitySpecificFields();

    abstract protected void createEntitySpecificFields();

    abstract protected PropertiesEditor<T, S, P> createPropertiesEditor(
            List<S> entityTypesPropertyTypes, String string,
            IViewContext<ICommonClientServiceAsync> context);

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
    }

    private final List<DatabaseModificationAwareField<?>> getAllFields()
    {
        List<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        fields.add(DatabaseModificationAwareField.wrapUnaware(codeField));
        for (DatabaseModificationAwareField<?> specificField : getEntitySpecificFields())
        {
            fields.add(specificField);
        }
        for (DatabaseModificationAwareField<?> propertyField : propertiesEditor.getPropertyFields())
        {
            fields.add(propertyField);
        }
        return fields;
    }

    private final void addFormFields()
    {
        for (DatabaseModificationAwareField<?> field : getAllFields())
        {
            formPanel.add(field.get());
        }
    }

    protected final List<P> extractProperties()
    {
        return propertiesEditor.extractProperties();
    }

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        createFormFields();
        addFormFields();
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        createDatabaseModificationObserver().update(observedModifications);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return createDatabaseModificationObserver().getRelevantModifications();
    }

    // the db modification observer is composed from all the fields' observers
    private IDatabaseModificationObserver createDatabaseModificationObserver()
    {
        CompositeDatabaseModificationObserver compositeObserver =
                new CompositeDatabaseModificationObserver();
        compositeObserver.addObservers(getAllFields());
        return compositeObserver;
    }
}
