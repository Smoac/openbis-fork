/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.yeastx.db;

import java.io.File;
import java.sql.SQLException;

import ch.systemsx.cisd.yeastx.db.generic.DMDataSetDTO;

/**
 * @author Tomasz Pylak
 */
public interface IDatasetLoader
{

    /**
     * Uploads files with recognized extensions to the additional database.<br>
     * If the exception is thrown than all the operations are rollbacked automatically and the next
     * upload can take place without calling {@link #rollback()} or {@link #commit()} explicitly.
     */
    void upload(final File file, final DMDataSetDTO dataSet) throws SQLException;

    /** Commits the previous {@link #upload} */
    void commit();

    /** Rollbacks the previous {@link #upload} */
    void rollback();
}
