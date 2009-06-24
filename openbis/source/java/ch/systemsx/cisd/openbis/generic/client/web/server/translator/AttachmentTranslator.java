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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;

/**
 * A {@link Attachment} &lt;---&gt; {@link AttachmentPE} translator.
 * 
 * @author Tomasz Pylak
 */
public final class AttachmentTranslator
{

    private AttachmentTranslator()
    {
        // Can not be instantiated.
    }

    public final static Attachment translate(final AttachmentPE attachment)
    {
        if (attachment == null)
        {
            return null;
        }
        final Attachment result = new Attachment();
        result.setFileName(StringEscapeUtils.escapeHtml(attachment.getFileName()));
        result.setTitle(StringEscapeUtils.escapeHtml(attachment.getTitle()));
        result.setDescription(StringEscapeUtils.escapeHtml(attachment.getDescription()));
        result.setRegistrator(PersonTranslator.translate(attachment.getRegistrator()));
        result.setRegistrationDate(attachment.getRegistrationDate());
        result.setVersion(attachment.getVersion());
        return result;
    }

    public final static List<Attachment> translate(final Collection<AttachmentPE> attachments)
    {
        if (attachments == null)
        {
            return null;
        }
        final List<Attachment> result = new ArrayList<Attachment>();
        for (final AttachmentPE attachment : attachments)
        {
            result.add(translate(attachment));
        }
        return result;
    }

}
