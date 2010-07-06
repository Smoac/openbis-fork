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

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.AttachmentVersionModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.AttachmentColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentVersions;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IAttachmentHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
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

    public static final String DOWNLOAD_BUTTON_ID_SUFFIX = "_download-button";

    private final IDelegatedAction postRegistrationCallback;

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
        super(viewContext, createBrowserId(attachmentHolder), createGridId(attachmentHolder),
                DisplayTypeIDGenerator.ATTACHMENT_BROWSER_GRID);
        this.attachmentHolder = attachmentHolder;
        postRegistrationCallback = createRefreshGridAction();
        extendBottomToolbar();

        registerLinkClickListenerFor(AttachmentColDefKind.FILE_NAME.id(),
                new ICellListener<AttachmentVersions>()
                    {
                        public void handle(AttachmentVersions rowItem, boolean keyPressed)
                        {
                            // don't need to check whether the value is null
                            // because there will not be a link for null value
                            final String fileName = rowItem.getCurrent().getFileName();
                            final int version = rowItem.getCurrent().getVersion();

                            AttachmentDownloadHelper.download(fileName, version, attachmentHolder);
                        }
                    });
        registerLinkClickListenerFor(AttachmentColDefKind.VERSION.id(),
                new ICellListener<AttachmentVersions>()
                    {
                        public void handle(AttachmentVersions rowItem, boolean keyPressed)
                        {
                            // don't need to check whether the value is null
                            // because there will not be a link for null value
                            final String fileName = rowItem.getCurrent().getFileName();
                            final List<Attachment> versions = rowItem.getVersions();

                            showVersionsPanel(fileName, versions, keyPressed);
                        }
                    });
    }

    @Override
    protected ColumnDefsAndConfigs<AttachmentVersions> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<AttachmentVersions> schema = super.createColumnsDefinition();
        schema.setGridCellRendererFor(AttachmentColDefKind.PERMLINK.id(), LinkRenderer
                .createExternalLinkRenderer(viewContext.getMessage(Dict.PERMLINK)));
        return schema;
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

        String downloadTitle = viewContext.getMessage(Dict.BUTTON_DOWNLOAD);
        Button downloadButton =
                createSelectedItemButton(downloadTitle, asDownloadAttachmentInvoker());
        downloadButton.setId(createBrowserId(attachmentHolder) + DOWNLOAD_BUTTON_ID_SUFFIX);
        addButton(downloadButton);

        String showAllVersionsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_ALL_VERSIONS);
        Button showAllVersionsButton =
                createSelectedItemButton(showAllVersionsTitle, asShowEntityInvoker(false));
        addButton(showAllVersionsButton);

        if (viewContext.isSimpleMode() == false)
        {
            addButton(createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_EDIT),
                    new ISelectedEntityInvoker<BaseEntityModel<AttachmentVersions>>()
                        {

                            public void invoke(BaseEntityModel<AttachmentVersions> selectedItem,
                                    boolean keyPressed)
                            {
                                AttachmentVersions versions = selectedItem.getBaseObject();
                                createEditAttachmentDialog(versions).show();

                            }
                        }));
            addButton(createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                    new AbstractCreateDialogListener()
                        {
                            @Override
                            protected Dialog createDialog(
                                    List<AttachmentVersions> attachmentVersions,
                                    IBrowserGridActionInvoker invoker)
                            {
                                return new AttachmentListDeletionConfirmationDialog(viewContext,
                                        attachmentVersions, createDeletionCallback(invoker),
                                        attachmentHolder);
                            }
                        }));
        }
        allowMultipleSelection(); // we allow deletion of multiple attachments

        addEntityOperationsSeparator();
    }

    protected final ISelectedEntityInvoker<BaseEntityModel<AttachmentVersions>> asDownloadAttachmentInvoker()
    {
        return new ISelectedEntityInvoker<BaseEntityModel<AttachmentVersions>>()
            {
                public void invoke(BaseEntityModel<AttachmentVersions> selectedItem,
                        boolean keyPressed)
                {
                    AttachmentVersions versions = selectedItem.getBaseObject();
                    final String fileName = versions.getCurrent().getFileName();
                    final int version = versions.getCurrent().getVersion();
                    AttachmentDownloadHelper.download(fileName, version, attachmentHolder);
                }
            };
    }

    private Window createEditAttachmentDialog(final AttachmentVersions versions)
    {
        final Attachment current = versions.getCurrent();
        String title =
                viewContext.getMessage(Dict.EDIT_TITLE, "Attachment", "'" + current.getFileName()
                        + "'");
        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final TextField<String> titleField;
                {
                    titleField = createTextField(viewContext.getMessage(Dict.TITLE));
                    titleField.setValue(StringEscapeUtils.unescapeHtml(current.getTitle()));
                    addField(titleField);
                }

                private final DescriptionField descriptionField;
                {
                    descriptionField = createDescriptionField(viewContext);
                    descriptionField.setValueAndUnescape(StringEscapeUtils.unescapeHtml(current
                            .getDescription()));
                    addField(descriptionField);
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    current.setTitle(titleField.getValue());
                    current.setDescription(descriptionField.getValue());
                    viewContext.getService().updateAttachment(TechId.create(attachmentHolder),
                            attachmentHolder.getAttachmentHolderKind(), current,
                            registrationCallback);
                }
            };
    }

    @Override
    protected void showEntityViewer(AttachmentVersions entity, boolean editMode, boolean active)
    {
        assert editMode == false : "edit mode is not implemented";

        // detail view of AttachmentVersions is a view that shows all versions of an attachment
        final String fileName = entity.getCurrent().getFileName();
        final List<Attachment> versions = entity.getVersions();

        showVersionsPanel(fileName, versions, active);
    }

    private void showVersionsPanel(final String fileName, final List<Attachment> versions,
            boolean inBackground)
    {
        assert versions != null : "versions not set!";

        final VersionsPanelHelper helper =
                new VersionsPanelHelper(fileName, attachmentHolder, viewContext);
        final AbstractTabItemFactory tabFactory = new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    final String tabTitle = helper.createTabTitle();
                    final Component component = helper.createVersionsPanel(versions);
                    return DefaultTabItem.createUnaware(tabTitle, component, false);
                }

                @Override
                public String getId()
                {
                    return helper.createTabId();
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.ATTACHMENTS, HelpPageAction.VIEW);
                }
            };
        tabFactory.setInBackground(inBackground);
        DispatcherHelper.dispatchNaviEvent(tabFactory);
    }

    @Override
    protected IColumnDefinitionKind<AttachmentVersions>[] getStaticColumnsDefinition()
    {
        return AttachmentColDefKind.values();
    }

    @Override
    protected BaseEntityModel<AttachmentVersions> createModel(
            GridRowModel<AttachmentVersions> entity)
    {
        BaseEntityModel<AttachmentVersions> model = super.createModel(entity);
        model.renderAsLinkWithAnchor(AttachmentColDefKind.FILE_NAME.id());
        model.renderAsMultilineStringWithTooltip(AttachmentColDefKind.DESCRIPTION.id());
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
            panel.setLayout(new FitLayout());
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
            attachmentGrid.addListener(Events.CellClick,
                    new Listener<GridEvent<AttachmentVersionModel>>()
                        {
                            public void handleEvent(final GridEvent<AttachmentVersionModel> be)
                            {
                                if (ColumnListener.isLinkTarget(be))
                                {
                                    String column =
                                            attachmentGrid.getColumnModel().getColumn(
                                                    be.getColIndex()).getId();
                                    if (AttachmentVersionModel.VERSION_FILE_NAME.equals(column))
                                    {
                                        final AttachmentVersionModel selectedItem =
                                                be.getGrid().getStore().getAt(be.getRowIndex());
                                        Attachment selectedAttachment =
                                                (Attachment) selectedItem
                                                        .get(ModelDataPropertyNames.OBJECT);
                                        int version = selectedAttachment.getVersion();
                                        AttachmentDownloadHelper.download(fileName, version,
                                                attachmentHolder);
                                    }
                                    attachmentGrid.getSelectionModel().deselectAll();
                                }
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
                            AttachmentVersionModel.VERSION_FILE_NAME);
            column.setWidth(200);
            column.setRenderer(new GridCellRenderer<AttachmentVersionModel>()
                {

                    public Object render(AttachmentVersionModel model, String property,
                            ColumnData config, int rowIndex, int colIndex,
                            ListStore<AttachmentVersionModel> store,
                            Grid<AttachmentVersionModel> grid)
                    {
                        Object value = model.get(property);
                        if (value == null)
                        {
                            return "";
                        }
                        return LinkRenderer.renderAsLinkWithAnchor((String) value);
                    }
                });
            return column;

        }

    }

}
