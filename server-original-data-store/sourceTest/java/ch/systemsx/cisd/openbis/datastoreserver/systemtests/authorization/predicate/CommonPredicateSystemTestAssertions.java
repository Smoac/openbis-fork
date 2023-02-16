/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public abstract class CommonPredicateSystemTestAssertions<O>
{

    public abstract void assertWithNullObject(ProjectAuthorizationUser user, Throwable t, Object param);

    public abstract void assertWithNullCollection(ProjectAuthorizationUser user, Throwable t, Object param);

    public abstract void assertWithNonexistentObject(ProjectAuthorizationUser user, Throwable t, Object param);

    public abstract void assertWithProject11Object(ProjectAuthorizationUser user, Throwable t, Object param);

    public abstract void assertWithProject21Object(ProjectAuthorizationUser user, Throwable t, Object param);

    public abstract void assertWithProject11ObjectAndProject21Object(ProjectAuthorizationUser user, Throwable t,
            Object param);

}