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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.extjs.gxt.ui.client.widget.form.LabelField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;

/**
 * @author Christian Ribeaud
 */
public final class InfoBox extends LabelField
{

    public InfoBox()
    {
        setVisible(false);
        setStyleAttribute("textAlign", "center");
        setPosition(-2, 0);
    }

    private void setStrongStyle()
    {
        setStyleAttribute("backgroundColor", "#feffbe");
        setStyleAttribute("border", "1px solid #edee8b");
    }

    private void setLightStyle()
    {
        setStyleAttribute("backgroundColor", "#feffdf");
        setStyleAttribute("color", "gray");
        setStyleAttribute("border", "1px solid #e7e7e7");
    }

    public void fade()
    {
        setLightStyle();
    }

    public void display(final String text)
    {
        if (StringUtils.isBlank(text) == false)
        {
            setStrongStyle();
            setVisible(true);
            setText(text);
        }
    }
}