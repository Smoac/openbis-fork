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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.columns;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractColumnDefinition;

/**
 * Definition of experiment table columns.
 * 
 * @author Tomasz Pylak
 */
public enum CommonExperimentColDefKind implements IsSerializable
{
    CODE(ModelDataPropertyNames.CODE, Dict.CODE),

    EXPERIMENT_TYPE(ModelDataPropertyNames.EXPERIMENT_TYPE_CODE_FOR_EXPERIMENT,
            Dict.EXPERIMENT_TYPE),

    EXPERIMENT_IDENTIFIER(ModelDataPropertyNames.EXPERIMENT_IDENTIFIER, Dict.EXPERIMENT_IDENTIFIER,
            150, true),

    GROUP(ModelDataPropertyNames.GROUP_FOR_EXPERIMENT, Dict.GROUP),

    PROJECT(ModelDataPropertyNames.PROJECT, Dict.PROJECT),

    REGISTRATOR(ModelDataPropertyNames.REGISTRATOR, Dict.REGISTRATOR),

    REGISTRATION_DATE(ModelDataPropertyNames.REGISTRATION_DATE, Dict.REGISTRATION_DATE, 200, false),

    IS_INVALID(ModelDataPropertyNames.IS_INVALID, Dict.IS_INVALID, true);

    // TODO 2008-12-08, Tomasz Pylak: refactor the code to remove this field. It has to have the
    // same name as the Sample field because grid sorting was implemented in that ugly way.
    private String sortField;

    private String headerMsgKey;

    private int width;

    private boolean isHidden;

    private CommonExperimentColDefKind(final String sortField, final String headerMsgKey,
            final int width, final boolean isHidden)
    {
        this.sortField = sortField;
        this.headerMsgKey = headerMsgKey;
        this.width = width;
        this.isHidden = isHidden;
    }

    private CommonExperimentColDefKind(final String sortField, final String headerMsgKey,
            final boolean isHidden)
    {
        this(sortField, headerMsgKey, AbstractColumnDefinition.DEFAULT_COLUMN_WIDTH, isHidden);
    }

    private CommonExperimentColDefKind(final String sortField, final String headerMsgKey)
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