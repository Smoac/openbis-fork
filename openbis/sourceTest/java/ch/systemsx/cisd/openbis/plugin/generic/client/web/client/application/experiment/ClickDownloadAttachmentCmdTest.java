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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.attachment.AttachmentBrowser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.AttachmentColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util.GridTestUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentVersions;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * Simulates clicking on the specified attachment in the experiment info window.
 * 
 * @author Tomasz Pylak
 */
public class ClickDownloadAttachmentCmdTest extends AbstractDefaultTestCommand
{
    private final String fileName;

    private final TechId experimentId;

    public ClickDownloadAttachmentCmdTest(final String fileName, final TechId experimentId)
    {
        this.fileName = fileName;
        this.experimentId = experimentId;
        addCallbackClass(AttachmentBrowser.createGridId(experimentId,
                AttachmentHolderKind.EXPERIMENT));
    }

    @SuppressWarnings("unchecked")
    public void execute()
    {
        String attachmentGridId =
                AttachmentBrowser.createGridId(experimentId, AttachmentHolderKind.EXPERIMENT);
        final Widget widget = GWTTestUtil.getWidgetWithID(attachmentGridId);
        final Grid<BaseEntityModel<AttachmentVersions>> table =
                (Grid<BaseEntityModel<AttachmentVersions>>) widget;
        GridTestUtils.fireSingleClick(table, AttachmentColDefKind.FILE_NAME.id(), LinkRenderer
                .renderAsLinkWithAnchor(fileName));
    }
}