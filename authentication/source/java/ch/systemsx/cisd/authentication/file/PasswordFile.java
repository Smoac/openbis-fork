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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A class to read and write password files.
 * 
 * @author Bernd Rinn
 */
final class PasswordFile
{

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, PasswordFile.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PasswordFile.class);

    private final File passwordFile;

    PasswordFile(final File passwordFile)
    {
        this.passwordFile = passwordFile;
    }

    @SuppressWarnings("unchecked")
    private static List<String> primReadLines(File file) throws IOException
    {
        return FileUtils.readLines(file);
    }

    private List<String> readPasswordLines()
    {
        if (passwordFile.canRead() == false)
        {
            final String msg =
                    String.format(passwordFile.exists() ? "File '%s' cannot be read."
                            : "File '%s' does not exist.", passwordFile.getAbsolutePath());
            operationLog.error(msg);
            throw new ConfigurationFailureException(msg);
        }
        try
        {
            return primReadLines(passwordFile);
        } catch (IOException ex)
        {
            final String msg =
                    String.format("Error when reading file '%s'.", passwordFile.getAbsolutePath());
            machineLog.error(msg, ex);
            throw new EnvironmentFailureException(msg, ex);
        }
    }

    private static void writePasswordLines(File file, List<String> lines)
    {
        if (file.canWrite() == false)
        {
            final String msg =
                    String.format(file.exists() ? "File '%s' cannot be written."
                            : "File '%s' does not exist.", file.getAbsolutePath());
            operationLog.error(msg);
            throw new ConfigurationFailureException(msg);
        }
        try
        {
            FileUtils.writeLines(file, lines);
        } catch (IOException ex)
        {
            final String msg =
                    String.format("Error when writing file '%s'.", file.getAbsolutePath());
            machineLog.error(msg, ex);
            throw new EnvironmentFailureException(msg, ex);
        }
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

    /**
     * Returns the path of the password file.
     */
    String getPath()
    {
        return passwordFile.getPath();
    }

    /**
     * Returns <code>true</code>, if the password file backing this class exists.
     */
    boolean exists()
    {
        return passwordFile.exists();
    }

    /**
     * Returns the {@link UserEntry} of <var>user</var>, or <code>null</code>, if this user does
     * not exist.
     */
    UserEntry tryGetUser(String user)
    {
        return tryFindUserEntry(user, readPasswordLines());
    }

    /**
     * Adds the <var>user</var> if it exists, otherwise updates (replaces) the entry with the given
     * entry.
     */
    void addOrUpdateUser(UserEntry user)
    {
        assert user != null;

        final File oldPasswordFile = new File(passwordFile.getPath() + ".sv");
        final File newPasswordFile = new File(passwordFile.getPath() + ".tmp");

        checkWritable(passwordFile);
        checkWritable(oldPasswordFile);
        checkWritable(newPasswordFile);

        final List<String> passwordLines =
                passwordFile.exists() ? readPasswordLines() : new ArrayList<String>();
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
        writePasswordLines(newPasswordFile, passwordLines);
        oldPasswordFile.delete();
        passwordFile.renameTo(oldPasswordFile);
        newPasswordFile.renameTo(passwordFile);
    }

    /**
     * Removes the user with id <var>userId</var> if it exists.
     * 
     * @return <code>true</code>, if the user has been removed.
     */
    boolean removeUser(String userId)
    {
        assert userId != null;

        final File oldPasswordFile = new File(passwordFile.getPath() + ".sv");
        final File newPasswordFile = new File(passwordFile.getPath() + ".tmp");

        checkWritable(passwordFile);
        checkWritable(oldPasswordFile);
        checkWritable(newPasswordFile);

        final List<String> passwordLines = readPasswordLines();
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
            writePasswordLines(newPasswordFile, passwordLines);
            oldPasswordFile.delete();
            passwordFile.renameTo(oldPasswordFile);
            newPasswordFile.renameTo(passwordFile);
        }
        return found;
    }

    private void checkWritable(File file) throws UserFailureException
    {
        if (file.exists() == false)
        {
            try
            {
                FileUtils.touch(file);
            } catch (IOException ex)
            {
                final String msg =
                        String
                                .format("Password file '%s' is not writable.", file
                                        .getAbsolutePath());
                operationLog.error(msg);
                throw new UserFailureException(msg);
            }
        }
        if (file.canWrite() == false)
        {
            final String msg =
                    String.format("Password file '%s' is not writable.", file.getAbsolutePath());
            operationLog.error(msg);
            throw new UserFailureException(msg);
        }
    }

    /**
     * Returns <code>true</code>, if <var>user</var> is known and has the given <var>password</var>.
     */
    boolean isPasswordCorrect(String user, String password)
    {
        assert user != null;
        assert password != null;

        final UserEntry userEntryOrNull = tryFindUserEntry(user, readPasswordLines());
        if (userEntryOrNull == null)
        {
            return false;
        }
        return userEntryOrNull.isPasswordCorrect(password);
    }

    /**
     * Returns a list of all users currently found in the password file.
     */
    List<UserEntry> listUsers()
    {
        final List<UserEntry> list = new ArrayList<UserEntry>();
        for (String line : readPasswordLines())
        {
            final UserEntry user = new UserEntry(line);
            list.add(user);
        }
        return list;
    }

    void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        if (passwordFile.canRead() == false)
        {
            final String msg =
                    String.format(passwordFile.exists() ? "Password file '%s' is not readable."
                            : "Password file '%s' does not exist.", passwordFile.getAbsolutePath());
            operationLog.error(msg);
            throw new ConfigurationFailureException(msg);
        }
    }

}
