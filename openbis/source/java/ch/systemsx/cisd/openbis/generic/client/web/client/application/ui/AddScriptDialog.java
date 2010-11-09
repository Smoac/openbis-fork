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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ScriptField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;

/**
 * {@link Window} containing script registration form.
 * 
 * @author Izabela Adamczyk
 */
public class AddScriptDialog extends AbstractRegistrationDialog
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final TextField<String> nameField;

    private final DescriptionField descriptionField;

    private final MultilineVarcharField scriptField;

    private SimpleComboBox<String> entityKindField;

    public AddScriptDialog(final IViewContext<ICommonClientServiceAsync> viewContext,
            final IDelegatedAction postRegistrationCallback, EntityKind entityKindOrNull)
    {
        super(viewContext, viewContext.getMessage(Dict.ADD_SCRIPT_TITLE), postRegistrationCallback);
        this.viewContext = viewContext;
        this.nameField = new VarcharField(viewContext.getMessage(Dict.NAME), true);
        addField(nameField);

        this.entityKindField = createEntityKindOrAllField(entityKindOrNull);
        addField(entityKindField);

        this.descriptionField = createDescriptionField(viewContext);
        addField(descriptionField);

        this.scriptField = createScriptField(viewContext);
        new MultilineVarcharField(viewContext.getMessage(Dict.SCRIPT), true, 20);
        addField(scriptField);
    }

    private SimpleComboBox<String> createEntityKindOrAllField(EntityKind entityKindOrNull)
    {
        SimpleComboBox<String> options = new SimpleComboBox<String>();
        options.add(GenericConstants.ALL_ENTITY_KINDS);
        if (entityKindOrNull != null)
        {
            options.add(entityKindOrNull.name());
        } else
        {
            for (EntityKind val : EntityKind.values())
            {
                options.add(val.name());
            }
        }
        options.setFieldLabel(viewContext.getMessage(Dict.ENTITY_KIND));
        options.setTriggerAction(TriggerAction.ALL);
        options.setForceSelection(true);
        options.setEditable(false);
        options.setAllowBlank(false);
        FieldUtil.markAsMandatory(options);
        return options;
    }

    private static MultilineVarcharField createScriptField(
            IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final MultilineVarcharField field = new ScriptField(viewContext);
        field.treatTabKeyAsInput();
        return field;
    }

    @Override
    protected void register(AsyncCallback<Void> registrationCallback)
    {
        Script newScript = new Script();
        newScript.setDescription(descriptionField.getValue());
        newScript.setName(nameField.getValue());
        newScript.setScript(scriptField.getValue());
        EntityKind kind = null;
        String selectedEntityKind = entityKindField.getValue().getValue();
        if (selectedEntityKind.equals(GenericConstants.ALL_ENTITY_KINDS) == false)
        {
            kind = EntityKind.valueOf(selectedEntityKind);
        }
        newScript.setEntityKind(kind);
        viewContext.getService().registerScript(newScript, registrationCallback);
    }
}
