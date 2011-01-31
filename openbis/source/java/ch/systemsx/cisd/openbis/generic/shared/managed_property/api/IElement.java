/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.managed_property.api;

import java.util.List;

/**
 * TODO KE: write javadoc (API will be exposed publicly)
 * 
 * @author Piotr Buczek
 * @author Kaloyan Enimanev
 */
public interface IElement
{
    
    String getName();
    
    String getAttribute(String key);
    
    String getData();

    List<IElement> getChildren();
    
    List<IElementAttribute> getAttributes();
    
    IElement setData(String data);

    IElement setChildren(List<IElement> children);

    IElement setAttributes(List<IElementAttribute> attributes);
    
    IElement addChildren(IElement... child);

    IElement addAttributes(IElementAttribute... attribute);

}
