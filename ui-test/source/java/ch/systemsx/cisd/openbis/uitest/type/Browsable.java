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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import ch.systemsx.cisd.openbis.uitest.layout.DataSetTypeBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.ExperimentBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.ExperimentTypeBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.Location;
import ch.systemsx.cisd.openbis.uitest.layout.ProjectBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.PropertyTypeAssignmentBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.PropertyTypeBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.SampleBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.SampleTypeBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.ScriptBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.SpaceBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.VocabularyBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.page.Browser;

/**
 * @author anttil
 */
public class Browsable
{
    private Collection<String> columns;

    private String code;

    private Location<? extends Browser> browserLocation;

    public Browsable(DataSetType dataSetType)
    {
        code = dataSetType.getCode();
        columns = Arrays.asList("Code", "Description");
        browserLocation = new DataSetTypeBrowserLocation();
    }

    public Browsable(Experiment experiment)
    {
        code = experiment.getCode();
        columns = Arrays.asList("Code", "Project", "Experiment Type");
        browserLocation = new ExperimentBrowserLocation();
    }

    public Browsable(ExperimentType experimentType)
    {
        code = experimentType.getCode();
        columns = Arrays.asList("Code", "Description");
        browserLocation = new ExperimentTypeBrowserLocation();
    }

    public Browsable(Project project)
    {
        code = project.getCode();
        columns = Arrays.asList("Code", "Description", "Space");
        browserLocation = new ProjectBrowserLocation();
    }

    public Browsable(PropertyType type)
    {
        code = type.getCode();
        columns = Arrays.asList("Code", "Data Type", "Label", "Description", "Vocabulary");
        browserLocation = new PropertyTypeBrowserLocation();
    }

    public Browsable(PropertyTypeAssignment assignment)
    {
        code = assignment.getPropertyType().getCode();
        columns = Arrays.asList("Property Type Code", "Entity Type", "Mandatory?");
        browserLocation = new PropertyTypeAssignmentBrowserLocation();
    }

    public Browsable(Sample sample)
    {
        code = sample.getCode();
        columns = getColumns(sample);
        browserLocation = new SampleBrowserLocation();
    }

    public Browsable(SampleType type)
    {
        code = type.getCode();
        columns = Arrays.asList("Code", "Description", "Database Instance", "Validation Script",
                "Listable?", "Show Container?", "Show Parents?", "Unique Subcodes",
                "Generate Codes Automatically", "Show Parent Metadata?", "Generated Code Prefix");
        browserLocation = new SampleTypeBrowserLocation();
    }

    public Browsable(Script script)
    {
        code = script.getCode();
        columns = Arrays.asList("Name", "Description", "Entity Kind", "Script Type", "Script");
        browserLocation = new ScriptBrowserLocation();
    }

    public Browsable(Space space)
    {
        code = space.getCode();
        columns = Arrays.asList("Code", "Description");
        browserLocation = new SpaceBrowserLocation();
    }

    public Browsable(Vocabulary vocabulary)
    {
        code = vocabulary.getCode();
        columns = Arrays.asList("Code", "Description", "URL Template");
        browserLocation = new VocabularyBrowserLocation();
    }

    public Collection<String> getColumns()
    {
        return this.columns;
    }

    public String getCode()
    {
        return this.code;
    }

    public Location<? extends Browser> getBrowserLocation()
    {
        return this.browserLocation;
    }

    private Collection<String> getColumns(Sample sample)
    {
        Collection<String> cols = new HashSet<String>();
        cols.addAll(Arrays.asList("Code", "Experiment", "Parents", "Space", "Sample Type",
                "Project"));
        for (PropertyTypeAssignment propertyTypeAssignment : sample.getType()
                .getPropertyTypeAssignments())
        {
            cols.add(propertyTypeAssignment.getPropertyType().getLabel());
        }
        return cols;
    }
}
