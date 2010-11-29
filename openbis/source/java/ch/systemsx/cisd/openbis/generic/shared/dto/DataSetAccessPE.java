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
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A PE for retrieving only the information necessary to determine if a user/person can access a
 * data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@Entity
@SqlResultSetMapping(name = "implicit", entities = @EntityResult(entityClass = DataSetAccessPE.class))
@NamedNativeQuery(name = "dataset_access", query = "select "
        + "g.code as groupCode, dbi.uuid as databaseInstanceUuid, dbi.code as databaseInstanceCode "
        + "from " + TableNames.PROJECTS_TABLE + " p, " + TableNames.SPACES_TABLE + " g, "
        + TableNames.DATABASE_INSTANCES_TABLE + " dbi " + "where p.id in "
        + "(select e.proj_id from " + TableNames.DATA_TABLE + " ds, "
        + TableNames.EXPERIMENTS_TABLE + " e "
        + "where ds.code in (:codes) and ds.expe_id = e.id group by e.proj_id) "
        + "and p.space_id = g.id and dbi.id = g.dbin_id", resultSetMapping = "implicit")
public class DataSetAccessPE
{
    private String spaceCode;

    private String databaseInstanceUuid;

    private String databaseInstanceCode;

    public final static String DATASET_ACCESS_QUERY_NAME = "dataset_access";

    public final static String DATA_SET_CODES_PARAMETER_NAME = "codes";

    /**
     * A factory method that should only be used for testing.
     */
    public static DataSetAccessPE createDataSetAccessPEForTest(String dataSetId,
            String dataSetCode, String groupCode, String databaseInstanceUuid,
            String databaseInstanceCode)
    {
        DataSetAccessPE newMe = new DataSetAccessPE();
        newMe.setSpaceCode(groupCode);
        newMe.setDatabaseInstanceUuid(databaseInstanceUuid);
        newMe.setDatabaseInstanceCode(databaseInstanceCode);
        return newMe;
    }

    void setSpaceCode(String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    void setDatabaseInstanceUuid(String databaseInstanceUuid)
    {
        this.databaseInstanceUuid = databaseInstanceUuid;
    }

    void setDatabaseInstanceCode(String databaseInstanceCode)
    {
        this.databaseInstanceCode = databaseInstanceCode;
    }

    @Id
    public String getSpaceCode()
    {
        return spaceCode;
    }

    public String getDatabaseInstanceUuid()
    {
        return databaseInstanceUuid;
    }

    public String getDatabaseInstanceCode()
    {
        return databaseInstanceCode;
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
        if (obj instanceof DataSetAccessPE == false)
        {
            return false;
        }
        final DataSetAccessPE that = (DataSetAccessPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getSpaceCode(), that.getSpaceCode());
        builder.append(getDatabaseInstanceCode(), that.getDatabaseInstanceCode());
        builder.append(getDatabaseInstanceUuid(), that.getDatabaseInstanceUuid());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getSpaceCode());
        builder.append(getDatabaseInstanceCode());
        builder.append(getDatabaseInstanceUuid());
        return builder.toHashCode();
    }
}
