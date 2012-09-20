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

import ch.systemsx.cisd.openbis.uitest.infra.Browsable;
import ch.systemsx.cisd.openbis.uitest.infra.EntityType;
import ch.systemsx.cisd.openbis.uitest.page.common.Cell;
import ch.systemsx.cisd.openbis.uitest.page.common.Row;

/**
 * @author anttil
 */
public class Experiment implements EntityType, Browsable
{
    private ExperimentType type;

    private final String code;

    private Project project;

    private Collection<Sample> samples;

    Experiment(ExperimentType type, String code, Project project, Collection<Sample> samples)
    {
        this.type = type;
        this.code = code;
        this.project = project;
        this.samples = samples;
    }

    @Override
    public boolean isRepresentedBy(Row row)
    {
        Cell codeCell = row.get("Code");
        return codeCell != null && codeCell.getText().equalsIgnoreCase(this.code);
    }

    @Override
    public String getCode()
    {
        return code;
    }

    public ExperimentType getType()
    {
        return type;
    }

    public Project getProject()
    {
        return project;
    }

    public Collection<Sample> getSamples()
    {
        return samples;
    }

    void setType(ExperimentType type)
    {
        this.type = type;
    }

    void setProject(Project project)
    {
        this.project = project;
    }

    void setSamples(Collection<Sample> samples)
    {
        this.samples = samples;
    }
}
