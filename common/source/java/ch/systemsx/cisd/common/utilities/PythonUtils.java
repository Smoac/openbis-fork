/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/**
 * Utility functions for Jython.
 * 
 * @author Bernd Rinn
 */
public class PythonUtils
{

    /**
     * Creates a new Jython interpreter with a fully isolated system state (i.e. interpreters in
     * different threads don't influence each other).
     */
    public static PythonInterpreter createIsolatedPythonInterpreter()
    {
        return new PythonInterpreter(null, new PySystemState());
    }

    /**
     * Creates a new Jython interpreter with a non-isolated system state (i.e. interpreters in
     * different threads influence each other and see each others variables).
     * <p>
     * Use this if you don't need thread isolation as the isolated interpreter has some gotchas. 
     */
    public static PythonInterpreter createNonIsolatedPythonInterpreter()
    {
        return new PythonInterpreter();
    }

}
