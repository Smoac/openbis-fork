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

define(["stjs", "as/dto/common/operation/IOperationResult"],
  function (stjs, IOperationResult) {
    var ImportOperationResult = function(importResult) {
      this.importResult = importResult;
    }

    stjs.extend(
      ImportOperationResult,
      IOperationResult,
      [IOperationResult],
      function (constructor, prototype) {
        prototype["@type"] = "as.dto.importer.ImportOperationResult";

        constructor.serialVersionUID = 1;
        prototype.importResult = null;

        prototype.getMessage = function() {
          return "ImportOperationResult";
        };

        prototype.getImportResult = function() {
          return this.importResult;
        };

        prototype.setImportResult = function(importResult) {
          this.importResult = importResult;
        };
      },
      {
        importResult: "ImportResult"
      }
    );

    return ImportOperationResult;
  });