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

import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.mail.IMailClient;

/**
 * Class which creates and sends an e-mail informing that the user has changed.
 * 
 * @author Basil Neff
 */
public class EMailBuilderForUpdateUser extends AbstractEMailBuilder
{

    private static final String UPDATED_ACCOUNT_EMAIL_SUBJECT_LINE =
        "updated-account-email-subject";

    private static final String UPDATED_ACCOUNT_EMAIL_TEMPLATE_FILE_NAME =
            "etc/updated-account-email.template";

    private final UserDTO updateUser;

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
        return createContent(UPDATED_ACCOUNT_EMAIL_TEMPLATE_FILE_NAME);
    }

    @Override
    protected String createSubject()
    {
        return StringUtils.capitalize(emailDict.get(UPDATED_ACCOUNT_EMAIL_SUBJECT_LINE));
    }

    @Override
    protected String getUserCode()
    {
        return updateUser.getUserCode();
    }

    @Override
    protected Date tryGetExpirationDate()
    {
        return updateUser.getExpirationDate();
    }

    @Override
    protected void addToDict(Properties emailProps, DateFormat dateFormat)
    {
        if (updateUser.isAdmin())
        {
            emailDict.put("account-type", emailProps.getProperty("account-type-admin"));
            emailDict.put("account-type2", emailProps.getProperty("account-type-admin2"));
        } else if (updateUser.isPermanent())
        {
            emailDict.put("account-type", emailProps.getProperty("account-type-regular"));
            emailDict.put("account-type2", emailProps.getProperty("account-type-regular2"));
        } else
        {
            emailDict.put("account-type", emailProps.getProperty("account-type-temp"));
            emailDict.put("account-type2", emailProps.getProperty("account-type-temp2"));
        }
    }
}
