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

import java.io.File;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractDataStructureFactory implements IDataStructureFactory
{
    protected final File baseDir;

    public AbstractDataStructureFactory(File baseDir)
    {
        assert baseDir != null : "Unspecified base directory.";
        assert baseDir.isDirectory() : "Is not a directory : " + baseDir.getAbsolutePath();
        this.baseDir = baseDir;
    }
}
