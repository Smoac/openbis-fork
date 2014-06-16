/*
 * Copyright 2011 ETH Zuerich, CISD
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
 * @author Pawel Glyzewski
 */
@Entity
@SqlResultSetMapping(name = "experiment_access_implicit", entities = @EntityResult(entityClass = ExperimentAccessPE.class))
@NamedNativeQuery(name = "deleted_experiment_access", query = "select "
        + "g.code as spaceCode "
        + "from " + TableNames.PROJECTS_TABLE + " p, " + TableNames.SPACES_TABLE + " g where p.id in "
        + "(select e.proj_id from " + TableNames.DELETED_EXPERIMENTS_VIEW + " e "
        + "where e.del_id in (:del_ids)) " + "and p.space_id = g.id", resultSetMapping = "experiment_access_implicit")
public class ExperimentAccessPE
{
    private String spaceCode;

    public final static String DELETED_EXPERIMENT_ACCESS_QUERY_NAME = "deleted_experiment_access";

    public final static String DELETION_IDS_PARAMETER_NAME = "del_ids";

    void setSpaceCode(String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    @Id
    public String getSpaceCode()
    {
        return spaceCode;
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
        if (obj instanceof ExperimentAccessPE == false)
        {
            return false;
        }
        final ExperimentAccessPE that = (ExperimentAccessPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getSpaceCode(), that.getSpaceCode());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getSpaceCode());
        return builder.toHashCode();
    }
}
