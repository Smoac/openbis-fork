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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Attachment;

/**
 * {@link ModelData} for {@link Attachment}.
 * 
 * @author Izabela Adamczyk
 */
public class AttachmentModel extends BaseModelData
{
    private static final long serialVersionUID = 1L;

    public AttachmentModel()
    {
    }

    private AttachmentModel(final Attachment attachament, final List<Attachment> oldVersions)
    {
        set(ModelDataPropertyNames.FILE_NAME, attachament.getFileName());
        set(ModelDataPropertyNames.VERSION, attachament.getVersion());
        set(ModelDataPropertyNames.REGISTRATOR, PersonRenderer.createPersonAnchor(attachament
                .getRegistrator()));
        set(ModelDataPropertyNames.REGISTRATION_DATE, attachament.getRegistrationDate());
        set(ModelDataPropertyNames.OBJECT, attachament);
        set(ModelDataPropertyNames.OLD_VERSIONS, oldVersions);
    }

    public final static List<AttachmentModel> convert(final List<Attachment> unprocessedAttachments)
    {
        Map<String, List<Attachment>> map = new HashMap<String, List<Attachment>>();
        for (Attachment a : unprocessedAttachments)
        {
            if (false == map.containsKey(a.getFileName()))
            {
                map.put(a.getFileName(), new ArrayList<Attachment>());
            }
            map.get(a.getFileName()).add(a);
        }
        final ArrayList<AttachmentModel> result = new ArrayList<AttachmentModel>();
        for (String fileName : map.keySet())
        {
            List<Attachment> attachments = map.get(fileName);
            Attachment current = Collections.max(attachments);
            attachments.remove(current);
            result.add(new AttachmentModel(current, attachments));
        }
        return result;
    }
}
