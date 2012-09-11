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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import ch.systemsx.cisd.openbis.uitest.infra.ApplicationRunner;

/**
 * @author anttil
 */
public class ExperimentBuilder implements Builder<Experiment>
{

    private ApplicationRunner openbis;

    private ExperimentType type;

    private String code;

    private Project project;

    private Collection<Sample> samples;

    public ExperimentBuilder(ApplicationRunner openbis)
    {
        this.openbis = openbis;
        this.type = null;
        this.code = UUID.randomUUID().toString();
        this.project = null;
        this.samples = new ArrayList<Sample>();
    }

    public ExperimentBuilder ofType(ExperimentType type)
    {
        this.type = type;
        return this;
    }

    public ExperimentBuilder in(Project project)
    {
        this.project = project;
        return this;
    }

    public ExperimentBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    public ExperimentBuilder withSamples(Sample... samples)
    {
        this.samples = new HashSet<Sample>();
        for (Sample sample : samples)
        {
            this.samples.add(sample);
        }
        return this;
    }

    @Override
    public Experiment build()
    {
        if (type == null)
        {
            type = new ExperimentTypeBuilder(openbis).build();
        }
        if (project == null)
        {
            project = new ProjectBuilder(openbis).build();
        }

        return openbis.create(new Experiment(type, code, project, samples));
    }

}
