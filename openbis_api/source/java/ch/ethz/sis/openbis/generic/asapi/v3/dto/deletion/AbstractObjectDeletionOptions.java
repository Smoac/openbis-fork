/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion;

import java.io.Serializable;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.deletion.AbstractObjectDeletionOptions")
public class AbstractObjectDeletionOptions<T extends AbstractObjectDeletionOptions<T>> implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String reason;

    public String getReason()
    {
        return reason;
    }

    @SuppressWarnings("unchecked")
    public T setReason(String reason)
    {
        this.reason = reason;
        return (T) this;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("reason", reason).toString();
    }

}
