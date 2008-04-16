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

package types;

/**
 * A type which represents a boolean which is third state <code>UNKNOWN</code>. It additionally
 * stores the database representation of this field.
 * 
 * @author Bernd Rinn
 */
public enum BooleanOrUnknown
{

    FALSE("F"), TRUE("T"), UNKNOWN("U");

    private final String databaseRepresentation;

    BooleanOrUnknown(String databaseRepresentation)
    {
        this.databaseRepresentation = databaseRepresentation;
    }

    /**
     * Returns the database representation of the value.
     */
    public String getDatabaseRepresentation()
    {
        return databaseRepresentation;
    }
    
    /**
     * Resolve the specified boolean flag to either {@link #TRUE} or {@link #FALSE}.
     */
    public static BooleanOrUnknown resolve(boolean flag)
    {
        return flag ? TRUE : FALSE;
    }

    /**
     * Returns the value for the corresponding <var>databaseRepresentation</var>. The
     * <var>databaseRepresentation</var> is converted to upper case.
     * 
     * @throws IllegalArgumentException If the <var>databaseRepresentation</var> provided does not
     *             correspond to any value of this type.
     */
    public static BooleanOrUnknown fromDatabaseRepresentation(String databaseRepresentation)
            throws IllegalArgumentException
    {
        if (databaseRepresentation.toUpperCase().equals("F"))
        {
            return FALSE;
        } else if (databaseRepresentation.toUpperCase().equals("T"))
        {
            return TRUE;
        } else if (databaseRepresentation.toUpperCase().equals("U"))
        {
            return UNKNOWN;
        } else
        {
            throw new IllegalArgumentException("Unknown database representation '"
                    + databaseRepresentation + "'.");
        }
    }

}
