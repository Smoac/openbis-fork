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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;

/**
 * Unique identifier for an experiment in openBIS.
 * 
 * @author Bernd Rinn
 */
@SuppressWarnings("unused")
@JsonObject("GenericExperimentIdentifier")
public class ExperimentIdentifier implements Serializable, IPermanentIdentifier, IDatabaseIdentifier
{
    private static final long serialVersionUID = 1L;

    private Long databaseId;

    private String permId;

    private String spaceCode;

    private String projectCode;

    private String code;

    /**
     * Creates an {@link ExperimentIdentifier} from the given <var>augmentedCode</code>.
     * 
     * @param augmentedCode The <var>augmentedCode</code> in the form
     *            <code>/SPACE/PROJECT/EXPERIMENT</code>
     * @return An experiment identifer corresponding to <var>augmentedCode</code>. Note that this
     *         experiment identifier has no perm id or database id set.
     * @throws IllegalArgumentException If the <var>augmentedCode</code> is not in the form
     *             <code>/SPACE/PROJECT/EXPERIMENT</code>.
     */
    public static ExperimentIdentifier createFromAugmentedCode(String augmentedCode)
            throws IllegalArgumentException
    {
        final String[] splitted = augmentedCode.split("/");
        if (splitted.length != 4 || splitted[0].length() != 0)
        {
            throw new IllegalArgumentException("Augmented code '" + augmentedCode
                    + "' needs to be either of the form '/SPACE/PROJECT/EXPERIMENT' "
                    + "or 'PROJECT/EXPERIMENT'.");
        }
        if (StringUtils.isBlank(splitted[3]))
        {
            throw new IllegalArgumentException("No code given.");
        }
        if (StringUtils.isBlank(splitted[2]))
        {
            throw new IllegalArgumentException("No project code given.");
        }
        if (StringUtils.isBlank(splitted[1]))
        {
            throw new IllegalArgumentException("No space code given.");
        }
        return new ExperimentIdentifier(null, null, splitted[3], splitted[2], splitted[1]);
    }

    /**
     * Creates an {@link ExperimentIdentifier} from the given <var>permId</code>.
     * 
     * @param permId The <var>permId</code>
     * @return An experiment identifier corresponding to <var>permId</code>. Note that this
     *         experiment identifier has no code, project or space information.
     */
    public static ExperimentIdentifier createFromPermId(String permId)
            throws IllegalArgumentException
    {
        if (StringUtils.isBlank(permId))
        {
            throw new IllegalArgumentException("No perm id given.");
        }
        return new ExperimentIdentifier(null, permId, null, null, null);
    }
    
    /**
     * Creates an {@link ExperimentIdentifier} from the given <var>entity</code>.
     * 
     * @param entity The entity as received from one of the other methods.
     * @return An experiment identifier corresponding to <var>entity</code>. Note that this
     *         experiment identifier has no permid, code, project or space information.
     */
    public static ExperimentIdentifier createFromEntity(Experiment entity)
    {
        return new ExperimentIdentifier(entity.getId(), null, null, null, null);
    }

    /**
     * A <code>spaceCode == null</code> is interpreted as the home space.
     */
    private ExperimentIdentifier(Long databaseId, String permId, String code,
            String projectCode, String spaceCode)
    {
        this.databaseId = databaseId;
        this.permId = permId;
        this.spaceCode = spaceCode;
        this.projectCode = projectCode;
        this.code = code;
    }

    @Override
    public Long getDatabaseId()
    {
        return databaseId;
    }

    @Override
    public String getPermId()
    {
        return permId;
    }

    /**
     * The code of the space of this experiment.
     */
    public String getSpaceCode()
    {
        return spaceCode;
    }

    /**
     * The code of the project of this experiment.
     */
    public String getProjectCode()
    {
        return projectCode;
    }

    /**
     * The experiment code.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Returns the augmented (full) code of this experiment.
     */
    @JsonIgnore
    public String getAugmentedCode()
    {
        if (code == null)
        {
            return null;
        }
        return "/" + spaceCode + "/" + projectCode + "/" + code;
    }

    //
    // JSON-RPC
    //

    private ExperimentIdentifier()
    {
    }

    private void setSpaceCode(String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    private void setProjectCode(String projectCode)
    {
        this.projectCode = projectCode;
    }

    private void setcode(String code)
    {
        this.code = code;
    }

    private void setPermId(String permId)
    {
        this.permId = permId;
    }

    private void setDatabaseId(Long databaseId)
    {
        this.databaseId = databaseId;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public final int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
