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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;

/**
 * Stores and manages {@link AttachmentFileUploadField} fields.
 * 
 * @author Piotr Buczek
 */
public class AttachmentsFileFieldManager extends FileFieldManager<AttachmentFileUploadField>
{

    public AttachmentsFileFieldManager(final String sessionKey, final int initialNumberOfFields,
            final IMessageProvider messageProvider)
    {
        super(sessionKey, initialNumberOfFields, messageProvider.getMessage(Dict.FILE),
                messageProvider);
    }

    @Override
    protected AttachmentFileUploadField createFileUploadField()
    {
        return new AttachmentFileUploadField(messageProvider);
    }

    public void addAttachmentFieldSetsToPanel(FormPanel panel)
    {
        boolean first = true;
        for (AttachmentFileUploadField attachmentField : this.getFields())
        {
            FieldSet fieldSet = attachmentField.getFieldSet();
            if (first)
            {
                fieldSet.add(createAddMoreAttachmentsLink(messageProvider
                        .getMessage(Dict.ADD_MORE_ATTACHMENTS)));
                first = false;
            } else
            {
                fieldSet.hide();
            }
            panel.add(fieldSet);
        }
    }

    public List<NewAttachment> extractAttachments()
    {
        final List<NewAttachment> result = new ArrayList<NewAttachment>();
        for (AttachmentFileUploadField field : getFields())
        {
            NewAttachment attachmentOrNull = field.tryExtractAttachment();
            if (attachmentOrNull != null)
            {
                result.add(attachmentOrNull);
            }
        }
        return result;
    }

    private LabelField createAddMoreAttachmentsLink(String label)
    {
        final LabelField result = new LabelField(LinkRenderer.renderAsLink(label));
        result.sinkEvents(Event.ONCLICK);
        result.addListener(Event.ONCLICK, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    for (AttachmentFileUploadField attachmentField : getFields())
                    {
                        attachmentField.getFieldSet().show();
                    }
                    result.hide();
                }
            });
        return result;
    }

}
