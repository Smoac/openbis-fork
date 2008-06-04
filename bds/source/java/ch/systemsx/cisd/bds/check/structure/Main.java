/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.check.structure;

import ch.systemsx.cisd.bds.check.structure.StructureChecker.StructureReport;

/**
 * The main class of the <i>BDS</i> structure checker.
 * 
 * @author Christian Ribeaud
 */
public final class Main
{

    public final static void main(final String[] args)
    {
        try
        {
            final Parameters parameters = new Parameters(args);
            final StructureReport report =
                    StructureChecker.checkStructure(parameters.getFile());
            System.out.println(report);
        } catch (final RuntimeException e)
        {
            System.err.println(e.getMessage());
        }
    }
}
