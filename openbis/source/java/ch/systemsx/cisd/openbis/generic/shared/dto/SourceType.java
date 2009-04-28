/*
 * Copyright 2008 ETH Zuerich, CISD
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

/**
 * The type of source represented by given <code>Sample</code> id.
 * <p>
 * This will specify if we have to look for field <code>SAMP_ID_ACQUIRED_FROM</code> or field
 * <code>SAMP_ID_DERIVED_FROM</code> in the database. In the database only one of this field is set.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public enum SourceType
{
    MEASUREMENT("sampleAcquiredFromInternal")
    {
        @Override
        public final void setSample(final DataPE data, final SamplePE sample)
        {
            data.setSampleAcquiredFrom(sample);
        }
    },
    DERIVED("sampleDerivedFromInternal")
    {
        @Override
        public final void setSample(final DataPE data, final SamplePE sample)
        {
            data.setSampleDerivedFrom(sample);
        }
    };

    private final String fieldName;

    private SourceType(final String fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * This is the field name in corresponding {@link DataPE}.
     */
    public final String getFieldName()
    {
        return fieldName;
    }

    /**
     * Nullifies the database field <code>SAMP_ID_DERIVED_FROM</code>
     */
    public final void nullifyProducerSample(final DataPE data)
    {
        data.setSampleDerivedFrom(null);
    }

    /**
     * Sets the right sample to given <var>data</var>.
     */
    public abstract void setSample(final DataPE data, final SamplePE sample);
}