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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ClientPluginProvider;

/**
 * Class which updates <code>OpenBIS.gwt.xml</code> and {@link ClientPluginProvider}.
 * 
 * @author Christian Ribeaud
 */
public final class WebClientFilesUpdater
{
    private static final String CLIENT_PLUGIN_PROVIDER_CLASS =
            "ch.systemsx.cisd.openbis.generic.client.web.client.application.ClientPluginProvider";

    private static final String INHERITS_ELEMENT = "inherits";

    private static final String MODULE_ELEMENT = "module";

    private static final String PLUGIN_PACKAGE_NAME = "plugin";

    private static final String OPENBIS_GWT_XML_FILE_NAME = "OpenBIS.gwt.xml";

    private static final String OPENBIS_PACKAGE_NAME = "ch/systemsx/cisd/openbis";

    private static final String SOURCE_PATH_TEMPLATE = "plugin/%s/client/web/client";

    private static final String PUBLIC_PATH_TEMPLATE = "plugin/%s/client/web/public";

    private static final String JAVA_MARKER_START = "// Automatically generated part - START";

    private static final String JAVA_MARKER_END = "// Automatically generated part - END";

    private static final String PLUGIN_FACTORY_CLASS_NAME_TEMPLATE =
            "ch.systemsx.cisd.openbis.plugin.%s.client.web.client.application.ClientPluginFactory";

    private static final String PLUGIN_FACTORY_REGISTRATION_TEMPLATE =
            "registerPluginFactory(new " + PLUGIN_FACTORY_CLASS_NAME_TEMPLATE
                    + "(originalViewContext));";

    private static final String SCRIPT_ELEMENT = "script";

    private static final String STYLESHEET_ELEMENT = "stylesheet";

    private static final String ENTRY_POINT_ELEMENT = "entry-point";

    private static final String PUBLIC_ELEMENT = "public";

    private static final String SOURCE_ELEMENT = "source";

    private final String[] allTechnologies;

    private final String[] technologies;

    private final File workingDirectory;

    private WebClientFilesUpdater(final String workingDirectory, final String... technologies)
    {
        this.workingDirectory = getWorkingDirectory(workingDirectory);
        this.allTechnologies = scanAllTechnologies(this.workingDirectory);
        this.technologies = getTechnologies(allTechnologies, technologies);
    }

    private final static void checkTechnologies(final String[] allTechnologies,
            final String[] technologies)
    {
        for (final String technology : technologies)
        {
            if (ArrayUtils.indexOf(allTechnologies, technology) < 0)
            {
                throw new IllegalArgumentException(String.format(
                        "Technology '%s' must be one of '%s'.", technology, Arrays
                                .toString(allTechnologies)));
            }
        }
    }

    private final static File getWorkingDirectory(final String workingDirectory)
    {
        return workingDirectory == null ? new File(".") : new File(workingDirectory);
    }

    private final static String[] getTechnologies(final String[] allTechnologies,
            final String[] technologies)
    {
        if (technologies == null || technologies.length == 0)
        {
            return allTechnologies;
        } else
        {
            checkTechnologies(allTechnologies, technologies);
            return technologies;
        }
    }

    private final static String[] scanAllTechnologies(final File rootDirectory)
    {
        final File pluginRootDirectory = getPluginRootDirectory(rootDirectory);
        final File[] pluginDirs =
                pluginRootDirectory.listFiles((FileFilter) FileFilterUtils
                        .makeSVNAware(FileFilterUtils.directoryFileFilter()));
        return toFileNames(pluginDirs);
    }

    private final static File getPluginRootDirectory(final File rootDirectory)
    {
        final File openBISPackage =
                new File(rootDirectory, OPENBIS_PACKAGE_NAME + "/" + PLUGIN_PACKAGE_NAME);
        final String response =
                FileUtilities.checkDirectoryFullyAccessible(openBISPackage, "openBIS package");
        if (response != null)
        {
            throw new RuntimeException(response);
        }
        return openBISPackage;
    }

    private final static String[] toFileNames(final File[] files)
    {
        final String[] fileNames = new String[files.length];
        int i = 0;
        for (final File file : files)
        {
            fileNames[i++] = file.getName();
        }
        return fileNames;
    }

    private final Document createXMLDocument(final File openBISGwtXmlFile)
    {
        try
        {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final Document document = db.newDocument();
            final Element module = document.createElement(MODULE_ELEMENT);
            document.appendChild(document.createComment("Automatically generated - DO NOT EDIT!"));
            document.appendChild(module);
            module.appendChild(createStylesheetElement(document, "css/openbis.css"));
            module.appendChild(createEntryPointElement(document,
                    "ch.systemsx.cisd.openbis.generic.client.web.client.application.Client"));
            // 'inherits' tag.
            module.appendChild(createInheritsElement(document, "com.google.gwt.user.User"));
            module.appendChild(createInheritsElement(document, "com.google.gwt.i18n.I18N"));
            module.appendChild(createInheritsElement(document, "com.extjs.gxt.ui.GXT"));
            // 'script' tag.
            module.appendChild(createScriptElement(document, "common-dictionary.js"));
            for (final String technology : technologies)
            {
                module.appendChild(createScriptElement(document, String.format("%s-dictionary.js",
                        technology)));
            }
            // 'source' tag.
            module.appendChild(createSourceElement(document, "generic/client/web/client"));
            for (final String technology : technologies)
            {
                module.appendChild(createSourceElement(document, String.format(
                        SOURCE_PATH_TEMPLATE, technology)));
            }
            // 'public' tag.
            module.appendChild(createPublicElement(document, "public"));
            module.appendChild(createPublicElement(document, "generic/client/web/public"));
            for (final String technology : technologies)
            {
                if (technology.equals("generic"))
                {
                    continue;
                }
                module.appendChild(createPublicElement(document, String.format(
                        PUBLIC_PATH_TEMPLATE, technology)));
            }
            return document;
        } catch (final Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    private final static Element createPublicElement(final Document document,
            final String attributeValue)
    {
        final Element element = document.createElement(PUBLIC_ELEMENT);
        element.setAttribute("path", attributeValue);
        return element;
    }

    private final static Element createSourceElement(final Document document,
            final String attributeValue)
    {
        final Element element = document.createElement(SOURCE_ELEMENT);
        element.setAttribute("path", attributeValue);
        return element;
    }

    private final static Element createEntryPointElement(final Document document,
            final String attributeValue)
    {
        final Element element = document.createElement(ENTRY_POINT_ELEMENT);
        element.setAttribute("class", attributeValue);
        return element;
    }

    private final static Element createScriptElement(final Document document,
            final String attributeValue)
    {
        final Element element = document.createElement(SCRIPT_ELEMENT);
        element.setAttribute("src", attributeValue);
        return element;
    }

    private final static Element createStylesheetElement(final Document document,
            final String attributeValue)
    {
        final Element element = document.createElement(STYLESHEET_ELEMENT);
        element.setAttribute("src", attributeValue);
        return element;
    }

    private final static Element createInheritsElement(final Document document,
            final String attributeValue)
    {
        final Element element = document.createElement(INHERITS_ELEMENT);
        element.setAttribute("name", attributeValue);
        return element;
    }

    private static void writeXmlFile(final Document doc, final File file)
    {
        try
        {
            final Source source = new DOMSource(doc);
            final Result result = new StreamResult(file);
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.transform(source, result);
        } catch (final Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    /**
     * Updates the <code>OpenBIS.gwt.xml</code> up to the technologies found.
     */
    public final void updateOpenBISGwtXmlFile()
    {
        if (technologies.length == 0)
        {
            return;
        }
        final File openBISGwtXmlFile =
                new File(workingDirectory, OPENBIS_PACKAGE_NAME + "/" + OPENBIS_GWT_XML_FILE_NAME);
        final Document document = createXMLDocument(openBISGwtXmlFile);
        writeXmlFile(document, openBISGwtXmlFile);
    }

    /**
     * Updates {@link ClientPluginProvider} class up to the technologies found.
     */
    public final void updateClientPluginProvider()
    {
        if (technologies.length == 0)
        {
            return;
        }
        final File clientPluginProviderJavaFile =
                new File(workingDirectory, CLIENT_PLUGIN_PROVIDER_CLASS.replace(".", "/") + ".java");
        final String response =
                FileUtilities.checkFileFullyAccessible(clientPluginProviderJavaFile, "java");
        if (response != null)
        {
            throw new RuntimeException(response);
        }
        final StringBuilder builder = new StringBuilder(JAVA_MARKER_START);
        builder.append(OSUtilities.LINE_SEPARATOR);
        final String indent = StringUtils.repeat(" ", 8);
        for (final String technology : technologies)
        {
            if (technology.equals("generic"))
            {
                continue;
            }
            try
            {
                Class.forName(String.format(PLUGIN_FACTORY_CLASS_NAME_TEMPLATE, technology));
            } catch (final ClassNotFoundException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
            builder.append(indent);
            builder.append(String.format(PLUGIN_FACTORY_REGISTRATION_TEMPLATE, technology)).append(
                    OSUtilities.LINE_SEPARATOR);
        }
        builder.append(indent);
        String content = FileUtilities.loadToString(clientPluginProviderJavaFile);
        content =
                content.substring(0, content.indexOf(JAVA_MARKER_START)) + builder.toString()
                        + content.substring(content.indexOf(JAVA_MARKER_END), content.length());
        FileUtilities.writeToFile(clientPluginProviderJavaFile, content);
    }

    //
    // Main method
    //

    public static void main(final String[] args)
    {
        final int len = args.length;
        final String workingDirectory;
        final String[] technologies;
        switch (len)
        {
            case 0:
                workingDirectory = null;
                technologies = null;
                break;
            default:
                workingDirectory = args[0];
                technologies = new String[len - 1];
                for (int i = 1; i < len; i++)
                {
                    technologies[i - 0] = args[i];
                }
                break;
        }
        final WebClientFilesUpdater webClientFilesUpdater =
                new WebClientFilesUpdater(workingDirectory, technologies);
        webClientFilesUpdater.updateOpenBISGwtXmlFile();
        System.out.println(String.format("'%s' has been written out.", OPENBIS_GWT_XML_FILE_NAME));
        webClientFilesUpdater.updateClientPluginProvider();
        System.out.println(String.format("'%s' has been updated.", CLIENT_PLUGIN_PROVIDER_CLASS));
    }
}
