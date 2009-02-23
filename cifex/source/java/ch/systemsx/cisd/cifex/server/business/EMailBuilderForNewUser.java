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
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.mail.IMailClient;

/**
 * Class which creates and sends an e-mail informing someone that a new login account has been
 * created.
 * 
 * @author Franz-Josef Elmer
 */
public class EMailBuilderForNewUser extends AbstractEMailBuilder
{
    private static final MessageFormat EXPIRATION_TEMPLATE =
            new MessageFormat("\n\nThis login account expires " + DATE_TEMPLATE
                    + ". Please access your account now!");

    private final UserDTO newUser;

    /**
     * Creates an instance for the specified mail client, registrator of the new user, and the new
     * user.
     */
    public EMailBuilderForNewUser(IMailClient mailClient, UserDTO registrator, UserDTO newUser)
    {
        super(mailClient, registrator, newUser.getEmail());
        this.newUser = newUser;
        setFullName(newUser.getUserFullName());
    }

    @Override
    protected String createContent()
    {
        assert password != null : "Missing password.";

        StringBuilder builder = new StringBuilder();
        addGreeting(builder);
        builder.append(getLongRegistratorDescription());
        builder.append(" has requested a ");
        builder.append(createTypeAdjective());
        builder.append(" account on our server for you.\n\n");
        builder.append("------------------------------------------------------------\n");
        builder.append("Information about the person who requested the account:\n");
        addRegistratorDetails(builder);
        builder.append("\n\n-------------------------------------------------\n");
        builder.append("Here\'s how to login:\n");
        builder.append("-------------------------------------------------\n");
        builder.append("\nVisit:\t\t").append(url);
        appendURLParam(builder, Constants.USERCODE_PARAMETER, newUser.getUserCode(), true);
        builder.append("\nUser:\t").append(newUser.getUserCode());
        builder.append("\nPassword:\t").append(password);
        if (newUser.isAdmin() == false && newUser.isPermanent() == false)
        {
            Date expirationDate = newUser.getExpirationDate();
            builder.append(EXPIRATION_TEMPLATE.format(new Object[]
                { expirationDate }));
        }
        return builder.toString();
    }

    @Override
    protected String createSubject()
    {
        return getShortRegistratorDescription() + " has requested a " + createTypeAdjective()
                + " account for you";
    }

    private String createTypeAdjective()
    {
        return newUser.isAdmin() ? "administrative" : (newUser.isPermanent() ? "permanent"
                : "temporary");
    }

}
