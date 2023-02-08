/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v1;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IProjectImmutable;

/**
 * Interface to specify an experiment to be modified or created.
 *
 * @author Kaloyan Enimanev
 */
public interface IProject extends IProjectImmutable
{
    /**
     * Set the project description.
     */
    void setDescription(String description);

    /**
     * Add a new attachment to this sample.
     * 
     * @param filePath The path of the attachment as reported to the database.
     * @param title The title of the attachment.
     * @param description A description of the attachment.
     * @param content The actual content of the attachment.
     */
    void addAttachment(String filePath, String title, String description,
            byte[] content);

}
