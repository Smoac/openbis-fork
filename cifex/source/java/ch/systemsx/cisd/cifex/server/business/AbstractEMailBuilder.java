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

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.mail.IMailClient;

/**
 * Abstract super class of all CIFEX e-mail builder.
 *
 * @author Franz-Josef Elmer
 */
abstract class AbstractEMailBuilder
{
    private static final String FOOTER = "\n\n--------------------------------------------------\n" 
                                       + "CIFEX - CISD File EXchanger\n"
                                       + "Center for Information Sciences and Databases\n" 
                                       + "ETH Zurich";

    protected static final String DATE_TEMPLATE = "on {0,date,d-MMM-yyyy} at {0,time,HH:mm:ss}";
    
    protected final UserDTO registrator;
    protected String comment;
    protected String url;

    private String fullName;
    private final IMailClient mailClient;
    private final String email;

    protected String password;

    protected AbstractEMailBuilder(IMailClient mailClient, UserDTO registrator, String email)
    {
        assert mailClient != null : "Unspecified mail client.";
        assert registrator != null : "Unspecified registrator.";
        assert email != null : "Unspecified email.";
        
        this.mailClient = mailClient;
        this.registrator = registrator;
        this.email = email;
    }
    
    /**
     * Sets the base URL used to creating links in the e-mail. Has to be called before e-mail will be send.
     */
    public void setURL(String url)
    {
        this.url = url;
    }
    
    /**
     * Sets an optional comment.
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }

    /**
     * Sets the password which might be needed.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    /**
     * Sets the full name, to be used in the greeting (if available).
     */
    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    /**
     * Sends the e-mail
     */
    public void sendEMail()
    {
        assert url != null : "Missing URL.";
        mailClient.sendMessage("[CIFEX] " + createSubject(), createContent() + FOOTER, email);
    }
    
    protected final void addGreeting(StringBuilder builder)
    {
        builder.append("Hello");
        if (StringUtils.isNotBlank(fullName))
        {
            builder.append(' ');
            builder.append(fullName);
        }
        builder.append(",\n\n");
    }

    protected final void addRegistratorDetails(StringBuilder builder)
    {
        builder.append("------------------------------------------------------------\n");
        builder.append("\nFrom:\t").append(getShortRegistratorDescription());
        builder.append("\nEmail:\t").append(registrator.getEmail());
        if (comment != null)
        {
            builder.append("\nComment:\t").append(comment);
        }
    }

    protected final String getShortRegistratorDescription()
    {
        final String registratorFullName = registrator.getUserFullName();
        return StringUtils.isBlank(registratorFullName) ? registrator.getUserCode() : registratorFullName;
    }
    
    protected final String getLongRegistratorDescription()
    {
        return getShortRegistratorDescription() + " <" + registrator.getEmail() + ">";
    }
    
    protected abstract String createSubject();
    
    protected abstract String createContent();

}
