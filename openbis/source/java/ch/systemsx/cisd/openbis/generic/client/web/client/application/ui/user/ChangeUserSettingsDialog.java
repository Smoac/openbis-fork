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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.user;

import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractSaveDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;

/**
 * {@link Window} containing form for changing logged user settings.
 * 
 * @author Piotr Buczek
 */
public class ChangeUserSettingsDialog extends AbstractSaveDialog
{
    public static final String DIALOG_ID =
            GenericConstants.ID_PREFIX + "change-user-settings-dialog";

    private final IViewContext<?> viewContext;

    private final CheckBoxField useWildcardSearchModeCheckbox;

    public ChangeUserSettingsDialog(final IViewContext<?> viewContext,
            final IDelegatedAction saveCallback)
    {
        super(viewContext, viewContext.getMessage(Dict.CHANGE_USER_SETTINGS_DIALOG_TITLE),
                saveCallback);
        this.viewContext = viewContext;

        useWildcardSearchModeCheckbox = createUseWildcardSearchModeField();
        addField(useWildcardSearchModeCheckbox);
    }

    private final CheckBoxField createUseWildcardSearchModeField()
    {
        CheckBoxField field =
                new CheckBoxField(viewContext.getMessage(Dict.USE_WILDCARD_SEARCH_MODE_LABEL), true);
        field.setValue(viewContext.getDisplaySettingsManager().isUseWildcardSearchMode());
        field
                .setTitle("Wildcard search treats '*' and '?' as wildcards.\n"
                        + "It requires adding a '*' at the beginning and the end of searched text for contains search.");
        return field;
    }

    @Override
    protected void save(AsyncCallback<Void> saveCallback)
    {
        boolean useWildcardSearchMode = extractUseWildcardSearchMode();
        viewContext.getDisplaySettingsManager().storeSearchMode(useWildcardSearchMode);
    }

    private boolean extractUseWildcardSearchMode()
    {
        return useWildcardSearchModeCheckbox.getValue();
    }
}
