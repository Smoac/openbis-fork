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

import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier.Constants;

/**
 * Identifies an owner of a sample: space or database instance.
 * 
 * @author Tomasz Pylak
 */
public class SampleOwnerIdentifier extends AbstractHashable implements Serializable,
        Comparable<SampleOwnerIdentifier>
{
    private static final long serialVersionUID = IServer.VERSION;

    // if not null, sample is defined on the space level
    private SpaceIdentifier spaceIdentOrNull;

    /** Database-instance level {@link SampleOwnerIdentifier}. */
    public SampleOwnerIdentifier()
    {
    }

    /** Space level {@link SampleOwnerIdentifier}. */
    public SampleOwnerIdentifier(final SpaceIdentifier groupIdentifier)
    {
        checkNotNull(groupIdentifier);
        this.spaceIdentOrNull = groupIdentifier;
    }

    private static SpaceIdentifier checkNotNull(final SpaceIdentifier identifier)
    {
        assert identifier != null : "space identifier cannot be null";
        return identifier;
    }

    /** @return if sample is connected to its home space */
    public boolean isInsideHomeSpace()
    {
        return isSpaceLevel() && spaceIdentOrNull.isHomeSpace();
    }

    /**
     * true if sample belongs to a space. {@link #isDatabaseInstanceLevel()} will return false in such a case.
     */
    public boolean isSpaceLevel()
    {
        return spaceIdentOrNull != null;
    }

    /**
     * true if sample belongs to the database instance. {@link #isSpaceLevel()} will return false in such a case.
     */
    public boolean isDatabaseInstanceLevel()
    {
        return this.spaceIdentOrNull == null;
    }

    /**
     * String representation of this identifier.
     */
    @Override
    public String toString()
    {
        if (isSpaceLevel())
        {
            if (isInsideHomeSpace())
            {
                return "";
            } else
            {
                return spaceIdentOrNull.toString() + Constants.IDENTIFIER_SEPARATOR;
            }
        } else if (isDatabaseInstanceLevel())
        {
            return "" + Constants.IDENTIFIER_SEPARATOR;
        } else
        {
            throw new IllegalStateException("sample owner is unknown");
        }
    }

    /**
     * It is a good pattern to use {@link #isSpaceLevel()} before calling this method.
     * 
     * @return The space which is the owner or null if the owner is not a space, but database instance.
     */
    public SpaceIdentifier getSpaceLevel()
    {
        return spaceIdentOrNull;
    }

    //
    // Comparable
    //

    @Override
    public int compareTo(final SampleOwnerIdentifier other)
    {
        if (isSpaceLevel())
        {
            if (other.isSpaceLevel())
            {
                return getSpaceLevel().compareTo(other.getSpaceLevel());
            } else
            {
                return 1;
            }
        } else if (isDatabaseInstanceLevel())
        {
            if (other.isSpaceLevel())
            {
                return -1;
            } else
            {
                return 0;
            }
        } else
            throw new IllegalStateException("sample owner is unknown");
    }
}
