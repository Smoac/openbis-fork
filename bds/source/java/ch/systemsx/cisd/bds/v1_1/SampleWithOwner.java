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

package ch.systemsx.cisd.bds.v1_1;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.systemsx.cisd.bds.ExperimentIdentifier;
import ch.systemsx.cisd.bds.Sample;
import ch.systemsx.cisd.bds.ToStringBuilder;
import ch.systemsx.cisd.bds.Utilities;
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
    public static final String GROUP_CODE = ExperimentIdentifier.GROUP_CODE;

    public static final String INSTANCE_CODE = ExperimentIdentifier.INSTANCE_CODE;

    public static final String INSTANCE_UUID = ExperimentIdentifierWithUUID.INSTANCE_UUID;

    private final String groupCode;

    private final String instanceCode;

    private final String instanceUUID;

    /**
     * Creates an instance for the specified {@link Sample}, group code and database instance code
     * of the sample.
     * 
     * @param groupCode A non-<code>null</code> string of the group code. Could be empty.
     * @param instanceCode A non-<code>null</code> string of the database instance code. Could
     *            not be empty.
     * @param instanceUUID the database instance <i>UUID</i>. Could not be empty.
     */
    public SampleWithOwner(final Sample sample, final String instanceUUID,
            final String instanceCode, final String groupCode)
    {
        this(sample.getCode(), sample.getTypeCode(), sample.getTypeDescription(), instanceUUID,
                instanceCode, groupCode);
    }

    /**
     * Creates an instance for the specified code, type code, type description, group code and
     * database instance code of the sample.
     * 
     * @param groupCode A non-<code>null</code> string of the group code. Could be empty.
     * @param instanceCode A non-<code>null</code> string of the database instance code. Could
     *            not be empty.
     * @param instanceUUID A non-<code>null</code> string of the database instance <i>UUID</i>.
     *            Could not be empty.
     */
    public SampleWithOwner(final String code, final String typeCode, final String typeDescription,
            final String instanceUUID, final String instanceCode, final String groupCode)
    {
        super(code, typeCode, typeDescription);
        assert groupCode != null : "Undefined group code.";
        assert instanceCode != null : "Undefined database instance code.";
        assert instanceUUID != null : "Undefined database instance UUID.";
        assertNonEmptyInstanceCodes(instanceUUID, instanceCode);
        this.instanceUUID = instanceUUID;
        this.instanceCode = instanceCode;
        this.groupCode = groupCode;
    }

    private final static void assertNonEmptyInstanceCodes(final String instanceUUID,
            final String instanceCode)
    {
        if (instanceCode.length() == 0)
        {
            throw new DataStructureException("Empty database instance code.");
        }
        if (instanceUUID.length() == 0)
        {
            throw new DataStructureException("Empty database instance UUID.");
        }
    }

    public final String getGroupCode()
    {
        return groupCode;
    }

    public final String getInstanceCode()
    {
        return instanceCode;
    }

    public final String getInstanceUUID()
    {
        return instanceUUID;
    }

    //
    // Sample
    //

    /**
     * Loads the entity from the specified directory.
     * 
     * @throws DataStructureException if file missing.
     */
    public final static SampleWithOwner loadFrom(final IDirectory directory)
    {
        assert directory != null : "Unspecified directory";
        final IDirectory folder = Utilities.getSubDirectory(directory, FOLDER);
        final String typeDescription = Utilities.getTrimmedString(folder, TYPE_DESCRIPTION);
        final String code = Utilities.getTrimmedString(folder, CODE);
        final String typeCode = Utilities.getTrimmedString(folder, TYPE_CODE);
        final String groupCode = Utilities.getTrimmedString(folder, GROUP_CODE);
        final String instanceCode = Utilities.getTrimmedString(folder, INSTANCE_CODE);
        final String instanceUUID = Utilities.getTrimmedString(folder, INSTANCE_UUID);
        return new SampleWithOwner(code, typeCode, typeDescription, instanceUUID, instanceCode,
                groupCode);
    }

    @Override
    public final void saveTo(final IDirectory directory)
    {
        super.saveTo(directory);
        final IDirectory folder = directory.makeDirectory(FOLDER);
        folder.addKeyValuePair(GROUP_CODE, groupCode);
        folder.addKeyValuePair(INSTANCE_CODE, instanceCode);
        folder.addKeyValuePair(INSTANCE_UUID, instanceUUID);
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder = createToStringBuilder();
        builder.append(INSTANCE_UUID, instanceUUID);
        builder.append(INSTANCE_CODE, instanceCode);
        if (groupCode.length() > 0)
        {
            builder.append(GROUP_CODE, groupCode);
        }
        return builder.toString();
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof SampleWithOwner == false)
        {
            return false;
        }
        final SampleWithOwner that = (SampleWithOwner) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(that.getCode(), getCode());
        if (groupCode.length() > 1)
        {
            builder.append(that.groupCode, groupCode);
        } else
        {
            builder.append(that.instanceUUID, instanceUUID);
        }
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        if (groupCode.length() > 1)
        {
            builder.append(groupCode);
        } else
        {
            builder.append(instanceUUID);
        }
        return builder.toHashCode();
    }

}
