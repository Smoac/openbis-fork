/*
 * Copyright 2009 ETH Zuerich, CISD
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

import com.google.gwt.user.client.rpc.IsSerializable;

public final class ExperimentIdentifier implements IsSerializable
{
    private String experimentCode;

    private String projectCode;

    private String groupCode;

    private String instanceCode;

    public static ExperimentIdentifier createIdentifier(Experiment entity)
    {
        Project project = entity.getProject();
        Group group = project.getGroup();

        ExperimentIdentifier ident = new ExperimentIdentifier();
        ident.setExperimentCode(entity.getCode());
        ident.setProjectCode(project.getCode());
        ident.setGroupCode(group.getCode());
        ident.setDatabaseInstanceCode(group.getInstance().getCode());

        return ident;
    }

    public String getExperimentCode()
    {
        return experimentCode;
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public String getGroupCode()
    {
        return groupCode;
    }

    public String getDatabaseInstanceCode()
    {
        return instanceCode;
    }

    private void setExperimentCode(String experimentCode)
    {
        this.experimentCode = experimentCode;
    }

    private void setProjectCode(String projectCode)
    {
        this.projectCode = projectCode;
    }

    private void setGroupCode(String groupCode)
    {
        this.groupCode = groupCode;
    }

    private void setDatabaseInstanceCode(String instanceCode)
    {
        this.instanceCode = instanceCode;
    }

    // -----------------

    public static final String TYPE_SEPARATOR_PREFIX = " (";

    public static final String TYPE_SEPARATOR_SUFFIX = ")";

    /**
     * Parses the material code and type. Assumes the syntax: "code (type)". Returns the chosen
     * material if parsing went ok, null otherwise.
     */
    public static ExperimentIdentifier tryParseIdentifier(String value, Group group)
    {
        if (value == null || value.length() == 0)
        {
            return null;
        }
        int typePrefix = value.indexOf(TYPE_SEPARATOR_PREFIX);
        if (typePrefix == -1)
        {
            return null;
        }
        String code = value.substring(0, typePrefix);
        String projectCode = value.substring(typePrefix + TYPE_SEPARATOR_PREFIX.length());
        // we allow to omit the closing brace
        if (projectCode.endsWith(TYPE_SEPARATOR_SUFFIX))
        {
            projectCode =
                    projectCode.substring(0, projectCode.length() - TYPE_SEPARATOR_SUFFIX.length());
        }
        return createIdentifier(group, code, projectCode);
    }

    private static ExperimentIdentifier createIdentifier(Group group, String code,
            String projectCode)
    {
        ExperimentIdentifier ident = new ExperimentIdentifier();
        ident.setExperimentCode(code);
        ident.setProjectCode(projectCode);
        ident.setGroupCode(group.getCode());
        ident.setDatabaseInstanceCode(group.getInstance().getCode());
        return ident;
    }

    /** Prints the identifier in the canonical form */
    public String print()
    {
        return getExperimentCode() + TYPE_SEPARATOR_PREFIX + getProjectCode()
                + TYPE_SEPARATOR_SUFFIX;
    }

}