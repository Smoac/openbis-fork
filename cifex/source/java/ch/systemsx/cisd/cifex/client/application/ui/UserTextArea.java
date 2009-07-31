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

package ch.systemsx.cisd.cifex.client.application.ui;

import java.util.ArrayList;
import java.util.List;

import com.gwtext.client.widgets.form.TextArea;
import com.gwtext.client.widgets.form.TextAreaConfig;

import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * @author Basil Neff
 */
public class UserTextArea extends TextArea
{

    public UserTextArea(TextAreaConfig textAreaConfig)
    {
        super(textAreaConfig);
    }

    public final String[] getUserEntries()
    {
        if (this.isValid() == false)
        {
            return new String[0];
        }

        final List list = new ArrayList();
        String[] entries = this.getValueAsString().split("[,\n\r\t\f ]");
        for (int i = 0; i < entries.length; i++)
        {
            if (StringUtils.isBlank(entries[i]) == false)
            {
                list.add(entries[i]);
            }
        }
        if (list.size() > 0)
        {
            return (String[]) list.toArray(new String[list.size()]);
        } else
        {
            return new String[0];
        }
    }

    public final void setUserEntries(final String[] users)
    {
        this.setValue("");
        if (users != null && users.length != 0)
        {
            for (int i = 0; i < users.length; i++)
            {
                addUser(users[i]);
            }
        }
    }
    
    /**
     * Adds the email address to the textfield.
     */
    public final void addUser(final String email)
    {
        if (StringUtils.isBlank(email) == false)
        {
            if (StringUtils.isBlank(getValueAsString()))
            {
                this.setValue(email);
            } else
            {
                this.setValue(this.getValueAsString() + ", " + email);
            }
            render();
        }
    }

    /**
     * Adds the given user to the text area.
     */
    public final void addUser(final UserInfoDTO user)
    {
        if (user != null)
        {
            if (StringUtils.isBlank(user.getUserCode()) == false)
            {
                addUser(Constants.USER_ID_PREFIX + user.getUserCode());
            } else if (StringUtils.isBlank(user.getEmail()) == false)
            {
                addUser(user.getEmail());
            }
        }
    }
}
