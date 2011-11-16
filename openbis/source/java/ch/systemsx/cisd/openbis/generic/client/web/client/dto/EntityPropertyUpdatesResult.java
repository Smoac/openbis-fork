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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Stores result of update of properties of an entity. Currently used only to transfer error
 * messages to the client side.
 * 
 * @author Piotr Buczek
 */
public class EntityPropertyUpdatesResult implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String errorMessage;

    public EntityPropertyUpdatesResult()
    {
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public String tryGetErrorMessage()
    {
        return errorMessage;
    }

}
