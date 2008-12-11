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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.columns;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;

public enum CommonSampleColDefKind implements IsSerializable
{
    DATABASE_INSTANCE(ModelDataPropertyNames.DATABASE_INSTANCE, "database_instance", true),

    GROUP(ModelDataPropertyNames.GROUP, "group"),

    CODE(ModelDataPropertyNames.CODE, "code"),

    SAMPLE_IDENTIFIER(ModelDataPropertyNames.SAMPLE_IDENTIFIER, "sample_identifier", 150, true),

    IS_INSTANCE_SAMPLE(ModelDataPropertyNames.IS_INSTANCE_SAMPLE, "is_instance_sample", true),

    REGISTRATOR(ModelDataPropertyNames.REGISTRATOR, "registrator", true),

    REGISTRATION_DATE(ModelDataPropertyNames.REGISTRATION_DATE, "registration_date", 200, true),

    IS_INVALID(ModelDataPropertyNames.IS_INVALID, "is_invalid", true),

    PROJECT_FOR_SAMPLE(ModelDataPropertyNames.PROJECT_FOR_SAMPLE, "project"),

    EXPERIMENT_FOR_SAMPLE(ModelDataPropertyNames.EXPERIMENT_FOR_SAMPLE, "experiment"),

    EXPERIMENT_IDENTIFIER_FOR_SAMPLE(ModelDataPropertyNames.EXPERIMENT_IDENTIFIER_FOR_SAMPLE,
            "experiment_identifier", 200, true);

    // TODO 2008-12-08, Tomasz Pylak: refactor the code to remove this field. It has to have the
    // same name as the Sample field because grid sorting was implemented in that ugly way.
    private String sortField;

    private String headerMsgKey;

    private int width;

    private boolean isHidden;

    private CommonSampleColDefKind(final String sortField, final String headerMsgKey,
            final int width, final boolean isHidden)
    {
        this.sortField = sortField;
        this.headerMsgKey = headerMsgKey;
        this.width = width;
        this.isHidden = isHidden;
    }

    private CommonSampleColDefKind(final String sortField, final String headerMsgKey,
            final boolean isHidden)
    {
        this(sortField, headerMsgKey, AbstractSampleColDef.DEFAULT_COLUMN_WIDTH, isHidden);
    }

    private CommonSampleColDefKind(final String sortField, final String headerMsgKey)
    {
        this(sortField, headerMsgKey, false);
    }

    public int getWidth()
    {
        return width;
    }

    public boolean isHidden()
    {
        return isHidden;
    }

    // key in the translations dictionary
    public String getHeaderMsgKey()
    {
        return headerMsgKey;
    }

    public String id()
    {
        return sortField; // NOTE: it should be possible to use name() when sorting will be fixed
    }
}