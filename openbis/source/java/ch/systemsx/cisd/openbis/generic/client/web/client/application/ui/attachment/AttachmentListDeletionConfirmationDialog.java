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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.attachment;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.shared.basic.IAttachmentHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentVersions;

public final class AttachmentListDeletionConfirmationDialog extends
        AbstractDataListDeletionConfirmationDialog<AttachmentVersions>
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final AbstractAsyncCallback<Void> callback;

    private final IAttachmentHolder attachmentHolder;

    public AttachmentListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext,
            List<AttachmentVersions> attachments, AbstractAsyncCallback<Void> callback,
            IAttachmentHolder attachmentHolder)
    {
        super(viewContext, attachments);
        this.viewContext = viewContext;
        this.callback = callback;
        this.attachmentHolder = attachmentHolder;
    }

    @Override
    protected void executeConfirmedAction()
    {
        viewContext.getCommonService().deleteAttachments(TechId.create(attachmentHolder),
                attachmentHolder.getAttachmentHolderKind(), getAttachmentFileNames(data),
                reason.getValue(), callback);
    }

    @Override
    protected String getEntityName()
    {
        return messageProvider.getMessage(Dict.ATTACHMENT);
    }

    private List<String> getAttachmentFileNames(List<AttachmentVersions> attachmentVersions)
    {
        List<String> fileNames = new ArrayList<String>();
        for (AttachmentVersions attachmentVersion : attachmentVersions)
        {
            fileNames.add(attachmentVersion.getCurrent().getFileName());
        }
        return fileNames;
    }

}
