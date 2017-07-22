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

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A PE for retrieving only the information necessary to determine if a user/person can access a sample.
 * 
 * @author Piotr Buczek
 */
@Entity
@SqlResultSetMapping(name = SampleAccessPE.RESULT_SET_MAPPING, entities = @EntityResult(entityClass = SampleAccessPE.class))
@NamedNativeQueries(value = {
        @NamedNativeQuery(name = SampleAccessPE.SAMPLE_ACCESS_QUERY_NAME, query = SampleAccessPE.SAMPLE_ACCESS_QUERY, resultSetMapping = SampleAccessPE.RESULT_SET_MAPPING),
        @NamedNativeQuery(name = SampleAccessPE.DELETED_SAMPLE_ACCESS_QUERY_NAME, query = SampleAccessPE.DELETED_SAMPLE_ACCESS_QUERY, resultSetMapping = SampleAccessPE.RESULT_SET_MAPPING) })
public class SampleAccessPE
{

    public final static String SAMPLE_ACCESS_QUERY =
            "SELECT g.code as spaceCode, p.code as projectCode, ep.code as experimentProjectCode, e.code as experimentCode, s.code as sampleCode, c.code as containerCode "
                    + "FROM "
                    + TableNames.SAMPLES_VIEW
                    + " s left outer join "
                    + TableNames.SPACES_TABLE
                    + " g on s.space_id = g.id left outer join "
                    + TableNames.PROJECTS_TABLE
                    + " p on s.proj_id = p.id left outer join "
                    + TableNames.SAMPLES_ALL_TABLE
                    + " c on s.samp_id_part_of = c.id left outer join "
                    + TableNames.EXPERIMENTS_ALL_TABLE
                    + " e on s.expe_id = e.id left outer join "
                    + TableNames.PROJECTS_TABLE
                    + " ep on e.proj_id = ep.id "
                    + "WHERE s.id in (:ids)";

    public final static String DELETED_SAMPLE_ACCESS_QUERY =
            "SELECT g.code as spaceCode, p.code as projectCode, ep.code as experimentProjectCode, e.code as experimentCode, s.code as sampleCode, c.code as containerCode "
                    + "FROM "
                    + TableNames.DELETED_SAMPLES_VIEW
                    + " s left outer join "
                    + TableNames.SPACES_TABLE
                    + " g on s.space_id = g.id left outer join "
                    + TableNames.PROJECTS_TABLE
                    + " p on s.proj_id = p.id left outer join "
                    + TableNames.SAMPLES_ALL_TABLE
                    + " c on s.samp_id_part_of = c.id left outer join "
                    + TableNames.EXPERIMENTS_ALL_TABLE
                    + " e on s.expe_id = e.id left outer join "
                    + TableNames.PROJECTS_TABLE
                    + " ep on e.proj_id = ep.id "
                    + "WHERE s.del_id in (:del_ids)";

    public final static String SAMPLE_ACCESS_QUERY_NAME = "sample_access";

    public final static String DELETED_SAMPLE_ACCESS_QUERY_NAME = "deleted_sample_access";

    public final static String SAMPLE_IDS_PARAMETER_NAME = "ids";

    public final static String DELETION_IDS_PARAMETER_NAME = "del_ids";

    public final static String RESULT_SET_MAPPING = "sample_access_implicit";

    private String spaceCode;

    private String projectCode;

    private String experimentProjectCode;

    private String experimentCode;

    private String sampleCode;

    private String containerCode;

    public String getSpaceCode()
    {
        return spaceCode;
    }

    public void setSpaceCode(String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public void setProjectCode(String projectCode)
    {
        this.projectCode = projectCode;
    }

    public String getExperimentProjectCode()
    {
        return experimentProjectCode;
    }

    public void setExperimentProjectCode(String experimentProjectCode)
    {
        this.experimentProjectCode = experimentProjectCode;
    }

    public String getExperimentCode()
    {
        return experimentCode;
    }

    public void setExperimentCode(String experimentCode)
    {
        this.experimentCode = experimentCode;
    }

    @Id
    public String getSampleCode()
    {
        return sampleCode;
    }

    public void setSampleCode(String sampleCode)
    {
        this.sampleCode = sampleCode;
    }

    public String getContainerCode()
    {
        return containerCode;
    }

    public void setContainerCode(String containerCode)
    {
        this.containerCode = containerCode;
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
        builder.append(getSpaceCode(), that.getSpaceCode());
        builder.append(getProjectCode(), that.getProjectCode());
        builder.append(getExperimentProjectCode(), that.getExperimentProjectCode());
        builder.append(getExperimentCode(), that.getExperimentCode());
        builder.append(getSampleCode(), that.getSampleCode());
        builder.append(getContainerCode(), that.getContainerCode());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getSpaceCode());
        builder.append(getProjectCode());
        builder.append(getExperimentProjectCode());
        builder.append(getExperimentCode());
        builder.append(getSampleCode());
        builder.append(getContainerCode());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

}
