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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import com.extjs.gxt.ui.client.widget.form.TextArea;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;

/**
 * A {@link TextArea} extension for registering multiline text with adjustable height.
 * 
 * @author Piotr Buczek
 */
public class MultilineVarcharField extends TextArea
{

    private static final double DEFAULT_LINE_HEIGHT = 1.6;

    private static final int DEFAULT_LINES = 5;

    /** Constructor for default sized field (5 lines). */
    public MultilineVarcharField(final String label, final boolean mandatory)
    {
        this(label, mandatory, DEFAULT_LINES);
    }

    /** Constructor for multiline field with given number of lines. */
    public MultilineVarcharField(final String label, final boolean mandatory, int lines)
    {
        this.setFieldLabel(label);
        this.setValidateOnBlur(true);
        this.setAutoValidate(true);
        FieldUtil.setMandatoryFlag(this, mandatory);

        this.setHeightInLines(lines);
    }

    public void setHeightInLines(int lines)
    {
        setHeight(lines * DEFAULT_LINE_HEIGHT + "em");
    }

}
