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

package ch.systemsx.cisd.bds;

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * Entity of measurement or calculation covered by the data. This is an immutable value object
 * class. It extends {@link Sample} with an owner (a database instance OR a group).
 * 
 * @author Christian Ribeaud
 */
public final class SampleWithOwner extends Sample
{
    static final String GROUP_CODE = "group_code";

    static final String DATABASE_INSTANCE_CODE = "database_instance_code";

    private final String groupCode;

    private final String databaseInstanceCode;

    /**
     * Creates an instance for the specified {@link Sample}, group code and database instance code
     * of the sample.
     * 
     * @param groupCode A non-<code>null</code> string of the group code. Could be empty.
     * @param databaseInstanceCode A non-<code>null</code> string of the database instance code.
     *            Could be empty.
     */
    public SampleWithOwner(final Sample sample, final String groupCode,
            final String databaseInstanceCode)
    {
        this(sample.getCode(), sample.getTypeCode(), sample.getTypeDescription(), groupCode,
                databaseInstanceCode);
    }

    /**
     * Creates an instance for the specified code, type code, type description, group code and
     * database instance code of the sample.
     * 
     * @param groupCode A non-<code>null</code> string of the group code. Could be empty.
     * @param databaseInstanceCode A non-<code>null</code> string of the database instance code.
     *            Could be empty.
     */
    public SampleWithOwner(final String code, final String typeCode, final String typeDescription,
            final String groupCode, final String databaseInstanceCode)
    {
        super(code, typeCode, typeDescription);
        assert groupCode != null : "Undefined group code.";
        assert databaseInstanceCode != null : "Undefined database instance code.";
        assertNonEmptyDatabaseInstanceCode(databaseInstanceCode);
        this.groupCode = groupCode;
        this.databaseInstanceCode = databaseInstanceCode;
    }

    private final static void assertNonEmptyDatabaseInstanceCode(final String databaseInstanceCode)
    {
        if (databaseInstanceCode.length() == 0)
        {
            throw new DataStructureException("Empty database instance code.");
        }
    }

    public final String getGroupCode()
    {
        return groupCode;
    }

    public final String getDatabaseInstanceCode()
    {
        return databaseInstanceCode;
    }

    //
    // Sample
    //

    /**
     * Loads the entity from the specified directory.
     * 
     * @throws DataStructureException if file missing.
     */
    final static SampleWithOwner loadFrom(final IDirectory directory)
    {
        assert directory != null : "Unspecified directory";
        final IDirectory folder = Utilities.getSubDirectory(directory, FOLDER);
        final String typeDescription = Utilities.getTrimmedString(folder, TYPE_DESCRIPTION);
        final String code = Utilities.getTrimmedString(folder, CODE);
        final String typeCode = Utilities.getTrimmedString(folder, TYPE_CODE);
        final String groupCode = Utilities.getTrimmedString(folder, GROUP_CODE);
        final String databaseInstanceCode =
                Utilities.getTrimmedString(folder, DATABASE_INSTANCE_CODE);
        return new SampleWithOwner(code, typeCode, typeDescription, groupCode, databaseInstanceCode);
    }

    @Override
    public final void saveTo(final IDirectory directory)
    {
        super.saveTo(directory);
        final IDirectory folder = directory.makeDirectory(FOLDER);
        folder.addKeyValuePair(GROUP_CODE, groupCode);
        folder.addKeyValuePair(DATABASE_INSTANCE_CODE, databaseInstanceCode);
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder = createToStringBuilder();
        if (groupCode.length() > 0)
        {
            builder.append(GROUP_CODE, groupCode);
        } else if (databaseInstanceCode.length() > 0)
        {
            builder.append(DATABASE_INSTANCE_CODE, databaseInstanceCode);
        }
        return builder.toString();
    }
}
