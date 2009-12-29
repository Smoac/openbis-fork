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

import static ch.systemsx.cisd.cifex.server.util.ExpirationUtilities.fixExpiration;

import java.util.Date;

import ch.systemsx.cisd.cifex.server.business.IBusinessContext;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.common.Password;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Contains the logic of creating and updating users.
 * 
 * @author Franz-Josef Elmer
 * @author Bernd Rinn
 */
class UserBO extends AbstractBusinessObject implements IUserBO
{
    private UserDTO userDTO;

    private UserDTO existingUser;

    private boolean dataChanged;

    private boolean createUser;

    UserBO(IDAOFactory daoFactory, IBusinessContext businessContext)
    {
        super(daoFactory, businessContext);
    }

    public void defineForCreate(UserDTO user, UserDTO requestUserOrNull, boolean forceTemporaryUser)
    {
        assert user != null : "Given user can not be null.";

        user.setRegistrator(requestUserOrNull);

        // Logic of deciding on what quota group to use. If no quota group is set here, a new quota
        // group will be created for the user automatically.
        // Note: Here we assume that the registrator information is reliable and complete, i.e. that
        // is comes from the server side.
        final boolean noAdmin =
                (requestUserOrNull != null) && (requestUserOrNull.isAdmin() == false);
        if (noAdmin)
        {
            user.setQuotaGroupId(requestUserOrNull.getQuotaGroupId());
        }
        checkAndFixUserExpiration(null, user, requestUserOrNull, noAdmin || forceTemporaryUser);
        this.userDTO = user;
        dataChanged = true;
        createUser = true;
    }

    public void defineForUpdate(UserDTO oldUserToUpdateOrNull, UserDTO userToUpdate,
            Password passwordOrNull, UserDTO requestUserOrNull)
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        // Get old user entry
        existingUser =
                (oldUserToUpdateOrNull != null) ? oldUserToUpdateOrNull : getUserByCode(userDAO,
                        userToUpdate.getUserCode());

        userToUpdate.setID(existingUser.getID());
        userToUpdate.setQuotaGroupId(existingUser.getQuotaGroupId());

        if (userToUpdate.isExternallyAuthenticated() != existingUser.isExternallyAuthenticated())
        {
            // If there has been a change to the user's external authentication state, apply the
            // consequences
            if (userToUpdate.isExternallyAuthenticated())
            {
                userToUpdate.setExternallyAuthenticated(true);
                userToUpdate.setExpirationDate(null);
                userToUpdate.setRegistrator(null);
            } else
            {
                userToUpdate.setExternallyAuthenticated(false);
                userToUpdate.setExpirationDate(null);
                userToUpdate.setRegistrator(requestUserOrNull);
            }
        }

        // Check that the new expiration date is in the valid range.
        checkAndFixUserExpiration(existingUser, userToUpdate, requestUserOrNull, false);

        // Password, update it if it has been provided.
        if (Password.isEmpty(passwordOrNull) == false)
        {
            userToUpdate.setPassword(passwordOrNull);
        }

        // Permanent users are always the main user of the quota group that they are in.
        // If that is not the case (e.g. when a temporary user switches to external
        // authentication), then we give the user a quota group of its own here.
        if ((userToUpdate.isAdmin() || userToUpdate.isPermanent())
                && userDAO.isMainUserOfQuotaGroup(userToUpdate) == false)
        {
            // The trigger UPDATE_ACCOUNTING_ON_UPDATE_USER() will create and set a new quota
            // group if we set it to null here.
            userToUpdate.setQuotaGroupId(null);
        }

        // If we switch a temporary user to permanent and the registrator of the user is no
        // administrator, make the current request user the new registrator.
        // Same is true when the user doesn't yet have a registrator.
        final UserDTO registratorOrNull = userToUpdate.getRegistrator();
        if ((existingUser.isPermanent() == false && userToUpdate.isPermanent()
                && registratorOrNull != null && registratorOrNull.isAdmin() == false)
                || registratorOrNull == null)
        {
            userToUpdate.setRegistrator(requestUserOrNull);
        }
        this.userDTO = userToUpdate;
        dataChanged = true;
        createUser = false;
    }

    private static UserDTO getUserByCode(final IUserDAO userDAO, final String userCode)
            throws UserFailureException
    {
        assert userCode != null;

        final UserDTO existingUser = userDAO.tryFindUserByCode(userCode);
        if (existingUser == null)
        {
            final String msg = String.format("User '%s' does not exist in the database.", userCode);
            throw new UserFailureException(msg);
        }
        assert userCode.equals(existingUser.getUserCode()) : "Mismatch in user code";
        return existingUser;
    }

    private void checkAndFixUserExpiration(UserDTO oldUserOrNull, final UserDTO userToUpdate,
            final UserDTO requestUserOrNull, final boolean forceExpiration)
    {
        if (userToUpdate.isPermanent() == false || forceExpiration)
        {
            final Date registrationDate = getRegistrationDate(oldUserOrNull);
            final Integer maxRetentionDaysOrNull = tryGetMaxUserRetentionDays(requestUserOrNull);
            final Date expirationDate =
                    fixExpiration(userToUpdate.getExpirationDate(), registrationDate,
                            maxRetentionDaysOrNull, businessContext.getUserRetention());
            userToUpdate.setExpirationDate(expirationDate);
        }
    }

    private Date getRegistrationDate(UserDTO userOrNull)
    {
        return (userOrNull == null) ? new Date() : userOrNull.getRegistrationDate();
    }

    private Integer tryGetMaxUserRetentionDays(final UserDTO requestUserOrNull)
    {
        if (requestUserOrNull == null)
        {
            return businessContext.getMaxUserRetention();
        } else
        {
            if (requestUserOrNull.isAdmin())
            {
                return null;
            }
            return (requestUserOrNull.getMaxUserRetention() == null) ? businessContext
                    .getMaxUserRetention() : requestUserOrNull.getMaxUserRetention();
        }
    }

    public void save()
    {
        if (dataChanged)
        {
            if (createUser)
            {
                daoFactory.getUserDAO().createUser(userDTO);
            } else
            {
                daoFactory.getUserDAO().updateUser(userDTO);
            }
            dataChanged = false;
        }
    }

    public UserDTO getOldUser() throws IllegalStateException
    {
        if (dataChanged == false || createUser)
        {
            throw new IllegalStateException();
        }
        return existingUser;
    }

}
