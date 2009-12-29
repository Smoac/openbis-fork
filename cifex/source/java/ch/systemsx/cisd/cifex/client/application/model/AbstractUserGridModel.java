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

package ch.systemsx.cisd.cifex.client.application.model;

import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.Model;
import ch.systemsx.cisd.cifex.client.application.grid.AbstractFilterField;
import ch.systemsx.cisd.cifex.client.application.ui.UserRenderer;
import ch.systemsx.cisd.cifex.client.application.utils.DateTimeUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * An <code>AbstractDataGridModel</code> extension for user grid.
 * 
 * @author Basil Neff
 */
public abstract class AbstractUserGridModel extends AbstractDataGridModel
{
    private static final long serialVersionUID = Constants.VERSION;

    public static final String USER_CODE = "userCode";

    public static final String USER_EMAIL = "userEmail";

    public static final String FULL_NAME = "fullName";

    public static final String STATUS = "status";

    public static final String ACTIVE = "active";

    public static final String REGISTRATOR = "registrator";

    public static final String ACTION = "action";

    public static <M extends ModelData> List<AbstractFilterField<M>> createFilterItems(
            IMessageResources messageResources, List<ColumnConfig> columnConfigs)
    {
        return createFilterItems(columnConfigs, getInitialFilters());
    }

    private static List<String> getInitialFilters()
    {
        return Arrays.asList(AbstractUserGridModel.USER_CODE, AbstractUserGridModel.FULL_NAME,
                AbstractUserGridModel.STATUS);
    }

    /**
     * The currently logged-in user.
     * <p>
     * You typically call {@link Model#getUser()} to get him.
     * </p>
     */
    protected final UserInfoDTO currentUser;

    public AbstractUserGridModel(final IMessageResources messageResources,
            final UserInfoDTO currentUser)
    {
        super(messageResources);
        this.currentUser = currentUser;
    }

    protected final static ColumnConfig createActionColumnConfig(IMessageResources messageResources)
    {
        final ColumnConfig actionColumn =
                createSortableColumnConfig(ACTION, messageResources.getActionLabel(), 200);
        return actionColumn;
    }

    protected final static ColumnConfig createUserCodeColumnConfig(
            IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(USER_CODE, messageResources.getUserCodeLabel(), 180);
        return columnConfig;
    }

    protected final static ColumnConfig createUserEmailColumnConfig(
            IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(USER_EMAIL, messageResources.getUserEmailLabel(), 180);
        columnConfig.setRenderer(UserRenderer.USER_RENDERER);
        return columnConfig;
    }

    /**
     * Note that this column is NOT sortable.
     */
    protected final static ColumnConfig createRegistratorColumnConfig(
            IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createColumnConfig(REGISTRATOR, messageResources.getRegistratorLabel(), 180);
        return columnConfig;
    }

    protected final static ColumnConfig createStatusColumnConfig(IMessageResources messageResources)
    {
        return createSortableColumnConfig(STATUS, messageResources.getStatusLabel(), 250);
    }

    protected final static ColumnConfig createActiveColumnConfig(IMessageResources messageResources)
    {
        return createSortableColumnConfig(ACTIVE, messageResources.getUserActiveLabel(), 100);
    }

    protected final static ColumnConfig createFullNameColumnConfig(
            IMessageResources messageResources)
    {
        return createSortableColumnConfig(FULL_NAME, messageResources.getUserFullNameLabel(), 180);
    }

    protected final String getUserRoleDescription(final UserInfoDTO user)
    {
        String stateField = "";
        if (user.isAdmin())
        {
            stateField = messageResources.getAdminRoleName();
        } else if (user.isPermanent())
        {
            stateField += messageResources.getPermanentRoleName() + " User";
        } else
        {
            stateField += messageResources.getTemporaryRoleName();
            if (user.getExpirationDate() != null)
            {
                stateField +=
                        " User expires on ".concat(DateTimeUtils.formatDate(user
                                .getExpirationDate()));
            }
        }
        return stateField;
    }

}
