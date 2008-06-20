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

package ch.systemsx.cisd.common.highwatermark;

import java.io.File;
import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * A {@link File} with a <i>high water mark</i> attached to it.
 * <p>
 * This file could be located on an external host.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class HostAwareFileWithHighwaterMark extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = 1L;

    static final String SEP = "-";

    public static final char HOST_FILE_SEP = ':';

    public static final int DEFAULT_HIGHWATER_MARK = -1;

    /**
     * The property name under which you must find a <code>long</code> for the high water mark (in
     * <i>kilobytes</i>).
     */
    public static final String HIGHWATER_MARK_PROPERTY_KEY = "highwater-mark";

    private final String host;

    private final File path;

    private final long highwaterMarkInKb;

    /**
     * @param hostOrNull the host on which given <var>file</var> is located.
     * @param file the file path.
     * @param highwaterMarkInKb the high water mark in <i>kilobytes</i>. <code>-1</code> means
     *            that the system will not be watching.
     */
    public HostAwareFileWithHighwaterMark(final String hostOrNull, final File file,
            final long highwaterMarkInKb)
    {
        this.host = hostOrNull;
        this.path = file;
        this.highwaterMarkInKb = highwaterMarkInKb;
    }

    /**
     * @param path the file path.
     * @param highwaterMarkInKb the high water mark in <i>kilobytes</i>. <code>-1</code> means
     *            that the system will not be watching.
     */
    public HostAwareFileWithHighwaterMark(final File path, final long highwaterMarkInKb)
    {
        this(null, path, highwaterMarkInKb);
    }

    /**
     * @param path the file path.
     */
    public HostAwareFileWithHighwaterMark(final File path)
    {
        this(path, DEFAULT_HIGHWATER_MARK);
    }

    /**
     * @param hostOrNull the host on which given <var>file</var> is located.
     * @param path the file path.
     */
    public HostAwareFileWithHighwaterMark(final String hostOrNull, final File path)
    {
        this(hostOrNull, path, DEFAULT_HIGHWATER_MARK);
    }

    /**
     * Instantiates a new <code>FileWithHighwaterMark</code> from given <var>properties</var>.
     * Some examples:
     * 
     * <pre>
     * &lt;host-file-property-key&gt; = /temp
     * &lt;host-file-property-key&gt; = localhost:/temp
     * &lt;host-file-property-key&gt;-highwater-mark = 123456
     * </pre>
     * 
     * @param hostFilePropertyKey the property key under which the host/file path can be found.
     * @throws ConfigurationFailureException if given <var>hostFilePropertyKey</var> could not be
     *             found in given <var>properties</var>.
     */
    public final static HostAwareFileWithHighwaterMark fromProperties(final Properties properties,
            final String hostFilePropertyKey) throws ConfigurationFailureException
    {
        assert properties != null : "Unspecified properties";
        assert StringUtils.isNotBlank(hostFilePropertyKey) : "Host-file property key is blank";
        final String hostFile = PropertyUtils.getMandatoryProperty(properties, hostFilePropertyKey);
        final String filePath;
        String hostNameOrNull = null;
        final int index = hostFile.indexOf(HOST_FILE_SEP);
        if (index > -1)
        {
            hostNameOrNull = hostFile.substring(0, index);
            filePath = hostFile.substring(index + 1);
        } else
        {
            filePath = hostFile;
        }
        final long highwaterMarkInKb =
                PropertyUtils.getLong(properties, hostFilePropertyKey.concat(SEP).concat(
                        HIGHWATER_MARK_PROPERTY_KEY), -1L);
        return new HostAwareFileWithHighwaterMark(hostNameOrNull, new File(filePath),
                highwaterMarkInKb);
    }

    /** Returns the host on which {@link #getFile()} is located on. */
    public final String tryGetHost()
    {
        return host;
    }

    /**
     * Returns the file path.
     */
    public final File getFile()
    {
        return path;
    }

    /**
     * Returns the high water mark for this file.
     */
    public final long getHighwaterMark()
    {
        return highwaterMarkInKb;
    }

    /** Return the canonical path of the encapsulated <code>path</code>. */
    public final String getCanonicalPath()
    {
        if (tryGetHost() == null)
        {
            return FileUtilities.getCanonicalPath(getFile());
        } else
        {
            return tryGetHost() + HOST_FILE_SEP + getFile();
        }
    }
}
