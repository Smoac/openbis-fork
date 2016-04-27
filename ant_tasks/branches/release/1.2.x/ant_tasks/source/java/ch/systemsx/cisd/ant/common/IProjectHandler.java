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
 * Call-back interface used by {@link RecursiveProjectTraverser} for traversing project dependency graphs.
 *
 * @author felmer
 */
public interface IProjectHandler
{
    /**
     * Handle project when entering its graph vertex.
     */
    public void handleOnEntering();
    
    /**
     * Handle project when entering its graph vertex.
     */
    public void handleOnLeaving();
 
    /**
     * Returns the Eclipse classpath location for the correcponding project vertex.
     * 
     * @return <code>null</code> if this project has no Eclipse classpath location.
     */
    public IEclipseClasspathLocation createLocation();
    
    /**
     * Handles specified entry.
     */
    public void handleEntry(EclipseClasspathEntry entry);
    
    /**
     * Creates a new project handler based on the specified entry. 
     * Implementations should never return <code>null</code>.
     */
    public IProjectHandler createHandler(EclipseClasspathEntry entry);
}
