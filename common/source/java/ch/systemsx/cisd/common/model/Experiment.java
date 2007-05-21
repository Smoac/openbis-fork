/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Kind of <i>Java Bean</i> or <i>Value Object</i> which contains any information we would like to know about one
 * experiment.
 * <p>
 * This class is the <i>Java Object</i> representation of the corresponding data in the database.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class Experiment implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier this experiment as defined in and by the database.
     */
    private long id;

    /** Name of the experiment. */
    private String name;

    /** Description of the experiment. */
    private String description;

    /**
     * Registration date of the experiment.
     * <p>
     * It is represented by a millisecond value that is an offset from the <em>Epoch</em>, January 1, 1970
     * 00:00:00.000 GMT (Gregorian).
     * </p>
     */
    private long registrationDate;

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(String description)
    {
        this.description = description;
    }

    public final long getId()
    {
        return id;
    }

    public final void setId(long id)
    {
        this.id = id;
    }

    public final String getName()
    {
        return name;
    }

    public final void setName(String name)
    {
        this.name = name;
    }

    public final long getRegistrationDate()
    {
        return registrationDate;
    }

    public final void setRegistrationDate(long registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    ///////////////////////////////////////////////////////
    // Object
    ///////////////////////////////////////////////////////

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
    
    @Override
    public final boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Experiment == false)
        {
            return false;
        }
        Experiment that = (Experiment) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(that.id, id);
        return builder.isEquals();
    }
    
    @Override
    public final int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(id);
        return builder.toHashCode();
    }
}
