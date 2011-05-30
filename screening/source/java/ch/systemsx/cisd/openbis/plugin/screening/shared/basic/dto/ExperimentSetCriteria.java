/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

/**
 * Points to a set of experiments.
 * 
 * @author Tomasz Pylak
 */
// TODO 2011-05-30, Tomasz Pylak: this is a skeleton which has to be implemented and used!
public class ExperimentSetCriteria
{
    /** Creates criteria which point to all accessible experiments in the database. */
    public static final ExperimentSetCriteria createAll()
    {
        // TODO 2011-05-30, Tomasz Pylak: implement me!
        return null;
    }

    /** Creates criteria which point to all accessible experiments in the specified project. */
    public static final ExperimentSetCriteria createFromProject(String spaceCode, String projectCode)
    {
        // TODO 2011-05-30, Tomasz Pylak: implement me!
        return null;
    }

}
