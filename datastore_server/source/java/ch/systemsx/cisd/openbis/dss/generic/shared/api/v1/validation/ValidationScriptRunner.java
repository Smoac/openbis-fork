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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation;

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

    // Factory methods

    /**
     * Factory method for creating a ValidationScriptRunner given a path to a script.
     */
    public static ValidationScriptRunner createValidatorFromScriptPath(String scriptPath)
    {
        if (scriptPath == null)
        {
            return new NullValidationScriptRunner();
        }
        File scriptFile = new File(scriptPath);
        if (false == scriptFile.exists())
        {
            return new NullValidationScriptRunner();
        }
        String fileString = FileUtilities.loadToString(scriptFile);
        String scriptString = getValidationScriptString(fileString);
        return new ValidationScriptRunner(scriptString);
    }

    /**
     * Factory method for creating a ValidationScriptRunner given the script as a string.
     */
    public static ValidationScriptRunner createValidatorFromScriptString(String scriptString)
    {
        if (scriptString == null)
        {
            return new NullValidationScriptRunner();
        }
        String theScriptString = getValidationScriptString(scriptString);
        return new ValidationScriptRunner(theScriptString);
    }

    private static String getValidationScriptString(String scriptString)
    {
        String standardImports = "from " + ValidationError.class.getCanonicalName() + " import *";
        return standardImports + "\n" + scriptString;

        // String scriptString = FileUtilities.loadToString(validationScriptFile);
        // return scriptString;
    }

    private final PythonInterpreter interpreter;

    private final String scriptString;

    private ValidationScriptRunner(String scriptString)
    {
        this.interpreter = new PythonInterpreter();
        // Load the script
        this.scriptString = scriptString;

        interpreter.exec(this.scriptString);
    }

    /**
     * Protected constructor for the null script runner.
     * 
     * @param scriptIsNull
     */
    protected ValidationScriptRunner(boolean scriptIsNull)
    {
        this.interpreter = null;
        // Load the script
        this.scriptString = null;

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

    public String getScriptString()
    {
        return scriptString;
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

class NullValidationScriptRunner extends ValidationScriptRunner
{
    protected NullValidationScriptRunner()
    {
        super(true);
    }

    @Override
    public List<ValidationError> validate(File dataSetFile)
    {
        ArrayList<ValidationError> errors = new ArrayList<ValidationError>();
        return errors;
    }
}
