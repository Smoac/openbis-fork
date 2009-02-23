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
 * Class which creates and sends an e-mail informing that the user has changed.
 * 
 * @author Basil Neff
 */
public class EMailBuilderForUpdateUser extends AbstractEMailBuilder
{

    private final UserDTO updateUser;

    private static final MessageFormat EXPIRATION_TEMPLATE =
            new MessageFormat("\n\nThis login account expires " + DATE_TEMPLATE
                    + ". Please access your account now!");

    /**
     * Creates an instance for the specified mail client, registrator is the one who edited the
     * account, and the user.
     */
    public EMailBuilderForUpdateUser(IMailClient mailClient, UserDTO registrator, UserDTO user)
    {
        super(mailClient, registrator, user.getEmail());
        this.updateUser = user;
        setFullName(updateUser.getUserFullName());
    }

    @Override
    protected String createContent()
    {
        StringBuilder builder = new StringBuilder();
        addGreeting(builder);
        builder.append(getLongRegistratorDescription());
        builder.append(" has updated your ");
        builder.append(createTypeAdjective());
        builder.append(" account on our server for you.\n\n");
        builder.append("-------------------------------------------------\n");
        builder.append("Here\'s how to login:\n");
        builder.append("-------------------------------------------------\n");
        builder.append("\nVisit:\t\t").append(url);
        appendURLParam(builder, Constants.USERCODE_PARAMETER, updateUser.getUserCode(), true);
        builder.append("\nUser:\t").append(updateUser.getUserCode());
        if (password != null)
        {
            builder.append("\nPassword:\t").append(password);
        } else
        {
            builder.append("\nThe password has not changed, it is still to old one!");
        }
        if (updateUser.isAdmin() == false && updateUser.isPermanent() == false)
        {
            Date expirationDate = updateUser.getExpirationDate();
            builder.append(EXPIRATION_TEMPLATE.format(new Object[]
                { expirationDate }));
        }
        return builder.toString();
    }

    @Override
    protected String createSubject()
    {
        return getShortRegistratorDescription() + " has updated your " + createTypeAdjective()
                + " account";
    }

    private String createTypeAdjective()
    {
        return updateUser.isAdmin() ? "administrative" : (updateUser.isPermanent() ? "permanent"
                : "temporary");
    }

}
