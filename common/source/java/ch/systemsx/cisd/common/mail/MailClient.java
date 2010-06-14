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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

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

    private final static InternetAddress createInternetAddress(From internetAddressOrNull)
    {
        if (internetAddressOrNull == null)
        {
            return null;
        }
        return createInternetAddress(internetAddressOrNull.getValue());
    }
    
    private final static InternetAddress createInternetAddress(String internetAddressOrNull)
    {
        if (internetAddressOrNull == null)
        {
            return null;
        }
        try
        {
            return new InternetAddress(internetAddressOrNull);
        } catch (AddressException e)
        {
            operationLog.error("Could not parse address [" + internetAddressOrNull + "].", e);
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    private final static InternetAddress[] createInternetAddresses(EMailAddress[] addressesOrNull)
    {
        if (addressesOrNull == null)
        {
            return null;
        }
        final InternetAddress[] addresses = new InternetAddress[addressesOrNull.length];
        for (int i = 0; i < addresses.length; ++i)
        {
            addresses[i] = createInternetAddress(addressesOrNull[i]);
        }
        return addresses;
    }
    
    private final static InternetAddress[] createInternetAddresses(String[] addressesOrNull)
    {
        if (addressesOrNull == null)
        {
            return null;
        }
        final InternetAddress[] addresses = new InternetAddress[addressesOrNull.length];
        for (int i = 0; i < addresses.length; ++i)
        {
            addresses[i] = createInternetAddress(addressesOrNull[i]);
        }
        return addresses;
    }
    
    private final static InternetAddress createInternetAddress(EMailAddress addressOrNull)
    {
        if (addressOrNull == null || addressOrNull.tryGetEmailAddress() == null)
        {
            return null;
        }
        try
        {
            return new InternetAddress(addressOrNull.tryGetEmailAddress(), addressOrNull
                    .tryGetPersonalName());
        } catch (Exception e)
        {
            operationLog.error("Could not parse address [" + addressOrNull + "].", e);
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    /**
     * Sends a mail with given <var>subject</var> and <var>content</var> to given
     * <var>recipients</var>.
     * 
     * @param recipients list of recipients (of type <code>Message.RecipientType.TO</code>)
     */
    public final void sendMessage(final String subject, final String content,
            final String replyToOrNull, final From fromOrNull, final String... recipients)
            throws EnvironmentFailureException
    {
        IMessagePreparer messagePreparer = new IMessagePreparer()
            {
                public void prepareMessage(MimeMessage msg) throws MessagingException
                {
                    msg.setText(content);
                }
            };
        privateSendMessage(messagePreparer, subject, content, createInternetAddress(replyToOrNull),
                createInternetAddress(fromOrNull), createInternetAddresses(recipients));
    }

    public void sendEmailMessage(final String subject, final String content,
            final EMailAddress replyToOrNull, final EMailAddress fromOrNull,
            EMailAddress... recipients) throws EnvironmentFailureException
    {
        IMessagePreparer messagePreparer = new IMessagePreparer()
            {
                public void prepareMessage(MimeMessage msg) throws MessagingException
                {
                    msg.setText(content);
                }
            };
        privateSendMessage(messagePreparer, subject, content, createInternetAddress(replyToOrNull),
                createInternetAddress(fromOrNull), createInternetAddresses(recipients));
    }

    /**
     * Sends a mail with given <var>subject</var> and <var>content</var> to given
     * <var>recipients</var>, includig the given <var>attachment</var>
     * 
     * @param recipients list of recipients (of type <code>Message.RecipientType.TO</code>)
     */
    public final void sendMessageWithAttachment(final String subject, final String content,
            final String filename, final DataHandler attachmentContent, final String replyTo,
            final From fromOrNull, final String... recipients) throws EnvironmentFailureException
    {
        IMessagePreparer messagePreparer = new IMessagePreparer()
            {

                public void prepareMessage(MimeMessage msg) throws MessagingException
                {
                    // Create a MIME message with 2 parts: text + attachments
                    Multipart multipart = new MimeMultipart();

                    // Create the text
                    MimeBodyPart messageText = new MimeBodyPart();
                    messageText.setText(content);
                    multipart.addBodyPart(messageText);

                    // Create the attachment
                    MimeBodyPart messageAttachment = new MimeBodyPart();
                    messageAttachment.setDataHandler(attachmentContent);
                    messageAttachment.setFileName(filename);
                    multipart.addBodyPart(messageAttachment);

                    msg.setContent(multipart);
                }
            };
        privateSendMessage(messagePreparer, subject, content, createInternetAddress(replyTo),
                createInternetAddress(fromOrNull), createInternetAddresses(recipients));
    }

    public void sendEmailMessageWithAttachment(final String subject, final String content,
            final String filename, final DataHandler attachmentContent,
            final EMailAddress replyToOrNull, final EMailAddress fromOrNull,
            final EMailAddress... recipients) throws EnvironmentFailureException
    {
        IMessagePreparer messagePreparer = new IMessagePreparer()
            {

                public void prepareMessage(MimeMessage msg) throws MessagingException
                {
                    // Create a MIME message with 2 parts: text + attachments
                    Multipart multipart = new MimeMultipart();

                    // Create the text
                    MimeBodyPart messageText = new MimeBodyPart();
                    messageText.setText(content);
                    multipart.addBodyPart(messageText);

                    // Create the attachment
                    MimeBodyPart messageAttachment = new MimeBodyPart();
                    messageAttachment.setDataHandler(attachmentContent);
                    messageAttachment.setFileName(filename);
                    multipart.addBodyPart(messageAttachment);

                    msg.setContent(multipart);
                }
            };
        privateSendMessage(messagePreparer, subject, content, createInternetAddress(replyToOrNull),
                createInternetAddress(fromOrNull), createInternetAddresses(recipients));
    }

    /**
     * Sends a mail with given <var>subject</var> and <var>content</var> to given
     * <var>recipients</var>.
     * 
     * @param recipients list of recipients (of type <code>Message.RecipientType.TO</code>)
     */
    private final void privateSendMessage(IMessagePreparer messagePreparerOrNull, String subject,
            String content, InternetAddress replyTo, InternetAddress fromOrNull,
            InternetAddress[] recipients) throws EnvironmentFailureException
    {
        final InternetAddress fromPerMail =
                (fromOrNull != null) ? fromOrNull : createInternetAddress(from);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Sending message from '" + fromPerMail + "' to recipients '"
                    + Arrays.asList(recipients) + "'");
        }
        MimeMessage msg = new MimeMessage(createSession());
        try
        {
            msg.setFrom(fromPerMail);
            if (replyTo != null)
            {
                InternetAddress[] replyToAddress =
                    { replyTo };
                msg.setReplyTo(replyToAddress);
            }
            msg.addRecipients(Message.RecipientType.TO, recipients);
            msg.setSubject(subject, UNICODE_CHARSET);
            if (null != messagePreparerOrNull)
            {
                messagePreparerOrNull.prepareMessage(msg);
            }
            msg.setSentDate(new Date());
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
            // We don't have a real SMTP server
            writeMessageToFile(msg);
        } else
        {
            // We are dealing with a real SMTP server -- use the Transport
            Transport.send(msg);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeMessageToFile(MimeMessage msg) throws MessagingException
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
        final Enumeration<String> headers = msg.getAllHeaderLines();
        while (headers.hasMoreElements())
        {
            builder.append(headers.nextElement()).append('\n');
        }
        builder.append("Content:\n");
        try
        {
            Object content = msg.getContent();
            // If this is a mime message, handle the printing a bit differently
            if (content instanceof Multipart)
            {
                Multipart multipart = (Multipart) content;
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                multipart.writeTo(os);
                builder.append(os.toString());
            } else
            {
                builder.append(content);
            }
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        FileUtilities.writeToFile(file, builder.toString());
    }

    //
    // Authenticator
    //

    @Override
    protected final PasswordAuthentication getPasswordAuthentication()
    {
        return new PasswordAuthentication(smtpUsername, smtpPassword);
    }

    /**
     * Interface for closures that prepare and email messages.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static interface IMessagePreparer
    {
        void prepareMessage(MimeMessage msg) throws MessagingException;
    }

}
