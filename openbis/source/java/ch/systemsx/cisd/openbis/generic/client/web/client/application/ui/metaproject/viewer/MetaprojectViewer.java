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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.viewer;

import java.util.HashSet;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.dialog.MetaprojectDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;

/**
 * @author pkupczyk
 */
public final class MetaprojectViewer extends AbstractViewer<IEntityInformationHolder> implements
        IDatabaseModificationObserver
{
    private static final String PREFIX = "metaproject-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final Long metaprojectId;

    private Metaproject originalMetaproject;

    public static DatabaseModificationAwareComponent create(final IViewContext<?> viewContext,
            final Long metaprojectId)
    {
        MetaprojectViewer viewer = new MetaprojectViewer(viewContext, metaprojectId);
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private MetaprojectViewer(final IViewContext<?> viewContext, final Long metaprojectId)
    {
        super(viewContext, createId(metaprojectId));
        this.metaprojectId = metaprojectId;
        setLayout(new BorderLayout());
        extendToolBar();
        reloadAllData();
    }

    public static String createId(final Long metaprojectId)
    {
        return ID_PREFIX + metaprojectId;
    }

    private void extendToolBar()
    {
        if (viewContext.isSimpleOrEmbeddedMode())
        {
            return;
        }
        addToolBarButton(createDeleteButton(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    new MetaprojectDeletionConfirmationDialog(viewContext,
                            originalMetaproject.getId(), createPermanentDeletionCallback()).show();
                }
            }));
    }

    @Override
    protected void reloadAllData()
    {
        reloadData(new AbstractAsyncCallback<Metaproject>(viewContext)
            {
                @Override
                protected void process(Metaproject result)
                {
                    recreateView(result);
                }

                @Override
                public void finishOnFailure(Throwable caught)
                {
                    setupRemovedEntityView();
                }
            });
    }

    protected void reloadData(AbstractAsyncCallback<Metaproject> callback)
    {
        viewContext.getCommonService().getMetaproject(metaprojectId, callback);
    }

    private void recreateView(final Metaproject metaproject)
    {
        updateOriginalMetaproject(metaproject);
        removeAll();

        // TODO layout
        add(new Label(metaproject.getIdentifier()));

        layout();
    }

    private void updateOriginalMetaproject(Metaproject metaproject)
    {
        this.originalMetaproject = metaproject;
        updateBreadcrumbs();
        setToolBarButtonsEnabled(true);
    }

    @Override
    public void setupRemovedEntityView()
    {
        removeAll();
        updateTitle(getOriginalDataDescription() + " does not exist any more.");
        setToolBarButtonsEnabled(false);
    }

    @Override
    protected String getOriginalDataDescription()
    {
        return viewContext.getMessage(Dict.METAPROJECT) + " " + originalMetaproject.getName();
    }

    @Override
    protected void showEntityEditor(boolean inBackground)
    {
        // TODO
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        reloadAllData();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        Set<DatabaseModificationKind> result = new HashSet<DatabaseModificationKind>();
        DatabaseModificationKind.addAny(result, ObjectKind.METAPROJECT);
        DatabaseModificationKind.addAny(result, ObjectKind.EXPERIMENT);
        DatabaseModificationKind.addAny(result, ObjectKind.SAMPLE);
        DatabaseModificationKind.addAny(result, ObjectKind.DATA_SET);
        DatabaseModificationKind.addAny(result, ObjectKind.MATERIAL);
        return result.toArray(DatabaseModificationKind.EMPTY_ARRAY);
    }

    @Override
    protected String getDeleteButtonLabel()
    {
        return viewContext.getMessage(Dict.BUTTON_DELETE_METAPROJECT);
    }
}
