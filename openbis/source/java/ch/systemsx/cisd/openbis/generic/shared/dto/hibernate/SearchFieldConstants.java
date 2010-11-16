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

package ch.systemsx.cisd.openbis.generic.shared.dto.hibernate;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * Prefixes for indexed fields. Should be used everywhere where {@link IndexedEmbedded} or
 * {@link Field} annotation is used, in this way we can make search query syntax independent from
 * bean field names.
 * 
 * @author Tomasz Pylak
 */
public final class SearchFieldConstants
{
    private static final String SEPARATOR = " ";

    public static final String PREFIX_PROPERTIES = "property" + SEPARATOR;

    public static final String PREFIX_EXPERIMENT = "experiment" + SEPARATOR;

    public static final String PREFIX_SAMPLE = "sample" + SEPARATOR;

    public static final String PREFIX_ENTITY_TYPE = "type" + SEPARATOR;

    public static final String PREFIX_FILE_FORMAT_TYPE = "file format type" + SEPARATOR;

    public static final String PREFIX_PROJECT = "project" + SEPARATOR;

    public static final String PREFIX_GROUP = "space" + SEPARATOR;

    public static final String PREFIX_REGISTRATOR = "registrator" + SEPARATOR;

    public static final String PREFIX_ATTACHMENT = "attachment" + SEPARATOR;

    public static final String DELETED = "deleted";

    public static final String PERSON_LAST_NAME = "Last Name";

    public static final String PERSON_FIRST_NAME = "First Name";

    public static final String PERSON_EMAIL = "Email";

    public static final String PERSON_USER_ID = "User Id";

    public static final String ID = "id";

    public static final String CODE = "code";

    public static final String PERM_ID = "perm_id";

    public static final String IDENTIFIER = "identifier";

    public static final String FILE_NAME = "name";

    public static final String FILE_TITLE = "title";

    public static final String FILE_DESCRIPTION = "description";

}
