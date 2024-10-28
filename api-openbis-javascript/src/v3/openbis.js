define([ 'jquery', 'util/Json', 'as/dto/datastore/search/DataStoreSearchCriteria', 'as/dto/datastore/fetchoptions/DataStoreFetchOptions',
	'as/dto/common/search/SearchResult', 'afs'], function(jquery,
		stjsUtil, DataStoreSearchCriteria, DataStoreFetchOptions, SearchResult, AfsServer) {
	jquery.noConflict();

	var __private = function() {

		this.ajaxRequest = function(settings) {
			var thisPrivate = this;

			settings.type = "POST";
			settings.processData = false;
			settings.dataType = "json";
			settings.contentType = "application/json";

			var returnType = settings.returnType;
			if (returnType) {
				delete settings.returnType;
			}

			var data = settings.data;
			data["id"] = "1";
			data["jsonrpc"] = "2.0";

			// decycle each parameter separately (jackson does not recognize
			// object ids across different parameters)

			if (data.params && data.params.length > 0) {
				var newParams = [];
				data.params.forEach(function(param) {
					var newParam = stjsUtil.decycle(param);
					newParams.push(newParam);
				});
				data.params = newParams;
			}

			settings.data = JSON.stringify(data);

			var originalSuccess = settings.success || function() {
			};
			var originalError = settings.error || function() {
			};

			var dfd = jquery.Deferred();
			function success(response) {
				if (response.error) {
					thisPrivate.log("Request failed - data: " + JSON.stringify(settings.data) + ", error: " + JSON.stringify(response.error));
					originalError(response.error);
					dfd.reject(response.error);
				} else {
					thisPrivate.log("Request succeeded - data: " + JSON.stringify(settings.data));
					stjsUtil.fromJson(returnType, response.result).done(function(dtos) {
						originalSuccess(dtos);
						dfd.resolve(dtos);
					}).fail(function() {
						originalError(arguments);
						dfd.reject(arguments);
					});
				}
			}

			function error(xhr, status, error) {
				thisPrivate.log("Request failed - data: " + JSON.stringify(settings.data) + ", error: " + JSON.stringify(error));
				originalError(error);
				dfd.reject(error);
			}

			jquery.ajax(settings).done(success).fail(error);

			return dfd.promise();
		};

		this.ajaxRequestTransactional = function(transactionParticipantId, settings) {
		    if (this.transactionId) {
		        var transactionalSettings = {
                    url : this.transactionCoordinatorUrl,
                    data : {
                        "method" : "executeOperation",
                        "params" : [ this.transactionId,
                                     this.sessionToken,
                                     this.interactiveSessionKey,
                                     transactionParticipantId,
                                     settings.data.method,
                                     settings.data.params ]
                    },
                    returnType : settings.returnType
		        }
                return this.ajaxRequest(transactionalSettings)
		    } else {
		        return this.ajaxRequest(settings)
		    }
		}

		this.loginCommon = function(user, isAnonymousUser, response) {
			var thisPrivate = this;
			var dfd = jquery.Deferred();

			response.done(function(sessionToken) {
				if (sessionToken && (isAnonymousUser || sessionToken.indexOf(user) > -1)) {
					thisPrivate.sessionToken = sessionToken;
					dfd.resolve(sessionToken);
				} else {
					dfd.reject();
				}
			}).fail(function() {
				dfd.reject();
			});
			return dfd.promise();
		};

		this.checkSessionTokenExists = function(){
		    if (!this.sessionToken)
            {
                throw new Error("Session token hasn't been set");
            }
		}

		this.checkInteractiveSessionKeyExists = function(){
            if (!this.interactiveSessionKey)
            {
                throw new Error("Interactive session token hasn't been set");
            }
		}

        this.checkTransactionDoesNotExist = function(){
            if (this.transactionId){
                throw new Error("Operation cannot be executed. Expected no active transactions, but found transaction '" + this.transactionId + "'.");
            }
        }

        this.checkTransactionExists = function(){
            if (!this.transactionId){
                throw new Error("Operation cannot be executed. No active transaction found.");
            }
        }

		this.log = function(msg) {
			if (console) {
				console.log(msg);
			}
		}
	}

	var dataStoreFacade = function(facade, dataStoreCodes) {

		this._getDataStores = function() {
			if (this._dataStores) {
				var dfd = jquery.Deferred();
				dfd.resolve(this._dataStores);
				return dfd.promise();
			} else {
				var thisFacade = this;
				var criteria = new DataStoreSearchCriteria();
				criteria.withKind().thatIn([]);
				criteria.withOrOperator();

				for (var i = 0; i < dataStoreCodes.length; i++) {
					criteria.withCode().thatEquals(dataStoreCodes[i]);
				}

				return facade.searchDataStores(criteria, new DataStoreFetchOptions()).then(function(results) {
					var dataStores = results.getObjects();
					var dfd = jquery.Deferred();

					if (dataStores && dataStores.length > 0) {
						thisFacade._dataStores = dataStores;
						dfd.resolve(dataStores);
					} else {
						if (dataStoreCodes.length > 0) {
							dfd.reject("No data stores found for codes: " + dataStoreCodes);
						} else {
							dfd.reject("No data stores found");
						}
					}

					return dfd.promise();
				});
			}
		}

		function createUrlWithParameters(dataStore, servlet, parameters) {
			return dataStore.downloadUrl + "/datastore_server/" + servlet + parameters;
		}

		function createUrl(dataStore) {
			return dataStore.downloadUrl + "/datastore_server/rmi-data-store-server-v3.json";
		}

		function checkTransactionsNotSupported(){
		    if(facade._private.transactionId){
		        throw Error("Transactions are not supported for data store methods.");
		    }
		}

		this.searchFiles = function(criteria, fetchOptions) {
		    checkTransactionsNotSupported()

			var thisFacade = this;
			return this._getDataStores().then(function(dataStores) {
				var promises = dataStores.map(function(dataStore) {
					return facade._private.ajaxRequest({
						url : createUrl(dataStore),
						data : {
							"method" : "searchFiles",
							"params" : [ facade._private.sessionToken, criteria, fetchOptions ]
						},
						returnType : "SearchResult"
					});
				});

				return jquery.when.apply(jquery, promises).then(function() {
					var objects = [];
					var totalCount = 0;

					for (var i = 0; i < arguments.length; i++) {
						var result = arguments[i];

						if (result.getObjects()) {
							Array.prototype.push.apply(objects, result.getObjects());
						}
						if (result.getTotalCount()) {
							totalCount += result.getTotalCount();
						}
					}

					var combinedResult = new SearchResult();
					combinedResult.setObjects(objects);
					combinedResult.setTotalCount(totalCount);
					return combinedResult;
				});
			});
		}

		this.createDataSets = function(creations) {
		    checkTransactionsNotSupported()

			var thisFacade = this;
			var creationsByStore = {};
			for (var i = 0; i < creations.length; i++) {
				var creation = creations[i];
				var dataStoreCode = creation.getMetadataCreation().getDataStoreId().toString();
				if (dataStoreCode in creationsByStore) {
					creationsByStore[dataStoreCode].append(creation);
				} else {
					creationsByStore[dataStoreCode] = [ creation ];
				}
			}
			return this._getDataStores().then(function(dataStores) {
				var promises = [];
				for (var i = 0; i < dataStores.length; i++) {
					var dataStore = dataStores[i];
					var dsCode = dataStore.getCode();
					if (dsCode in creationsByStore) {
						promises.push(facade._private.ajaxRequest({
							url : createUrl(dataStore),
							data : {
								"method" : "createDataSets",
								"params" : [ facade._private.sessionToken, creationsByStore[dsCode] ]
							},
							returnType : {
								name : "List",
								arguments : [ "DataSetPermId" ]
							}
						}));
					}
				}
				return jquery.when.apply(jquery, promises).then(function() {
					var dataSetIds = [];
					for (var i = 0; i < arguments.length; i++) {
						dataSetIds = jquery.merge(dataSetIds, arguments[i]);
					}
					return dataSetIds;
				});

			});
		}

		this.createDataSetUpload = function(dataSetType) {
		    checkTransactionsNotSupported()

			var pad = function(value, length) {
				var result = "" + value;
				while (result.length < length) {
					result = "0" + result;
				}
				return result;
			}

			return this._getDataStores().then(
					function(dataStores) {
						var dfd = jquery.Deferred();

						if (dataStores.length > 1) {
							dfd.reject("Please specify exactly one data store");
						} else {
							var dataStore = dataStores[0];
							var now = new Date();
							var id = "upload-" + now.getFullYear() + pad(now.getMonth() + 1, 2) + pad(now.getDate(), 2) + pad(now.getHours(), 2) + pad(now.getMinutes(), 2) + pad(now.getSeconds(), 2)
									+ "-" + pad(Math.round(Math.random() * 100000), 5);

							dfd.resolve({
								"getId" : function() {
									return id;
								},
								"getUrl" : function(folderPath, ignoreFilePath) {
									var params = {
										"sessionID" : facade._private.sessionToken,
										"uploadID" : id,
										"dataSetType" : dataSetType
									};

									if (folderPath != null) {
										params["folderPath"] = folderPath;
									}

									if (ignoreFilePath != null) {
										params["ignoreFilePath"] = ignoreFilePath;
									}

									return dataStore.downloadUrl + "/datastore_server/store_share_file_upload?" + jquery.param(params);
								},
								"getDataSetType" : function() {
									return dataSetType;
								}
							});
						}

						return dfd.promise();
					});
		}

		this.createUploadedDataSet = function(creation) {
		    checkTransactionsNotSupported()

			var dfd = jquery.Deferred();
			this._getDataStores().done(function(dataStores) {
				if (dataStores.length === 1) {
					facade._private.ajaxRequest({
						url: createUrl(dataStores[0]),
						data: {
							"method": "createUploadedDataSet",
							"params": [facade._private.sessionToken, creation]
						},
						returnType: {
							name: "DataSetPermId"
						}
					}).done(function (response) {
						dfd.resolve(response);
					}).fail(function (error) {
						dfd.reject(error);
					});
				} else {
					dfd.reject("Please specify exactly one data store");
				}
			});
			return dfd.promise();
		}

		this.executeCustomDSSService = function(serviceId, options) {
		    checkTransactionsNotSupported()

		    var dfd = jquery.Deferred();
            this._getDataStores().done(function(dataStores) {
                if (dataStores.length === 1) {
                    facade._private.ajaxRequest({
                        url: createUrl(dataStores[0]),
                        data: {
                            "method": "executeCustomDSSService",
                            "params": [facade._private.sessionToken, serviceId, options]
                        }
                    }).done(function (response) {
                        dfd.resolve(response);
                    }).fail(function (error) {
                        dfd.reject(error);
                    });
                } else {
                    dfd.reject("Please specify exactly one data store");
                }
            });
            return dfd.promise();
		}

		function getUUID() {
			return ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c =>
				(c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
			);
		}

	    this.uploadFilesWorkspaceDSS = function(files) {
	        checkTransactionsNotSupported()

			var thisFacade = this;
			var uploadId = getUUID();
			var dfd = jquery.Deferred();

			this._uploadFileWorkspaceDSSEmptyDir(uploadId).then(function() {
				thisFacade._uploadFilesWorkspaceDSS(files, uploadId).then(function(result) {
					dfd.resolve(result);
				}).catch(function(error) {
					dfd.reject(error);
				});
			}).catch(function(error) {
				dfd.reject(error);
			});

			return dfd;
    	}

		this._uploadFilesWorkspaceDSS = async function(files, parentId) {
			var createdDirectories = new Set();
			var filesCount = files.length;
			for (var i = 0; i < filesCount; i++) {
				var relativePath = files[i].webkitRelativePath;
				var directoryRelativePath = relativePath.substring(0, relativePath.lastIndexOf("/") + 1);
				if (directoryRelativePath && !createdDirectories.has(directoryRelativePath)) {
					await this._uploadFileWorkspaceDSSEmptyDir(parentId + "/" + directoryRelativePath);
					createdDirectories.add(directoryRelativePath);
				}
				await this._uploadFileWorkspaceDSSFile(files[i], parentId);
			}
			return parentId;
		}

		this._uploadFileWorkspaceDSSEmptyDir = function(pathToDir) {
			var thisFacade = this;
			var sessionID = facade._private.sessionToken;
			var filename = encodeURIComponent(pathToDir);
			return new Promise(function(resolve, reject) {
				thisFacade._getDataStores().done(function(dataStores) {
					if (dataStores.length === 1) {
						fetch(createUrlWithParameters(dataStores[0], "session_workspace_file_upload",
							"?sessionID=" + sessionID +
							"&filename=" + filename +
							"&id=1&startByte=0&endByte=0&size=0&emptyFolder=true"), {
							method: "POST",
							headers: {
								"Content-Type": "multipart/form-data"
							}
						}).then(function (response) {
							resolve(response);
						}).catch(function (error) {
							reject(error);
						});
					} else {
						reject("Please specify exactly one data store");
					}
				}).fail(function(error) {
					reject(error);
				});
			});
		}

		this._uploadFileWorkspaceDSSFile = function(file, parentId) {
			var thisFacade = this;
			return new Promise(function(resolve, reject) {
				thisFacade._getDataStores().done(function(dataStores) {
					uploadBlob(dataStores[0], parentId, facade._private.sessionToken, file, 0, 1048576)
						.then(function (value) {
							resolve(value);
						})
						.catch(function (reason) {
							reject(reason);
						});
				}).fail(function(error) {
					reject(error);
				});
			});
		}

		async function uploadBlob(dataStore, parentId, sessionID, file, startByte, chunkSize) {
			var fileSize = file.size;
			for (var byte = startByte; byte < fileSize; byte += chunkSize) {
				await fetch(createUrlWithParameters(dataStore, "session_workspace_file_upload",
					"?sessionID=" + sessionID +
					"&filename=" + encodeURIComponent(parentId + "/" +
						(file.webkitRelativePath ? file.webkitRelativePath : file.name)) +
					"&id=1&startByte=" + byte +
					"&endByte=" + (byte + chunkSize) +
					"&size=" + fileSize +
					"&emptyFolder=false"), {
					method: "POST",
					headers: {
						"Content-Type": "multipart/form-data"
					},
					body: makeChunk(file, byte, Math.min(byte + chunkSize, fileSize))
				});
			}
		}

		function makeChunk(file, startByte, endByte) {
			var blob = undefined;
			if (file.slice) {
				blob = file.slice(startByte, endByte);
			} else if (file.webkitSlice) {
				blob = file.webkitSlice(startByte, endByte);
			} else if (file.mozSlice) {
				blob = file.mozSlice(startByte, endByte);
			}
			return blob;
		}
	}

    var AfsServerFacade = function(asFacade) {

        if(!asFacade._private.afsUrl){
            throw Error("Please specify AFS server url");
        }

        var afsServer = new AfsServer(asFacade._private.afsUrl);
        var afsServerTransactionParticipantId = "afs-server"

		this.list = function(owner, source, recursively){
		    if(asFacade._private.transactionId){
                return asFacade._private.ajaxRequestTransactional(afsServerTransactionParticipantId, {
                    data : {
                        "method" : "list",
                        "params" : [ owner, source, recursively ]
                    }
                }).then(function(response){
                    if (response && Array.isArray(response)) {
                        return response.map(function(fileObject){
                            return new File(fileObject)
                        });
                    } else {
                        return response;
                    }
                })
		    }else{
		        afsServer.useSession(asFacade._private.sessionToken)
                return afsServer.list(owner, source, recursively);
		    }
		}

		this.read = function(owner, source, offset, limit){
		    if(asFacade._private.transactionId){
                return asFacade._private.ajaxRequestTransactional(afsServerTransactionParticipantId, {
                    data : {
                        "method" : "read",
                        "params" : [ owner, source, offset, limit ]
                    }
                }).then(function(response){
                    return new Blob([atob(response)])
                });
            }else{
                afsServer.useSession(asFacade._private.sessionToken)
                return afsServer.read(owner, source, offset, limit);
            }
		}

		this.write = function(owner, source, offset, data){
		    if(asFacade._private.transactionId){

                var dataString = ""
                if(data && data instanceof Uint8Array) {
                    dataString = unit8ArrayToString(data)
                }

                return asFacade._private.ajaxRequestTransactional(afsServerTransactionParticipantId, {
                    data : {
                        "method" : "write",
		                // use base64 url version of encoding that produces url safe characters only (default version of base64 produces "+" and "/" which need to be further converted by encodeURIComponent to "%2B" and "%2F" and therefore they unnecessarily increase the request size)

                        "params" : [ owner, source, offset, base64URLEncode(dataString), base64URLEncode(hex2a(md5(dataString))) ]
                    }
                })
            }else{
                afsServer.useSession(asFacade._private.sessionToken)
                return afsServer.write(owner, source, offset, data);
            }
		}

		this.delete = function(owner, source){
		    if(asFacade._private.transactionId){
                return asFacade._private.ajaxRequestTransactional(afsServerTransactionParticipantId, {
                    data : {
                        "method" : "delete",
                        "params" : [ owner, source ]
                    }
                })
            }else{
                afsServer.useSession(asFacade._private.sessionToken)
                return afsServer.delete(owner, source);
            }
		}

		this.copy = function(sourceOwner, source, targetOwner, target){
		    if(asFacade._private.transactionId){
                return asFacade._private.ajaxRequestTransactional(afsServerTransactionParticipantId, {
                    data : {
                        "method" : "copy",
                        "params" : [ sourceOwner, source, targetOwner, target ]
                    }
                })
            }else{
                afsServer.useSession(asFacade._private.sessionToken)
                return afsServer.copy(sourceOwner, source, targetOwner, target);
            }
        }

		this.move = function(sourceOwner, source, targetOwner, target){
		    if(asFacade._private.transactionId){
                return asFacade._private.ajaxRequestTransactional(afsServerTransactionParticipantId, {
                    data : {
                        "method" : "move",
                        "params" : [ sourceOwner, source, targetOwner, target ]
                    }
                })
            }else{
                afsServer.useSession(asFacade._private.sessionToken)
                return afsServer.move(sourceOwner, source, targetOwner, target);
            }
		}

		this.create = function(owner, source, directory){
		    if(asFacade._private.transactionId){
                return asFacade._private.ajaxRequestTransactional(afsServerTransactionParticipantId, {
                    data : {
                        "method" : "create",
                        "params" : [ owner, source, directory ]
                    }
                })
            }else{
                afsServer.useSession(asFacade._private.sessionToken)
                return afsServer.create(owner, source, directory);
            }
		}

		this.free = function(owner, source){
		    if(asFacade._private.transactionId){
                return asFacade._private.ajaxRequestTransactional(afsServerTransactionParticipantId, {
                    data : {
                        "method" : "free",
                        "params" : [ owner, source ]
                    }
                }).then(function(response){
                    if (response) {
                        return new FreeSpace(response)
                    } else {
                        return response;
                    }
                })
            }else{
                afsServer.useSession(asFacade._private.sessionToken)
                return afsServer.free(owner, source);
            }
		}

	}

	var facade = function(asUrl, afsUrl) {

        var openbisUrl = "/openbis/openbis/rmi-application-server-v3.json";
        var transactionCoordinatorUrl = "/openbis/openbis/rmi-transaction-coordinator.json";
        var transactionParticipantId = "application-server"

        if(asUrl){
            var asUrlParts = parseUri(asUrl)
            if (asUrlParts.protocol && asUrlParts.authority) {
                openbisUrl = asUrlParts.protocol + "://" + asUrlParts.authority + openbisUrl;
                transactionCoordinatorUrl = asUrlParts.protocol + "://" + asUrlParts.authority + transactionCoordinatorUrl;
            }
		}

		this._private = new __private();
		this._private.openbisUrl = openbisUrl
		this._private.transactionCoordinatorUrl = transactionCoordinatorUrl
		this._private.afsUrl = afsUrl

        this.setSessionToken = function(sessionToken) {
            var thisFacade = this;
            thisFacade._private.checkTransactionDoesNotExist();
            thisFacade._private.sessionToken = sessionToken;
        }

        this.getSessionToken = function() {
            var thisFacade = this;
            return thisFacade._private.sessionToken;
        }

		this.getAfsUrl = function() {
			return this._private.afsUrl;
		}

		this.login = function(user, password) {
			var thisFacade = this;
			thisFacade._private.checkTransactionDoesNotExist();
			return thisFacade._private.loginCommon(user, false, thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "login",
					"params" : [ user, password ]
				}
			}));
		}

		this.loginAs = function(user, password, asUserId) {
			var thisFacade = this;
			thisFacade._private.checkTransactionDoesNotExist();
			return thisFacade._private.loginCommon(asUserId, false, thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "loginAs",
					"params" : [ user, password, asUserId ]
				}
			}));
		}

		this.loginAsAnonymousUser = function() {
			var thisFacade = this;
			thisFacade._private.checkTransactionDoesNotExist();
			return thisFacade._private.loginCommon(null, true, thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "loginAsAnonymousUser",
					"params" : []
				}
			}));
		}

		this.loginFromContext = function() {
		    this._private.checkTransactionDoesNotExist();
			this._private.sessionToken = this.getWebAppContext().getSessionId();
		}

		this.logout = function() {
			var thisFacade = this;
			thisFacade._private.checkTransactionDoesNotExist();
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "logout",
					"params" : [ thisFacade._private.sessionToken ]
				}
			}).done(function() {
				thisFacade._private.sessionToken = null;
			});
		}

		this.setInteractiveSessionKey = function(interactiveSessionKey) {
		    this._private.interactiveSessionKey = interactiveSessionKey;
		}

		this.beginTransaction = function() {
		    var thisFacade = this;

            thisFacade._private.checkTransactionDoesNotExist();
            thisFacade._private.checkSessionTokenExists();
            thisFacade._private.checkInteractiveSessionKeyExists();

		    thisFacade._private.transactionId = crypto.randomUUID();

		    return thisFacade._private.ajaxRequest({
                url : transactionCoordinatorUrl,
                data : {
                    "method" : "beginTransaction",
                    "params" : [ thisFacade._private.transactionId, thisFacade._private.sessionToken, thisFacade._private.interactiveSessionKey ]
                }
            }).then(function(){
                return thisFacade._private.transactionId;
            })
		}

		this.commitTransaction = function(){
		    var thisFacade = this;

		    thisFacade._private.checkTransactionExists();
            thisFacade._private.checkSessionTokenExists();
            thisFacade._private.checkInteractiveSessionKeyExists();

		    return thisFacade._private.ajaxRequest({
                url : transactionCoordinatorUrl,
                data : {
                    "method" : "commitTransaction",
                    "params" : [ thisFacade._private.transactionId, thisFacade._private.sessionToken, thisFacade._private.interactiveSessionKey ]
                }
            }).then(function(){
                thisFacade._private.transactionId = null;
            });
		}

        this.rollbackTransaction = function(){
            var thisFacade = this;

		    thisFacade._private.checkTransactionExists();
            thisFacade._private.checkSessionTokenExists();
            thisFacade._private.checkInteractiveSessionKeyExists();

		    return thisFacade._private.ajaxRequest({
                url : transactionCoordinatorUrl,
                data : {
                    "method" : "rollbackTransaction",
                    "params" : [ thisFacade._private.transactionId, thisFacade._private.sessionToken, thisFacade._private.interactiveSessionKey ]
                }
            }).then(function(){
                thisFacade._private.transactionId = null;
            });
        }

		this.getSessionInformation = function() {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getSessionInformation",
					"params" : [ thisFacade._private.sessionToken ]
				},
				returnType : "SessionInformation"
			});
		}

		this.createSpaces = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createSpaces",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "SpacePermId" ]
				}
			});
		}

		this.createProjects = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createProjects",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "ProjectPermId" ]
				}
			});
		}

		this.createExperiments = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createExperiments",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "ExperimentPermId" ]
				}
			});
		}

		this.createExperimentTypes = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createExperimentTypes",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "EntityTypePermId" ]
				}
			});
		}

        /**
         * @deprecated Use "createExternalDataManagementSystems" method instead.
         */
		this.createExternalDms = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createExternalDataManagementSystems",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "ExternalDmsPermId" ]
				}
			});
		}

        this.createExternalDataManagementSystems = function(creations) {
            var thisFacade = this;
            return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
                url : openbisUrl,
                data : {
                    "method" : "createExternalDataManagementSystems",
                    "params" : [ thisFacade._private.sessionToken, creations ]
                },
                returnType : {
                    name : "List",
                    arguments : [ "ExternalDmsPermId" ]
                }
            });
        }

		this.createSamples = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createSamples",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "SamplePermId" ]
				}
			});
		}

		this.createSampleTypes = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createSampleTypes",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "EntityTypePermId" ]
				}
			});
		}

		this.createDataSetTypes = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createDataSetTypes",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "EntityTypePermId" ]
				}
			});
		}

		this.createDataSets = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createDataSets",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "DataSetPermId" ]
				}
			});
		}

		this.createMaterials = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createMaterials",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "MaterialPermId" ]
				}
			});
		}

		this.createMaterialTypes = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createMaterialTypes",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "EntityTypePermId" ]
				}
			});
		}

		this.createPropertyTypes = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createPropertyTypes",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "PropertyTypePermId" ]
				}
			});
		}

		this.createPlugins = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createPlugins",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "PluginPermId" ]
				}
			});
		}

		this.createVocabularies = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createVocabularies",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "VocabularyPermId" ]
				}
			});
		}

		this.createVocabularyTerms = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createVocabularyTerms",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "VocabularyTermPermId" ]
				}
			});
		}

		this.createTags = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createTags",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "TagPermId" ]
				}
			});
		}

		this.createAuthorizationGroups = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createAuthorizationGroups",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "AuthorizationGroupPermId" ]
				}
			});
		}

		this.createRoleAssignments = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createRoleAssignments",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "RoleAssignmentTechId" ]
				}
			});
		}

		this.createPersons = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createPersons",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "PersonPermId" ]
				}
			});
		}

		this.createSemanticAnnotations = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createSemanticAnnotations",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "SemanticAnnotationPermId" ]
				}
			});
		}

		this.createQueries = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createQueries",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "QueryTechId" ]
				}
			});
		}

		this.createPersonalAccessTokens = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createPersonalAccessTokens",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "PersonalAccessTokenPermId" ]
				}
			});
		}

		this.updateSpaces = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateSpaces",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateProjects = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateProjects",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateExperiments = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateExperiments",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateExperimentTypes = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateExperimentTypes",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateSamples = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateSamples",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateSampleTypes = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateSampleTypes",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateDataSets = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateDataSets",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateDataSetTypes = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateDataSetTypes",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateMaterials = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateMaterials",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateMaterialTypes = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateMaterialTypes",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateExternalDataManagementSystems = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateExternalDataManagementSystems",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updatePropertyTypes = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updatePropertyTypes",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updatePlugins = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updatePlugins",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateVocabularies = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateVocabularies",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateVocabularyTerms = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateVocabularyTerms",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateTags = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateTags",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateAuthorizationGroups = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateAuthorizationGroups",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updatePersons = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updatePersons",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateOperationExecutions = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateOperationExecutions",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateSemanticAnnotations = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateSemanticAnnotations",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateQueries = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updateQueries",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updatePersonalAccessTokens = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "updatePersonalAccessTokens",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.getRights = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getRights",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IObjectId", "Rights" ]
				}
			});
		}

		this.getSpaces = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getSpaces",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "ISpaceId", "Space" ]
				}
			});
		}

		this.getProjects = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getProjects",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IProjectId", "Project" ]
				}
			});
		}

		this.getExperiments = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getExperiments",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IExperimentId", "Experiment" ]
				}
			});
		}

		this.getExperimentTypes = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getExperimentTypes",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IEntityTypeId", "ExperimentType" ]
				}
			});
		}

		this.getSamples = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getSamples",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "ISampleId", "Sample" ]
				}
			});
		}

		this.getSampleTypes = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getSampleTypes",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IEntityTypeId", "SampleType" ]
				}
			});
		}

		this.getDataSets = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getDataSets",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IDataSetId", "DataSet" ]
				}
			});
		}

		this.getDataSetTypes = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getDataSetTypes",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IEntityTypeId", "DataSetType" ]
				}
			});
		}

		this.getMaterials = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getMaterials",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IMaterialId", "Material" ]
				}
			});
		}

		this.getMaterialTypes = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getMaterialTypes",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IEntityTypeId", "MaterialType" ]
				}
			});
		}

		this.getPropertyTypes = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getPropertyTypes",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IPropertyTypeId", "PropertyType" ]
				}
			});
		}

		this.getPlugins = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getPlugins",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IPluginId", "Plugin" ]
				}
			});
		}

		this.getVocabularies = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getVocabularies",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IVocabularyId", "Vocabulary" ]
				}
			});
		}

		this.getVocabularyTerms = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getVocabularyTerms",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IVocabularyTermId", "VocabularyTerm" ]
				}
			});
		}

		this.getTags = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getTags",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "ITagId", "Tag" ]
				}
			});
		}

		this.getAuthorizationGroups = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getAuthorizationGroups",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IAuthorizationGroupId", "AuthorizationGroup" ]
				}
			});
		}

		this.getRoleAssignments = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getRoleAssignments",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IRoleAssignmentId", "RoleAssignment" ]
				}
			});
		}

		this.getPersons = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getPersons",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IRoleAssignmentId", "RoleAssignment" ]
				}
			});
		}

		this.getSemanticAnnotations = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getSemanticAnnotations",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "ISemanticAnnotationId", "SemanticAnnotation" ]
				}
			});
		}

		this.getExternalDataManagementSystems = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getExternalDataManagementSystems",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IExternalDmsId", "ExternalDms" ]
				}
			});
		}

		this.getOperationExecutions = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getOperationExecutions",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IOperationExecutionId", "OperationExecution" ]
				}
			});
		}

		this.getQueries = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getQueries",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IQueryId", "Query" ]
				}
			});
		}

		this.getQueryDatabases = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getQueryDatabases",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IQueryDatabaseId", "QueryDatabase" ]
				}
			});
		}

		this.getPersonalAccessTokens = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getPersonalAccessTokens",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IPersonalAccessTokenId", "PersonalAccessToken" ]
				}
			});
		}

		this.searchSpaces = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchSpaces",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchProjects = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchProjects",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchExperiments = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchExperiments",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			})
		}

		this.searchExperimentTypes = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchExperimentTypes",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			})
		}

		this.searchSamples = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchSamples",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchSampleTypes = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchSampleTypes",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchDataSets = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchDataSets",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchDataSetTypes = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchDataSetTypes",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchMaterials = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchMaterials",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchMaterialTypes = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchMaterialTypes",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchExternalDataManagementSystems = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchExternalDataManagementSystems",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchPlugins = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchPlugins",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchVocabularies = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchVocabularies",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchVocabularyTerms = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchVocabularyTerms",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchTags = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchTags",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchAuthorizationGroups = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchAuthorizationGroups",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchRoleAssignments = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchRoleAssignments",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchPersons = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchPersons",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchCustomASServices = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchCustomASServices",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchSearchDomainServices = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchSearchDomainServices",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchAggregationServices = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchAggregationServices",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchReportingServices = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchReportingServices",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchProcessingServices = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchProcessingServices",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchObjectKindModifications = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchObjectKindModifications",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchGlobally = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchGlobally",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchOperationExecutions = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchOperationExecutions",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchDataStores = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchDataStores",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchPropertyTypes = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchPropertyTypes",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchPropertyAssignments = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchPropertyAssignments",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchSemanticAnnotations = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchSemanticAnnotations",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchQueries = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchQueries",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchQueryDatabases = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchQueryDatabases",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchPersonalAccessTokens = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchPersonalAccessTokens",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchSessionInformation = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchSessionInformation",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.deleteSpaces = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteSpaces",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteProjects = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteProjects",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteExperiments = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteExperiments",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				},
				returnType : "IDeletionId"
			});
		}

		this.deleteSamples = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteSamples",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				},
				returnType : "IDeletionId"
			});
		}

		this.deleteDataSets = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteDataSets",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				},
				returnType : "IDeletionId"
			});
		}

		this.deleteMaterials = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteMaterials",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteExternalDataManagementSystems = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteExternalDataManagementSystems",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deletePlugins = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deletePlugins",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deletePropertyTypes = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deletePropertyTypes",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteVocabularies = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteVocabularies",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteVocabularyTerms = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteVocabularyTerms",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteExperimentTypes = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteExperimentTypes",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteSampleTypes = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteSampleTypes",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteDataSetTypes = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteDataSetTypes",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteMaterialTypes = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteMaterialTypes",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteTags = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteTags",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteAuthorizationGroups = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteAuthorizationGroups",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteRoleAssignments = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteRoleAssignments",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteOperationExecutions = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteOperationExecutions",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteSemanticAnnotations = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteSemanticAnnotations",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteQueries = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deleteQueries",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deletePersons = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deletePersons",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deletePersonalAccessTokens = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "deletePersonalAccessTokens",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.searchDeletions = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchDeletions",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : {
					name : "List",
					arguments : [ "Deletion" ]
				}
			});
		}

		this.searchEvents = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "searchEvents",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : {
					name : "List",
					arguments : [ "Event" ]
				}
			});
		}

		this.revertDeletions = function(ids) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "revertDeletions",
					"params" : [ thisFacade._private.sessionToken, ids ]
				}
			});
		}

		this.confirmDeletions = function(ids) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "confirmDeletions",
					"params" : [ thisFacade._private.sessionToken, ids ]
				}
			});
		}

		this.executeCustomASService = function(serviceId, options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "executeCustomASService",
					"params" : [ thisFacade._private.sessionToken, serviceId, options ]
				}
			});
		}

		this.executeSearchDomainService = function(options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "executeSearchDomainService",
					"params" : [ thisFacade._private.sessionToken, options ]
				},
				returnType : "SearchResult"
			});
		}

		this.executeAggregationService = function(serviceId, options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "executeAggregationService",
					"params" : [ thisFacade._private.sessionToken, serviceId, options ]
				},
				returnType : "TableModel"
			});
		}

		this.executeReportingService = function(serviceId, options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "executeReportingService",
					"params" : [ thisFacade._private.sessionToken, serviceId, options ]
				},
				returnType : "TableModel"
			});
		}

		this.executeProcessingService = function(serviceId, options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "executeProcessingService",
					"params" : [ thisFacade._private.sessionToken, serviceId, options ]
				}
			});
		}

		this.executeQuery = function(queryId, options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "executeQuery",
					"params" : [ thisFacade._private.sessionToken, queryId, options ]
				}
			});
		}

		this.executeSql = function(sql, options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "executeSql",
					"params" : [ thisFacade._private.sessionToken, sql, options ]
				}
			});
		}

		this.evaluatePlugin = function(options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "evaluatePlugin",
					"params" : [ thisFacade._private.sessionToken, options ]
				}
			});
		}

		this.archiveDataSets = function(ids, options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "archiveDataSets",
					"params" : [ thisFacade._private.sessionToken, ids, options ]
				}
			});
		}

		this.unarchiveDataSets = function(ids, options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "unarchiveDataSets",
					"params" : [ thisFacade._private.sessionToken, ids, options ]
				}
			});
		}

		this.lockDataSets = function(ids, options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "lockDataSets",
					"params" : [ thisFacade._private.sessionToken, ids, options ]
				}
			});
		}

		this.unlockDataSets = function(ids, options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "unlockDataSets",
					"params" : [ thisFacade._private.sessionToken, ids, options ]
				}
			});
		}

		this.executeOperations = function(operations, options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "executeOperations",
					"params" : [ thisFacade._private.sessionToken, operations, options ]
				}
			});
		}

		this.getServerInformation = function() {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getServerInformation",
					"params" : [ thisFacade._private.sessionToken ]
				}
			});
		}

		this.getServerPublicInformation = function() {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "getServerPublicInformation",
					"params" : []
				}
			});
		}

		this.createPermIdStrings = function(count) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createPermIdStrings",
					"params" : [ thisFacade._private.sessionToken, count ]
				}
			});
		}

		this.createCodes = function(prefix, entityKind, count) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "createCodes",
					"params" : [ thisFacade._private.sessionToken, prefix, entityKind, count ]
				}
			});
		}

		this.executeImport = function(importData, importOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "executeImport",
					"params" : [ thisFacade._private.sessionToken, importData, importOptions ]
				}
			});
		}

		this.executeExport = function(exportData, exportOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequestTransactional(transactionParticipantId, {
				url : openbisUrl,
				data : {
					"method" : "executeExport",
					"params" : [ thisFacade._private.sessionToken, exportData, exportOptions ]
				}
			});
		}

        this.isSessionActive = function() {
            var thisFacade = this;
            return thisFacade._private.ajaxRequest({
                url : openbisUrl,
                data : {
                    "method" : "isSessionActive",
                    "params" : [ thisFacade._private.sessionToken ]
                }
            });
        }

		this.getDataStoreFacade = function() {
			var dataStoreCodes = [];
			for (var i = 0; i < arguments.length; i++) {
			    var argument = arguments[i]
			    if(Array.isArray(argument)) {
                    Array.prototype.push.apply(dataStoreCodes, argument)
			    } else {
				    dataStoreCodes.push(argument);
				}
			}
			return new dataStoreFacade(this, dataStoreCodes);
		}

		this.getAfsServerFacade = function() {
            return new AfsServerFacade(this)
		}

		this.getMajorVersion = function() {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getMajorVersion",
					"params" : []
				}
			})
		}

		this.getMinorVersion = function() {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getMinorVersion",
					"params" : []
				}
			})
		}

		this.uploadToSessionWorkspace = function(file) {
			//Building Form Data Object for Multipart File Upload
			var formData = new FormData();
			formData.append("sessionKeysNumber", "1");
			formData.append("sessionKey_0", "openbis-file-upload");
			formData.append("openbis-file-upload", file);
			formData.append("keepOriginalFileName", "True");
			formData.append("sessionID", this._private.sessionToken);

			var dfd = jquery.Deferred();

			jquery.ajax({
				type: "POST",
				url: "/openbis/openbis/upload",
				contentType: false,
				processData: false,
				data: formData,
				success: function() {
					dfd.resolve();
				},
				error: function() {
					dfd.reject();
				}
			});

			return dfd.promise();
		}

		/**
		 * =======================
		 * OpenBIS webapp context
		 * =======================
		 *
		 * Provides a context information for webapps that are embedded inside
		 * the OpenBIS UI.
		 *
		 * @class
		 *
		 */
		var openbisWebAppContext = function() {
			this.getWebAppParameter = function(parameterName) {
				var match = location.search.match(RegExp("[?|&]" + parameterName + '=(.+?)(&|$)'));
				if (match && match[1]) {
					return decodeURIComponent(match[1].replace(/\+/g, ' '));
				} else {
					return null;
				}
			}

			this.webappCode = this.getWebAppParameter("webapp-code");
			this.sessionId = this.getWebAppParameter("session-id");
			this.entityKind = this.getWebAppParameter("entity-kind");
			this.entityType = this.getWebAppParameter("entity-type");
			this.entityIdentifier = this.getWebAppParameter("entity-identifier");
			this.entityPermId = this.getWebAppParameter("entity-perm-id");

			this.getWebappCode = function() {
				return this.webappCode;
			}

			this.getSessionId = function() {
				return this.sessionId;
			}

			this.getEntityKind = function() {
				return this.entityKind;
			}

			this.getEntityType = function() {
				return this.entityType;
			}

			this.getEntityIdentifier = function() {
				return this.entityIdentifier;
			}

			this.getEntityPermId = function() {
				return this.entityPermId;
			}

			this.getParameter = function(parameterName) {
				return this.getParameter(parameterName);
			}
		}

		this.getWebAppContext = function() {
			return new openbisWebAppContext();
		}
	}

	/*********
	    DTO
	*********/

    var File = function(fileObject){
        this.owner = fileObject.owner;
        this.path = fileObject.path;
        this.name = fileObject.name;
        this.directory = fileObject.directory;
        this.size = fileObject.size;
        this.lastModifiedTime = fileObject.lastModifiedTime ? Date.parse(fileObject.lastModifiedTime) : null;
        this.creationTime = fileObject.creationTime ? Date.parse(fileObject.creationTime) : null;
        this.lastAccessTime = fileObject.lastAccessTime ? Date.parse(fileObject.lastAccessTime) : null;

        this.getOwner = function(){
            return this.owner;
        }
        this.getPath = function(){
            return this.path;
        }
        this.getName = function(){
            return this.name;
        }
        this.getDirectory = function(){
            return this.directory;
        }
        this.getSize = function(){
            return this.size;
        }
        this.getLastModifiedTime = function(){
            return this.lastModifiedTime;
        }
        this.getCreationTime = function(){
            return this.creationTime;
        }
        this.getLastAccessTime = function(){
            return this.lastAccessTime;
        }
    }

    var FreeSpace = function(freeSpaceObject){

        this.free = freeSpaceObject.free;
        this.total = freeSpaceObject.total;

        this.getFree = function(){
            return this.free;
        }
        this.getTotal = function(){
            return this.total;
        }

    }

    /*********
       UTILS
    *********/

    /**
     * [js-md5]{@link https://github.com/emn178/js-md5}
     *
     * @namespace md5
     * @version 0.8.3
     * @author Chen, Yi-Cyuan [emn178@gmail.com]
     * @copyright Chen, Yi-Cyuan 2014-2023
     * @license MIT
     */
      !function(){"use strict";var $="input is invalid type",t="object"==typeof window,_=t?window:{};_.JS_MD5_NO_WINDOW&&(t=!1);var e=!t&&"object"==typeof self,i=!_.JS_MD5_NO_NODE_JS&&"object"==typeof process&&process.versions&&process.versions.node;i?_=global:e&&(_=self),_.JS_MD5_NO_COMMON_JS||"object"!=typeof module||module.exports,"function"==typeof define&&define.amd;var r,h=!_.JS_MD5_NO_ARRAY_BUFFER&&"undefined"!=typeof ArrayBuffer,s="0123456789abcdef".split(""),f=[128,32768,8388608,-2147483648],n=[0,8,16,24],o=["hex","array","digest","buffer","arrayBuffer","base64"],x="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".split(""),a=[];if(h){var u=new ArrayBuffer(68);r=new Uint8Array(u),a=new Uint32Array(u)}var c=Array.isArray;(_.JS_MD5_NO_NODE_JS||!c)&&(c=function($){return"[object Array]"===Object.prototype.toString.call($)});var F=ArrayBuffer.isView;h&&(_.JS_MD5_NO_ARRAY_BUFFER_IS_VIEW||!F)&&(F=function($){return"object"==typeof $&&$.buffer&&$.buffer.constructor===ArrayBuffer});var p=function(t){var _=typeof t;if("string"===_)return[t,!0];if("object"!==_||null===t)throw Error($);if(h&&t.constructor===ArrayBuffer)return[new Uint8Array(t),!1];if(!c(t)&&!F(t))throw Error($);return[t,!1]},y=function($){return function(t){return new v(!0).update(t)[$]()}},d=function(t){var e,i=require("crypto"),r=require("buffer").Buffer;return e=r.from&&!_.JS_MD5_NO_BUFFER_FROM?r.from:function($){return new r($)},function(_){if("string"==typeof _)return i.createHash("md5").update(_,"utf8").digest("hex");if(null==_)throw Error($);return _.constructor===ArrayBuffer&&(_=new Uint8Array(_)),c(_)||F(_)||_.constructor===r?i.createHash("md5").update(e(_)).digest("hex"):t(_)}},l=function($){return function(t,_){return new b(t,!0).update(_)[$]()}};function v($){if($)a[0]=a[16]=a[1]=a[2]=a[3]=a[4]=a[5]=a[6]=a[7]=a[8]=a[9]=a[10]=a[11]=a[12]=a[13]=a[14]=a[15]=0,this.blocks=a,this.buffer8=r;else if(h){var t=new ArrayBuffer(68);this.buffer8=new Uint8Array(t),this.blocks=new Uint32Array(t)}else this.blocks=[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0];this.h0=this.h1=this.h2=this.h3=this.start=this.bytes=this.hBytes=0,this.finalized=this.hashed=!1,this.first=!0}function b($,t){var _,e=p($);if($=e[0],e[1]){var i,r=[],h=$.length,s=0;for(_=0;_<h;++_)(i=$.charCodeAt(_))<128?r[s++]=i:i<2048?(r[s++]=192|i>>>6,r[s++]=128|63&i):i<55296||i>=57344?(r[s++]=224|i>>>12,r[s++]=128|i>>>6&63,r[s++]=128|63&i):(i=65536+((1023&i)<<10|1023&$.charCodeAt(++_)),r[s++]=240|i>>>18,r[s++]=128|i>>>12&63,r[s++]=128|i>>>6&63,r[s++]=128|63&i);$=r}$.length>64&&($=new v(!0).update($).array());var f=[],n=[];for(_=0;_<64;++_){var o=$[_]||0;f[_]=92^o,n[_]=54^o}v.call(this,t),this.update(n),this.oKeyPad=f,this.inner=!0,this.sharedMemory=t}v.prototype.update=function($){if(this.finalized)throw Error("finalize already called");var t=p($);$=t[0];for(var _,e,i=t[1],r=0,s=$.length,f=this.blocks,o=this.buffer8;r<s;){if(this.hashed&&(this.hashed=!1,f[0]=f[16],f[16]=f[1]=f[2]=f[3]=f[4]=f[5]=f[6]=f[7]=f[8]=f[9]=f[10]=f[11]=f[12]=f[13]=f[14]=f[15]=0),i){if(h)for(e=this.start;r<s&&e<64;++r)(_=$.charCodeAt(r))<128?o[e++]=_:_<2048?(o[e++]=192|_>>>6,o[e++]=128|63&_):_<55296||_>=57344?(o[e++]=224|_>>>12,o[e++]=128|_>>>6&63,o[e++]=128|63&_):(_=65536+((1023&_)<<10|1023&$.charCodeAt(++r)),o[e++]=240|_>>>18,o[e++]=128|_>>>12&63,o[e++]=128|_>>>6&63,o[e++]=128|63&_);else for(e=this.start;r<s&&e<64;++r)(_=$.charCodeAt(r))<128?f[e>>>2]|=_<<n[3&e++]:_<2048?(f[e>>>2]|=(192|_>>>6)<<n[3&e++],f[e>>>2]|=(128|63&_)<<n[3&e++]):_<55296||_>=57344?(f[e>>>2]|=(224|_>>>12)<<n[3&e++],f[e>>>2]|=(128|_>>>6&63)<<n[3&e++],f[e>>>2]|=(128|63&_)<<n[3&e++]):(_=65536+((1023&_)<<10|1023&$.charCodeAt(++r)),f[e>>>2]|=(240|_>>>18)<<n[3&e++],f[e>>>2]|=(128|_>>>12&63)<<n[3&e++],f[e>>>2]|=(128|_>>>6&63)<<n[3&e++],f[e>>>2]|=(128|63&_)<<n[3&e++])}else if(h)for(e=this.start;r<s&&e<64;++r)o[e++]=$[r];else for(e=this.start;r<s&&e<64;++r)f[e>>>2]|=$[r]<<n[3&e++];this.lastByteIndex=e,this.bytes+=e-this.start,e>=64?(this.start=e-64,this.hash(),this.hashed=!0):this.start=e}return this.bytes>4294967295&&(this.hBytes+=this.bytes/4294967296<<0,this.bytes=this.bytes%4294967296),this},v.prototype.finalize=function(){if(!this.finalized){this.finalized=!0;var $=this.blocks,t=this.lastByteIndex;$[t>>>2]|=f[3&t],t>=56&&(this.hashed||this.hash(),$[0]=$[16],$[16]=$[1]=$[2]=$[3]=$[4]=$[5]=$[6]=$[7]=$[8]=$[9]=$[10]=$[11]=$[12]=$[13]=$[14]=$[15]=0),$[14]=this.bytes<<3,$[15]=this.hBytes<<3|this.bytes>>>29,this.hash()}},v.prototype.hash=function(){var $,t,_,e,i,r,h=this.blocks;this.first?(_=((_=(-271733879^(e=((e=(-1732584194^2004318071&($=(($=h[0]-680876937)<<7|$>>>25)-271733879<<0))+h[1]-117830708)<<12|e>>>20)+$<<0)&(-271733879^$))+h[2]-1126478375)<<17|_>>>15)+e<<0,t=((t=($^_&(e^$))+h[3]-1316259209)<<22|t>>>10)+_<<0):($=this.h0,t=this.h1,_=this.h2,$+=((e=this.h3)^t&(_^e))+h[0]-680876936,e+=(_^($=($<<7|$>>>25)+t<<0)&(t^_))+h[1]-389564586,_+=(t^(e=(e<<12|e>>>20)+$<<0)&($^t))+h[2]+606105819,t+=($^(_=(_<<17|_>>>15)+e<<0)&(e^$))+h[3]-1044525330,t=(t<<22|t>>>10)+_<<0),$+=(e^t&(_^e))+h[4]-176418897,e+=(_^($=($<<7|$>>>25)+t<<0)&(t^_))+h[5]+1200080426,_+=(t^(e=(e<<12|e>>>20)+$<<0)&($^t))+h[6]-1473231341,t+=($^(_=(_<<17|_>>>15)+e<<0)&(e^$))+h[7]-45705983,$+=(e^(t=(t<<22|t>>>10)+_<<0)&(_^e))+h[8]+1770035416,e+=(_^($=($<<7|$>>>25)+t<<0)&(t^_))+h[9]-1958414417,_+=(t^(e=(e<<12|e>>>20)+$<<0)&($^t))+h[10]-42063,t+=($^(_=(_<<17|_>>>15)+e<<0)&(e^$))+h[11]-1990404162,$+=(e^(t=(t<<22|t>>>10)+_<<0)&(_^e))+h[12]+1804603682,e+=(_^($=($<<7|$>>>25)+t<<0)&(t^_))+h[13]-40341101,_+=(t^(e=(e<<12|e>>>20)+$<<0)&($^t))+h[14]-1502002290,t+=($^(_=(_<<17|_>>>15)+e<<0)&(e^$))+h[15]+1236535329,t=(t<<22|t>>>10)+_<<0,$+=(_^e&(t^_))+h[1]-165796510,$=($<<5|$>>>27)+t<<0,e+=(t^_&($^t))+h[6]-1069501632,e=(e<<9|e>>>23)+$<<0,_+=($^t&(e^$))+h[11]+643717713,_=(_<<14|_>>>18)+e<<0,t+=(e^$&(_^e))+h[0]-373897302,t=(t<<20|t>>>12)+_<<0,$+=(_^e&(t^_))+h[5]-701558691,$=($<<5|$>>>27)+t<<0,e+=(t^_&($^t))+h[10]+38016083,e=(e<<9|e>>>23)+$<<0,_+=($^t&(e^$))+h[15]-660478335,_=(_<<14|_>>>18)+e<<0,t+=(e^$&(_^e))+h[4]-405537848,t=(t<<20|t>>>12)+_<<0,$+=(_^e&(t^_))+h[9]+568446438,$=($<<5|$>>>27)+t<<0,e+=(t^_&($^t))+h[14]-1019803690,e=(e<<9|e>>>23)+$<<0,_+=($^t&(e^$))+h[3]-187363961,_=(_<<14|_>>>18)+e<<0,t+=(e^$&(_^e))+h[8]+1163531501,t=(t<<20|t>>>12)+_<<0,$+=(_^e&(t^_))+h[13]-1444681467,$=($<<5|$>>>27)+t<<0,e+=(t^_&($^t))+h[2]-51403784,e=(e<<9|e>>>23)+$<<0,_+=($^t&(e^$))+h[7]+1735328473,_=(_<<14|_>>>18)+e<<0,t+=(e^$&(_^e))+h[12]-1926607734,$+=((i=(t=(t<<20|t>>>12)+_<<0)^_)^e)+h[5]-378558,e+=(i^($=($<<4|$>>>28)+t<<0))+h[8]-2022574463,_+=((r=(e=(e<<11|e>>>21)+$<<0)^$)^t)+h[11]+1839030562,t+=(r^(_=(_<<16|_>>>16)+e<<0))+h[14]-35309556,$+=((i=(t=(t<<23|t>>>9)+_<<0)^_)^e)+h[1]-1530992060,e+=(i^($=($<<4|$>>>28)+t<<0))+h[4]+1272893353,_+=((r=(e=(e<<11|e>>>21)+$<<0)^$)^t)+h[7]-155497632,t+=(r^(_=(_<<16|_>>>16)+e<<0))+h[10]-1094730640,$+=((i=(t=(t<<23|t>>>9)+_<<0)^_)^e)+h[13]+681279174,e+=(i^($=($<<4|$>>>28)+t<<0))+h[0]-358537222,_+=((r=(e=(e<<11|e>>>21)+$<<0)^$)^t)+h[3]-722521979,t+=(r^(_=(_<<16|_>>>16)+e<<0))+h[6]+76029189,$+=((i=(t=(t<<23|t>>>9)+_<<0)^_)^e)+h[9]-640364487,e+=(i^($=($<<4|$>>>28)+t<<0))+h[12]-421815835,_+=((r=(e=(e<<11|e>>>21)+$<<0)^$)^t)+h[15]+530742520,t+=(r^(_=(_<<16|_>>>16)+e<<0))+h[2]-995338651,t=(t<<23|t>>>9)+_<<0,$+=(_^(t|~e))+h[0]-198630844,$=($<<6|$>>>26)+t<<0,e+=(t^($|~_))+h[7]+1126891415,e=(e<<10|e>>>22)+$<<0,_+=($^(e|~t))+h[14]-1416354905,_=(_<<15|_>>>17)+e<<0,t+=(e^(_|~$))+h[5]-57434055,t=(t<<21|t>>>11)+_<<0,$+=(_^(t|~e))+h[12]+1700485571,$=($<<6|$>>>26)+t<<0,e+=(t^($|~_))+h[3]-1894986606,e=(e<<10|e>>>22)+$<<0,_+=($^(e|~t))+h[10]-1051523,_=(_<<15|_>>>17)+e<<0,t+=(e^(_|~$))+h[1]-2054922799,t=(t<<21|t>>>11)+_<<0,$+=(_^(t|~e))+h[8]+1873313359,$=($<<6|$>>>26)+t<<0,e+=(t^($|~_))+h[15]-30611744,e=(e<<10|e>>>22)+$<<0,_+=($^(e|~t))+h[6]-1560198380,_=(_<<15|_>>>17)+e<<0,t+=(e^(_|~$))+h[13]+1309151649,t=(t<<21|t>>>11)+_<<0,$+=(_^(t|~e))+h[4]-145523070,$=($<<6|$>>>26)+t<<0,e+=(t^($|~_))+h[11]-1120210379,e=(e<<10|e>>>22)+$<<0,_+=($^(e|~t))+h[2]+718787259,_=(_<<15|_>>>17)+e<<0,t+=(e^(_|~$))+h[9]-343485551,t=(t<<21|t>>>11)+_<<0,this.first?(this.h0=$+1732584193<<0,this.h1=t-271733879<<0,this.h2=_-1732584194<<0,this.h3=e+271733878<<0,this.first=!1):(this.h0=this.h0+$<<0,this.h1=this.h1+t<<0,this.h2=this.h2+_<<0,this.h3=this.h3+e<<0)},v.prototype.hex=function(){this.finalize();var $=this.h0,t=this.h1,_=this.h2,e=this.h3;return s[$>>>4&15]+s[15&$]+s[$>>>12&15]+s[$>>>8&15]+s[$>>>20&15]+s[$>>>16&15]+s[$>>>28&15]+s[$>>>24&15]+s[t>>>4&15]+s[15&t]+s[t>>>12&15]+s[t>>>8&15]+s[t>>>20&15]+s[t>>>16&15]+s[t>>>28&15]+s[t>>>24&15]+s[_>>>4&15]+s[15&_]+s[_>>>12&15]+s[_>>>8&15]+s[_>>>20&15]+s[_>>>16&15]+s[_>>>28&15]+s[_>>>24&15]+s[e>>>4&15]+s[15&e]+s[e>>>12&15]+s[e>>>8&15]+s[e>>>20&15]+s[e>>>16&15]+s[e>>>28&15]+s[e>>>24&15]},v.prototype.toString=v.prototype.hex,v.prototype.digest=function(){this.finalize();var $=this.h0,t=this.h1,_=this.h2,e=this.h3;return[255&$,$>>>8&255,$>>>16&255,$>>>24&255,255&t,t>>>8&255,t>>>16&255,t>>>24&255,255&_,_>>>8&255,_>>>16&255,_>>>24&255,255&e,e>>>8&255,e>>>16&255,e>>>24&255]},v.prototype.array=v.prototype.digest,v.prototype.arrayBuffer=function(){this.finalize();var $=new ArrayBuffer(16),t=new Uint32Array($);return t[0]=this.h0,t[1]=this.h1,t[2]=this.h2,t[3]=this.h3,$},v.prototype.buffer=v.prototype.arrayBuffer,v.prototype.base64=function(){for(var $,t,_,e="",i=this.array(),r=0;r<15;)$=i[r++],t=i[r++],_=i[r++],e+=x[$>>>2]+x[($<<4|t>>>4)&63]+x[(t<<2|_>>>6)&63]+x[63&_];return e+(x[($=i[r])>>>2]+x[$<<4&63]+"==")},b.prototype=new v,b.prototype.finalize=function(){if(v.prototype.finalize.call(this),this.inner){this.inner=!1;var $=this.array();v.call(this,this.sharedMemory),this.update(this.oKeyPad),this.update($),v.prototype.finalize.call(this)}};var w=function(){var $=y("hex");i&&($=d($)),$.create=function(){return new v},$.update=function(t){return $.create().update(t)};for(var t=0;t<o.length;++t){var _=o[t];$[_]=y(_)}return $}();w.md5=w,w.md5.hmac=function(){var $=l("hex");$.create=function($){return new b($)},$.update=function(t,_){return $.create(t).update(_)};for(var t=0;t<o.length;++t){var _=o[t];$[_]=l(_)}return $}(),window.md5=w}();

    // parseUri 1.2.2 (c) Steven Levithan <stevenlevithan.com> MIT License (see http://blog.stevenlevithan.com/archives/parseuri)

    var parseUri = function(str) {
        var options = {
            strictMode: false,
            key: ["source","protocol","authority","userInfo","user","password","host","port","relative","path","directory","file","query","anchor"],
            q:   {
                name:   "queryKey",
                parser: /(?:^|&)([^&=]*)=?([^&]*)/g
            },
            parser: {
                strict: /^(?:([^:\/?#]+):)?(?:\/\/((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?))?((((?:[^?#\/]*\/)*)([^?#]*))(?:\?([^#]*))?(?:#(.*))?)/,
                loose:  /^(?:(?![^:@]+:[^:@\/]*@)([^:\/?#.]+):)?(?:\/\/)?((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?)(((\/(?:[^?#](?![^?#\/]*\.[^?#\/.]+(?:[?#]|$)))*\/?)?([^?#\/]*))(?:\?([^#]*))?(?:#(.*))?)/
            }
        };

        var	o   = options,
            m   = o.parser[o.strictMode ? "strict" : "loose"].exec(str),
            uri = {},
            i   = 14;

        while (i--) uri[o.key[i]] = m[i] || "";

        uri[o.q.name] = {};
        uri[o.key[12]].replace(o.q.parser, function ($0, $1, $2) {
            if ($1) uri[o.q.name][$1] = $2;
        });

        return uri;
    }

    /** Helper function to convert string md5Hash into an array. */
    var hex2a = function(hexx) {
        var hex = hexx.toString(); //force conversion
        var str = '';
        for (var i = 0; i < hex.length; i += 2)
            str += String.fromCharCode(parseInt(hex.substr(i, 2), 16));
        return str;
    }

    function base64URLEncode(str) {
        const base64Encoded = btoa(str);
        return base64Encoded.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
    }

    function unit8ArrayToString(bytes) {
        var string = ""
        for (var i = 0; i < bytes.length; i++) {
            string += String.fromCharCode(bytes[i])
        }
        return string
    }

    return facade;

});