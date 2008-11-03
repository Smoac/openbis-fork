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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

import org.hibernate.Session;
import org.springframework.dao.DataAccessException;

/**
 * Each implementation is able to perform a <i>full-text</i> index.
 * 
 * @author Christian Ribeaud
 */
public interface IFullTextIndexer
{

    /**
     * Performs a <i>full-text</i> index on given <var>clazz</var> using given <i>Hibernate</i>
     * session.
     */
    public <T> void doFullTextIndex(final Session hibernateSession, final Class<T> clazz)
            throws DataAccessException;
}
