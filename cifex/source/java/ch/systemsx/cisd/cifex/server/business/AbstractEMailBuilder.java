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

package ch.systemsx.cisd.cifex.server.business;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.Template;

/**
 * Abstract super class of all CIFEX e-mail builder.
 * 
 * @author Franz-Josef Elmer
 */
abstract class AbstractEMailBuilder
{
    private static final String EMAIL_PROPERTIES_FILE_PATH = "etc/email.properties";

    protected final UserDTO registrator;

    protected String comment;

    protected String url;

    private String fullName;

    private final IMailClient mailClient;

    private final String email;

    protected String password;

    protected Map<String, String> emailDict = new HashMap<String, String>();

    protected AbstractEMailBuilder(final IMailClient mailClient, final UserDTO registrator,
            final String email)
    {
        assert mailClient != null : "Unspecified mail client.";
        assert registrator != null : "Unspecified registrator.";
        assert StringUtils.isNotEmpty(email) : "Empty email.";

        this.mailClient = mailClient;
        this.registrator = registrator;
        this.email = email;
    }

    private final static String encodeURLParam(final String value)
    {
        try
        {
            return URLEncoder.encode(value, "UTF-8");
        } catch (final UnsupportedEncodingException ex)
        {
            return value;
        }
    }

    /**
     * Sets the base URL used to creating links in the e-mail. Has to be called before e-mail will
     * be send.
     */
    public void setURL(final String url)
    {
        this.url = url;
    }

    /**
     * Sets an optional comment.
     */
    public void setComment(final String comment)
    {
        this.comment = comment;
    }

    /**
     * Sets the password which might be needed.
     */
    public void setPassword(final String password)
    {
        this.password = password;
    }

    /**
     * Sets the full name, to be used in the greeting (if available).
     */
    public void setFullName(final String fullName)
    {
        this.fullName = fullName;
    }

    /**
     * Sends the e-mail
     */
    public void sendEMail()
    {
        assert url != null : "Missing URL.";
        emailDict.clear();
        populateDict();
        mailClient.sendMessage("[CIFEX] " + createSubject(), createContent(),
                getLongRegistratorDescription(), new From(getLongRegistratorDescription()), email);
    }

    /**
     * Sub-classes should add their own key-value pairs to the {@link #emailDict} by implementing
     * {@link #addToDict(Properties, DateFormat)}.
     */
    protected void populateDict()
    {
        final Properties emailProps = PropertyUtils.loadProperties(EMAIL_PROPERTIES_FILE_PATH);
        final DateFormat dateFormat = new SimpleDateFormat(emailProps.getProperty("date-format"));
        emailDict.put("user-id", getUserCode());
        emailDict.put("login-link", createURL(Constants.USERCODE_PARAMETER, getUserCode()));
        if (password != null)
        {
            emailDict.put("password", password);
        }
        if (StringUtils.isBlank(fullName))
        {
            emailDict.put("user-name", getUserCode());
        } else
        {
            emailDict.put("user-name", fullName);
        }
        emailDict.put("uploader-name", getShortRegistratorDescription());
        emailDict.put("uploader-email", registrator.getEmail());
        if (comment != null)
        {
            emailDict.put("comment", comment);
        }
        if (tryGetExpirationDate() != null)
        {
            emailDict.put("expiration-date", dateFormat.format(tryGetExpirationDate()));
        }
        addToDict(emailProps, dateFormat);
        addPropertiesToDict(emailProps);
        // Optional properties: set to "-" if null
        if (emailDict.containsKey("password") == false)
        {
            emailDict.put("password", "-");
        }
        if (emailDict.containsKey("comment") == false)
        {
            emailDict.put("comment", "-");
        }
    }

    @SuppressWarnings("unchecked")
    private void addPropertiesToDict(final Properties emailProps)
    {
        for (final Enumeration<String> enumeration =
                (Enumeration<String>) emailProps.propertyNames(); enumeration.hasMoreElements(); /**/)
        {
            final String key = enumeration.nextElement();
            if (key.endsWith("-EMPTY"))
            {
                continue;
            }
            final String value = emailProps.getProperty(key).replace("<br>", "\n");
            final Template template = new Template(value);
            for (String placeholder : template.getPlaceholderNames())
            {
                if (emailDict.containsKey(placeholder))
                {
                    template.bind(placeholder, emailDict.get(placeholder));
                }
            }
            if (template.allVariablesAreBound())
            {
                emailDict.put(key, template.createText());
            } else
            {
                emailDict.put(key, emailProps.getProperty(key + "-EMPTY", ""));
            }
        }
    }

    protected final String getShortRegistratorDescription()
    {
        final String registratorFullName = registrator.getUserFullName();
        return StringUtils.isBlank(registratorFullName) ? registrator.getUserCode()
                : registratorFullName;
    }

    protected final String getLongRegistratorDescription()
    {
        return getShortRegistratorDescription() + " <" + registrator.getEmail() + ">";
    }

    protected final StringBuilder addURL(final StringBuilder builder, final Object... paramValues)
    {
        assert paramValues.length % 2 == 0;

        builder.append(url);
        if (url.endsWith("/") == false)
        {
            builder.append('/');
        }
        boolean firstParam = true;
        for (int i = 0; i < paramValues.length / 2; ++i)
        {
            if (firstParam)
            {
                builder.append("?");
            } else
            {
                builder.append("&");
            }
            builder.append(paramValues[2 * i].toString()).append("=").append(
                    encodeURLParam(paramValues[2 * i + 1].toString()));
            firstParam = false;
        }
        return builder;
    }

    protected final String createURL(final Object... paramValues)
    {
        return addURL(new StringBuilder(), paramValues).toString();
    }

    protected final String createContent(String templateFilename)
    {
        final Template template =
                new Template(FileUtilities.loadExactToString(new File(templateFilename)));
        for (String placeholder : template.getPlaceholderNames())
        {
            if (emailDict.containsKey(placeholder))
            {
                template.bind(placeholder, emailDict.get(placeholder));
            } else
            {
                template.bind(placeholder, "?{" + placeholder + "}");
            }
        }
        return template.createText();
    }

    /**
     * Can add additional key-value pairs to the {@link #emailDict}.
     */
    protected void addToDict(Properties emailProps, DateFormat dateFormat)
    {
        // Override in sub-classes if you need to add additional key-value paris to emailDict.
    }

    /**
     * Called by {@link #sendEMail()} to create the subject line.
     */
    protected abstract String createSubject();

    /**
     * Called by {@link #sendEMail()} to create the email body.
     */
    protected abstract String createContent();

    /**
     * Returns the user code of the user addressed.
     */
    protected abstract String getUserCode();

    /**
     * Returns the expiration date of either the user or the files or <code>null</code>, if it does
     * not expire.
     */
    protected abstract Date tryGetExpirationDate();

}
