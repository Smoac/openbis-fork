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

package ch.systemsx.cisd.openbis.uitest.dsl.type;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.request.CreateSample;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
@SuppressWarnings("hiding")
public class SampleBuilder implements Builder<Sample>
{
    private SampleType type;

    private String code;

    private Experiment experiment;

    private Space space;

    private Collection<Sample> parents;

    private Map<PropertyType, Object> properties;

    private UidGenerator uid;

    public SampleBuilder(UidGenerator uid)
    {
        this.uid = uid;
        this.code = uid.uid();
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

    public SampleBuilder in(Experiment experiment)
    {
        this.experiment = experiment;
        return this;
    }

    public SampleBuilder withProperty(PropertyType propertyType, Object value)
    {
        this.properties.put(propertyType, value);
        return this;
    }

    @Override
    public Sample build(Application openbis)
    {
        if (experiment != null)
        {
            space = experiment.getProject().getSpace();
        } else if (space == null)
        {
            space = new SpaceBuilder(uid).build(openbis);
        }

        if (type == null)
        {
            type = new SampleTypeBuilder(uid).build(openbis);
        }

        for (PropertyTypeAssignment assignment : type.getPropertyTypeAssignments())
        {
            if (assignment.isMandatory()
                    && properties.get(assignment.getPropertyType()) == null)
            {
                throw new IllegalStateException("missing property");
            }
        }

        return openbis.execute(new CreateSample(new SampleDsl(type, code, experiment, space,
                parents, properties, new HashSet<MetaProject>())));
    }
}
