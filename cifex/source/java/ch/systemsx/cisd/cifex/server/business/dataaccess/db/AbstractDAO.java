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

package ch.systemsx.cisd.cifex.server.business.dataaccess.db;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import ch.systemsx.cisd.common.db.ISequencerHandler;

/**
 * Abstract super class of all Data Access Objects (DAOs).
 * 
 * @author Franz-Josef Elmer
 */
abstract class AbstractDAO extends SimpleJdbcDaoSupport
{
    protected final ISequencerHandler sequencerHandler;

    protected final boolean supportsAnyOperator;

    AbstractDAO(final DataSource dataSource, final ISequencerHandler sequencerHandler,
            final boolean supportsAnyOperator)
    {
        assert dataSource != null : "Unspecified data source.";
        assert sequencerHandler != null : "Unspecified sequencer handler.";

        this.sequencerHandler = sequencerHandler;
        this.supportsAnyOperator = supportsAnyOperator;
        setDataSource(dataSource);

    }

    protected final long getNextValueOf(final String sequencer) throws DataAccessException
    {
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        return template.queryForLong(sequencerHandler.getNextValueScript(sequencer));
    }
}
