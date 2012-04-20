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

package ch.systemsx.cisd.common.jython;

import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;

/**
 * @author pkupczyk
 */
public class JythonScriptCommand extends JythonScriptLines
{

    public boolean isNextCommand(String line)
    {
        if (isEmptyLine(line) || isIndentedLine(line))
        {
            return false;
        } else
        {
            try
            {
                PyObject object =
                        Py.compile_command_flags(getLines(), "<input>", CompileMode.single,
                                new CompilerFlags(), true);
                return object != Py.None;
            } catch (PyException e)
            {
                return false;
            }
        }
    }

    private static boolean isEmptyLine(String line)
    {
        return line.trim().length() == 0;
    }

    private static boolean isIndentedLine(String line)
    {
        return Character.isWhitespace(line.charAt(0));
    }

}
