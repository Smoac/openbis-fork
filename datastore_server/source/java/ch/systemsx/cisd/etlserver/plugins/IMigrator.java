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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.util.Properties;

/**
 * Interface of classes which are able to migrate a single data set. Classes implementing this
 * interface should have a public constructor with a single argument of type {@link Properties}.
 * 
 * @author Tomasz Pylak
 * @author Franz-Josef Elmer
 */
public interface IMigrator
{
    /** 
     * Migrates specified dataset.
     * 
     * @return <code>true</code> if migration was successful.
     */
    boolean migrate(File dataset);

    /** user-friendly description of the migrator */
    String getDescription();

}
