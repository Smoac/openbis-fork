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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project;

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ProjectUpdates;

/**
 * {@link AbstractProjectEditRegisterForm} extension for editing projects.
 * 
 * @author Izabela Adamczyk
 */
public class ProjectEditForm extends AbstractProjectEditRegisterForm
{
    private final Project project;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, Project project)
    {
        ProjectEditForm form = new ProjectEditForm(viewContext, project);
        return new DatabaseModificationAwareComponent(form, form.getGroupField());
    }

    protected ProjectEditForm(IViewContext<ICommonClientServiceAsync> viewContext, Project project)
    {
        super(viewContext, project.getId(), project.getGroup().getCode());
        this.project = project;
    }

    @Override
    protected void saveProject()
    {
        ProjectUpdates updates = new ProjectUpdates();
        updates.setAttachmentSessionKey(sessionKey);
        updates.setDescription(projectDescriptionField.getValue());
        updates.setProjectIdentifier(project.getIdentifier());
        updates.setVersion(project.getModificationDate());
        Group group = groupField.tryGetSelected();
        updates.setGroupCode(group == null ? null : group.getCode());
        viewContext.getCommonService().updateProject(updates, new ProjectEditCallback(viewContext));
    }

    public final class ProjectEditCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Date>
    {

        ProjectEditCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final Date result)
        {
            project.setModificationDate(result);
            updateOriginalValues();
            super.process(result);
        }

        @Override
        protected String createSuccessfullRegistrationInfo(Date result)
        {
            return "Project <b>" + project.getIdentifier().toUpperCase()
                    + "</b> successfully updated.";
        }
    }

    @Override
    protected void setValues()
    {
        projectDescriptionField.setValue(StringEscapeUtils.unescapeHtml(project.getDescription()));
        projectCodeField.setValue(project.getCode());
        projectCodeField.setEnabled(false);
    }

    public void updateOriginalValues()
    {
        projectDescriptionField.setOriginalValue(projectDescriptionField.getValue());
        projectCodeField.setOriginalValue(projectCodeField.getValue());
        groupField.setOriginalValue(groupField.getValue());
    }

}
