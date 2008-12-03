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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * The <i>GWT</i> equivalent to {@link ExperimentPE}.
 * 
 * @author Tomasz Pylak
 */
public class Experiment extends CodeWithRegistration<Experiment>
{
    private Project project;

    private ExperimentType experimentType;

    private String experimentIdentifier;

    private List<ExperimentProperty> properties;

    private Invalidation invalidation;

    public Project getProject()
    {
        return project;
    }

    public void setProject(final Project project)
    {
        this.project = project;
    }

    public ExperimentType getExperimentType()
    {
        return experimentType;
    }

    public void setExperimentType(final ExperimentType experimentType)
    {
        this.experimentType = experimentType;
    }

    public final String getExperimentIdentifier()
    {
        return experimentIdentifier;
    }

    public final void setExperimentIdentifier(final String experimentIdentifier)
    {
        this.experimentIdentifier = experimentIdentifier;
    }

    //
    // Comparable
    //

    @Override
    public final int compareTo(final Experiment o)
    {
        return getExperimentIdentifier().compareTo(o.getExperimentIdentifier());
    }

    public List<ExperimentProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(final List<ExperimentProperty> properties)
    {
        this.properties = properties;
    }

    public Invalidation getInvalidation()
    {
        return invalidation;
    }

    public void setInvalidation(final Invalidation invalidation)
    {
        this.invalidation = invalidation;
    }
}
