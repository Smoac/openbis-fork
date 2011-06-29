/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IEmailBuilder;

/**
 * @author Piotr Buczek
 */
public class EmailBuilder implements IEmailBuilder
{
    private String subject;

    private String bodyText;

    private String attachmentNameOrNull;

    // it shouldn't be possible to set both attachmentFilePath and attachmentText

    private String attachmentFilePathOrNull;

    private String attachmentTextOrNull;

    public EmailBuilder(String defaultSubject, String defaultBodyText)
    {
        this.subject = defaultSubject;
        this.bodyText = defaultBodyText;
    }

    public IEmailBuilder withSubject(String aSubject)
    {
        this.subject = aSubject;
        return this;
    }

    public IEmailBuilder withBody(String aBodyText)
    {
        this.bodyText = aBodyText;
        return this;
    }

    public IEmailBuilder withAttachedFile(String filePath, String attachmentName)
    {
        if (attachmentTextOrNull != null)
        {
            throw new IllegalStateException("Attachment text was already set.");
        }
        this.attachmentFilePathOrNull = filePath;
        this.attachmentNameOrNull = attachmentName;
        return this;
    }

    public IEmailBuilder withAttachedText(String text, String attachmentName)
    {
        if (attachmentFilePathOrNull != null)
        {
            throw new IllegalStateException("Attachment file path was already set.");
        }
        this.attachmentTextOrNull = text;
        this.attachmentNameOrNull = attachmentName;
        return this;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getBodyText()
    {
        return bodyText;
    }

    public String tryGetAttachmentName()
    {
        return attachmentNameOrNull;
    }

    public String tryGetAttachmentFilePath()
    {
        return attachmentFilePathOrNull;
    }

    public String tryGetAttachmentText()
    {
        return attachmentTextOrNull;
    }

}
