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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion;

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AsyncCallbackWithProgressBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityTypeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityWithDeletionInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;

public final class RevertDeletionConfirmationDialog extends
        AbstractDataConfirmationDialog<List<Deletion>>
{
    private static final int LABEL_WIDTH = 60;

    private static final int FIELD_WIDTH = 180;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final AsyncCallback<Void> callback;

    private final IEntityWithDeletionInformation deletedEntityOrNull;

    public RevertDeletionConfirmationDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            List<Deletion> deletions, AsyncCallback<Void> callback)
    {
        super(viewContext, deletions, viewContext
                .getMessage(Dict.REVERT_DELETIONS_CONFIRMATION_TITLE));
        this.viewContext = viewContext;
        this.callback = callback;
        this.deletedEntityOrNull = null;
    }

    public RevertDeletionConfirmationDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            Deletion deletion, AsyncCallback<Void> callback)
    {
        this(viewContext, Collections.singletonList(deletion), callback);
    }

    public RevertDeletionConfirmationDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            IEntityWithDeletionInformation deletedEntity, AsyncCallback<Void> callback)
    {
        super(viewContext, Collections.singletonList(deletedEntity.getDeletion()), viewContext
                .getMessage(Dict.REVERT_DELETIONS_CONFIRMATION_TITLE));
        this.viewContext = viewContext;
        this.callback = callback;
        this.deletedEntityOrNull = deletedEntity;
    }

    @Override
    protected void executeConfirmedAction()
    {
        viewContext.getCommonService().revertDeletions(
                TechId.createList(data),
                AsyncCallbackWithProgressBar.decorate(callback,
                        viewContext.getMessage(Dict.REVERT_DELETIONS_PROGRESS)));
    }

    @Override
    protected String createMessage()
    {
        if (deletedEntityOrNull != null)
        {
            String deletedEntity =
                    EntityTypeUtils.translatedEntityKindForUI(viewContext, deletedEntityOrNull.getEntityKind()) + " '"
                            + deletedEntityOrNull.getCode() + "'";
            Deletion deletion = deletedEntityOrNull.getDeletion();
            String deletedBy = deletion.getRegistrator().toString();
            String deletionDate =
                    DateRenderer.renderDate(deletion.getRegistrationDate(),
                            BasicConstant.DATE_WITHOUT_TIMEZONE_PATTERN);
            String deletionReason = StringEscapeUtils.unescapeHtml(deletion.getReason());
            return viewContext.getMessage(Dict.REVERT_ENTITY_DELETION_CONFIRMATION_MSG,
                    deletedEntity, deletedBy, deletionDate, deletionReason);
        } else
        {
            return viewContext.getMessage(Dict.REVERT_DELETIONS_CONFIRMATION_MSG, data.size());
        }
    }

    @Override
    protected void extendForm()
    {
        formPanel.setLabelWidth(LABEL_WIDTH);
        formPanel.setFieldWidth(FIELD_WIDTH);
    }

}
