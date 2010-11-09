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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Validator;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;

/**
 * A text area to specify a script.
 * <p>
 * If the script is multiline it should contain definition of <code>calculate()</code> function.
 * 
 * @author Piotr Buczek
 */
public class ScriptField extends MultilineVarcharField
{

    private final static String CALCULATE_DEFINITION = "def calculate():";

    private final static String CALCULATE_DEFINITION_NOT_FOUND_MSG =
            "Multiline script should contain definition of 'calculate()' function.";

    private final static String BLANK_TEXT_MSG = "Script text required";

    public ScriptField(IMessageProvider messageProvider)
    {
        super(messageProvider.getMessage(Dict.SCRIPT), true, 20);
        setValidator(new ScriptValidator());
        getMessages().setBlankText(BLANK_TEXT_MSG);
        treatTabKeyAsInput();
    }

    /** {@link Validator} for script. */
    protected class ScriptValidator implements Validator
    {
        private static final String NEWLINE = "\n";

        public String validate(Field<?> field, final String fieldValue)
        {
            if (fieldValue.contains(NEWLINE))
            {
                final String[] lines = fieldValue.split(NEWLINE);
                for (String line : lines)
                {
                    if (line.equals(CALCULATE_DEFINITION))
                    {
                        // validated value is valid
                        return null;
                    }
                }
                return CALCULATE_DEFINITION_NOT_FOUND_MSG;
            }
            // validated value is valid
            return null;
        }
    }

}
