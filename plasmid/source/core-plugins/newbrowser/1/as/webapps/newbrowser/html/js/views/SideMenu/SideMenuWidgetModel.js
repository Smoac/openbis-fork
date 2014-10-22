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

/**
 * Creates an instance of SideMenuWidget.
 *
 * @constructor
 * @this {SideMenuWidgetModel}
 */
function SideMenuWidgetModel() {
    this.menuDOMTitle = null;
    this.menuDOMBody = null;
    this.menuStructure = new SideMenuWidgetComponent(false, true, "Main Menu", "Main Menu", null, {children: []}, 'showBlancPage', null, "");
    this.pointerToMenuNode = this.menuStructure;
    this.isHidden = false;
    this.$container = null;
}