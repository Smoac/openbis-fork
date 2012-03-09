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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.dom.client.Element;

/**
 * @author pkupczyk
 */
public class ImageDialog extends Dialog
{

    public ImageDialog()
    {
        addListener(Events.Show, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    autosize(getElement());
                }
            });
    }

    protected void autosize(Element element)
    {
        int preferedWidth = element.getOffsetWidth() + getFrameWidth() + 20;
        int preferedHeight = element.getOffsetHeight() + getFrameHeight() + 20;

        int maxWidth = (9 * XDOM.getBody().getOffsetWidth()) / 10;
        int maxHeight = (9 * XDOM.getBody().getOffsetHeight()) / 10;

        int w = Math.min(maxWidth, preferedWidth);
        int h = Math.min(maxHeight, preferedHeight);
        setSize(w, h);
        center();
    }

}
