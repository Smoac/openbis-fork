/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.mail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A small mail client that simplifies the sending of emails using of <i>JavaMail API</i>.
 * <p>
 * Just instantiate this class and use {@link #sendMessage(String, String, String, From, String[])}
 * to send the email via SMTP.
 * </p>
 * If the SMTP host starts with <code>file://</code> the mail is not send to a real SMTP server but
 * it is stored in a file in the directory specified by the relative path following this prefix.
 * 
 * @author Christian Ribeaud
 */
public final class MailClient extends Authenticator implements IMailClient
{

    private static final String FILE_PREFIX = "file://";

    /** This system property is not supported by the <i>JavaMail API</i> */
    public final static String MAIL_SMTP_PASSWORD = "mail.smtp.password";

    private static final String UNICODE_CHARSET = "utf-8";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, MailClient.class);

    private final String smtpUsername;

    private final String smtpPassword;

    private final String smtpHost;

    private final String from;

    public MailClient(final String from, final String smtpHost)
    {
        this(from, smtpHost, null, null);
    }

    public MailClient(Properties properties)
    {
        this(properties.getProperty(JavaMailProperties.MAIL_FROM), properties
                .getProperty(JavaMailProperties.MAIL_SMTP_HOST), properties
                .getProperty(JavaMailProperties.MAIL_SMTP_USER), properties
                .getProperty(MAIL_SMTP_PASSWORD));
    }

    public MailClient(final String from, final String smtpHost, final String smtpUsername,
            final String smtpPassword)
    {
        assert from != null;
        assert smtpHost != null;

        this.from = from;
        this.smtpHost = smtpHost;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

    private final Properties createProperties()
    {
        Properties properties = null;
        try
        {
            properties = new Properties(System.getProperties());
        } catch (SecurityException ex)
        {
            properties = new Properties();
        }
        if (smtpUsername != null)
        {
            properties.put(JavaMailProperties.MAIL_SMTP_USER, smtpUsername);
        }
        if (smtpHost != null)
        {
            properties.put(JavaMailProperties.MAIL_SMTP_HOST, smtpHost);
        }
        if (smtpPassword != null && smtpUsername != null)
        {
            properties.put(JavaMailProperties.MAIL_SMTP_AUTH, Boolean.TRUE.toString());
        }
        properties.put(JavaMailProperties.MAIL_DEBUG, operationLog.isDebugEnabled() ? Boolean.TRUE
                .toString() : Boolean.FALSE.toString());
        properties.put(JavaMailProperties.MAIL_TRANSPORT_PROTOCOL, "smtp");
        return properties;
    }

    private final Session createSession()
    {
        Properties properties = createProperties();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Creating mail session with following properties '" + properties
                    + "'.");
        }
        boolean mailSmtpAuth =
                Boolean.parseBoolean(properties.getProperty(JavaMailProperties.MAIL_SMTP_AUTH));
        Session session = Session.getInstance(properties, mailSmtpAuth ? this : null);
        session.setDebug(operationLog.isDebugEnabled());
        return session;
    }

    private final static InternetAddress createInternetAddress(String internetAddress)
    {
        try
        {
            return new InternetAddress(internetAddress);
        } catch (AddressException e)
        {
            operationLog.error("Could not parse address [" + internetAddress + "].", e);
            return null;
        }
    }

    /**
     * Sends a mail with given <var>subject</var> and <var>content</var> to given
     * <var>recipients</var>.
     * 
     * @param recipients list of recipients (of type <code>Message.RecipientType.TO</code>)
     */
    public final void sendMessage(String subject, String content, String replyTo, From fromOrNull,
            String... recipients) throws EnvironmentFailureException
    {
        String fromPerMail = fromOrNull != null ? fromOrNull.getValue() : from;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Sending message from '" + fromPerMail + "' to recipients '"
                    + Arrays.asList(recipients) + "'");
        }
        int len = recipients.length;
        InternetAddress[] internetAddresses = new InternetAddress[len];
        for (int i = 0; i < len; i++)
        {
            internetAddresses[i] = createInternetAddress(recipients[i]);
        }
        MimeMessage msg = new MimeMessage(createSession());
        try
        {
            msg.setFrom(createInternetAddress(fromPerMail));
            if (replyTo != null)
            {
                InternetAddress[] replyToAddress =
                    { createInternetAddress(replyTo) };
                msg.setReplyTo(replyToAddress);
            }
            msg.addRecipients(Message.RecipientType.TO, internetAddresses);
            msg.setSubject(subject, UNICODE_CHARSET);
            msg.setText(content, UNICODE_CHARSET);
            send(msg);
        } catch (MessagingException ex)
        {
            final StringBuilder b = new StringBuilder();
            b.append("Sending e-mail with subject '");
            b.append(subject);
            b.append("' to recipients ");
            b.append(Arrays.asList(recipients));
            b.append(" failed.");
            if (ex instanceof SendFailedException)
            {
                final Address[] invalidAddressesOrNull =
                        ((SendFailedException) ex).getInvalidAddresses();
                if (invalidAddressesOrNull != null && invalidAddressesOrNull.length > 0)
                {
                    b.append(" These email addresses are invalid:\n");
                    for (Address address : invalidAddressesOrNull)
                    {
                        b.append(address.toString());
                        b.append('\n');
                    }
                }
            }
            b.append("\nDetailed failure description:\n");
            b.append(ex.toString());
            throw new EnvironmentFailureException(b.toString(), ex);
        }
    }

    private void send(MimeMessage msg) throws MessagingException
    {
        if (smtpHost.startsWith(FILE_PREFIX))
        {
            File emailFolder = new File(smtpHost.substring(FILE_PREFIX.length()));
            if (emailFolder.exists())
            {
                if (emailFolder.isDirectory() == false)
                {
                    throw new EnvironmentFailureException(
                            "There exists already a file but not a folder with path '"
                                    + emailFolder.getAbsolutePath() + "'.");
                }
            } else
            {
                if (emailFolder.mkdirs() == false)
                {
                    throw new EnvironmentFailureException("Couldn't create email folder '"
                            + emailFolder.getAbsolutePath() + "'.");
                }
            }
            File file = FileUtilities.createNextNumberedFile(new File(emailFolder, "email"), null);
            StringBuilder builder = new StringBuilder();
            builder.append("Subj: ").append(msg.getSubject()).append('\n');
            builder.append("From: ").append(renderAddresses(msg.getFrom())).append('\n');
            builder.append("To:   ").append(renderAddresses(msg.getAllRecipients())).append('\n');
            builder.append("Reply-To: ").append(renderAddresses(msg.getReplyTo())).append('\n');
            builder.append("Content:\n");
            try
            {
                Object content = msg.getContent();
                builder.append(content);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
            FileUtilities.writeToFile(file, builder.toString());
        } else
        {
            Transport.send(msg);
        }
    }

    private String renderAddresses(Address[] addresses)
    {
        StringBuilder builder = new StringBuilder();
        if (addresses != null)
        {
            for (int i = 0; i < addresses.length; i++)
            {
                builder.append(addresses[i]);
                if (i < addresses.length - 1)
                {
                    builder.append(", ");
                }
            }
        }
        return builder.toString();
    }

    //
    // Authenticator
    //

    @Override
    protected final PasswordAuthentication getPasswordAuthentication()
    {
        return new PasswordAuthentication(smtpUsername, smtpPassword);
    }
}
