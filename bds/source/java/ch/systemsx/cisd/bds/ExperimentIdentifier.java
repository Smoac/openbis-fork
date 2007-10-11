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

package ch.systemsx.cisd.bds;

import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * Identifier of the experiment which corresponds to the data. This is an immutable but extendable value object class.
 * An instance of this class allows unique identification in the database. 
 *
 * @author Franz-Josef Elmer
 */
public class ExperimentIdentifier
{
    private static final String FOLDER = "experiment_identifier";
    private static final String GROUP_CODE = "group_code";
    private static final String PROJECT_CODE = "project_code";
    private static final String EXPERIMENT_CODE = "experiment_code";
    
    public static ExperimentIdentifier loadFrom(IDirectory directory)
    {
        IDirectory idFolder = Utilities.getSubDirectory(directory, FOLDER);
        String groupCode = Utilities.getTrimmedString(idFolder, GROUP_CODE);
        String projectCode = Utilities.getTrimmedString(idFolder, PROJECT_CODE);
        String experimentCode = Utilities.getTrimmedString(idFolder, EXPERIMENT_CODE);
        return new ExperimentIdentifier(groupCode, projectCode, experimentCode);
    }
    
    private final String groupCode;
    private final String projectCode;
    private final String experimentCode;

    /**
     * Creates an instance for the specified codes of group, project, and experiment.
     *
     * @param groupCode A non-empty string of the group code.
     * @param projectCode A non-empty string of the project code.
     * @param experimentCode A non-empty string of the experiment code.
     */
    public ExperimentIdentifier(String groupCode, String projectCode, String experimentCode)
    {
        assert groupCode != null && groupCode.length() > 0 : "Undefined group code";
        this.groupCode = groupCode;
        assert projectCode != null && projectCode.length() > 0 : "Undefined project code";
        this.projectCode = projectCode;
        assert experimentCode != null && experimentCode.length() > 0 : "Undefined experiment code";
        this.experimentCode = experimentCode;
    }

    /**
     * Returns the group code;
     */
    public final String getGroupCode()
    {
        return groupCode;
    }

    /**
     * Returns the project code;
     */
    public final String getProjectCode()
    {
        return projectCode;
    }
    
    /**
     * Returns the experiment code;
     */
    public final String getExperimentCode()
    {
        return experimentCode;
    }
    
    public void saveTo(IDirectory directory)
    {
        IDirectory folder = directory.makeDirectory(FOLDER);
        folder.addKeyValuePair(GROUP_CODE, groupCode);
        folder.addKeyValuePair(PROJECT_CODE, projectCode);
        folder.addKeyValuePair(EXPERIMENT_CODE, experimentCode);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ExperimentIdentifier == false)
        {
            return false;
        }
        ExperimentIdentifier id = (ExperimentIdentifier) obj;
        return id.groupCode.equals(groupCode) && id.projectCode.equals(projectCode)
                && id.experimentCode.equals(experimentCode);
    }

    @Override
    public int hashCode()
    {
        return (groupCode.hashCode() * 37 + projectCode.hashCode()) * 37 + experimentCode.hashCode();
    }

    @Override
    public String toString()
    {
        return "[group:" + groupCode + ",project:" + projectCode + ",experiment" + experimentCode;
    }
    
    
}
