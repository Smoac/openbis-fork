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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.logicalimage;

import com.extjs.gxt.ui.client.widget.Label;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author pkupczyk
 */
@SuppressWarnings("unchecked")
public class LogicalImageSeriesControlWithLabel<C extends Widget> extends Grid
{

    public LogicalImageSeriesControlWithLabel()
    {
        super(2, 1);
    }

    public C getControl()
    {
        return (C) getWidget(1, 0);
    }

    public void setControl(C control)
    {
        setWidget(1, 0, control);
    }

    public String getLabelText()
    {
        Label label = (Label) getWidget(0, 0);
        if (label != null)
        {
            return label.getText();
        } else
        {
            return null;
        }
    }

    public void setLabelText(String text)
    {
        Label label = (Label) getWidget(0, 0);
        if (label == null)
        {
            label = new Label();
            setWidget(0, 0, label);
        }
        label.setText(text);
    }

}
