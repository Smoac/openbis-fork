/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds;

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * Immutable value object for the version of something.
 * 
 * @author Franz-Josef Elmer
 */
public final class Version implements IStorable
{
    static final String VERSION = "version";

    static final String MAJOR = "major";

    static final String MINOR = "minor";

    /**
     * Loads the version from the specified directory.
     */
    static Version loadFrom(IDirectory directory)
    {
        IDirectory versionFolder = Utilities.getSubDirectory(directory, VERSION);
        return new Version(Utilities.getNumber(versionFolder, MAJOR), Utilities.getNumber(versionFolder, MINOR));
    }

    /**
     * Creates a <code>Version</code> from given <var>value</var>.
     * 
     * @param value an example: <code>2.3</code>.
     * @return <code>null</code> if operation fails.
     */
    public final static Version createVersionFromString(final String value)
    {
        assert value != null : "Given value can not be null.";
        final int index = value.indexOf(".");
        if (index > -1)
        {
            final String strMajor = value.substring(0, index);
            final String strMinor = value.substring(index + 1);
            try
            {
                return new Version(Integer.parseInt(strMajor), Integer.parseInt(strMinor));
            } catch (NumberFormatException ex)
            {
            }
        }
        return null;
    }

    private final int major;

    private final int minor;

    /**
     * Creates a new instance for the specified major and minor number.
     * 
     * @param major A positive number.
     * @param minor A non-negative number.
     */
    public Version(int major, int minor)
    {
        assert major > 0 : "invalid major version number: " + major;
        this.major = major;
        assert minor >= 0 : "invalid minor version number: " + minor;
        this.minor = minor;
    }

    /**
     * Returns major version number.
     */
    public final int getMajor()
    {
        return major;
    }

    /**
     * Returns minor version number.
     */
    public final int getMinor()
    {
        return minor;
    }

    /**
     * Returns <code>true</code> if this version is backwards compatible to the specified version. That is, if
     * <code>version.getMajor() == this.getMajor()</code> and <code>version.getMinor() &lt;= this.getMinor()</code>.
     */
    public boolean isBackwardsCompatibleWith(Version version)
    {
        return version.major == major && version.minor <= minor;
    }

    /**
     * Returns the previous minor version.
     * 
     * @throws DataStructureException if minor version is 0.
     */
    public Version getPreviousMinorVersion()
    {
        if (minor == 0)
        {
            throw new DataStructureException("There is no previous minor version of " + this);
        }
        return new Version(major, minor - 1);
    }

    //
    // IStorable
    //

    public final void saveTo(IDirectory directory)
    {
        IDirectory versionFolder = directory.makeDirectory(VERSION);
        versionFolder.addKeyValuePair(MAJOR, Integer.toString(major));
        versionFolder.addKeyValuePair(MINOR, Integer.toString(minor));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Version == false)
        {
            return false;
        }
        Version version = (Version) obj;
        return version.major == major && version.minor == minor;
    }

    @Override
    public int hashCode()
    {
        return major * 1000 + minor;
    }

    @Override
    public String toString()
    {
        return "V" + major + "." + minor;
    }

}
