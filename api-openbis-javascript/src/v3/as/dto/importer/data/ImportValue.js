/*
 *  Copyright ETH 2024 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

define(["stjs"],
  function (stjs) {
    var ImportValue = function() {
    }

    stjs.extend(
      ImportValue,
      null,
      [],
      function (constructor, prototype) {
        prototype["@type"] = "as.dto.importer.data.ImportValue";

        constructor.serialVersionUID = 1;
        prototype.name = null;
        prototype.value = null;

        prototype.getName = function() {
          return this.name;
        };

        prototype.setName = function(name) {
          this.name = name;
        };

        prototype.getValue = function() {
          return this.value;
        };

        prototype.setValue = function(value) {
          this.value = value;
        };
      },
      {}
    );

    return ImportValue;
  });