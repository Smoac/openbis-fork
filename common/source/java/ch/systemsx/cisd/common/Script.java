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

package ch.systemsx.cisd.common;

/**
 * Bean class for a script. Holds script name and code. 
 *
 * @author Franz-Josef Elmer
 */
public class Script
{
    private final String name;
    private final String code;
    private final String version;

    /**
     * Creates an instance for the specified script name, code and version.
     */
    public Script(String name, String code)
    {
        this(name, code, "UNKNOWN");
    }
    
    /**
     * Creates an instance for the specified script name, code and version.
     */
    public Script(String name, String code, String version)
    {
        assert name != null;
        assert code != null;
        assert version != null;
        this.name = name;
        this.code = code;
        this.version = version;
    }

    /**
     * Returns script code.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Returns script name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the version of the script.
     */
    public final String getVersion()
    {
        return version;
    }
    
}
