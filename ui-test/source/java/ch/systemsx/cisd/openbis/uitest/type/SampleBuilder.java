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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ch.systemsx.cisd.openbis.uitest.infra.ApplicationRunner;

/**
 * @author anttil
 */
@SuppressWarnings("hiding")
public class SampleBuilder implements Builder<Sample>
{

    private ApplicationRunner openbis;

    private SampleType type;

    private String code;

    private Experiment experiment;

    private Space space;

    private Collection<Sample> parents;

    private Map<PropertyType, Object> properties;

    public SampleBuilder(ApplicationRunner openbis)
    {
        this.openbis = openbis;
        this.code = openbis.uid();
        this.properties = new HashMap<PropertyType, Object>();
        this.parents = new HashSet<Sample>();
    }

    public SampleBuilder ofType(SampleType type)
    {
        this.type = type;
        return this;
    }

    public SampleBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    public SampleBuilder in(Space space)
    {
        this.space = space;
        return this;
    }

    public SampleBuilder withProperty(PropertyType propertyType, Object value)
    {
        this.properties.put(propertyType, value);
        return this;
    }

    @Override
    public Sample create()
    {
        if (experiment != null)
        {
            space = experiment.getProject().getSpace();
        } else if (space == null)
        {
            space = new SpaceBuilder(this.openbis).create();
        }

        if (type == null)
        {
            type = new SampleTypeBuilder(this.openbis).create();
        }

        for (PropertyTypeAssignment assignment : type.getPropertyTypeAssignments())
        {
            if (assignment.isMandatory()
                    && properties.get(assignment.getPropertyType().getLabel()) == null)
            {
                throw new IllegalStateException("missing property");
            }
        }

        return openbis.create(build());
    }

    @Override
    public Sample build()
    {
        return new Sample(type, code, experiment, space, parents, properties);
    }
}
