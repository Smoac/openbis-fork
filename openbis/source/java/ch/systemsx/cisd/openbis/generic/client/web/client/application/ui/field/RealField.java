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

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Validator;

public final class RealField extends BasicTextField<Double>
{
    public RealField(final String label, final boolean mandatory)
    {
        super(label, mandatory);
        setValidator(new Validator<Double, Field<Double>>()
            {

                //
                // Validator
                //

                public final String validate(final Field<Double> field, final String val)
                {
                    try
                    {
                        Double.parseDouble(val);
                        return null;
                    } catch (final NumberFormatException e)
                    {
                        return "Real number required";
                    }
                }
            });
    }
}