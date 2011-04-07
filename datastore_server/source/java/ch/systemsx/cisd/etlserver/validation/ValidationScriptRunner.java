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

package ch.systemsx.cisd.etlserver.validation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ValidationScriptRunner
{
    private final static String FILE_VALIDATION_FUNCTION_NAME = "validate_data_set_file";

    private static String getValidationScriptString(File validationScriptFile)
    {
        String standardImports = "from " + ValidationError.class.getCanonicalName() + " import *";
        String scriptString = FileUtilities.loadToString(validationScriptFile);
        return standardImports + "\n" + scriptString;

        // String scriptString = FileUtilities.loadToString(validationScriptFile);
        // return scriptString;
    }

    private final PythonInterpreter interpreter;

    private final String scriptPath;

    private final String scriptString;

    public ValidationScriptRunner(String scriptPath)
    {
        this.interpreter = new PythonInterpreter();
        this.scriptPath = scriptPath;
        // Load the script
        scriptString = getValidationScriptString(new File(this.scriptPath));

        interpreter.exec(this.scriptString);
    }

    @SuppressWarnings("unchecked")
    public List<ValidationError> validate(File dataSetFile)
    {
        ArrayList<ValidationError> errors = new ArrayList<ValidationError>();
        PyFunction function = tryJythonFunction(FILE_VALIDATION_FUNCTION_NAME);
        PyObject result = function.__call__(Py.java2py(dataSetFile));
        errors.addAll((Collection<? extends ValidationError>) result);

        return errors;
    }

    private PyFunction tryJythonFunction(String functionName)
    {
        try
        {
            PyFunction function = (PyFunction) interpreter.get(functionName, PyFunction.class);
            return function;
        } catch (Exception e)
        {
            return null;
        }
    }
}
