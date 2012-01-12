/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.ethz.cisd.hcscld;

/**
 * The kind of namespace of a feature group.
 * 
 * @author Bernd Rinn
 */
public enum FeatureNamespaceKind
{
    /**
     * Name space is an object type.
     */
    OBJECT_TYPE,
    /**
     * Name space is a companion group.
     */
    COMPANION_GROUP
}