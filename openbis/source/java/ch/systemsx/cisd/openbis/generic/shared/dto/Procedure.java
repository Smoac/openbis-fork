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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Kind of <i>Java Bean</i> or <i>Value Object</i> which transports through Web Service any
 * information we would like to know about a procedure.
 * <p>
 * <i>Note that equality, hash code calculation and comparison is based on the code of the
 * {@link ProcedureType} since the procedure itself does not have a code.</i>
 * 
 * @author Christian Ribeaud
 */
public final class Procedure implements Comparable<Procedure>, Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private Date registrationDate;

    private ProcedureType procedureType;

    private Experiment experiment;

    private Sample[] resultSamples = new Sample[0];

    private Sample[] inputSamples = new Sample[0];

    public final ProcedureType getProcedureType()
    {
        return procedureType;
    }

    public final void setProcedureType(ProcedureType procedureType)
    {
        this.procedureType = procedureType;
    }

    /**
     * Returns registrationDate.
     * 
     * @return <code>null</code> when undefined.
     */
    public final Date getRegistrationDate()
    {
        return registrationDate;
    }

    /**
     * Sets registrationDate.
     * 
     * @param registrationDate New value. Can be <code>null</code>.
     */
    public final void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public final void setResultSamples(Sample[] samples)
    {
        this.resultSamples = samples;
    }

    /**
     * Returns experiment.
     * 
     * @return <code>null</code> when undefined.
     */
    public final Experiment getExperiment()
    {
        return experiment;
    }

    /**
     * Sets experiment.
     * 
     * @param experiment New value. Can be <code>null</code>.
     */
    public final void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    /**
     * Never returns <code>null</code> but could return an empty array.
     */
    public final Sample[] getResultSamples()
    {
        if (resultSamples == null)
        {
            return new Sample[0];
        }
        return resultSamples;
    }

    /**
     * Never returns <code>null</code> but could return an empty array.
     */
    public final Sample[] getInputSamples()
    {
        if (inputSamples == null)
        {
            return new Sample[0];
        }
        return inputSamples;
    }

    public final void setInputSamples(Sample[] inputSamples)
    {
        this.inputSamples = inputSamples;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Procedure == false)
        {
            return false;
        }
        Procedure that = (Procedure) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(that.registrationDate, registrationDate);
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(registrationDate);
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }

    //
    // Comparable
    //

    /**
     * If <code>null</code> values are present for <code>code</code>, then they come first.
     */
    public final int compareTo(Procedure o)
    {
        final String code = procedureType.getCode();
        final String thatCode = o.procedureType.getCode();
        if (code == null)
        {
            return thatCode == null ? 0 : -1;
        }
        if (thatCode == null)
        {
            return 1;
        }
        return code.compareTo(thatCode);
    }
}