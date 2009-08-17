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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractSaveDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;

/**
 * {@link Window} containing form for changing logged user home group.
 * 
 * @author Piotr Buczek
 */
public class ChangeUserHomeGroupDialog extends AbstractSaveDialog
{
    public static final String DIALOG_ID =
            GenericConstants.ID_PREFIX + "change-user-home-group-dialog";

    public static final String GROUP_FIELD_ID = DIALOG_ID + "-group-field";

    private final IViewContext<?> viewContext;

    private final GroupSelectionWidget groupField;

    public ChangeUserHomeGroupDialog(final IViewContext<?> viewContext,
            final IDelegatedAction saveCallback)
    {
        super(viewContext, viewContext.getMessage(Dict.CHANGE_USER_HOME_GROUP_DIALOG_TITLE),
                saveCallback);
        this.viewContext = viewContext;

        groupField = createGroupField();
        addField(groupField);
    }

    private final GroupSelectionWidget createGroupField()
    {
        GroupSelectionWidget field = new GroupSelectionWidget(viewContext, GROUP_FIELD_ID, false);
        FieldUtil.setMandatoryFlag(field, false);
        field.setFieldLabel(viewContext.getMessage(Dict.GROUP));
        return field;
    }

    @Override
    protected void save(AsyncCallback<Void> saveCallback)
    {
        Group group = groupField.tryGetSelected();
        String groupCodeOrNull = group == null ? null : group.getCode();
        TechId groupIdOrNull = TechId.create(group);
        viewContext.getModel().getSessionContext().getUser().setHomeGroupCode(groupCodeOrNull);
        viewContext.getService().changeUserHomeGroup(groupIdOrNull, saveCallback);
    }

}
