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

package ch.systemsx.cisd.bds.storage.filesystem;

import java.io.File;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.ILink;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class Link implements ILink
{

    private final String name;
    private IDirectory parent;
    private final INode reference;

    Link(String name, INode reference)
    {
        this.name = name;
        this.reference = reference;
    }

    public String getName()
    {
        return name;
    }

    void setParent(IDirectory parentOrNull)
    {
        parent = parentOrNull;
    }
    
    public IDirectory tryToGetParent()
    {
        return parent;
    }

    public INode getReference()
    {
        return reference;
    }

    public void extractTo(File directory) throws UserFailureException, EnvironmentFailureException
    {
        // TODO Auto-generated method stub
    }

}
