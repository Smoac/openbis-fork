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

package ch.systemsx.cisd.cifex.server.business.dataaccess;

/**
 * A <i>Data Access Object</i> factory.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDAOFactory
{
    /** Returns the <code>IUserDAO</code> implementation. */
    public IUserDAO getUserDAO();

    /** Returns the <code>IFileDAO</code> implementation. */
    public IFileDAO getFileDAO();

}
