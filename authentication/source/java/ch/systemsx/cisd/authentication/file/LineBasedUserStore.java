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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * A class to read and write {@link UserEntry}.
 * 
 * @author Bernd Rinn
 */
final class LineBasedUserStore implements IUserStore
{
    private final ILineStore lineStore;

    private Map<String, UserEntry> idToEntryMap;

    private Map<String, UserEntry> emailToEntryMap;

    LineBasedUserStore(final ILineStore lineStore)
    {
        this.lineStore = lineStore;
        this.idToEntryMap = new LinkedHashMap<String, UserEntry>();
        this.emailToEntryMap = new LinkedHashMap<String, UserEntry>();
    }

    private synchronized Map<String, UserEntry> getIdToEntryMap()
    {
        return idToEntryMap;
    }

    private synchronized Map<String, UserEntry> getEmailToEntryMap()
    {
        return emailToEntryMap;
    }

    synchronized void setEntryMaps(Map<String, UserEntry> idToEntryMap,
            Map<String, UserEntry> emailToEntryMap)
    {
        this.idToEntryMap = idToEntryMap;
        this.emailToEntryMap = emailToEntryMap;
    }

    private void updateMaps()
    {
        if (lineStore.hasChanged())
        {
            final Map<String, UserEntry> newIdToEntryMap = new LinkedHashMap<String, UserEntry>();
            final Map<String, UserEntry> newEmailToEntryMap =
                    new LinkedHashMap<String, UserEntry>();
            for (String line : lineStore.readLines())
            {
                final UserEntry entry = new UserEntry(line);
                newIdToEntryMap.put(entry.getUserId(), entry);
                if (StringUtils.isNotBlank(entry.getEmail()))
                {
                    if (newEmailToEntryMap.put(entry.getEmail().toLowerCase(), entry) != null)
                    {
                        // Multiple users with the same email
                        emailToEntryMap.remove(entry.getEmail().toLowerCase());
                    }
                }
            }
            setEntryMaps(newIdToEntryMap, newEmailToEntryMap);
        }
    }

    private List<String> asPasswordLines()
    {
        final Collection<UserEntry> users = getIdToEntryMap().values();
        final List<String> lines = new ArrayList<String>(users.size());
        for (UserEntry user : users)
        {
            lines.add(user.asPasswordLine());
        }
        return lines;
    }

    @Override
    public String getId()
    {
        return lineStore.getId();
    }

    @Override
    public UserEntry tryGetUserById(String user)
    {
        updateMaps();
        return getIdToEntryMap().get(user);
    }

    @Override
    public UserEntry tryGetUserByEmail(String email) throws EnvironmentFailureException
    {
        updateMaps();
        return getEmailToEntryMap().get(email.toLowerCase());
    }

    @Override
    public synchronized void addOrUpdateUser(UserEntry user)
    {
        assert user != null;

        updateMaps();
        idToEntryMap.put(user.getUserId(), user);
        if (StringUtils.isNotBlank(user.getEmail()))
        {
            if (emailToEntryMap.put(user.getEmail().toLowerCase(), user) != null)
            {
                // Multiple users with the same email
                emailToEntryMap.remove(user.getEmail().toLowerCase());
            }
        }
        lineStore.writeLines(asPasswordLines());
    }

    @Override
    public synchronized boolean removeUser(String userId)
    {
        assert userId != null;

        updateMaps();
        final UserEntry oldEntryOrNull = idToEntryMap.remove(userId);
        if (oldEntryOrNull != null)
        {
            if (StringUtils.isNotBlank(oldEntryOrNull.getEmail()))
            {
                emailToEntryMap.remove(oldEntryOrNull.getEmail().toLowerCase());
            }
            lineStore.writeLines(asPasswordLines());
            return true;
        }
        return false;
    }

    @Override
    public boolean isPasswordCorrect(String user, String password)
    {
        assert user != null;
        assert password != null;

        final UserEntry userEntryOrNull = tryGetUserById(user);
        if (userEntryOrNull == null)
        {
            return false;
        }
        return userEntryOrNull.isPasswordCorrect(password);
    }

    @Override
    public List<UserEntry> listUsers()
    {
        updateMaps();
        return new ArrayList<UserEntry>(getIdToEntryMap().values());
    }

    /**
     * Checks whether this store is operational.
     * 
     * @throws ConfigurationFailureException If the store is not operational.
     */
    @Override
    public void check() throws ConfigurationFailureException
    {
        lineStore.check();
    }

    @Override
    public boolean isRemote()
    {
        return false;
    }

}
