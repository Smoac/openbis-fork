package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity;

import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityBrowserGrid.ICriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * The provider which is able to load and reload property types. When property types are loaded the
 * specified callback is executed.<br>
 */
public class PropertyTypesCriteriaProvider implements ICriteriaProvider<PropertyTypesCriteria>
{
    private final IViewContext<?> viewContext;

    private final PropertyTypesCriteria criteria;

    // if not null filters only property types assigned to specified entity
    private final EntityKind propertiesFilterOrNull;

    public PropertyTypesCriteriaProvider(IViewContext<?> viewContext,
            EntityKind propertiesFilterOrNull)
    {
        this.viewContext = viewContext;
        this.criteria = new PropertyTypesCriteria();
        this.propertiesFilterOrNull = propertiesFilterOrNull;
    }

    private void loadPropertyTypes(IDataRefreshCallback dataRefreshCallback)
    {
        DefaultResultSetConfig<String, PropertyType> config =
                DefaultResultSetConfig.createFetchAll();
        viewContext.getCommonService().listPropertyTypes(config,
                new ListPropertyTypesCallback(viewContext, dataRefreshCallback));
    }

    private class ListPropertyTypesCallback extends AbstractAsyncCallback<ResultSet<PropertyType>>
    {
        private final IDataRefreshCallback dataRefreshCallback;

        public ListPropertyTypesCallback(IViewContext<?> viewContext,
                IDataRefreshCallback dataRefreshCallback)
        {
            super(viewContext);
            this.dataRefreshCallback = dataRefreshCallback;
        }

        @Override
        protected void process(ResultSet<PropertyType> result)
        {
            List<PropertyType> properties = result.getList().extractOriginalObjects();
            if (propertiesFilterOrNull != null)
            {
                properties =
                        PropertyTypesFilterUtil.filterPropertyTypesForEntityKind(properties,
                                propertiesFilterOrNull);
            }
            criteria.setPropertyTypes(properties);
            dataRefreshCallback.postRefresh(true);
        }
    }

    public PropertyTypesCriteria tryGetCriteria()
    {
        if (criteria.tryGetPropertyTypes() == null)
        {
            return null;
        } else
        {
            return criteria;
        }
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.PROPERTY_TYPE_ASSIGNMENT);
    }

    public void update(Set<DatabaseModificationKind> observedModifications,
            IDataRefreshCallback dataRefreshCallback)
    {
        loadPropertyTypes(dataRefreshCallback);
    }
}
