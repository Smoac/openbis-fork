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

define(["stjs"], function (stjs) {
    var ImportResult = function(objectIds) {
      this.objectIds = objectIds;
    }

    stjs.extend(
      ImportResult,
      null,
      [],
      function (constructor, prototype) {
        prototype["@type"] = "as.dto.importer.ImportResult";

        constructor.serialVersionUID = 1;
        prototype.objectIds = null;

        prototype.getObjectIds = function() {
          return this.objectIds;
        };

        prototype.setObjectIds = function(objectIds) {
          this.objectIds = objectIds;
        };
      },
      {
        objectIds: {
          name: "List",
          arguments: ["IObjectId"]
        }
      }
    );

    return ImportResult;
  });