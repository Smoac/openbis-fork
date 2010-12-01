/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Marker interface indicating that a type is both {@link Serializable} and {@link IsSerializable}
 * (can be used as 'basic' DTOs that can be transferred between GWT client and server).
 * <p>
 * NOTE: All 'basic' DTO's should implement interface this because of java.io serialization used by
 * ReflectiveStringEscaper and DisplaySettings.
 * 
 * @author Piotr Buczek
 */
public interface ISerializable extends IsSerializable, Serializable
{

}
