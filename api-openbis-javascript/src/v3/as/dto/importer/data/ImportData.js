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
    var ImportData = function(format, sessionWorkspaceFiles) {
      this.format = format;
      this.sessionWorkspaceFiles = sessionWorkspaceFiles;
    }

    stjs.extend(
      ImportData,
      null,
      [],
      function (constructor, prototype) {
        prototype["@type"] = "as.dto.importer.data.ImportData";

        constructor.serialVersionUID = 1;
        prototype.format = null;
        prototype.sessionWorkspaceFiles = null;

        prototype.getFormat = function() {
          return this.format;
        };

        prototype.setFormat = function(format) {
          this.format = format;
        };

        prototype.getSessionWorkspaceFiles = function() {
          return this.sessionWorkspaceFiles;
        };

        prototype.setSessionWorkspaceFiles = function(sessionWorkspaceFiles) {
          this.sessionWorkspaceFiles = sessionWorkspaceFiles;
        };
      },
      {
        format: "ImportFormat",
        sessionWorkspaceFiles: "String[]"
      }
    );

    return ImportData;
  });