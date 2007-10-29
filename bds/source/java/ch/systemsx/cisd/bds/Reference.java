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


/**
 * Immutable class defining a reference between to paths.
 *
 * @author Franz-Josef Elmer
 */
public class Reference
{
    private final String path;
    private final String originalPath;
    private final ReferenceType referenceType;

    /**
     * Creates an instance for the specified paths and reference type.
     *
     * @param path Path which referes to <code>originalPath</code>.
     * @param originalPath Path to which <code>path</code> referes.
     * @param referenceType Type of reference.
     */
    public Reference(String path, String originalPath, ReferenceType referenceType)
    {
        this.path = path;
        this.originalPath = originalPath;
        this.referenceType = referenceType;
    }

    /**
     * Returns the original path.
     */
    public final String getOriginalPath()
    {
        return originalPath;
    }

    /**
     * Returns the reference path.
     */
    public final String getPath()
    {
        return path;
    }

    /**
     * Returns the type of reference.
     */
    public final ReferenceType getReferenceType()
    {
        return referenceType;
    }
}
