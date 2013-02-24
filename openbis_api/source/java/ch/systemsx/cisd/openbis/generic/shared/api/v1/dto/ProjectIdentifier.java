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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;

/**
 * Unique identifier for a project in openBIS.
 * 
 * @author Bernd Rinn
 */
@SuppressWarnings("unused")
@JsonObject("ProjectIdentifier")
public class ProjectIdentifier implements IPermanentIdentifier, IDatabaseIdentifier
{
    private static final long serialVersionUID = 1L;

    private Long databaseId;

    private String permId;

    private String spaceCode;

    private String code;

    /**
     * Creates an {@link ProjectIdentifier} from the given <var>augmentedCode</code>.
     * 
     * @param augmentedCode The <var>augmentedCode</code> in the form
     *            <code>/SPACE/PROJECT</code>. 
     * @return A sample identifier corresponding to <var>augmentedCode</code>. Note that this
     *         sample identifier has no perm id set.
     * @throws IllegalArgumentException If the <var>augmentedCode</code> is not in the form
     *             <code>/SPACE/SAMPLE</code> or <code>/SAMPLE</code>.
     */
    public static ProjectIdentifier createFromAugmentedCode(String augmentedCode)
            throws IllegalArgumentException
    {
        final String[] splitted = augmentedCode.split("/");
        if (splitted.length == 3 && splitted[0].length() == 0)
        {
            return new ProjectIdentifier(null, null, splitted[2], splitted[1]);
        }
        throw new IllegalArgumentException("Augmented code '" + augmentedCode
                + "' needs to be of the form '/SPACE/PROJECT'.");
    }

    /**
     * Creates an {@link ProjectIdentifier} from the given <var>permId</code>.
     * 
     * @param permId The <var>permId</code>
     * @return An identifier corresponding to <var>permId</code>. Note that this
     *         identifier has no code, project or space information.
     */
    public static ProjectIdentifier createFromPermId(String permId)
            throws IllegalArgumentException
    {
        return new ProjectIdentifier(null, permId, null, null);
    }
    
    /**
     * Creates an {@link ProjectIdentifier} from the given <var>entity</code>.
     * 
     * @param entity The entity as received from one of the other methods.
     * @return An identifier corresponding to <var>entity</code>. Note that this
     *         identifier has no perm id, code, project or space information.
     */
    public static ProjectIdentifier createFromEntity(Project entity)
    {
        return new ProjectIdentifier(entity.getId(), null, null, null);
    }

    private ProjectIdentifier(Long databaseId, String permId, String sampleCode,
            String spaceCode)
    {
        this.databaseId = databaseId;
        this.permId = permId;
        this.spaceCode = spaceCode;
        this.code = sampleCode;
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
     * The code of the space of this project.
     */
    public String getSpaceCode()
    {
        return spaceCode;
    }

    public String getCode()
    {
        return code;
    }

    /**
     * Returns the augmented (full) code of this project.
     */
    @JsonIgnore
    public String getAugmentedCode()
    {
        if (code == null)
        {
            return null;
        }
        return "/" + spaceCode + "/" + code;
    }

    //
    // JSON-RPC
    //

    private ProjectIdentifier()
    {
    }

    private void setSpaceCode(String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    private void setCode(String code)
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
