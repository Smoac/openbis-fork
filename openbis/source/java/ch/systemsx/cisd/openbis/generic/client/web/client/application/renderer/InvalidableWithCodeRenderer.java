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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.shared.basic.IInvalidationProvider;

/**
 * Renders code and marks if the entity (e.g. sample or experiment) is invalid
 * 
 * @author Tomasz Pylak
 */
public class InvalidableWithCodeRenderer
{

    public static String render(IInvalidationProvider entity, String code, String href)
    {
        final Element anchor = DOM.createAnchor();
        anchor.setInnerText(code);
        DOM.setElementProperty(anchor, "href", "#" + (href != null ? href : ""));
        String link = DOM.toString(anchor);
        boolean isValid = entity.getInvalidation() == null;
        if (isValid)
        {
            return link;
        }
        Element div = DOM.createDiv();
        div.setAttribute("class", "invalid");
        div.setInnerHTML(link);
        return DOM.toString(div);
    }
}
