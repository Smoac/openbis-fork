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

package ch.systemsx.cisd.openbis.generic.shared.dto.identifier;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.systemsx.cisd.common.utilities.StringUtilities;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Uniquely identifies a database instance.
 * 
 * @author Izabela Adamczyk
 */
public class DatabaseInstanceIdentifier implements Serializable
{
    public final static class Constants
    {
        public static final char IDENTIFIER_SEPARATOR = '/';

        public static final char DATABASE_INSTANCE_SEPARATOR = ':';
    }

    private static final long serialVersionUID = IServer.VERSION;

    /** see {@link #getDatabaseInstanceCode()} documentation */
    private String databaseInstanceCode;

    public static final String HOME = null;

    public static DatabaseInstanceIdentifier createHome()
    {
        return new DatabaseInstanceIdentifier(HOME);
    }

    public DatabaseInstanceIdentifier(final String databaseInstanceCode)
    {
        this.databaseInstanceCode = databaseInstanceCode;
    }

    /**
     * Usually you should not access database instance code directly. Look for appropriate helpers.
     * 
     * @return local or <i>UUID</i> of the database instance. It is treated as <i>UUID</i> if it has
     *         a canonical UUID format.
     */
    public final String getDatabaseInstanceCode()
    {
        return StringUtils.upperCase(databaseInstanceCode);
    }

    public final void setDatabaseInstanceCode(final String databaseInstanceCode)
    {
        this.databaseInstanceCode = databaseInstanceCode;
    }

    //
    // Object
    //

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof DatabaseInstanceIdentifier == false)
        {
            return false;
        }
        final DatabaseInstanceIdentifier that = (DatabaseInstanceIdentifier) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getDatabaseInstanceCode(), that.getDatabaseInstanceCode());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getDatabaseInstanceCode());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        if (databaseInstanceCode == null)
        {
            return "";
        } else
        {
            return databaseInstanceCode;
        }
    }

    public boolean isHomeDatabase()
    {
        return databaseInstanceCode == null;
    }

    /** Do not use this method. It exists just to make automatic bean conversion possible */
    @Deprecated
    public void setHomeDatabase(boolean isHomeDatabase)
    {
        // empty
    }

    // GWT only
    protected DatabaseInstanceIdentifier()
    {
    }

    public int compareTo(DatabaseInstanceIdentifier other)
    {
        return StringUtilities.compareNullable(databaseInstanceCode, other
                .getDatabaseInstanceCode());
    }
}
