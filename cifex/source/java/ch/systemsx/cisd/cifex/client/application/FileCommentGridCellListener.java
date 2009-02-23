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

package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.util.Format;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;

import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.shared.basic.Constants;

/**
 * A <code>GridCellListenerAdapter</code> extension which opens a {@link DefaultLayoutDialog} for
 * showing the comment.
 * <p>
 * Note that the comment should be HTML encoded to avoid a potential security issue.
 * </p>
 * 
 * @author Basil Neff
 */
public class FileCommentGridCellListener extends GridCellListenerAdapter
{
    private ViewContext viewContext;

    public FileCommentGridCellListener(ViewContext viewContext)
    {
        this.viewContext = viewContext;
    }

    //
    // GridCellListenerAdapter
    //

    public final void onCellClick(final Grid grid, final int rowIndex, final int colindex,
            final EventObject e)
    {
        final String dataIndex = grid.getColumnModel().getDataIndex(colindex);
        if (dataIndex.equals(AbstractFileGridModel.COMMENT))
        {
            final IMessageResources messageResources = viewContext.getMessageResources();
            final Element element = e.getTarget();
            if (element == null)
            {
                return;
            }
            final String targetId = DOM.getElementAttribute(e.getTarget(), "id");
            // Show Comment
            if (Constants.SHOW_COMMENT_ID.equals(targetId))
            {
                final String comment =
                        DOM.getElementAttribute(e.getTarget(), "title").replaceAll("\n", "<br/>");
                final DefaultLayoutDialog layoutDialog =
                        new DefaultLayoutDialog(viewContext.getMessageResources(), messageResources
                                .getFileCommentTitle(), DefaultLayoutDialog.DEFAULT_WIDTH,
                                DefaultLayoutDialog.DEFAULT_HEIGHT, true, true);
                layoutDialog.addContentPanel();
                layoutDialog.show();
                layoutDialog.getContentPanel().setContent(Format.htmlEncode(comment));
            }
        }
    }

}
