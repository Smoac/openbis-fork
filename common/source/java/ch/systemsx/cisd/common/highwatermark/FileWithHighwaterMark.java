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
 * 
 * @author Christian Ribeaud
 */
public final class FileWithHighwaterMark extends AbstractHashable implements Serializable
{

    static final String SEP = "-";

    public static final int DEFAULT_HIGHWATER_MARK = -1;

    private static final long serialVersionUID = 1L;

    /**
     * The property name under which you must find a <code>long</code> for the high water mark (in
     * <i>kilobytes</i>).
     */
    public static final String HIGHWATER_MARK_PROPERTY_KEY = "highwater-mark";

    private final File path;

    private final long highwaterMarkInKb;

    /**
     * @param file the file path.
     * @param highwaterMarkInKb the high water mark in <i>kilobytes</i>. <code>-1</code> means
     *            that the system will not be watching.
     */
    public FileWithHighwaterMark(final File file, final long highwaterMarkInKb)
    {
        this.path = file;
        this.highwaterMarkInKb = highwaterMarkInKb;
    }

    /**
     * @param path the file path.
     */
    public FileWithHighwaterMark(final File path)
    {
        this(path, DEFAULT_HIGHWATER_MARK);
    }

    /**
     * Instantiates a new <code>FileWithHighwaterMark</code> from given <var>properties</var>.
     * 
     * @param filePropertyKey the property key under which the file path can be found.
     * @throws ConfigurationFailureException if given <var>filePropertyKey</var> could not be found
     *             in given <var>properties</var>.
     */
    public final static FileWithHighwaterMark fromProperties(final Properties properties,
            final String filePropertyKey) throws ConfigurationFailureException
    {
        assert properties != null : "Unspecified properties";
        assert StringUtils.isNotBlank(filePropertyKey) : "File property key is blank";
        final String filePath = PropertyUtils.getMandatoryProperty(properties, filePropertyKey);
        final long highwaterMarkInKb =
                PropertyUtils.getLong(properties, filePropertyKey.concat(SEP).concat(
                        HIGHWATER_MARK_PROPERTY_KEY), -1L);
        return new FileWithHighwaterMark(new File(filePath), highwaterMarkInKb);
    }

    /**
     * Returns the file path.
     */
    public final File getFile()
    {
        return path;
    }

    /** Return the canonical path of the encapsulated <code>path</code>. */
    public final String getCanonicalPath()
    {
        return FileUtilities.getCanonicalPath(getFile());
    }

    /**
     * Returns the high water mark for this file.
     */
    public final long getHighwaterMark()
    {
        return highwaterMarkInKb;
    }
}
