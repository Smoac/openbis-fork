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

package ch.systemsx.cisd.cifex.server.business.bo;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import ch.systemsx.cisd.cifex.server.business.IBusinessContext;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * @author Franz-Josef Elmer
 */
class UserBO extends AbstractBusinessObject implements IUserBO
{
    private UserDTO userDTO;

    private boolean dataChanged;

    UserBO(IDAOFactory daoFactory, IBusinessContext businessContext)
    {
        super(daoFactory, businessContext);
    }

    public void define(UserDTO user)
    {
        assert user != null : "Given user can not be null.";
        assert user.getID() == null : "User ID is set, this will be done from the UserDAO.";
        assert user.getExpirationDate() == null : "Expiration date should not have been specified yet.";

        if (user.isPermanent() == false)
        {
            user.setExpirationDate(DateUtils.addMinutes(new Date(), businessContext
                    .getUserRetention()));
        }
        this.userDTO = user;
        dataChanged = true;
    }

    public void save()
    {
        if (dataChanged)
        {
            daoFactory.getUserDAO().createUser(userDTO);
            dataChanged = false;
        }
    }

}
