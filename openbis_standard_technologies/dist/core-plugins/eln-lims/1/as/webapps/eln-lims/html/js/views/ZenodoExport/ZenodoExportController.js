/*
 * Copyright 2011 ETH Zuerich, CISD
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

function ZenodoExportController(parentController) {
    var exportModel = new ZenodoExportModel();
    var exportView = new ZenodoExportView(this, exportModel);

    this.init = function(views) {
        exportView.repaint(views);
    };

    this.exportSelected = function() {
        var _this = this;
        var selectedNodes = $(exportModel.tree).fancytree('getTree').getSelectedNodes();

        var toExport = [];
        for (var eIdx = 0; eIdx < selectedNodes.length; eIdx++) {
            var node = selectedNodes[eIdx];
            toExport.push({type: node.data.entityType, permId: node.key, expand: !node.expanded});
        }

        if (toExport.length === 0) {
            Util.showInfo('First select something to export.');
        } else {
            Util.blockUI();
            this.getUserInformation(function(userInformation) {
                mainController.serverFacade.exportZenodo(toExport, true, false, userInformation,
                        function(operationExecutionPermId) {
                            _this.waitForOpExecutionResponse(operationExecutionPermId, function(error, result) {
                                Util.unblockUI();
                                if (result && result.data && result.data.url) {
                                    var win = window.open(result.data.url, '_blank');
                                    win.focus();
                                    mainController.refreshView();
                                } else {
                                    if (error) {
                                        Util.showError(error);
                                    } else {
                                        Util.showError('Returned result format is not correct.');
                                    }
                                }
                            });
                        });
            });
        }
    };

    this.waitForOpExecutionResponse = function(operationExecutionPermIdString, callbackFunction) {
        var _this = this;
        require(['as/dto/operation/id/OperationExecutionPermId',
                'as/dto/operation/fetchoptions/OperationExecutionFetchOptions'],
            function(OperationExecutionPermId, OperationExecutionFetchOptions) {
                var operationExecutionPermId = new OperationExecutionPermId(operationExecutionPermIdString);
                var fetchOptions = new OperationExecutionFetchOptions();
                var fetchOptionsDetails = fetchOptions.withDetails();
                fetchOptionsDetails.withResults();
                fetchOptionsDetails.withError();
                mainController.openbisV3.getOperationExecutions([operationExecutionPermId], fetchOptions).done(function(results) {
                    var result = results[operationExecutionPermIdString];
                    var v2Result = null;
                    if (result && result.details && result.details.results) {
                        v2Result = result.details.results[0];
                    }

                    if (result && result.state === 'FINISHED') {
                        mainController.serverFacade.customELNApiCallbackHandler(v2Result, callbackFunction);
                    } else if (!result || result.state === 'FAILED') {
                        mainController.serverFacade.customELNApiCallbackHandler(v2Result, callbackFunction);
                    } else {
                        setTimeout(function() {
                            _this.waitForOpExecutionResponse(operationExecutionPermIdString, callbackFunction);
                        }, 3000);
                    }
                });
            });
    };

    this.getUserInformation = function(callback) {
        var userId = mainController.serverFacade.getUserId();
        mainController.serverFacade.getSessionInformation(function(sessionInfo) {
            var userInformation = {
                firstName: sessionInfo.person.firstName,
                lastName: sessionInfo.person.lastName,
                email: sessionInfo.person.email,
                id: userId,
            };
            callback(userInformation);
        });
    };
}