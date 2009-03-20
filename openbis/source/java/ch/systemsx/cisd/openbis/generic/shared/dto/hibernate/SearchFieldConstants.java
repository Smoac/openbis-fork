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

    public static final String PREFIX_DATASET_TYPE = "dataset type" + SEPARATOR;

    public static final String PREFIX_FILE_FORMAT_TYPE = "file format type" + SEPARATOR;

    public static final String PREFIX_PROJECT = "project" + SEPARATOR;

    public static final String PREFIX_EXPERIMENT_TYPE = "type" + SEPARATOR;

    public static final String PREFIX_GROUP = "group" + SEPARATOR;

    public static final String PREFIX_SAMPLE_TYPE = "type" + SEPARATOR;

    public static final String PREFIX_REGISTRATOR = "registrator: ";

    public static final String PREFIX_ATTACHMENT = "attachment: ";

    public static final String PREFIX_ATTACHMENT_FILE_NAME = "attachment name: ";

    public static final String DELETED = "deleted";

    public static final String PERSON_LAST_NAME = "Last Name";

    public static final String PERSON_FIRST_NAME = "First Name";

    public static final String CODE = "code";

    /**
     * These constants were intentionally left empty. Constant can be empty only when we are sure,
     * that the name of the index field will be unique and non empty anyway (e.g. it is build in a
     * bridge class or the field name consists of some other non empty elements).
     */

    public static final String PREFIX_PROCEDURE = "";

    public static final String PREFIX_EXPERIMENT_ATTACHMENTS = "";
}
