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

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.AttachmentColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IAttachmentHolder;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * @author Piotr Buczek
 */
public class AttachmentBrowser extends AbstractSimpleBrowserGrid<Attachment>
{

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "attachment-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

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
        super(viewContext, BROWSER_ID, GRID_ID);
        this.attachmentHolder = attachmentHolder;
        setDisplayTypeIDGenerator(DisplayTypeIDGenerator.ATTACHMENT_BROWSER_GRID);
        extendBottomToolbar();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        addButton(createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                new AbstractCreateDialogListener()
                    {
                        @Override
                        protected Dialog createDialog(List<Attachment> attachments,
                                IBrowserGridActionInvoker invoker)
                        {
                            return new AttachmentDeletionConfirmationDialog(attachments, invoker);
                        }
                    }));
        allowMultipleSelection(); // we allow deletion of multiple attachments

        addEntityOperationsSeparator();
    }

    @Override
    protected IColumnDefinitionKind<Attachment>[] getStaticColumnsDefinition()
    {
        return AttachmentColDefKind.values();
    }

    @Override
    protected ColumnDefsAndConfigs<Attachment> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<Attachment> schema = super.createColumnsDefinition();
        schema.setGridCellRendererFor(AttachmentColDefKind.FILE_NAME.id(), LinkRenderer
                .createGridCellRenderer());
        schema.setGridCellRendererFor(AttachmentColDefKind.VERSIONS.id(),
                createVersionsLinkCellRenderer());
        return schema;
    }

    private GridCellRenderer<BaseEntityModel<?>> createVersionsLinkCellRenderer()
    {
        return new GridCellRenderer<BaseEntityModel<?>>()
            {

                public String render(BaseEntityModel<?> model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store)
                {
                    String originalValue = String.valueOf(model.get(property));
                    String text = viewContext.getMessage(Dict.VERSIONS_TEMPLATE, originalValue);
                    return LinkRenderer.renderAsLinkWithAnchor(text, originalValue, true);
                }

            };
    }

    @Override
    protected List<IColumnDefinition<Attachment>> getInitialFilters()
    {
        return Collections.emptyList();
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Attachment> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Attachment>> callback)
    {
        // TODO 2009-06-12, Piotr Buczek: implement listHolderAttachments
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Attachment> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportAttachments(exportCriteria, callback);
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
        public AttachmentDeletionConfirmationDialog(List<Attachment> attachments,
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

        private List<String> getAttachmentFileNames(List<Attachment> attachments)
        {
            List<String> fileNames = new ArrayList<String>();
            for (Attachment attachment : attachments)
            {
                fileNames.add(attachment.getFileName());
            }
            return fileNames;
        }

    }

}
