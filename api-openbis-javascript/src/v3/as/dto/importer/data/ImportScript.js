/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
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
    var ImportScript = function(name, source) {
      this.name = name;
      this.source = source;
    }

    stjs.extend(
      ImportScript,
      null,
      [],
      function (constructor, prototype) {
        prototype["@type"] = "as.dto.importer.data.ImportScript";

        constructor.serialVersionUID = 1;
        prototype.name = null;
        prototype.source = null;

        prototype.getName = function() {
          return this.name;
        };

        prototype.getSource = function() {
          return this.source;
        };
      },
      {}
    );

    return ImportScript;
  });