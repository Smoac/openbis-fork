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

package ch.systemsx.cisd.common.hdf5;

import java.io.OutputStream;
import java.util.List;

/**
 * A simple abstraction of the methods needed to read from an HDF5 container.
 *
 * @author Bernd Rinn
 */
public interface IHDF5ContainerReader
{

    /**
     * Closes this object and the file referenced by this object. This object must not be used after
     * being closed.
     */
    public void close();

    /**
     * Returns the members of <var>groupPath</var>. The order is <i>not</i> well defined.
     * 
     * @param groupPath The path of the group to get the members for.
     * @throws IllegalArgumentException If <var>groupPath</var> is not a group.
     */
    public List<String> getGroupMembers(final String groupPath);

    /**
     * Returns <code>true</code>, if <var>objectPath</var> exists and <code>false</code> otherwise.
     */
    public boolean exists(final String objectPath);

    /**
     * Returns <code>true</code> if the <var>objectPath</var> exists and represents a group and
     * <code>false</code> otherwise.
     */
    public boolean isGroup(final String objectPath);

    /**
     * Reads the data set <var>objectPath</var> into the <var>ostream</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file.
     */
    public void readFromHDF5Container(final String objectPath, final OutputStream ostream);

}
