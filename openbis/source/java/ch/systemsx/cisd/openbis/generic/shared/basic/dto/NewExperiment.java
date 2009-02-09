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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;


/**
 * A experiment to register.
 * 
 * @author Izabela Adamczyk
 */
public final class NewExperiment extends Identifier<NewExperiment>
{
    private String experimentTypeCode;

    private String[] samples = new String[0];

    private ExperimentProperty[] properties = ExperimentProperty.EMPTY_ARRAY;

    public NewExperiment()
    {
    }

    public NewExperiment(final String identifier, final String experimentTypeCode)
    {
        setIdentifier(identifier);
        setExperimentTypeCode(experimentTypeCode);
    }

    public final String getExperimentTypeCode()
    {
        return experimentTypeCode;
    }

    public final void setExperimentTypeCode(final String experimentTypeCode)
    {
        this.experimentTypeCode = experimentTypeCode;
    }

    public final ExperimentProperty[] getProperties()
    {
        return properties;
    }

    public final void setProperties(final ExperimentProperty[] properties)
    {
        this.properties = properties;
    }

    @Override
    public final String toString()
    {
        return getIdentifier();
    }

    public String[] getSamples()
    {
        return samples;
    }

    public void setSamples(String[] samples)
    {
        this.samples = samples;
    }
}