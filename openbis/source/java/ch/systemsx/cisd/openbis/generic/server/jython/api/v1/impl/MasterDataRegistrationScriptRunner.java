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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import java.io.File;

import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.PythonUtils;

/**
 * A class for running python scripts that register master data.
 * 
 * @author Kaloyan Enimanev
 */
public class MasterDataRegistrationScriptRunner implements IMasterDataScriptRegistrationRunner
{
    private final static String SERVICE_VARIABLE_NAME = "service";

    private final EncapsulatedCommonServer commonServer;

    public MasterDataRegistrationScriptRunner(EncapsulatedCommonServer commonServer)
    {
        this.commonServer = commonServer;
    }

    public void executeScript(File jythonScriptFile) throws MasterDataRegistrationException
    {
        checkValidJythonScript(jythonScriptFile);
        String scriptString = FileUtilities.loadToString(jythonScriptFile);

        executeScript(scriptString);
    }

    public void executeScript(String jythonScript) throws MasterDataRegistrationException
    {
        MasterDataRegistrationService service = new MasterDataRegistrationService(commonServer);

        // Configure the evaluator
        PythonInterpreter interpreter = PythonUtils.createIsolatedPythonInterpreter();
        interpreter.set(SERVICE_VARIABLE_NAME, service);

        // Invoke the evaluator
        interpreter.exec(jythonScript);

        service.commit();
    }

    private void checkValidJythonScript(File jythonScript)
    {
        if (false == jythonScript.exists())
        {
            throw new RuntimeException("Script file does not exist :"
                    + jythonScript.getAbsolutePath());
        }
        if (jythonScript.isDirectory())
        {
            throw new RuntimeException(
                    "Scripts must be files. It is not possible to execute directories:"
                            + jythonScript.getAbsolutePath());
        }
        if (false == jythonScript.canRead())
        {
            throw new RuntimeException("No read permissions for file:"
                    + jythonScript.getAbsolutePath());
        }
    }
}
