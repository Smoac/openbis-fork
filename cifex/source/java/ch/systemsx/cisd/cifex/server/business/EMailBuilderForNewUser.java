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

import java.text.MessageFormat;
import java.util.Date;

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.mail.IMailClient;

/**
 * @author Franz-Josef Elmer
 */
public class EMailBuilderForNewUser extends AbstractEMailBuilder
{
    private static final MessageFormat EXPIRATION_TEMPLATE =
            new MessageFormat("\n\nThis login account expires on {0,date,d-MMM-yyyy} at {0,time,HH:mm:ss}. "
                    + "Please access your account now!");

    private final UserDTO newUser;
    
    private String url;

    private String password;

    /**
     * @param mailClient
     * @param registrator
     */
    public EMailBuilderForNewUser(IMailClient mailClient, UserDTO registrator, UserDTO newUser)
    {
        super(mailClient, registrator, newUser.getEmail());
        this.newUser = newUser;
    }

    public void setURL(String url)
    {
        this.url = url;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Override
    protected String createContent()
    {
        assert url != null : "Missing URL.";
        assert password != null : "Missing password.";
        
        StringBuilder builder = new StringBuilder();
        builder.append("Hello,\n\n").append(registrator.getEmail());
        builder.append(" has requested a ");
        builder.append(createTypeAdjective());
        builder.append(" account on our server for you.\n\n");
        builder.append("------------------------------------------------------------\n");  
        builder.append("Information about the person who requested the account:\n");  
        builder.append("------------------------------------------------------------\n");
        String fullName = registrator.getUserFullName();
        builder.append("\nFrom:\t").append(fullName == null ? registrator.getUserCode() : fullName);
        builder.append("\nEmail:\t").append(registrator.getEmail());
        if (comment != null)
        {
            builder.append("\nComment:\t").append(comment);
        }
        builder.append("\n\n-------------------------------------------------\n");
        builder.append("Here\'s how to login:\n");
        builder.append("-------------------------------------------------\n");
        builder.append("\nVisit:\t\t").append(url).append("?email=").append(newUser.getUserCode());
        builder.append("\nUsername:\t").append(newUser.getUserCode());
        builder.append("\nPassword:\t").append(password);
        if (newUser.isAdmin() == false && newUser.isPermanent() == false)
        {
            Date expirationDate = newUser.getExpirationDate();
            System.out.println(expirationDate);
            builder.append(EXPIRATION_TEMPLATE.format(new Object[] {expirationDate}));
        }
        return builder.toString();
    }

    @Override
    protected String createSubject()
    {
        return registrator.getEmail() + " has requested a " + createTypeAdjective() + " account for you";
    }

    private String createTypeAdjective()
    {
        return newUser.isAdmin() ? "administrative" : (newUser.isPermanent() ? "permanent" : "temporary");
    }
    
}
