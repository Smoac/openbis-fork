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
            @NamedNativeQuery(name = "space_sample_access", query = "SELECT DISTINCT g.code as dummyId, g.code as groupCode, null as databaseInstanceCode "
                    + "FROM "
                    + TableNames.SAMPLES_TABLE
                    + " s, "
                    + TableNames.GROUPS_TABLE
                    + " g "
                    + "WHERE s.id in (:ids) and s.grou_id = g.id", resultSetMapping = "implicit1"),
            @NamedNativeQuery(name = "shared_sample_access", query = "SELECT DISTINCT dbi.code as dummyId, dbi.code as databaseInstanceCode, null as groupCode "
                    + "FROM "
                    + TableNames.SAMPLES_TABLE
                    + " s, "
                    + TableNames.DATABASE_INSTANCES_TABLE
                    + " dbi "
                    + "WHERE s.id in (:ids) and s.dbin_id = dbi.id", resultSetMapping = "implicit2")

    })
public class SampleAccessPE
{
    private String dummyId;

    private String groupCode;

    private String databaseInstanceCode;

    public final static String SPACE_SAMPLE_ACCESS_QUERY_NAME = "space_sample_access";

    public final static String SHARED_SAMPLE_ACCESS_QUERY_NAME = "shared_sample_access";

    public final static String SAMPLE_IDS_PARAMETER_NAME = "ids";

    /**
     * A factory method that should only be used for testing.
     */
    public static SampleAccessPE createSampleAccessPEForTest(String dataSetId, String dataSetCode,
            String groupCode, String databaseInstanceCode)
    {
        SampleAccessPE newMe = new SampleAccessPE();
        newMe.setGroupCode(groupCode);
        newMe.setDatabaseInstanceCode(databaseInstanceCode);
        return newMe;
    }

    // WORKAROUND we need a dummy id that is not null
    // otherwise null will be returned instead of entity when listing
    @Id
    String getDummyId()
    {
        return dummyId;
    }

    public String getGroupCode()
    {
        return groupCode;
    }

    public String getDatabaseInstanceCode()
    {
        return databaseInstanceCode;
    }

    void setDummyId(String dummyId)
    {
        this.dummyId = dummyId;
    }

    void setGroupCode(String groupCode)
    {
        this.groupCode = groupCode;
    }

    void setDatabaseInstanceCode(String databaseInstanceCode)
    {
        this.databaseInstanceCode = databaseInstanceCode;
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
        builder.append(getGroupCode(), that.getGroupCode());
        builder.append(getDatabaseInstanceCode(), that.getDatabaseInstanceCode());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getGroupCode());
        builder.append(getDatabaseInstanceCode());
        return builder.toHashCode();
    }
}
