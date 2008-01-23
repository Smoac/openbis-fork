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

import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class UserManager extends AbstractManager implements IUserManager
{

    public UserManager(IDAOFactory daoFactory)
    {
        super(daoFactory);
    }

    public UserDTO tryToFindUser(String email)
    {
        // TODO 2008-01-23, Franz-Josef Elmer: replace by code using data access layer
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("admin@localhost");
        userDTO.setUserName("admin");
        userDTO.setEncryptedPassword("21232f297a57a5a743894a0e4a801fc3");
        userDTO.setAdmin(true);
        userDTO.setPermanent(true);
        return userDTO;
    }

    public void createUser(UserDTO user)
    {
        // TODO Auto-generated method stub
    }

}
