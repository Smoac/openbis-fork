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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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
    static final String GROUP_CODE = ExperimentIdentifier.GROUP_CODE;

    static final String INSTANCE_CODE = ExperimentIdentifier.INSTANCE_CODE;

    static final String INSTANCE_GLOBAL_CODE = ExperimentIdentifier.INSTANCE_GLOBAL_CODE;

    private final String groupCode;

    private final String instanceCode;

    private final String instanceGlobalCode;

    /**
     * Creates an instance for the specified {@link Sample}, group code and database instance code
     * of the sample.
     * 
     * @param groupCode A non-<code>null</code> string of the group code. Could be empty.
     * @param instanceCode A non-<code>null</code> string of the database instance code. Could
     *            not be empty.
     * @param instanceGlobalCode A non-<code>null</code> string of the database instance global
     *            code (aka <i>UUID</i>). Could not be empty.
     */
    public SampleWithOwner(final Sample sample, final String instanceGlobalCode,
            final String instanceCode, final String groupCode)
    {
        this(sample.getCode(), sample.getTypeCode(), sample.getTypeDescription(),
                instanceGlobalCode, instanceCode, groupCode);
    }

    /**
     * Creates an instance for the specified code, type code, type description, group code and
     * database instance code of the sample.
     * 
     * @param groupCode A non-<code>null</code> string of the group code. Could be empty.
     * @param instanceCode A non-<code>null</code> string of the database instance code. Could
     *            not be empty.
     * @param instanceGlobalCode A non-<code>null</code> string of the database instance global
     *            code (aka <i>UUID</i>). Could not be empty.
     */
    public SampleWithOwner(final String code, final String typeCode, final String typeDescription,
            final String instanceGlobalCode, final String instanceCode, final String groupCode)
    {
        super(code, typeCode, typeDescription);
        assert groupCode != null : "Undefined group code.";
        assert instanceCode != null : "Undefined database instance code.";
        assert instanceGlobalCode != null : "Undefined database instance global code.";
        assertNonEmptyInstanceCodes(instanceGlobalCode, instanceCode);
        this.instanceGlobalCode = instanceGlobalCode;
        this.instanceCode = instanceCode;
        this.groupCode = groupCode;
    }

    private final static void assertNonEmptyInstanceCodes(final String instanceGlobalCode,
            final String instanceCode)
    {
        if (instanceCode.length() == 0)
        {
            throw new DataStructureException("Empty database instance code.");
        }
        if (instanceGlobalCode.length() == 0)
        {
            throw new DataStructureException("Empty database instance global code.");
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

    public final String getInstanceGlobalCode()
    {
        return instanceGlobalCode;
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
        final String instanceCode = Utilities.getTrimmedString(folder, INSTANCE_CODE);
        final String instanceGlobalCode = Utilities.getTrimmedString(folder, INSTANCE_GLOBAL_CODE);
        return new SampleWithOwner(code, typeCode, typeDescription, instanceGlobalCode,
                instanceCode, groupCode);
    }

    @Override
    public final void saveTo(final IDirectory directory)
    {
        super.saveTo(directory);
        final IDirectory folder = directory.makeDirectory(FOLDER);
        folder.addKeyValuePair(GROUP_CODE, groupCode);
        folder.addKeyValuePair(INSTANCE_CODE, instanceCode);
        folder.addKeyValuePair(INSTANCE_GLOBAL_CODE, instanceGlobalCode);
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder = createToStringBuilder();
        builder.append(INSTANCE_GLOBAL_CODE, instanceGlobalCode);
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
            builder.append(that.instanceGlobalCode, instanceGlobalCode);
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
            builder.append(instanceGlobalCode);
        }
        return builder.toHashCode();
    }

}
