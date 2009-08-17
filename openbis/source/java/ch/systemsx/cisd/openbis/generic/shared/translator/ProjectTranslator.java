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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link Person} &lt;---&gt; {@link PersonPE} translator.
 * 
 * @author Tomasz Pylak
 */
public final class ProjectTranslator
{

    private ProjectTranslator()
    {
        // Can not be instantiated.
    }

    public final static List<Project> translate(final List<ProjectPE> projects)
    {
        final List<Project> result = new ArrayList<Project>();
        for (final ProjectPE project : projects)
        {
            result.add(ProjectTranslator.translate(project));
        }
        return result;
    }

    public final static Project translate(final ProjectPE project)
    {
        if (project == null)
        {
            return null;
        }
        final Project result = new Project();
        result.setId(HibernateUtils.getId(project));
        result.setModificationDate(project.getModificationDate());
        result.setCode(StringEscapeUtils.escapeHtml(project.getCode()));
        result.setDescription(StringEscapeUtils.escapeHtml(project.getDescription()));
        result.setGroup(GroupTranslator.translate(project.getGroup()));
        result.setProjectLeader(PersonTranslator.translate(project.getProjectLeader()));
        result.setRegistrator(PersonTranslator.translate(project.getRegistrator()));
        result.setRegistrationDate(project.getRegistrationDate());
        result.setIdentifier(StringEscapeUtils.escapeHtml(IdentifierHelper.createProjectIdentifier(
                project).toString()));
        List<Attachment> attachments;
        if (project.attachmentsInitialized() == false)
        {
            attachments = DtoConverters.createUnmodifiableEmptyList();
        } else
        {
            attachments = AttachmentTranslator.translate(project.getAttachments());
        }
        result.setAttachments(attachments);
        return result;
    }

}
