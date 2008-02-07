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

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.mail.IMailClient;

/**
 * 
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
    
    private final IMailClient mailClient;
    private final String email;

    protected String password;

    /**
     *
     *
     */
    public AbstractEMailBuilder(IMailClient mailClient, UserDTO registrator, String email)
    {
        assert mailClient != null : "Unspecified mail client.";
        assert registrator != null : "Unspecified registrator.";
        assert email != null : "Unspecified email.";
        
        this.mailClient = mailClient;
        this.registrator = registrator;
        this.email = email;
    }
    
    public void setURL(String url)
    {
        this.url = url;
    }
    
    public void setComment(String comment)
    {
        this.comment = comment;
    }
    
    public void sendEMail()
    {
        assert url != null : "Missing URL.";
        mailClient.sendMessage("[CIFEX] " + createSubject(), createContent() + FOOTER, email);
    }
    
    protected abstract String createSubject();
    
    protected abstract String createContent();

    protected void addRegistratorDetails(StringBuilder builder)
    {
        builder.append("------------------------------------------------------------\n");
        String fullName = registrator.getUserFullName();
        builder.append("\nFrom:\t").append(fullName == null ? registrator.getUserCode() : fullName);
        builder.append("\nEmail:\t").append(registrator.getEmail());
        if (comment != null)
        {
            builder.append("\nComment:\t").append(comment);
        }
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
