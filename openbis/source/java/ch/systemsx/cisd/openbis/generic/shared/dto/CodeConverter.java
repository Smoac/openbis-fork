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

package ch.systemsx.cisd.openbis.generic.shared.dto;

/**
 * Methods for converter codes from business layer to database and from database to business layer.
 * 
 * @author Bernd Rinn
 */
public final class CodeConverter
{
    public static final String USER_PROPERTY_PREFIX = "USER.";

    private CodeConverter()
    {
        // Cannot be instantiated.
    }

    /**
     * Converts a code from database form to business layer form.
     * 
     * @return The code appropriate for the business layer.
     */
    public static String tryToBusinessLayer(final String codeFromDatabaseOrNull)
    {
        return tryToBusinessLayer(codeFromDatabaseOrNull, false);
    }

    /**
     * Converts a code from database form to business layer form.
     * <p>
     * If <var>userNamespace</var> is <code>true</code>, the prefix 'USER.' will be used,
     * because user properties will be represented as 'NAME' in the database and as USER.NAME in the
     * business layer.
     * 
     * @return The code appropriate for the business layer.
     */
    public static String tryToBusinessLayer(final String codeFromDatabaseOrNull,
            final boolean userNamespace)
    {
        if (userNamespace && codeFromDatabaseOrNull != null)
        {
            return USER_PROPERTY_PREFIX + codeFromDatabaseOrNull;
        } else
        {
            return codeFromDatabaseOrNull;
        }
    }

    /**
     * Converts a property type code from business layer form to database form.
     * <p>
     * The code will be translated to upper case. User properties will be represented as 'NAME' in
     * the database and as USER.NAME in the business layer.
     * 
     * @return The code appropriate for the database.
     */
    public static String tryToDatabase(final String codeFromBusinessLayerOrNull)
    {
        if (codeFromBusinessLayerOrNull == null)
        {
            return null;
        }
        final String upperCaseCode = codeFromBusinessLayerOrNull.toUpperCase();
        if (upperCaseCode.startsWith(USER_PROPERTY_PREFIX))
        {
            return upperCaseCode.substring(USER_PROPERTY_PREFIX.length());
        } else
        {
            return upperCaseCode;
        }
    }

    /**
     * Returns <code>true</code>, if the <var>codeFromBusinessLayerOrNull</var> represents a
     * user property code.
     */
    public static boolean isInternalNamespace(final String codeFromBusinessLayerOrNull)
    {
        if (codeFromBusinessLayerOrNull == null)
        {
            return false;
        } else
        {
            return codeFromBusinessLayerOrNull.toUpperCase().startsWith(USER_PROPERTY_PREFIX) == false;
        }
    }

}
