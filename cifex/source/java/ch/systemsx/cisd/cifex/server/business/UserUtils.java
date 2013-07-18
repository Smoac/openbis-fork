/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.IMultiKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.collection.TableMapNonUniqueKey;

/**
 * Methods helpful in users management.
 * 
 * @author Izabela Adamczyk
 */
public class UserUtils
{

    static final Pattern USER_CODE_WITH_ID_PREFIX_PATTERN =
            Pattern.compile(Constants.USER_CODE_WITH_ID_PREFIX_REGEX, Pattern.CASE_INSENSITIVE);

    static final Pattern EMAIL_PATTERN = Pattern.compile(Constants.EMAIL_REGEX);
    
    final static String USER_ID_PREFIX = Constants.USER_ID_PREFIX;

    /**
     * The Crowd property for the display name.
     */
    private static final String DISPLAY_NAME_PROPERTY = "displayName";

    static TableMap<String, UserDTO> createTableMapOfExistingUsersWithUserCodeAsKey(
            final Collection<UserDTO> users)
    {
        return new TableMap<String, UserDTO>(users, new IKeyExtractor<String, UserDTO>()
            {
                @Override
                public String getKey(final UserDTO user)
                {
                    return user.getUserCode();
                }
            });
    }

    static TableMapNonUniqueKey<String, UserDTO> createTableMapOfExistingUsersWithEmailAsKey(
            final Collection<UserDTO> users)
    {
        return new TableMapNonUniqueKey<String, UserDTO>(users,
                new IMultiKeyExtractor<String, UserDTO>()
                    {
                        @Override
                        public Collection<String> getKey(final UserDTO user)
                        {
                            if (user.getEmailAlias() == null)
                            {
                                return Collections.singleton(user.getEmail());
                            } else
                            {
                                return Arrays.asList(user.getEmail(), user.getEmailAlias());
                            }
                        }
                    });
    }

    /**
     * Checks whether the identifier starts with {@link #USER_ID_PREFIX}.
     */
    static boolean isUserCodeWithIdPrefix(final String identifier)
    {
        return USER_CODE_WITH_ID_PREFIX_PATTERN.matcher(identifier).matches();
    }
    
    /** Checks whether the <var>identifier</var> is a valid email address. */
    static boolean isEmail(final String identifier)
    {
        return UserUtils.EMAIL_PATTERN.matcher(identifier).matches();        
    }
    
    static String extractEmail(String identifier)
    {
        String normalizedIdentifier = identifier.toLowerCase();
        int indexOfBracket = normalizedIdentifier.indexOf('<');
        if (indexOfBracket < 0)
        {
            return normalizedIdentifier;
        }
        return normalizedIdentifier.substring(indexOfBracket + 1, normalizedIdentifier.length() - 1);
    }

    /**
     * Removes {@link #USER_ID_PREFIX} from the identifier.
     */
    static String extractUserCode(final String lowerCaseIdentifier)
    {
        assert isUserCodeWithIdPrefix(lowerCaseIdentifier);
        return lowerCaseIdentifier.substring(UserUtils.USER_ID_PREFIX.length());
    }

    public static String extractDisplayName(final Principal principal)
    {
        final String displayName;
        if (principal.getProperty(DISPLAY_NAME_PROPERTY) != null)
        {
            displayName = principal.getProperty(DISPLAY_NAME_PROPERTY);
        } else
        {
            displayName = principal.getFirstName() + " " + principal.getLastName();
        }
        return displayName;
    }

}
