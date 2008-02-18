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

package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.ui.AbstractLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.ui.EditUserWidget;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.application.ui.UserWidget;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * A <code>AbstractLayoutDialog</code> to edit an user.
 * <p>
 * This dialog window comes when the user is a <i>regular</i> one or an <i>administrator</i>.
 * </p>
 * 
 * @author Basil Neff
 */
public final class EditUserDialog extends AbstractLayoutDialog
{
    /** The User to edit. */
    private final User editUser;

    private final ModelBasedGrid userGrid;

    public EditUserDialog(final ViewContext context, final User user, final ModelBasedGrid userGrid)
    {
        super(context, context.getMessageResources().getEditUserDialogTitle(user.getUserCode()),
                UserWidget.TOTAL_WIDTH, 200);
        this.editUser = user;
        this.userGrid = userGrid;
        addContentPanel();
    }

    //
    // AbstractLayoutDialog
    //

    protected final Widget createContentWidget()
    {
        final EditUserWidget editUserWidget =
                new EditUserWidget(viewContext, viewContext.getModel().getUser().isAdmin(), editUser)
                    {

                        //
                        // EditUserWidget
                        //

                        protected final void finishEditing()
                        {
                            new UserGridRefresherCallback(context, userGrid).onSuccess(null);
                        }
                    };
        return editUserWidget;
    }
}
