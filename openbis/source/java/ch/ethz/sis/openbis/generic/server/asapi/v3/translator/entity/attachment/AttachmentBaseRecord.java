/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.attachment;

import java.util.Date;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.common.ObjectBaseRecord;

/**
 * @author Franz-Josef Elmer
 */
public class AttachmentBaseRecord extends ObjectBaseRecord
{
    public String title;

    public String fileName;

    public String description;

    public int version;

    public Date registrationDate;

    public String projectCode;

    public String spaceCode;

    public String samplePermId;

    public String experimentPermId;

}
