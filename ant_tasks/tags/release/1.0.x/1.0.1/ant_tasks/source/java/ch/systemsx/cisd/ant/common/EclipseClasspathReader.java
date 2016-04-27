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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper class which reads an Eclipse <code>.classpath</code> file and returns a list of
 * {@link EclipseClasspathEntry} instances for each <code>&lt;classpathentry&gt;</code> found.
 * 
 * @author felmer
 */
public class EclipseClasspathReader
{
    /** Name of the Eclipse classpath file. */
    public static final String CLASSPATH_FILE = ".classpath";
    
    private static final String CLASSPATH = "classpath";

    private static final String PATH_ATTRIBUTE = "path";

    private static final String KIND_ATTRIBUTE = "kind";

    private static final String CLASSPATHENTRY = "classpathentry";

    /**
     * Reads from the specified Eclipse classpath file all classpathentry elements.
     */
    public static List<EclipseClasspathEntry> readClasspathEntries(IEclipseClasspathLocation location)
    {
        String displayableLocation = location.getDisplayableLocation();
        Node root = location.getDocument().getChildNodes().item(0);
        if (CLASSPATH.equalsIgnoreCase(root.getNodeName()) == false)
        {
            throw new IllegalArgumentException("Root element of '" + displayableLocation + "' is not '" + CLASSPATH
                    + "'.");
        }
        NodeList children = root.getChildNodes();
        List<Element> elements = new ArrayList<Element>();
        for (int i = 0, n = children.getLength(); i < n; i++)
        {
            Node item = children.item(i);
            if (item instanceof Element)
            {
                elements.add((Element) item);
            }
        }
        List<EclipseClasspathEntry> entries = new ArrayList<EclipseClasspathEntry>();
        for (int i = 0, n = elements.size(); i < n; i++)
        {
            Element element = elements.get(i);
            try
            {
                if (CLASSPATHENTRY.equalsIgnoreCase(element.getNodeName()))
                {
                    String kind = getAttribute(element, KIND_ATTRIBUTE);
                    String path = getAttribute(element, PATH_ATTRIBUTE);
                    entries.add(new EclipseClasspathEntry(kind, path));
                } else
                {
                    throw new IllegalArgumentException("<" + CLASSPATHENTRY + "> expected.");
                }

            } catch (Exception e)
            {
                throw new IllegalArgumentException((i + 1) + ". element of '" + displayableLocation + "': "
                        + e.getMessage(), e);
            }
        }
        return entries;
    }

    private static String getAttribute(Node node, String name)
    {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null)
        {
            Node item = attributes.getNamedItem(name);
            if (item != null)
            {
                return item.getNodeValue();
            }
        }
        throw new RuntimeException("Attribute '" + name + "' missing.");
    }

    private EclipseClasspathReader()
    {
    }
}
