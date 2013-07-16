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

import ch.systemsx.cisd.cifex.client.application.ui.itemswidget.ItemsField;
import ch.systemsx.cisd.cifex.client.application.utils.CifexValidator;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * @author Franz-Josef Elmer
 * @author Juan Fuentes
 */
public class UserTextArea extends ItemsField
{

    public UserTextArea()
    {
        super(null, CifexValidator.USER_VALIDATOR);
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
                addItem(Constants.USER_ID_PREFIX + user.getUserCode());
            } else if (StringUtils.isBlank(user.getEmail()) == false)
            {
                addItem(user.getEmail());
            }
        }
    }
    
}
