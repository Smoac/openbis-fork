/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.script;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ScriptField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;

/**
 * {@link AbstractRegistrationForm} for registering and editing scripts.
 * 
 * @author Izabela Adamczyk
 */
abstract public class AbstractScriptEditRegisterForm extends AbstractRegistrationForm
{

    protected final IViewContext<ICommonClientServiceAsync> viewContext;

    protected final TextField<String> nameField;

    protected final DescriptionField descriptionField;

    protected final MultilineVarcharField scriptField;

    protected EntityKindSelectionWidget entityKindField;

    private ScriptExecutionFramework scriptExecution;

    abstract protected void saveScript();

    abstract protected void setValues();

    abstract protected Script getScript();

    protected AbstractScriptEditRegisterForm(
            final IViewContext<ICommonClientServiceAsync> viewContext, EntityKind entityKindOrNull)
    {
        this(viewContext, null, entityKindOrNull);
    }

    protected AbstractScriptEditRegisterForm(
            final IViewContext<ICommonClientServiceAsync> viewContext, TechId scriptIdOrNull,
            EntityKind entityKindOrNull)
    {
        super(viewContext, createId(scriptIdOrNull), DEFAULT_LABEL_WIDTH + 20, DEFAULT_FIELD_WIDTH);
        this.viewContext = viewContext;
        this.nameField = new VarcharField(viewContext.getMessage(Dict.NAME), true);
        this.entityKindField =
                new EntityKindSelectionWidget(viewContext, entityKindOrNull,
                        scriptIdOrNull == null, true);
        this.descriptionField = AbstractRegistrationDialog.createDescriptionField(viewContext);
        this.scriptField = createScriptField(viewContext);
        this.scriptExecution = new ScriptExecutionFramework(viewContext, asValidable(formPanel));
        scriptField.addListener(Events.Change, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    scriptExecution.update(scriptField.getValue());
                }
            });
    }

    private IValidable asValidable(final FormPanel panel)
    {
        return new IValidable()
            {

                public boolean isValid()
                {
                    return panel.isValid();
                }
            };
    }

    private static MultilineVarcharField createScriptField(
            IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final MultilineVarcharField field = new ScriptField(viewContext);
        field.treatTabKeyAsInput();
        field.setFireChangeEventOnSetValue(true);
        return field;
    }

    public static String createId(TechId id)
    {
        String editOrRegister = (id == null) ? "register" : ("edit_" + id);
        return GenericConstants.ID_PREFIX + "script-" + editOrRegister + "_form";
    }

    @Override
    protected void resetPanel()
    {
        super.resetPanel();
        nameField.reset();
    }

    private final void addFormFields()
    {
        formPanel.add(nameField);
        formPanel.add(entityKindField);
        formPanel.add(descriptionField);
        formPanel.add(scriptField);
        rightPanel.add(scriptExecution.getWidget());
        redefineSaveListeners();
    }

    void redefineSaveListeners()
    {
        saveButton.removeAllListeners();
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (formPanel.isValid())
                    {
                        saveScript();
                    }
                }
            });
    }

    @Override
    protected final void submitValidForm()
    {
    }

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        setLoading(true);
        loadForm();
    }

    protected abstract void loadForm();

    protected void initGUI()
    {
        addFormFields();
        setValues();
        setLoading(false);
        layout();
    }

}
