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

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IEmailBuilder;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IMailService;

import de.schlichtherle.io.FileInputStream;

/**
 * @author Piotr Buczek
 */
public class MailService implements IMailService
{
    static String DEFAULT_SUBJECT = "";

    static String DEFAULT_BODY_TEXT = "";

    private final IMailClient mailClient;

    private final String recipientAddressOrNull;

    private final String defaultSubject;

    private final String defaultBodyText;

    public MailService(IMailClient mailClient, String recipientAddressOrNull)
    {
        this(mailClient, recipientAddressOrNull, DEFAULT_SUBJECT, DEFAULT_BODY_TEXT);
    }

    public MailService(IMailClient mailClient, String recipientAddressOrNull,
            String defaultSubject, String defaultBodyText)
    {
        this.mailClient = mailClient;
        this.recipientAddressOrNull = recipientAddressOrNull;
        this.defaultSubject = defaultSubject;
        this.defaultBodyText = defaultBodyText;
    }

    public IEmailBuilder createEmailBuilder()
    {
        return new EmailBuilder(defaultSubject, defaultBodyText);
    }

    public void sendEmail(IEmailBuilder emailBuilder)
    {
        assert emailBuilder instanceof EmailBuilder;

        final EmailBuilder builder = (EmailBuilder) emailBuilder;
        String subject = builder.getSubject();
        String bodyText = builder.getBodyText();
        String attachmentFileNameOrNull = builder.tryGetAttachmentName();
        EMailAddress recipient = new EMailAddress(recipientAddressOrNull);
        try
        {
            if (attachmentFileNameOrNull == null)
            {
                trySendEmail(subject, bodyText, recipient);
            } else
            {
                String attachmentFilePathOrNull = builder.tryGetAttachmentFilePath();
                String attachmentTextOrNull = builder.tryGetAttachmentText();
                if (attachmentFilePathOrNull != null)
                {
                    trySendEmailWithFileAttachment(subject, bodyText, attachmentFileNameOrNull,
                            attachmentFilePathOrNull, recipient);
                } else if (attachmentTextOrNull != null)
                {
                    trySendEmailWithTextAttachment(subject, bodyText, attachmentFileNameOrNull,
                            attachmentTextOrNull, recipient);
                } else
                {
                    // in general it shouldn't happen unless the script put None as an argument
                    throw new IllegalStateException(
                            "Neither file path nor text of attachment was specified");
                }
            }
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private void trySendEmail(String subject, String bodyText, EMailAddress recipient)
    {
        mailClient.sendEmailMessage(subject, bodyText, null, null, recipient);
    }

    private void trySendEmailWithTextAttachment(String subject, String bodyText,
            String attachmentFileName, String attachmentText, EMailAddress recipient)
            throws IOException
    {
        DataSource dataSource = new ByteArrayDataSource(attachmentText, "text/plain");
        mailClient.sendEmailMessageWithAttachment(subject, bodyText, attachmentFileName,
                new DataHandler(dataSource), null, null, recipient);
    }

    private void trySendEmailWithFileAttachment(String subject, String bodyText,
            String attachmentFileName, String attachmentFilePath, EMailAddress recipient)
            throws FileNotFoundException, IOException
    {
        DataSource dataSource =
                new ByteArrayDataSource(new FileInputStream(attachmentFilePath), "text/plain");
        mailClient.sendEmailMessageWithAttachment(subject, bodyText, attachmentFileName,
                new DataHandler(dataSource), null, null, recipient);
    }
}
