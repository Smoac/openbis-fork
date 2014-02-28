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

package ch.ethz.bsse.cisd.dsu.tracking.email;

import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;

/**
 * Simple encapsulation of {@link IMailClient#sendMessage(String, String, String, From, String...)} method parameters.
 * 
 * @author Piotr Buczek
 */
public class Email
{
    private final String subject;

    private final String content;

    private final EMailAddress replyToOrNull;

    private final EMailAddress fromOrNull;

    private final EMailAddress[] recipients;

    public Email(String subject, String content, EMailAddress replyToOrNull, EMailAddress fromOrNull,
            EMailAddress... recipients)
    {
        super();
        this.subject = subject;
        this.content = content;
        this.replyToOrNull = replyToOrNull;
        this.fromOrNull = fromOrNull;
        this.recipients = recipients;
    }

    public Email(String subject, String content, EMailAddress replyToOrNull, String fromOrNull,
            EMailAddress... recipients)
    {
        this(subject, content, replyToOrNull, new EMailAddress(fromOrNull), recipients);
    }

    public String getSubject()
    {
        return subject;
    }

    public String getContent()
    {
        return content;
    }

    public EMailAddress getReplyToOrNull()
    {
        return replyToOrNull;
    }

    public EMailAddress getFromOrNull()
    {
        return fromOrNull;
    }

    public EMailAddress[] getRecipients()
    {
        return recipients;
    }

}
