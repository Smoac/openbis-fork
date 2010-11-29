/*
 * Copyright 2010 ETH Zuerich, CISD
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

import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A PE for retrieving only the information necessary to determine if a user/person can access a
 * sample.
 * 
 * @author Piotr Buczek
 */
@Entity
@SqlResultSetMappings(value =
    {
            @SqlResultSetMapping(name = "implicit1", entities = @EntityResult(entityClass = SampleAccessPE.class)),
            @SqlResultSetMapping(name = "implicit2", entities = @EntityResult(entityClass = SampleAccessPE.class))

    })
@NamedNativeQueries(value =
    {
            @NamedNativeQuery(name = "space_sample_access", query = "SELECT DISTINCT g.code as ownerCode, 'SPACE' as ownerType "
                    + "FROM "
                    + TableNames.SAMPLES_TABLE
                    + " s, "
                    + TableNames.SPACES_TABLE
                    + " g "
                    + "WHERE s.id in (:ids) and s.space_id = g.id", resultSetMapping = "implicit1"),
            @NamedNativeQuery(name = "shared_sample_access", query = "SELECT DISTINCT dbi.code as ownerCode, 'DATABASE_INSTANCE' as ownerType "
                    + "FROM "
                    + TableNames.SAMPLES_TABLE
                    + " s, "
                    + TableNames.DATABASE_INSTANCES_TABLE
                    + " dbi "
                    + "WHERE s.id in (:ids) and s.dbin_id = dbi.id", resultSetMapping = "implicit2")

    })
public class SampleAccessPE
{

    public final static String SPACE_SAMPLE_ACCESS_QUERY_NAME = "space_sample_access";

    public final static String SHARED_SAMPLE_ACCESS_QUERY_NAME = "shared_sample_access";

    public final static String SAMPLE_IDS_PARAMETER_NAME = "ids";

    public enum SampleOwnerType
    {
        SPACE, DATABASE_INSTANCE
    }

    private String ownerCode;

    private SampleOwnerType ownerType;

    /**
     * A factory method that should only be used for testing.
     */
    public static SampleAccessPE createSpaceSampleAccessPEForTest(String dataSetId,
            String dataSetCode, String groupCode)
    {
        SampleAccessPE newMe = new SampleAccessPE();
        newMe.setOwnerType(SampleOwnerType.SPACE);
        newMe.setOwnerCode(groupCode);
        return newMe;
    }

    /**
     * A factory method that should only be used for testing.
     */
    public static SampleAccessPE createSharedSampleAccessPEForTest(String dataSetId,
            String dataSetCode, String databaseInstanceCode)
    {
        SampleAccessPE newMe = new SampleAccessPE();
        newMe.setOwnerType(SampleOwnerType.DATABASE_INSTANCE);
        newMe.setOwnerCode(databaseInstanceCode);
        return newMe;
    }

    @Id
    public String getOwnerCode()
    {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode)
    {
        this.ownerCode = ownerCode;
    }

    public SampleOwnerType getOwnerType()
    {
        return ownerType;
    }

    public void setOwnerType(SampleOwnerType ownerType)
    {
        this.ownerType = ownerType;
    }

    //
    // Object
    //

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof SampleAccessPE == false)
        {
            return false;
        }
        final SampleAccessPE that = (SampleAccessPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getOwnerType(), that.getOwnerType());
        builder.append(getOwnerCode(), that.getOwnerCode());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getOwnerType());
        builder.append(getOwnerCode());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

}
