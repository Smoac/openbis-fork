/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;

/**
 * @author Pawel Glyzewski
 */
public class PredeployedPluginSelectionWidget extends
        DropDownList<SimpleComboValue<String>, String>
{
    private final IViewContext<?> viewContext;

    private ScriptType scriptType = ScriptType.DYNAMIC_PROPERTY;

    public PredeployedPluginSelectionWidget(IViewContext<?> viewContext)
    {
        super(viewContext, "-predeployed-plugin-names", Dict.PREDEPLOYED_PLUGIN_NAME, "value",
                "plugin", "plugins");

        this.viewContext = viewContext;

        setAutoSelectFirst(false);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.EMPTY_ARRAY;
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<String>> callback)
    {
        viewContext.getCommonService().listPredeployedPlugins(scriptType,
                new ListPredeployedPluginsCallback(viewContext));
        callback.ignore();
    }

    @Override
    protected List<SimpleComboValue<String>> convertItems(List<String> pluginNames)
    {
        ArrayList<SimpleComboValue<String>> results = new ArrayList<SimpleComboValue<String>>();

        for (String pluginName : pluginNames)
        {
            results.add(new SimpleComboValue<String>(pluginName)
                {
                    private static final long serialVersionUID = 1L;
                });
        }

        return results;
    }

    public void updateScriptType(@SuppressWarnings("hiding")
    ScriptType scriptType)
    {
        if (scriptType != this.scriptType)
        {
            this.scriptType = scriptType;
            refreshStore();
            clearSelections();
        }
    }

    public void setSelectedValue(String value)
    {
        clearSelections();
        setSelection(convertItems(Collections.singletonList(value)));
    }

    public String tryGetSelectedValue()
    {
        SimpleComboValue<String> selection = getValue();
        if (selection != null)
        {
            return selection.getValue();
        }

        return null;
    }

    private class ListPredeployedPluginsCallback extends
            PredeployedPluginSelectionWidget.ListItemsCallback
    {

        protected ListPredeployedPluginsCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public void process(List<String> result)
        {
            super.process(result);
        }
    }
}
