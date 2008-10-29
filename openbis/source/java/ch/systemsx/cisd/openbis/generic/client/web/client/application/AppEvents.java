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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;


/**
 * Additional event codes.
 * 
 * @author Izabela Adamczyk
 */
public class AppEvents
{
    public final static int CALLBACK_FINNISHED = 0x100000;

    public final static int Init = 0x100001;

    public final static int UserNotLoggedIn = 0x100002;

    public final static int Error = 0x100004;

    protected static final int MenuEvent = 0x100004;
}
