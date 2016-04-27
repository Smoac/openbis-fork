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

package ch.systemsx.cisd.ant.task.subprojectbuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Property;

import ch.systemsx.cisd.ant.common.MultiprojectBuildExecutor;

/**
 * @author felmer
 */
public class BuildJavaSubprojectsTask extends Task
{

    private final class SubprojectsExecutor extends MultiprojectBuildExecutor
    {
        public SubprojectsExecutor(File baseDir)
        {
            super(baseDir);
        }

        @Override
        protected void executeProjectBuild(File baseDir)
        {
            File projectAntFile = new File(baseDir, antFile);
            if (projectAntFile.exists())
            {
                Ant ant = new Ant();
                String targetForMessage;
                if (targetName == null)
                {
                    targetForMessage = "default target";
                } else
                {
                    ant.setTarget(targetName);
                    targetForMessage = "target '" + targetName + "'";
                }
                ant.setOwningTarget(getOwningTarget());
                ant.setDir(baseDir);
                ant.setAntfile(antFile);
                ant.setProject(getProject());
                for (Property property : properties)
                {
                    Property clonedProperty = ant.createProperty();
                    clonedProperty.setName(property.getName());
                    clonedProperty.setValue(property.getValue());
                }
                ant.init();
                log("Execute " + targetForMessage + " of build file '" + projectAntFile + "'.");
                ant.execute();
            }
        }

    }

    private List<Property> properties = new ArrayList<Property>();

    private String antFile = "build" + File.separatorChar + "build.xml";

    private String targetName;

    public void setAntFile(String antFile)
    {
        this.antFile = antFile;
    }

    public void setTarget(String target)
    {
        this.targetName = target;
    }

    public Property createProperty()
    {
        Property property = new Property();
        property.setProject(getProject());
        property.setTaskName("property");
        properties.add(property);
        return property;
    }

    @Override
    public void execute() throws BuildException
    {
        new SubprojectsExecutor(getProject().getBaseDir()).execute();
    }

}
