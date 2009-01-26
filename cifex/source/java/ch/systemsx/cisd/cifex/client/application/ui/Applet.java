/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client.application.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class Applet extends Widget
{

    public Applet()
    {
        Element element = DOM.createElement("applet");
//        DOM.setElementProperty(element, "code", "ch.systemsx.cisd.cifex.uploadclient.FileUploadApplet.class");
//        DOM.setElementProperty(element, "archive", "cifex.jar");
        DOM.setElementProperty(element, "width", "200");
        DOM.setElementProperty(element, "height", "20");
        DOM.setElementProperty(element, "alt", "applets not supported");
        
        setElement(element);
    }
    
}
