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

/**
 * @author felmer
 */
public class EclipseClasspathEntry
{
    public static final String SRC_KIND = "src";

    public static final String LIB_KIND = "lib";

    private final String kind;

    private final String path;

    public EclipseClasspathEntry(String kind, String path)
    {
        this.kind = kind;
        this.path = path;
    }

    public String getKind()
    {
        return kind;
    }

    public String getPath()
    {
        return path;
    }

    public boolean isSubprojectEntry()
    {
        return path.startsWith("/");
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof EclipseClasspathEntry == false)
        {
            return false;
        }
        EclipseClasspathEntry entry = (EclipseClasspathEntry) obj;
        return entry.kind.equals(kind) && entry.path.equals(path);
    }

    @Override
    public int hashCode()
    {
        return kind.hashCode() * 37 + path.hashCode();
    }

}