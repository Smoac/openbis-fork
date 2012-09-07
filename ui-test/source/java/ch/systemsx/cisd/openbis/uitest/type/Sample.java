/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.type;

import java.util.Collection;
import java.util.Map;

import ch.systemsx.cisd.openbis.uitest.infra.Browsable;
import ch.systemsx.cisd.openbis.uitest.infra.EntityType;

/**
 * @author anttil
 */
public class Sample implements EntityType, Browsable
{

    private SampleType type;

    private final String code;

    private Experiment experiment;

    private Space space;

    private Collection<Sample> parents;

    private Map<String, Object> properties;

    Sample(SampleType type, String code, Experiment experiment, Space space,
            Collection<Sample> parents, Map<String, Object> properties)
    {
        this.type = type;
        this.code = code;
        this.experiment = experiment;
        this.space = space;
        this.parents = parents;
        this.properties = properties;
    }

    @Override
    public boolean isRepresentedBy(Map<String, String> row)
    {
        return code.equalsIgnoreCase(row.get("Code"));
    }

    @Override
    public String getCode()
    {
        return code;
    }

    public SampleType getType()
    {
        return type;
    }

    public Experiment getExperiment()
    {
        return experiment;
    }

    public Space getSpace()
    {
        return space;
    }

    public Collection<Sample> getParents()
    {
        return parents;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    void setType(SampleType type)
    {
        this.type = type;
    }

    void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    void setSpace(Space space)
    {
        this.space = space;
    }

    void setParents(Collection<Sample> parents)
    {
        this.parents = parents;
    }

    void setProperties(Map<String, Object> properties)
    {
        this.properties = properties;
    }

}
