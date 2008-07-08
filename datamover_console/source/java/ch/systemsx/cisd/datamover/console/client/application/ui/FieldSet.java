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

package ch.systemsx.cisd.datamover.console.client.application.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget realizing an HTML fieldSet. Style class reads <code>cisd-fieldset</code>.
 *
 * @author Franz-Josef Elmer
 */
public class FieldSet extends Widget
{
    private Element fieldSet;

    /**
     * Creates a new instance.
     * 
     * @param legendOrNull If not-<code>null</code> the field set will get a legend.
     */
    public FieldSet(String legendOrNull)
    {
        fieldSet = DOM.createFieldSet();
        if (legendOrNull != null)
        {
            Element legend = DOM.createLegend();
            DOM.setInnerText(legend, legendOrNull);
            fieldSet.appendChild(legend);
        }
        setElement(fieldSet);
        setStyleName("cisd-fieldset");
    }
    
    /**
     * Add a widget to be surounded by the field set border.
     */
    public void add(Widget widget)
    {
        DOM.appendChild(fieldSet, widget.getElement());
    }
    
}
