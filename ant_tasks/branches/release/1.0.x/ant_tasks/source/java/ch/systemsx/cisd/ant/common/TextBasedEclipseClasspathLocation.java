/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.ant.common;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * 
 *
 * @author felmer
 */
public class TextBasedEclipseClasspathLocation implements IEclipseClasspathLocation
{
    private final String text;
    private final String displayableLocation;

    public TextBasedEclipseClasspathLocation(String text, String displayableLocation)
    {
        assert text != null;
        this.text = text;
        this.displayableLocation = displayableLocation == null ? "UNKNOWN" : displayableLocation;
    }
    
    public String getDisplayableLocation()
    {
        return displayableLocation;
    }

    public Document getDocument()
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(text.getBytes()));
            return document;
        } catch (Exception ex)
        {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

}
