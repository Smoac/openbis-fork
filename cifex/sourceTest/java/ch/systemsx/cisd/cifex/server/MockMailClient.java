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

package ch.systemsx.cisd.cifex.server;

import javax.activation.DataHandler;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;

final class MockMailClient implements IMailClient
{
    String subject;

    String content;

    EMailAddress[] recipients;

    @Override
    public void sendMessage(String subj, String contentText, String replyTo, From fromOrNull,
            String... emails) throws EnvironmentFailureException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendMessageWithAttachment(final String subj, final String contentText,
            final String filename, final DataHandler attachmentContent, final String replyToOrNull,
            final From fromOrNull, final String... emails) throws EnvironmentFailureException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendEmailMessage(String subj, String contentText, EMailAddress replyToOrNull,
            EMailAddress fromOrNull, EMailAddress... emails) throws EnvironmentFailureException
    {
        this.subject = subj;
        this.content = contentText;
        this.recipients = emails;
    }

    @Override
    public void sendEmailMessageWithAttachment(String subj, String contentText, String filename,
            DataHandler attachmentContent, EMailAddress replyToOrNull, EMailAddress fromOrNull,
            EMailAddress... emails) throws EnvironmentFailureException
    {
        this.subject = subj;
        this.content = contentText;
        this.recipients = emails;
    }

    @Override
    public void sendTestEmail()
    {
        this.subject = "test";
        this.content = "";
        this.recipients = new EMailAddress[]
            { new EMailAddress("test@localhost") };
    }
}