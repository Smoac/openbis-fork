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

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.cifex.client.application.model.AbstractFileGridModel;
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
public class FileCommentGridCellListener implements Listener<GridEvent<AbstractFileGridModel>>
{
    public void handleEvent(GridEvent<AbstractFileGridModel> be)
    {
        Grid<AbstractFileGridModel> grid = be.getGrid();
        int colindex = be.getColIndex();
        final String dataIndex = grid.getColumnModel().getDataIndex(colindex);
        if (dataIndex.equals(AbstractFileGridModel.COMMENT))
        {
            final Element element = be.getTarget();
            if (element == null)
            {
                return;
            }
            final String targetId = DOM.getElementAttribute(be.getTarget(), "id");
            // Show Comment
            if (Constants.SHOW_COMMENT_ID.equals(targetId))
            {
                final String comment =
                        DOM.getElementAttribute(be.getTarget(), "title").replaceAll("\n", "<br/>");
                final DefaultLayoutDialog layoutDialog =
                        new DefaultLayoutDialog(msg(LIST_FILES_COMMENT_MSGBOX_TITLE),
                                DefaultLayoutDialog.DEFAULT_WIDTH,
                                DefaultLayoutDialog.DEFAULT_HEIGHT, true, true);
                layoutDialog.add(new Html(Format.htmlEncode(comment.replaceAll("<br/>", "\n"))
                        .replaceAll("\n", "<br/>")));
                layoutDialog.show();
            }
        }

    }

}
