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
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Pawel Glyzewski
 */
@Entity
@SqlResultSetMapping(name = ExperimentAccessPE.RESULT_SET_MAPPING, entities = @EntityResult(entityClass = ExperimentAccessPE.class))
@NamedNativeQueries(value = {
        @NamedNativeQuery(name = ExperimentAccessPE.EXPERIMENT_ACCESS_QUERY_NAME, query = ExperimentAccessPE.EXPERIMENT_ACCESS_QUERY, resultSetMapping = ExperimentAccessPE.RESULT_SET_MAPPING),
        @NamedNativeQuery(name = ExperimentAccessPE.DELETED_EXPERIMENT_ACCESS_QUERY_NAME, query = ExperimentAccessPE.DELETED_EXPERIMENT_ACCESS_QUERY, resultSetMapping = ExperimentAccessPE.RESULT_SET_MAPPING)
})
public class ExperimentAccessPE
{

    public final static String EXPERIMENT_ACCESS_QUERY =
            "SELECT s.code as spaceCode, p.code as projectCode, e.code as experimentCode "
                    + "FROM "
                    + TableNames.EXPERIMENTS_VIEW
                    + " e inner join "
                    + TableNames.PROJECTS_TABLE
                    + " p on e.proj_id = p.id inner join "
                    + TableNames.SPACES_TABLE
                    + " s on p.space_id = s.id "
                    + "WHERE e.id in (:ids)";

    public final static String DELETED_EXPERIMENT_ACCESS_QUERY =
            "SELECT s.code as spaceCode, p.code as projectCode, e.code as experimentCode "
                    + "FROM "
                    + TableNames.DELETED_EXPERIMENTS_VIEW
                    + " e inner join "
                    + TableNames.PROJECTS_TABLE
                    + " p on e.proj_id = p.id inner join "
                    + TableNames.SPACES_TABLE
                    + " s on p.space_id = s.id "
                    + "WHERE e.del_id in (:del_ids)";

    private String spaceCode;

    private String projectCode;

    private String experimentCode;

    public final static String EXPERIMENT_ACCESS_QUERY_NAME = "experiment_access";

    public final static String DELETED_EXPERIMENT_ACCESS_QUERY_NAME = "deleted_experiment_access";

    public final static String EXPERIMENT_IDS_PARAMETER_NAME = "ids";

    public final static String DELETION_IDS_PARAMETER_NAME = "del_ids";

    public final static String RESULT_SET_MAPPING = "experiment_access_implicit";

    void setSpaceCode(String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    @Id
    public String getSpaceCode()
    {
        return spaceCode;
    }

    public void setProjectCode(String projectCode)
    {
        this.projectCode = projectCode;
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public void setExperimentCode(String experimentCode)
    {
        this.experimentCode = experimentCode;
    }

    public String getExperimentCode()
    {
        return experimentCode;
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
        builder.append(getProjectCode(), that.getProjectCode());
        builder.append(getExperimentCode(), that.getExperimentCode());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getSpaceCode());
        builder.append(getProjectCode());
        builder.append(getExperimentCode());
        return builder.toHashCode();
    }
}
