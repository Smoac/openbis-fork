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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate;

import java.util.List;

/**
 * Translator from long IDs to {@link OBJECT_PE} type.
 *
 * @param <OBJECT_PE>
 */
public interface IID2PETranslator<OBJECT_PE>
{

    List<OBJECT_PE> translate(final List<Long> ids);

}
