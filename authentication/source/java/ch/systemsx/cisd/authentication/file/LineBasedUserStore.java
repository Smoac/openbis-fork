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

package ch.systemsx.cisd.authentication.file;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * A class to read and write {@link UserEntry}.
 * 
 * @author Bernd Rinn
 */
final class LineBasedUserStore implements IUserStore
{

    private final ILineStore lineStore;
    
    LineBasedUserStore(final ILineStore lineStore)
    {
        this.lineStore = lineStore;
    }

    private UserEntry tryFindUserEntry(String user, List<String> passwordLines)
    {
        assert user != null;
        assert passwordLines != null;

        for (String line : passwordLines)
        {
            final UserEntry entry = new UserEntry(line);
            if (user.equals(entry.getUserId()))
            {
                return entry;
            }
        }
        return null;
    }

    public String getId()
    {
        return lineStore.getId();
    }

    public boolean exists()
    {
        return lineStore.exists();
    }

    public UserEntry tryGetUser(String user)
    {
        return tryFindUserEntry(user, lineStore.readLines());
    }

    public void addOrUpdateUser(UserEntry user)
    {
        assert user != null;

        final List<String> passwordLines = lineStore.readLines();
        boolean found = false;
        for (int i = 0; i < passwordLines.size(); ++i)
        {
            final String line = passwordLines.get(i);
            final UserEntry entry = new UserEntry(line);
            if (entry.getUserId().equals(user.getUserId()))
            {
                passwordLines.set(i, user.asPasswordLine());
                found = true;
                break;
            }
        }
        if (found == false)
        {
            passwordLines.add(user.asPasswordLine());
        }
        lineStore.writeLines(passwordLines);
    }

    public boolean removeUser(String userId)
    {
        assert userId != null;

        final List<String> passwordLines = lineStore.readLines();
        boolean found = false;
        for (int i = 0; i < passwordLines.size(); ++i)
        {
            final String line = passwordLines.get(i);
            final UserEntry entry = new UserEntry(line);
            if (userId.equals(entry.getUserId()))
            {
                passwordLines.remove(i);
                found = true;
                break;
            }
        }
        if (found)
        {
            lineStore.writeLines(passwordLines);
        }
        return found;
    }

    public boolean isPasswordCorrect(String user, String password)
    {
        assert user != null;
        assert password != null;

        final UserEntry userEntryOrNull = tryFindUserEntry(user, lineStore.readLines());
        if (userEntryOrNull == null)
        {
            return false;
        }
        return userEntryOrNull.isPasswordCorrect(password);
    }

    public List<UserEntry> listUsers()
    {
        final List<UserEntry> list = new ArrayList<UserEntry>();
        for (String line : lineStore.readLines())
        {
            final UserEntry user = new UserEntry(line);
            list.add(user);
        }
        return list;
    }

    /**
     * Checks whether this store is operational.
     * 
     * @throws ConfigurationFailureException If the store is not operational.
     */
    public void check() throws ConfigurationFailureException
    {
        lineStore.check();
    }

}
