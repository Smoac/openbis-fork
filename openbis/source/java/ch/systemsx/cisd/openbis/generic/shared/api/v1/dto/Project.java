/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;

/**
 * Immutable value object representing a project. A project is specified by its code and the code
 * of the space to which it belongs.
 *
 * @author Franz-Josef Elmer
 */
public final class Project implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final String spaceCode;
    
    private final String code;

    /**
     * Creates a new instance for the specified space code and project code.
     * 
     * @throws IllegalArgumentException if either the code or the space code is <code>null</code> or
     *             an empty string.
     */
    public Project(String spaceCode, String code)
    {
        if (spaceCode == null || spaceCode.length() == 0)
        {
            throw new IllegalArgumentException("Unspecified space code.");
        }
        this.spaceCode = spaceCode;
        if (code == null || code.length() == 0)
        {
            throw new IllegalArgumentException("Unspecified code.");
        }
        this.code = code;
    }
    
    /**
     * Returns the space code.
     */
    public String getSpaceCode()
    {
        return spaceCode;
    }
    
    /**
     * Returns the project code.
     */
    public String getCode()
    {
        return code;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Project == false)
        {
            return false;
        }
        Project project = (Project) obj;
        return project.spaceCode.equals(spaceCode) && project.code.equals(code);
    }

    @Override
    public int hashCode()
    {
        return 37 * spaceCode.hashCode() + code.hashCode();
    }

    @Override
    public String toString()
    {
        return "/" + spaceCode + "/" + code;
    }

}
