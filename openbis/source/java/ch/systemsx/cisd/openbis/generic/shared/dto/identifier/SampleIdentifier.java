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

import ch.systemsx.cisd.common.exceptions.InternalErr;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Identifies a sample.
 * 
 * @author Izabela Adamczyk
 * @author Tomasz Pylak
 */
public class SampleIdentifier extends SampleOwnerIdentifier
{
    private static final long serialVersionUID = IServer.VERSION;

    public static final String CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING = ":";

    public static final SampleIdentifier[] EMPTY_ARRAY = new SampleIdentifier[0];

    private String sampleCode;

    private String sampleSubCode;

    private SampleIdentifier(final DatabaseInstanceIdentifier databaseInstanceIdentOrNull,
            final SpaceIdentifier spaceIdentOrNull, final String sampleCode)
    {
        super(databaseInstanceIdentOrNull, spaceIdentOrNull);
        setSampleCode(sampleCode);
    }

    public static SampleIdentifier createOwnedBy(final SampleOwnerIdentifier owner,
            final String sampleCode)
    {
        if (owner.isDatabaseInstanceLevel())
        {
            return new SampleIdentifier(owner.getDatabaseInstanceLevel(), sampleCode);
        } else if (owner.isSpaceLevel())
        {
            return new SampleIdentifier(owner.getSpaceLevel(), sampleCode);
        } else
        {
            throw InternalErr.error();
        }
    }

    /** Database-instance level {@link SampleIdentifier}. */
    public SampleIdentifier(final DatabaseInstanceIdentifier instanceIdentifier,
            final String sampleCode)
    {
        this(instanceIdentifier, null, sampleCode);
    }

    /** Group level {@link SampleIdentifier}. */
    public SampleIdentifier(final SpaceIdentifier spaceIdentifier, final String sampleCode)
    {
        this(null, spaceIdentifier, sampleCode);
    }

    /** Home group level {@link SampleIdentifier} with type. */
    public static SampleIdentifier createHomeGroup(final String sampleCode)
    {
        return new SampleIdentifier(GroupIdentifier.createHome(), sampleCode);
    }

    public String getSampleCode()
    {
        return sampleCode;
    }

    public String getSampleSubCode()
    {
        return sampleSubCode;
    }

    /**
     * Returns an object that only contains the owner information of this sample identifier.
     * {@link #hashCode()} will be the same when called on two different samples with same owner.
     */
    public SampleOwnerIdentifier createSampleOwnerIdentifier()
    {
        return new SampleOwnerIdentifier(getDatabaseInstanceLevel(), getSpaceLevel());
    }

    @Override
    public String toString()
    {
        return super.toString() + sampleCode;
    }

    @Deprecated
    public void setSampleCode(final String sampleCode)
    {
        this.sampleCode = sampleCode;
        if (sampleCode != null) // for tests
        {
            String[] sampleCodeTokens = sampleCode.split(CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING);
            this.sampleSubCode = sampleCodeTokens[sampleCodeTokens.length - 1];
        } else
        {
            this.sampleSubCode = sampleCode;
        }
    }

    public void addContainerCode(final String containerCode)
    {
        assert containerCode != null;
        assert containerCode.contains(CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING) == false;
        assert sampleSubCode.equals(sampleCode);
        setSampleCode(containerCode + CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING + sampleSubCode);
    }

    // for bean conversion only!
    @Deprecated
    public SampleIdentifier()
    {
        super(null, null);
    }

    //
    // Comparable
    //

    @Override
    public final int compareTo(final SampleOwnerIdentifier o)
    {
        final int ownerCmp = super.compareTo(o);
        if (ownerCmp == 0)
        {
            return sampleCode.compareTo(((SampleIdentifier) o).sampleCode);
        } else
        {
            return ownerCmp;
        }
    }
}
