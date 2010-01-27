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

package ch.systemsx.cisd.bds;

import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.INode;

/**
 * Immutable value object of a versioned format.
 * 
 * @author Franz-Josef Elmer
 */
public class Format implements IStorable
{

    public static final String FORMAT = "format";

    public static final String CODE = "code";

    public static final String VARIANT = "variant";

    /**
     * Loads the format from the specified directory.
     * 
     * @throws DataStructureException if the format could be loaded.
     */
    public final static Format loadFrom(final IDirectory directory)
    {
        final INode dir = directory.tryGetNode(FORMAT);
        if (dir instanceof IDirectory == false)
        {
            throw new DataStructureException("Not a directory: " + dir);
        }
        final IDirectory formatDir = (IDirectory) dir;
        INode file = formatDir.tryGetNode(CODE);
        if (file instanceof IFile == false)
        {
            throw new DataStructureException("Not a plain file: " + file);
        }
        final IFile codeFile = (IFile) file;
        final String formatCode = codeFile.getStringContent().trim();
        final Version formatVersion = Version.loadFrom(formatDir);
        String variant = null;
        file = formatDir.tryGetNode(VARIANT);
        if (file != null)
        {
            if (file instanceof IFile == false)
            {
                throw new DataStructureException("Not a plain file: " + file);
            }
            variant = ((IFile) file).getStringContent().trim();
        }
        Format format = FormatStore.getFormat(formatCode, formatVersion, variant);
        if (format == null)
        {
            format = new Format(formatCode, formatVersion, variant);
        }
        return format;
    }

    /**
     * Creates a <code>Format</code> from given <var>value</var>.
     * 
     * @param value an example: <code>UNKNOWN [A] V1.2</code>.
     * @return <code>null</code> if operation fails.
     */
    public static final Format tryToCreateFormatFromString(final String value)
    {
        return tryToCreateFormatFromString(value, false);
    }

    static Format tryToCreateFormatFromString(final String value, final boolean createIfWellFormed)
    {
        assert value != null : "Format string is not expected to be null.";
        int index = value.lastIndexOf('V');
        if (index > -1)
        {
            final Version version = Version.createVersionFromString(value.substring(index + 1));
            if (version != null)
            {
                String firstPart = value.substring(0, index).trim();
                String variant = null;
                if (firstPart.endsWith("]"))
                {
                    index = firstPart.indexOf('[');
                    if (index > -1)
                    {
                        variant = firstPart.substring(index + 1, firstPart.length() - 1);
                        firstPart = firstPart.substring(0, index).trim();
                    }
                }
                Format format = FormatStore.getFormat(firstPart, version, variant);
                if (format == null && createIfWellFormed)
                {
                    format = new Format(firstPart, version, variant);
                }
                return format;
            }
        }
        return null;
    }

    private final String code;

    private final Version version;

    private final String variant;

    /**
     * Creates a new instance based on the specified format code, format variant (optional), and
     * version.
     */
    public Format(final String code, final Version version, final String variantOrNull)
    {
        assert code != null : "Unspecified format code.";
        assert version != null : "Unpsecified version.";
        this.code = code;
        this.version = version;
        variant = variantOrNull;
    }

    /**
     * Returns the format code.
     */
    public final String getCode()
    {
        return code;
    }

    /**
     * Returns the format version.
     */
    public final Version getVersion()
    {
        return version;
    }

    /**
     * Returns the format variant.
     * 
     * @return <code>null</code> if undefined.
     */
    public final String getVariant()
    {
        return variant;
    }

    /**
     * Returns the <code>IFormatParameterFactory</code> implementation for this <code>Format</code>.
     */
    public IFormatParameterFactory getFormatParameterFactory()
    {
        return IFormatParameterFactory.DEFAULT_FORMAT_PARAMETER_FACTORY;
    }

    /**
     * Returns an unmodifiable list of mandatory parameters that are specific to this format.
     * <p>
     * They can be found in <code>metadata/parameters</code> directory. Default implementation
     * returns an empty list.
     * </p>
     */
    public List<String> getMandatoryParameterNames()
    {
        return Collections.emptyList();
    }

    /**
     * Returns an unmodifiable list of optional parameters that are specific to this format.
     * <p>
     * Default implementation returns an empty list.
     * </p>
     */
    public List<String> getOptionalParameterNames()
    {
        return Collections.emptyList();
    }

    //
    // IStorable
    //

    public final void saveTo(final IDirectory directory)
    {
        final IDirectory dir = directory.makeDirectory(FORMAT);
        dir.addKeyValuePair(CODE, code);
        version.saveTo(dir);
        if (variant != null)
        {
            dir.addKeyValuePair(VARIANT, variant);
        }
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Format == false)
        {
            return false;
        }
        final Format format = (Format) obj;
        return format.code.equals(code) && format.version.equals(version)
                && (format.variant == null ? null == variant : format.variant.equals(variant));
    }

    @Override
    public final int hashCode()
    {
        return (code.hashCode() * 37 + version.hashCode()) * 37
                + (variant == null ? 0 : variant.hashCode());
    }

    @Override
    public final String toString()
    {
        return "Format: " + code + " " + version + (variant == null ? "" : " [" + variant + "]");
    }

}
