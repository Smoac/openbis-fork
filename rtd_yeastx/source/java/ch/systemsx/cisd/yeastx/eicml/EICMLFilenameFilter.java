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

package ch.systemsx.cisd.yeastx.eicml;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A {@link FilenameFilter} for <code>eicML</code> files.
 *
 * @author Bernd Rinn
 */
final class EICMLFilenameFilter implements FilenameFilter
{
    public boolean accept(File myDir, String name)
    {
        return name.endsWith(".eicML");
    }
}