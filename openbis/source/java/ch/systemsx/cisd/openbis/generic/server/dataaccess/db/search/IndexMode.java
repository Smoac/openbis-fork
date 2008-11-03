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

/**
 * The mode in which the indexing is working:
 * <ul>
 * <li><b>NO_INDEX</b>: no manual index will be performed.</li>
 * <li><b>SKIP_IF_MARKER_FOUND</b>: skips the indexing if
 * {@link FullTextIndexerRunnable#FULL_TEXT_INDEX_MARKER_FILENAME} marker file is found (meaning
 * that a manual index has already been performed).</li>
 * <li><b>ALWAYS_INDEX</b>: always performs a manual index, letting <i>Hibernate Search</i>
 * managing the indices which are out-of-date.</li>
 * <li><b>INDEX_FROM_SCRATCH</b>: performs a manual index from the scratch, deleting the whole
 * <code>index-base</code> directory.</li>
 * </ul>
 * 
 * @author Christian Ribeaud
 */
public enum IndexMode
{
    NO_INDEX, SKIP_IF_MARKER_FOUND, ALWAYS_INDEX, INDEX_FROM_SCRATCH;
}
