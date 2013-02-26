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

import com.extjs.gxt.ui.client.widget.Component;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;

/**
 * {@link AbstractScriptEditRegisterForm} extension for editing scripts.
 * 
 * @author Izabela Adamczyk
 */
public class ScriptEditForm extends AbstractScriptEditRegisterForm
{
    private Script originalScript;

    private TechId scriptId;

    protected ScriptEditForm(IViewContext<ICommonClientServiceAsync> viewContext, TechId scriptId)
    {
        super(viewContext, scriptId, null, null);
        setRevertButtonVisible(true);
        this.scriptId = scriptId;
    }

    @Override
    protected void saveScript()
    {
        Script script = getScript();
        viewContext.getService().updateScript(script, new ScriptEditCallback(viewContext));
    }

    @Override
    public Script getScript()
    {
        Script script = new Script();
        script.setId(scriptId.getId());
        script.setDescription(descriptionField.getValue());
        if (originalScript.getPluginType() == PluginType.JYTHON)
        {
            script.setScript(scriptField.getValue());
            script.setName(nameField.getValue());
        } else
        {
            script.setName(originalScript.getName());
        }
        return script;
    }

    @Override
    protected void setValues()
    {
        FieldUtil.setValueWithUnescaping(descriptionField, originalScript.getDescription());
        FieldUtil.setValueWithUnescaping(scriptField, originalScript.getScript());
        FieldUtil.setValueWithUnescaping(nameField, originalScript.getName());

        String entityKind =
                originalScript.getEntityKind() == null
                        || originalScript.getEntityKind().length != 1 ? GenericConstants.ALL_ENTITY_KINDS
                        : originalScript.getEntityKind()[0].name();
        entityKindField.setSimpleValue(entityKind);
        descriptionField.setEnabled(originalScript.getPluginType() == PluginType.JYTHON);
    }

    public void updateOriginalValues()
    {
        descriptionField.setOriginalValue(descriptionField.getValue());
        scriptField.setOriginalValue(scriptField.getValue());
        nameField.setOriginalValue(nameField.getValue());
        entityKindField.setOriginalValue(entityKindField.getValue());
    }

    @Override
    protected void loadForm()
    {
        viewContext.getService().getScriptInfo(scriptId, new ScriptInfoCallback(viewContext));
    }

    void setOriginalScript(Script script)
    {
        this.originalScript = script;
        onPluginOrScriptTypeChanged(script.getPluginType(), script.getScriptType());
        scriptExecution.update(script.getName(), script.getScript(), script.getPluginType());
    }

    private final class ScriptEditCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Void>
    {
        ScriptEditCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final Void result)
        {
            updateOriginalValues();
            super.process(result);
        }

        @Override
        protected String createSuccessfullRegistrationInfo(Void result)
        {
            return "Script <b>" + originalScript.getName() + "</b> successfully updated.";
        }
    }

    private final class ScriptInfoCallback extends AbstractAsyncCallback<Script>
    {

        private ScriptInfoCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final Script result)
        {
            setOriginalScript(result);
            initGUI();
        }
    }

    public static Component create(IViewContext<ICommonClientServiceAsync> viewContext,
            TechId scriptId)
    {
        return new ScriptEditForm(viewContext, scriptId);
    }

}
