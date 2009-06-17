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

package ch.systemsx.cisd.openbis.generic.shared.dto.identifier;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Identifies the experiment. This identifier points to an experiment if a group and database
 * instance is provided in addition.
 * 
 * @author Tomasz Pylak
 */
public class LocalExperimentIdentifier implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private final String projectCode;

    private final String experimentCode;

    public LocalExperimentIdentifier(String projectCode, String experimentCode)
    {
        this.projectCode = projectCode;
        this.experimentCode = experimentCode;
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public String getExperimentCode()
    {
        return experimentCode;
    }

    @Override
    public String toString()
    {
        return projectCode + "/" + experimentCode;
    }
}
