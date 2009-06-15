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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.attachment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.grid.CellSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.AttachmentVersionModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.AttachmentColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentVersions;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IAttachmentHolder;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * Grid displaying {@link AttachmentVersions}.
 * 
 * @author Piotr Buczek
 */
public class AttachmentBrowser extends AbstractSimpleBrowserGrid<AttachmentVersions>
{
    private static final String PREFIX = "attachment-browser_";

    private static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final IAttachmentHolder attachmentHolder)
    {
        final AttachmentBrowser browser = new AttachmentBrowser(viewContext, attachmentHolder);
        return browser.asDisposableWithoutToolbar();
    }

    private final IAttachmentHolder attachmentHolder;

    public AttachmentBrowser(IViewContext<ICommonClientServiceAsync> viewContext,
            final IAttachmentHolder attachmentHolder)
    {
        super(viewContext, createBrowserId(attachmentHolder), createGridId(attachmentHolder));
        this.attachmentHolder = attachmentHolder;
        setDisplayTypeIDGenerator(DisplayTypeIDGenerator.ATTACHMENT_BROWSER_GRID);
        extendBottomToolbar();

        registerCellClickListenerFor(AttachmentColDefKind.FILE_NAME.id(),
                new ICellListener<AttachmentVersions>()
                    {
                        public void handle(AttachmentVersions rowItem)
                        {
                            // don't need to check whether the value is null
                            // because there will not be a link for null value
                            final String fileName = rowItem.getCurrent().getFileName();
                            final int version = rowItem.getCurrent().getVersion();

                            downloadAttachment(fileName, version, attachmentHolder);
                        }
                    });
        registerLinkClickListenerFor(AttachmentColDefKind.VERSION.id(),
                new ICellListener<AttachmentVersions>()
                    {
                        public void handle(AttachmentVersions rowItem)
                        {
                            // don't need to check whether the value is null
                            // because there will not be a link for null value
                            final String fileName = rowItem.getCurrent().getFileName();
                            List<Attachment> versions = rowItem.getVersions();

                            showVersionsPanel(fileName, versions);
                        }
                    });
    }

    private static String createGridId(final IAttachmentHolder holder)
    {
        return createGridId(TechId.create(holder), holder.getAttachmentHolderKind());
    }

    private static String createBrowserId(final IAttachmentHolder holder)
    {
        return createBrowserId(TechId.create(holder), holder.getAttachmentHolderKind());
    }

    public static String createGridId(TechId id, AttachmentHolderKind kind)
    {
        return createBrowserId(id, kind) + "-grid";
    }

    public static String createBrowserId(TechId id, AttachmentHolderKind kind)
    {
        return ID_PREFIX + kind.name() + "-" + id;
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        addButton(createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                new AbstractCreateDialogListener()
                    {
                        @Override
                        protected Dialog createDialog(List<AttachmentVersions> attachmentVersions,
                                IBrowserGridActionInvoker invoker)
                        {
                            return new AttachmentDeletionConfirmationDialog(attachmentVersions,
                                    invoker);
                        }
                    }));
        allowMultipleSelection(); // we allow deletion of multiple attachments

        addEntityOperationsSeparator();
    }

    private static void downloadAttachment(String fileName, int version, IAttachmentHolder holder)
    {
        WindowUtils.openWindow(createURL(version, fileName, holder));
    }

    private final static String createURL(final int version, final String fileName,
            final IAttachmentHolder attachmentHolder)
    {
        URLMethodWithParameters methodWithParameters =
                new URLMethodWithParameters(GenericConstants.ATTACHMENT_DOWNLOAD_SERVLET_NAME);
        methodWithParameters.addParameter(GenericConstants.VERSION_PARAMETER, version);
        methodWithParameters.addParameter(GenericConstants.FILE_NAME_PARAMETER, fileName);
        methodWithParameters.addParameter(GenericConstants.ATTACHMENT_HOLDER_PARAMETER,
                attachmentHolder.getAttachmentHolderKind().name());
        // NOTE: this exp.getId() could be null if exp is a proxy
        methodWithParameters.addParameter(GenericConstants.TECH_ID_PARAMETER, attachmentHolder
                .getId());
        return methodWithParameters.toString();
    }

    private void showVersionsPanel(final String fileName, final List<Attachment> versions)
    {
        assert versions != null : "versions not set!";

        final VersionsPanelHelper helper =
                new VersionsPanelHelper(fileName, attachmentHolder, viewContext);
        final ITabItemFactory tabFactory = new ITabItemFactory()
            {
                public ITabItem create()
                {
                    final String tabTitle = helper.createTabTitle();
                    final Component component = helper.createVersionsPanel(versions);
                    return DefaultTabItem.createUnaware(tabTitle, component, false);
                }

                public String getId()
                {
                    return helper.createTabId();
                }
            };
        DispatcherHelper.dispatchNaviEvent(tabFactory);
    }

    @Override
    protected IColumnDefinitionKind<AttachmentVersions>[] getStaticColumnsDefinition()
    {
        return AttachmentColDefKind.values();
    }

    @Override
    protected BaseEntityModel<AttachmentVersions> createModel(AttachmentVersions entity)
    {
        BaseEntityModel<AttachmentVersions> model = super.createModel(entity);
        model.renderAsLink(AttachmentColDefKind.FILE_NAME.id());
        renderVersionAsLink(model);
        return model;
    }

    private void renderVersionAsLink(ModelData model)
    {
        String versionId = AttachmentColDefKind.VERSION.id();
        String originalValue = model.get(versionId);
        String linkText = viewContext.getMessage(Dict.SHOW_ALL_VERSIONS);
        String link = LinkRenderer.renderAsLinkWithAnchor(linkText);
        model.set(versionId, originalValue + " (" + link + ")");
    }

    @Override
    protected List<IColumnDefinition<AttachmentVersions>> getInitialFilters()
    {
        return Collections.emptyList();
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, AttachmentVersions> resultSetConfig,
            AbstractAsyncCallback<ResultSet<AttachmentVersions>> callback)
    {
        viewContext.getService().listAttachmentVersions(TechId.create(attachmentHolder),
                attachmentHolder.getAttachmentHolderKind(), resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<AttachmentVersions> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportAttachmentVersions(exportCriteria, callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.edit(getObjectKind()) };
    }

    private ObjectKind getObjectKind()
    {
        return ObjectKind.valueOf(attachmentHolder.getAttachmentHolderKind().toString());
    }

    //
    // Helpers
    //

    private final class AttachmentDeletionConfirmationDialog extends DeletionConfirmationDialog
    {
        public AttachmentDeletionConfirmationDialog(List<AttachmentVersions> attachments,
                IBrowserGridActionInvoker invoker)
        {
            super(attachments, invoker);
        }

        @Override
        protected void executeConfirmedAction()
        {
            viewContext.getCommonService().deleteAttachments(TechId.create(attachmentHolder),
                    attachmentHolder.getAttachmentHolderKind(), getAttachmentFileNames(data),
                    reason.getValue(), new DeletionCallback(viewContext, invoker));
        }

        @Override
        protected String getEntityName()
        {
            return EntityKind.SAMPLE.getDescription();
        }

        private List<String> getAttachmentFileNames(List<AttachmentVersions> attachmentVersions)
        {
            List<String> fileNames = new ArrayList<String>();
            for (AttachmentVersions attachmentVersion : attachmentVersions)
            {
                fileNames.add(attachmentVersion.getCurrent().getFileName());
            }
            return fileNames;
        }

    }

    private final static class VersionsPanelHelper
    {
        private final String fileName;

        private final IAttachmentHolder attachmentHolder;

        private final IMessageProvider messageProvider;

        public VersionsPanelHelper(String fileName, IAttachmentHolder attachmentHolder,
                IMessageProvider messageProvider)
        {
            this.fileName = fileName;
            this.attachmentHolder = attachmentHolder;
            this.messageProvider = messageProvider;
        }

        public ContentPanel createVersionsPanel(final List<Attachment> oldVersions)
        {
            ContentPanel panel = new ContentPanel();
            panel.setHeading("Versions of file '" + fileName + "' from "
                    + attachmentHolder.getAttachmentHolderKind().name().toLowerCase() + " '"
                    + attachmentHolder.getCode() + "'");
            final ListStore<AttachmentVersionModel> attachmentStore =
                    new ListStore<AttachmentVersionModel>();
            attachmentStore.add(AttachmentVersionModel.convert(oldVersions));
            final Grid<AttachmentVersionModel> attachmentGrid =
                    new Grid<AttachmentVersionModel>(attachmentStore, new ColumnModel(
                            defineAttachmentVersionsColumns()));
            final CellSelectionModel<AttachmentVersionModel> selectionModel =
                    new CellSelectionModel<AttachmentVersionModel>();
            attachmentGrid.setSelectionModel(selectionModel);
            selectionModel.bindGrid(attachmentGrid);
            attachmentGrid.addListener(Events.CellClick, new Listener<GridEvent>()
                {
                    public void handleEvent(final GridEvent be)
                    {
                        String column =
                                attachmentGrid.getColumnModel().getColumn(be.colIndex).getId();
                        if (ModelDataPropertyNames.VERSION_FILE_NAME.equals(column))
                        {
                            final AttachmentVersionModel selectedItem =
                                    (AttachmentVersionModel) be.grid.getStore().getAt(be.rowIndex);
                            Attachment selectedAttachment =
                                    (Attachment) selectedItem.get(ModelDataPropertyNames.OBJECT);
                            int version = selectedAttachment.getVersion();
                            downloadAttachment(fileName, version, attachmentHolder);
                        }
                        attachmentGrid.getSelectionModel().deselectAll();
                    }
                });
            panel.setId(createTabId());
            panel.add(attachmentGrid);
            return panel;
        }

        // @Private
        public String createTabId()
        {
            return GenericConstants.ID_PREFIX + "attachment-versions-" + attachmentHolder.getId()
                    + "_" + fileName;
        }

        // @Private
        public String createTabTitle()
        {
            return "Attachment " + attachmentHolder.getCode() + "/" + fileName;
        }

        private List<ColumnConfig> defineAttachmentVersionsColumns()
        {
            final ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
            columns.add(createVersionFileNameColumn());
            columns.add(ColumnConfigFactory.createRegistrationDateColumnConfig(messageProvider));
            columns.add(ColumnConfigFactory.createRegistratorColumnConfig(messageProvider));
            return columns;
        }

        private ColumnConfig createVersionFileNameColumn()
        {
            final ColumnConfig column =
                    ColumnConfigFactory.createDefaultColumnConfig(messageProvider
                            .getMessage(Dict.VERSION_FILE_NAME),
                            ModelDataPropertyNames.VERSION_FILE_NAME);
            column.setWidth(200);
            column.setRenderer(new GridCellRenderer<AttachmentVersionModel>()
                {

                    public String render(final AttachmentVersionModel model, final String property,
                            final ColumnData config, final int rowIndex, final int colIndex,
                            final ListStore<AttachmentVersionModel> store)
                    {
                        Object value = model.get(property);
                        if (value == null)
                        {
                            return "";
                        }
                        return LinkRenderer.renderAsLink((String) value);
                    }
                });
            return column;

        }

    }

}
