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

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Kind of <i>Java Bean</i> or <i>Value Object</i> which transports through Web Service any
 * information we would like to know about a sample.
 * 
 * @author Christian Ribeaud
 */
public final class Sample implements Serializable, Comparable<Sample>, ISimpleEntityPropertiesHolder
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private SampleIdentifier sampleIdentifier;

    private SampleType sampleType;

    private PersonPE registrator;

    private ExternalData[] measurementData = new ExternalData[0];

    private ExternalData[] derivedData = new ExternalData[0];

    private Procedure procedure;

    private Sample generatedFrom;

    private Sample top;

    private InvalidationPE invalidationOrNull;

    /**
     * The identifier of control layout assigned to the sample or null if nothing is assigned.
     */
    private SampleIdentifier controlLayoutIdentifierOrNull;

    /** Registration date of this sample. */
    private Date registrationDate;

    private SimpleEntityProperty[] properties;

    public EntityType getEntityType()
    {
        return getSampleType();
    }

    public final SampleType getSampleType()
    {
        return sampleType;
    }

    public final void setSampleType(final SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    /**
     * Returns registrator.
     * 
     * @return <code>null</code> when undefined.
     */
    public final PersonPE getRegistrator()
    {
        return registrator;
    }

    /**
     * Sets registrator.
     * 
     * @param registrator New value. Can be <code>null</code>.
     */
    public final void setRegistrator(final PersonPE registrator)
    {
        this.registrator = registrator;
    }

    public final Procedure getProcedure()
    {
        return procedure;
    }

    public final void setProcedure(final Procedure procedure)
    {
        this.procedure = procedure;
    }

    /**
     * Never returns <code>null</code> but could return an empty array of
     * <code>ExternalData</code>.
     */
    public final ExternalData[] getMeasurementData()
    {
        if (measurementData == null)
        {
            return new ExternalData[0];
        }
        return measurementData;
    }

    /**
     * Sets <code>measurementData</code>.
     * 
     * @param externalData new value. Could be <code>null</code>.
     */
    public final void setMeasurementData(final ExternalData[] externalData)
    {
        this.measurementData = externalData;
    }

    /**
     * Never returns <code>null</code> but could return an empty array of
     * <code>ExternalData</code>.
     */
    public final ExternalData[] getDerivedData()
    {
        if (derivedData == null)
        {
            return new ExternalData[0];
        }
        return derivedData;
    }

    /**
     * Sets <code>derivedData</code>.
     * 
     * @param externalData new value. Could be <code>null</code>.
     */
    public final void setDerivedData(final ExternalData[] externalData)
    {
        this.derivedData = externalData;
    }

    public final Sample getGeneratedFrom()
    {
        return generatedFrom;
    }

    public final void setGeneratedFrom(final Sample generatedFrom)
    {
        this.generatedFrom = generatedFrom;
    }

    public final Sample getTop()
    {
        return top;
    }

    public final void setTop(final Sample top)
    {
        this.top = top;
    }

    public final Date getRegistrationDate()
    {
        return registrationDate;
    }

    public final void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    /** Returns invalidation data or null */
    public InvalidationPE getInvalidation()
    {
        return invalidationOrNull;
    }

    public void setInvalidation(final InvalidationPE invalidation)
    {
        this.invalidationOrNull = invalidation;
    }

    public SampleIdentifier getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    public void setSampleIdentifier(SampleIdentifier sampleIdentifier)
    {
        this.sampleIdentifier = sampleIdentifier;
    }

    /** can be null */
    public final SampleIdentifier getControlLayoutIdentifier()
    {
        return controlLayoutIdentifierOrNull;
    }

    public final void setControlLayoutIdentifier(SampleIdentifier controlLayoutIdentifierOrNull)
    {
        this.controlLayoutIdentifierOrNull = controlLayoutIdentifierOrNull;
    }

    //
    // IEntityPropertiesHolder
    //

    public final SimpleEntityProperty[] getProperties()
    {
        return properties;
    }

    public final void setProperties(final SimpleEntityProperty[] properties)
    {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || obj instanceof Sample == false)
        {
            return false;
        }
        Sample sample = (Sample) obj;
        return sampleIdentifier.equals(sample.getSampleIdentifier());
    }

    @Override
    public int hashCode()
    {
        return sampleIdentifier == null ? 43 : sampleIdentifier.hashCode();
    }

    public int compareTo(Sample sample)
    {
        SampleIdentifier thatIdent = sample.getSampleIdentifier();
        SampleIdentifier thisIdent = getSampleIdentifier();
        if (thisIdent == null)
        {
            return thatIdent == null ? 0 : -1;
        }
        if (thatIdent == null)
        {
            return 1;
        }

        return thisIdent.compareTo(thatIdent);
    }
}
