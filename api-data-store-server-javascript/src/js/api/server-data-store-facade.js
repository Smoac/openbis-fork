;(function(global){
  'use strict'

/**
 * ======================================================
 * OpenBIS Data Store Server facade internal code (DO NOT USE!!!)
 * ======================================================
 */

function _DataStoreServerInternal(datastoreUrlOrNull, httpServerUri){
	this.init(datastoreUrlOrNull, httpServerUri);
}

_DataStoreServerInternal.prototype.init = function(datastoreUrlOrNull, httpServerUri){
	this.datastoreUrl = this.normalizeUrl(datastoreUrlOrNull, httpServerUri) + "/api";
	this.httpServerUri = httpServerUri;
}

_DataStoreServerInternal.prototype.log = function(msg){
	if(console){
		console.log(msg);
	}
}

_DataStoreServerInternal.prototype.normalizeUrl = function(openbisUrlOrNull, httpServerUri){
	var parts = this.parseUri(window.location);
	
	if(openbisUrlOrNull){
		var openbisParts = this.parseUri(openbisUrlOrNull);
		
		for(var openbisPartName in openbisParts){
			var openbisPartValue = openbisParts[openbisPartName];
			
			if(openbisPartValue){
				parts[openbisPartName] = openbisPartValue;
			}
		}
	}
	
	return parts.protocol + "://" + parts.authority + (httpServerUri || parts.path);
}

_DataStoreServerInternal.prototype.getUrlForMethod = function(method) {
    return this.datastoreUrl + "?method=" + method;
}

_DataStoreServerInternal.prototype.jsonRequestData = function(params) {
	return JSON.stringify(params);
}

_DataStoreServerInternal.prototype.sendHttpRequest = function(httpMethod, contentType, url, data) {
	const xhr = new XMLHttpRequest();
	xhr.open(httpMethod, url);
	xhr.responseType = "blob";

	return new Promise((resolve, reject) => {
		xhr.onreadystatechange = function() {
			if (xhr.readyState === XMLHttpRequest.DONE) {
				const status = xhr.status;
				const response = xhr.response;

				if (status >= 200 && status < 300) {
					const contentType = this.getResponseHeader('content-type');

					switch (contentType) {
						case 'text/plain':
							// Fall through.
						case'application/json': {
							response.text().then((blobResponse) => resolve(blobResponse))
								.catch((error) => reject(error));
							break;
						}
						case 'application/octet-stream': {
							resolve(response);
							break;
						}
						default: {
							reject(new Error("Client error HTTP response. Unsupported content-type received."));
							break;
						}
					}
				} else if (status >= 400 && status < 600) {
					response.text().then((textResponse) => {
						try {
							const errorMessage = JSON.parse(textResponse).error[1].message;
							reject(new Error(errorMessage));
						} catch (e) {
							reject(new Error(textResponse || xhr.statusText));
						}
					}).catch(() => {
						reject(new Error("HTTP Error: " + status));
					});
				}
			}
		};
		xhr.send(data);
	});
}



  _DataStoreServerInternal.prototype.buildGetUrl = function(queryParams) {
	const queryString = Object.keys(queryParams)
	  .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(queryParams[key])}`)
	  .join('&');
	return `${this.datastoreUrl}?${queryString}`;
  }



// Functions for working with cookies (see http://www.quirksmode.org/js/cookies.html)

_DataStoreServerInternal.prototype.createCookie = function(name,value,days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}

_DataStoreServerInternal.prototype.readCookie = function(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

_DataStoreServerInternal.prototype.eraseCookie = function(name) {
	this.createCookie(name,"",-1);
}

// parseUri 1.2.2 (c) Steven Levithan <stevenlevithan.com> MIT License (see http://blog.stevenlevithan.com/archives/parseuri)

_DataStoreServerInternal.prototype.parseUri = function(str) {
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


/** Helper method for checking response from DSS server */
function parseJsonResponse(rawResponse) {
	return new Promise((resolve, reject) => {
		let response = JSON.parse(rawResponse);
		if (response.error) {
			reject(new Error(response.error[1].message));
		} else {
			resolve(response);
		}
	});
}



/**
 * ===============
 * DSS facade
 * ===============
 * 
 * The facade provides access to the DSS methods
 * 
 */
function DataStoreServer(datastoreUrlOrNull, httpServerUri) {
	this._internal = new _DataStoreServerInternal(datastoreUrlOrNull, httpServerUri);
}


/**
 * ==================================================================================
 * ch.ethz.sis.afsapi.api.AuthenticationAPI methods
 * ==================================================================================
 */

/**
 * Stores the current session in a cookie. 
 *
 * @method
 */
DataStoreServer.prototype.rememberSession = function() {
	this._internal.createCookie('dataStoreServer', this.getSession(), 1);
}

/**
 * Removes the current session from a cookie. 
 *
 * @method
 */
DataStoreServer.prototype.forgetSession = function() {
	this._internal.eraseCookie('dataStoreServer');
}

/**
 * Restores the current session from a cookie.
 *
 * @method
 */
DataStoreServer.prototype.restoreSession = function() {
	this._internal.sessionToken = this._internal.readCookie('dataStoreServer');
}

/**
 * Sets the current session.
 *
 * @method
 */
DataStoreServer.prototype.useSession = function(sessionToken){
	this._internal.sessionToken = sessionToken;
}

/**
 * Returns the current session.
 * 
 * @method
 */
DataStoreServer.prototype.getSession = function(){
	return this._internal.sessionToken;
}

/**
 * Sets interactiveSessionKey.
 * 
 * @method
 */
DataStoreServer.prototype.setInteractiveSessionKey = function(interactiveSessionKey){
	this._internal.interactiveSessionKey = interactiveSessionKey;
}

/**
 * Returns the current session.
 * 
 * @method
 */
DataStoreServer.prototype.getInteractiveSessionKey = function(){
	return this._internal.interactiveSessionKey;
}

/**
 * Sets transactionManagerKey.
 * 
 * @method
 */
DataStoreServer.prototype.setTransactionManagerKey = function(transactionManagerKey){
	this._internal.transactionManagerKey = transactionManagerKey;
}

/**
 * Returns the current session.
 * 
 * @method
 */
DataStoreServer.prototype.getTransactionManagerKey = function(){
	return this._internal.transactionManagerKey;
}

DataStoreServer.prototype.fillCommonParameters = function(params) {
	if(this.getSession()) {
		params["sessionToken"] = this.getSession();
	}
	if(this.getInteractiveSessionKey()) {
		params["interactiveSessionKey"] = this.getInteractiveSessionKey();
	}
	if(this.getTransactionManagerKey()) {
		params["transactionManagerKey"] = this.getTransactionManagerKey();
	}
	return params;
}

const encodeParams = (params) => {
	return Object.entries(params)
		.map(kv => {
			const key = kv[0]
			const value =  kv[1]
			const encodedValue = (key === "data" || key === "md5Hash")
				? value : encodeURIComponent(value)
			return `${encodeURIComponent(key)}=${encodedValue}`
		})
		.join("&")
};

/**
 * Log into DSS.
 * 
 * @method
 */
DataStoreServer.prototype.login = function(userId, userPassword) {
	var datastoreObj = this
	const data =  this.fillCommonParameters({
		"method": "login",
		"userId": userId,
		"password": userPassword
	});
	return this._internal.sendHttpRequest(
		"POST",
		"application/octet-stream",
		this._internal.datastoreUrl,
		encodeParams(data)
	).then((loginResponse) => {
		return new Promise((resolve, reject) => {
			datastoreObj._internal.sessionToken = loginResponse;
			datastoreObj.rememberSession();
			resolve(loginResponse);
		})
	});
}


/**
 * Checks whether the current session is still active.
 *
 */
DataStoreServer.prototype.isSessionValid = function() {
	return new Promise((resolve, reject) => {
		if (this.getSession()) {
			const data =  this.fillCommonParameters({"method":"isSessionValid"});
			this._internal.sendHttpRequest(
				"GET",
				"application/octet-stream",
				this._internal.datastoreUrl,
				encodeParams(data)
			).then((response) => parseJsonResponse(response).then((value) => resolve(value))
				.catch((reason) => reject(reason)));
		} else {
			resolve({ result : false })
		}
	});
}

/**
 * Restores the current session from a cookie.
 * 
 * @see restoreSession()
 * @see isSessionActive()
 * @method
 */
DataStoreServer.prototype.ifRestoredSessionActive = function() {
	this.restoreSession();
	return this.isSessionValid();
}

/**
 * Log out of DSS.
 * 
 * @method
 */
DataStoreServer.prototype.logout = function() {
	return new Promise((resolve, reject) => {
		this.forgetSession();

		if (this.getSession()) {
			const data = this.fillCommonParameters({"method": "logout"});
			this._internal.sendHttpRequest(
				"POST",
				"application/octet-stream",
				this._internal.datastoreUrl,
				encodeParams(data)
			).then((response) => parseJsonResponse(response).then((value) => resolve(value))
				.catch((reason) => reject(reason)));
		} else {
			resolve({result: null});
		}
	});
}


/**
 * ==================================================================================
 * ch.ethz.sis.afsapi.api.OperationsAPI methods
 * ==================================================================================
 */

/**
 * List files in the DSS for given owner and source
 */
DataStoreServer.prototype.list = function(owner, source, recursively){
	const data =  this.fillCommonParameters({
		"method": "list",
		"owner" :  owner,
		"source":  source,
		"recursively":  recursively
	});
	return this._internal.sendHttpRequest(
		"GET",
		"application/octet-stream",
		this._internal.buildGetUrl(data),
		{}
	).then((response) => parseJsonResponse(response)).then((response) => {
        if(response && Array.isArray(response.result) && response.result.length === 2){
            var files = []
            if(Array.isArray(response.result[1])){
                response.result[1].forEach(function(item){
                    if(Array.isArray(item) && item.length === 2){
                        files.push(new File(item[1]));
                    }
                });
            }
            return files;
        } else {
            return response
        }
	});
}

/**
 * Read the contents of selected file
 * @param {str} owner owner of the file 
 * @param {str} source path to file
 * @param {int} offset offset from which to start reading
 * @param {int} limit how many characters to read
 */
DataStoreServer.prototype.read = function(owner, source, offset, limit){
	const data =  this.fillCommonParameters({
		"method": "read",
		"owner" :  owner,
		"source":  source,
		"offset":  offset,
		"limit":  limit
	});
	return this._internal.sendHttpRequest(
		"GET",
		"application/octet-stream",
		this._internal.buildGetUrl(data),
		{}
	);
}

/**
 * Write data to file (or create it)
 * @param {str} owner owner of the file
 * @param {str} source path to file
 * @param {int} offset offset from which to start writing
 * @param {str} data data to write
 */
DataStoreServer.prototype.write = function(owner, source, offset, data){
	const params =  this.fillCommonParameters({
		"method": "write",
		"owner" : owner,
		"source": source,
		"offset": offset,
		"md5Hash": base64URLEncode(hex2a(md5(data))),
	});

	return this._internal.sendHttpRequest(
		"POST",
		"application/octet-stream",
		this._internal.buildGetUrl(params),
		data
	);
}

/**
 * Delete file from the DSS
 * @param {str} owner owner of the file
 * @param {str} source path to file
 */
DataStoreServer.prototype.delete = function(owner, source){
	const data =  this.fillCommonParameters({
		"method": "delete",
		"owner" : owner,
		"source": source
	});
	return this._internal.sendHttpRequest(
		"DELETE",
		"application/octet-stream",
		this._internal.datastoreUrl,
		encodeParams(data)
	);
}

/**
 * Copy file within DSS
 */
DataStoreServer.prototype.copy = function(sourceOwner, source, targetOwner, target){
	const data =  this.fillCommonParameters({
		"method": "copy",
		"sourceOwner" : sourceOwner,
		"source": source,
		"targetOwner": targetOwner,
		"target" : target
	});
	return this._internal.sendHttpRequest(
		"POST",
		"application/octet-stream",
		this._internal.datastoreUrl,
		encodeParams(data)
	);
}

/**
 * Move file within DSS
 */
DataStoreServer.prototype.move = function(sourceOwner, source, targetOwner, target){
	const data =  this.fillCommonParameters({
		"method": "move",
		"sourceOwner" : sourceOwner,
		"source": source,
		"targetOwner": targetOwner,
		"target" : target
	});
	return this._internal.sendHttpRequest(
		"POST",
		"application/octet-stream",
		this._internal.datastoreUrl,
		encodeParams(data)
	);
}

/**
 * Create a file/directory within DSS
 */
DataStoreServer.prototype.create = function(owner, source, directory){
	const data =  this.fillCommonParameters({
		"method": "create",
		"owner" : owner,
		"source": source,
		"directory": directory
	});
	return this._internal.sendHttpRequest(
		"POST",
		"application/octet-stream",
		this._internal.datastoreUrl,
		encodeParams(data)
	);
}

/**
 * Get the space information for given owner and source
 */
DataStoreServer.prototype.free = function(owner, source){
	const data =  this.fillCommonParameters({
		"method": "free",
		"owner" :  owner,
		"source":  source,
	});
	return this._internal.sendHttpRequest(
		"GET",
		"application/octet-stream",
		this._internal.buildGetUrl(data),
		{}
	).then((response) => parseJsonResponse(response)).then((response) => {
        if(response && Array.isArray(response.result) && response.result.length === 2){
            return new FreeSpace(response.result[1])
        } else {
            return response
        }
    });
}

/**
 * ==================================================================================
 * ch.ethz.sis.afsapi.api.TwoPhaseTransactionAPI methods
 * ==================================================================================
 */

DataStoreServer.prototype.begin = function(transactionId){
	const data =  this.fillCommonParameters({
		"method": "begin",
		"transactionId" : transactionId
	});
	return this._internal.sendHttpRequest(
		"POST",
		"application/octet-stream",
		this._internal.datastoreUrl,
		encodeParams(data)
	);
}

DataStoreServer.prototype.prepare = function(){
	const data =  this.fillCommonParameters({
		"method": "prepare"
	});
	return this._internal.sendHttpRequest(
		"POST",
		"application/octet-stream",
		this._internal.datastoreUrl,
		encodeParams(data)
	);
}

DataStoreServer.prototype.commit = function(){
	const data =  this.fillCommonParameters({
		"method": "commit"
	});
	return this._internal.sendHttpRequest(
		"POST",
		"application/octet-stream",
		this._internal.datastoreUrl,
		encodeParams(data)
	);
}


DataStoreServer.prototype.rollback = function(){
	const data =  this.fillCommonParameters({
		"method": "rollback"
	});
	return this._internal.sendHttpRequest(
		"POST",
		"application/octet-stream",
		this._internal.datastoreUrl,
		encodeParams(data)
	);
}

DataStoreServer.prototype.recover = function(){
	const data =  this.fillCommonParameters({
		"method": "recover"
	});
	return this._internal.sendHttpRequest(
		"POST",
		"application/octet-stream",
		this._internal.datastoreUrl,
		encodeParams(data)
	);
}

/**
 * ==================================================================================
 * DTO
 * ==================================================================================
 */

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

/**
 * ==================================================================================
 * UTILS
 * ==================================================================================
 */

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

/**
 * ==================================================================================
 * EXPORT
 * ==================================================================================
 */

if (typeof define === 'function' && define.amd) {
  define(function () {
    return DataStoreServer
  })
} else if (typeof module === 'object' && module.exports) {
  module.exports = DataStoreServer
} else {
  global.DataStoreServer = DataStoreServer
}

})(this);

/**
 * ==================================================================================
 * EXPORT
 * ==================================================================================
 */

/**
 * [js-md5]{@link https://github.com/emn178/js-md5}
 *
 * @namespace md5
 * @version 0.8.3
 * @author Chen, Yi-Cyuan [emn178@gmail.com]
 * @copyright Chen, Yi-Cyuan 2014-2023
 * @license MIT
 */ !function(){"use strict";var $="input is invalid type",t="object"==typeof window,_=t?window:{};_.JS_MD5_NO_WINDOW&&(t=!1);var e=!t&&"object"==typeof self,i=!_.JS_MD5_NO_NODE_JS&&"object"==typeof process&&process.versions&&process.versions.node;i?_=global:e&&(_=self),_.JS_MD5_NO_COMMON_JS||"object"!=typeof module||module.exports,"function"==typeof define&&define.amd;var r,h=!_.JS_MD5_NO_ARRAY_BUFFER&&"undefined"!=typeof ArrayBuffer,s="0123456789abcdef".split(""),f=[128,32768,8388608,-2147483648],n=[0,8,16,24],o=["hex","array","digest","buffer","arrayBuffer","base64"],x="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".split(""),a=[];if(h){var u=new ArrayBuffer(68);r=new Uint8Array(u),a=new Uint32Array(u)}var c=Array.isArray;(_.JS_MD5_NO_NODE_JS||!c)&&(c=function($){return"[object Array]"===Object.prototype.toString.call($)});var F=ArrayBuffer.isView;h&&(_.JS_MD5_NO_ARRAY_BUFFER_IS_VIEW||!F)&&(F=function($){return"object"==typeof $&&$.buffer&&$.buffer.constructor===ArrayBuffer});var p=function(t){var _=typeof t;if("string"===_)return[t,!0];if("object"!==_||null===t)throw Error($);if(h&&t.constructor===ArrayBuffer)return[new Uint8Array(t),!1];if(!c(t)&&!F(t))throw Error($);return[t,!1]},y=function($){return function(t){return new v(!0).update(t)[$]()}},d=function(t){var e,i=require("crypto"),r=require("buffer").Buffer;return e=r.from&&!_.JS_MD5_NO_BUFFER_FROM?r.from:function($){return new r($)},function(_){if("string"==typeof _)return i.createHash("md5").update(_,"utf8").digest("hex");if(null==_)throw Error($);return _.constructor===ArrayBuffer&&(_=new Uint8Array(_)),c(_)||F(_)||_.constructor===r?i.createHash("md5").update(e(_)).digest("hex"):t(_)}},l=function($){return function(t,_){return new b(t,!0).update(_)[$]()}};function v($){if($)a[0]=a[16]=a[1]=a[2]=a[3]=a[4]=a[5]=a[6]=a[7]=a[8]=a[9]=a[10]=a[11]=a[12]=a[13]=a[14]=a[15]=0,this.blocks=a,this.buffer8=r;else if(h){var t=new ArrayBuffer(68);this.buffer8=new Uint8Array(t),this.blocks=new Uint32Array(t)}else this.blocks=[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0];this.h0=this.h1=this.h2=this.h3=this.start=this.bytes=this.hBytes=0,this.finalized=this.hashed=!1,this.first=!0}function b($,t){var _,e=p($);if($=e[0],e[1]){var i,r=[],h=$.length,s=0;for(_=0;_<h;++_)(i=$.charCodeAt(_))<128?r[s++]=i:i<2048?(r[s++]=192|i>>>6,r[s++]=128|63&i):i<55296||i>=57344?(r[s++]=224|i>>>12,r[s++]=128|i>>>6&63,r[s++]=128|63&i):(i=65536+((1023&i)<<10|1023&$.charCodeAt(++_)),r[s++]=240|i>>>18,r[s++]=128|i>>>12&63,r[s++]=128|i>>>6&63,r[s++]=128|63&i);$=r}$.length>64&&($=new v(!0).update($).array());var f=[],n=[];for(_=0;_<64;++_){var o=$[_]||0;f[_]=92^o,n[_]=54^o}v.call(this,t),this.update(n),this.oKeyPad=f,this.inner=!0,this.sharedMemory=t}v.prototype.update=function($){if(this.finalized)throw Error("finalize already called");var t=p($);$=t[0];for(var _,e,i=t[1],r=0,s=$.length,f=this.blocks,o=this.buffer8;r<s;){if(this.hashed&&(this.hashed=!1,f[0]=f[16],f[16]=f[1]=f[2]=f[3]=f[4]=f[5]=f[6]=f[7]=f[8]=f[9]=f[10]=f[11]=f[12]=f[13]=f[14]=f[15]=0),i){if(h)for(e=this.start;r<s&&e<64;++r)(_=$.charCodeAt(r))<128?o[e++]=_:_<2048?(o[e++]=192|_>>>6,o[e++]=128|63&_):_<55296||_>=57344?(o[e++]=224|_>>>12,o[e++]=128|_>>>6&63,o[e++]=128|63&_):(_=65536+((1023&_)<<10|1023&$.charCodeAt(++r)),o[e++]=240|_>>>18,o[e++]=128|_>>>12&63,o[e++]=128|_>>>6&63,o[e++]=128|63&_);else for(e=this.start;r<s&&e<64;++r)(_=$.charCodeAt(r))<128?f[e>>>2]|=_<<n[3&e++]:_<2048?(f[e>>>2]|=(192|_>>>6)<<n[3&e++],f[e>>>2]|=(128|63&_)<<n[3&e++]):_<55296||_>=57344?(f[e>>>2]|=(224|_>>>12)<<n[3&e++],f[e>>>2]|=(128|_>>>6&63)<<n[3&e++],f[e>>>2]|=(128|63&_)<<n[3&e++]):(_=65536+((1023&_)<<10|1023&$.charCodeAt(++r)),f[e>>>2]|=(240|_>>>18)<<n[3&e++],f[e>>>2]|=(128|_>>>12&63)<<n[3&e++],f[e>>>2]|=(128|_>>>6&63)<<n[3&e++],f[e>>>2]|=(128|63&_)<<n[3&e++])}else if(h)for(e=this.start;r<s&&e<64;++r)o[e++]=$[r];else for(e=this.start;r<s&&e<64;++r)f[e>>>2]|=$[r]<<n[3&e++];this.lastByteIndex=e,this.bytes+=e-this.start,e>=64?(this.start=e-64,this.hash(),this.hashed=!0):this.start=e}return this.bytes>4294967295&&(this.hBytes+=this.bytes/4294967296<<0,this.bytes=this.bytes%4294967296),this},v.prototype.finalize=function(){if(!this.finalized){this.finalized=!0;var $=this.blocks,t=this.lastByteIndex;$[t>>>2]|=f[3&t],t>=56&&(this.hashed||this.hash(),$[0]=$[16],$[16]=$[1]=$[2]=$[3]=$[4]=$[5]=$[6]=$[7]=$[8]=$[9]=$[10]=$[11]=$[12]=$[13]=$[14]=$[15]=0),$[14]=this.bytes<<3,$[15]=this.hBytes<<3|this.bytes>>>29,this.hash()}},v.prototype.hash=function(){var $,t,_,e,i,r,h=this.blocks;this.first?(_=((_=(-271733879^(e=((e=(-1732584194^2004318071&($=(($=h[0]-680876937)<<7|$>>>25)-271733879<<0))+h[1]-117830708)<<12|e>>>20)+$<<0)&(-271733879^$))+h[2]-1126478375)<<17|_>>>15)+e<<0,t=((t=($^_&(e^$))+h[3]-1316259209)<<22|t>>>10)+_<<0):($=this.h0,t=this.h1,_=this.h2,$+=((e=this.h3)^t&(_^e))+h[0]-680876936,e+=(_^($=($<<7|$>>>25)+t<<0)&(t^_))+h[1]-389564586,_+=(t^(e=(e<<12|e>>>20)+$<<0)&($^t))+h[2]+606105819,t+=($^(_=(_<<17|_>>>15)+e<<0)&(e^$))+h[3]-1044525330,t=(t<<22|t>>>10)+_<<0),$+=(e^t&(_^e))+h[4]-176418897,e+=(_^($=($<<7|$>>>25)+t<<0)&(t^_))+h[5]+1200080426,_+=(t^(e=(e<<12|e>>>20)+$<<0)&($^t))+h[6]-1473231341,t+=($^(_=(_<<17|_>>>15)+e<<0)&(e^$))+h[7]-45705983,$+=(e^(t=(t<<22|t>>>10)+_<<0)&(_^e))+h[8]+1770035416,e+=(_^($=($<<7|$>>>25)+t<<0)&(t^_))+h[9]-1958414417,_+=(t^(e=(e<<12|e>>>20)+$<<0)&($^t))+h[10]-42063,t+=($^(_=(_<<17|_>>>15)+e<<0)&(e^$))+h[11]-1990404162,$+=(e^(t=(t<<22|t>>>10)+_<<0)&(_^e))+h[12]+1804603682,e+=(_^($=($<<7|$>>>25)+t<<0)&(t^_))+h[13]-40341101,_+=(t^(e=(e<<12|e>>>20)+$<<0)&($^t))+h[14]-1502002290,t+=($^(_=(_<<17|_>>>15)+e<<0)&(e^$))+h[15]+1236535329,t=(t<<22|t>>>10)+_<<0,$+=(_^e&(t^_))+h[1]-165796510,$=($<<5|$>>>27)+t<<0,e+=(t^_&($^t))+h[6]-1069501632,e=(e<<9|e>>>23)+$<<0,_+=($^t&(e^$))+h[11]+643717713,_=(_<<14|_>>>18)+e<<0,t+=(e^$&(_^e))+h[0]-373897302,t=(t<<20|t>>>12)+_<<0,$+=(_^e&(t^_))+h[5]-701558691,$=($<<5|$>>>27)+t<<0,e+=(t^_&($^t))+h[10]+38016083,e=(e<<9|e>>>23)+$<<0,_+=($^t&(e^$))+h[15]-660478335,_=(_<<14|_>>>18)+e<<0,t+=(e^$&(_^e))+h[4]-405537848,t=(t<<20|t>>>12)+_<<0,$+=(_^e&(t^_))+h[9]+568446438,$=($<<5|$>>>27)+t<<0,e+=(t^_&($^t))+h[14]-1019803690,e=(e<<9|e>>>23)+$<<0,_+=($^t&(e^$))+h[3]-187363961,_=(_<<14|_>>>18)+e<<0,t+=(e^$&(_^e))+h[8]+1163531501,t=(t<<20|t>>>12)+_<<0,$+=(_^e&(t^_))+h[13]-1444681467,$=($<<5|$>>>27)+t<<0,e+=(t^_&($^t))+h[2]-51403784,e=(e<<9|e>>>23)+$<<0,_+=($^t&(e^$))+h[7]+1735328473,_=(_<<14|_>>>18)+e<<0,t+=(e^$&(_^e))+h[12]-1926607734,$+=((i=(t=(t<<20|t>>>12)+_<<0)^_)^e)+h[5]-378558,e+=(i^($=($<<4|$>>>28)+t<<0))+h[8]-2022574463,_+=((r=(e=(e<<11|e>>>21)+$<<0)^$)^t)+h[11]+1839030562,t+=(r^(_=(_<<16|_>>>16)+e<<0))+h[14]-35309556,$+=((i=(t=(t<<23|t>>>9)+_<<0)^_)^e)+h[1]-1530992060,e+=(i^($=($<<4|$>>>28)+t<<0))+h[4]+1272893353,_+=((r=(e=(e<<11|e>>>21)+$<<0)^$)^t)+h[7]-155497632,t+=(r^(_=(_<<16|_>>>16)+e<<0))+h[10]-1094730640,$+=((i=(t=(t<<23|t>>>9)+_<<0)^_)^e)+h[13]+681279174,e+=(i^($=($<<4|$>>>28)+t<<0))+h[0]-358537222,_+=((r=(e=(e<<11|e>>>21)+$<<0)^$)^t)+h[3]-722521979,t+=(r^(_=(_<<16|_>>>16)+e<<0))+h[6]+76029189,$+=((i=(t=(t<<23|t>>>9)+_<<0)^_)^e)+h[9]-640364487,e+=(i^($=($<<4|$>>>28)+t<<0))+h[12]-421815835,_+=((r=(e=(e<<11|e>>>21)+$<<0)^$)^t)+h[15]+530742520,t+=(r^(_=(_<<16|_>>>16)+e<<0))+h[2]-995338651,t=(t<<23|t>>>9)+_<<0,$+=(_^(t|~e))+h[0]-198630844,$=($<<6|$>>>26)+t<<0,e+=(t^($|~_))+h[7]+1126891415,e=(e<<10|e>>>22)+$<<0,_+=($^(e|~t))+h[14]-1416354905,_=(_<<15|_>>>17)+e<<0,t+=(e^(_|~$))+h[5]-57434055,t=(t<<21|t>>>11)+_<<0,$+=(_^(t|~e))+h[12]+1700485571,$=($<<6|$>>>26)+t<<0,e+=(t^($|~_))+h[3]-1894986606,e=(e<<10|e>>>22)+$<<0,_+=($^(e|~t))+h[10]-1051523,_=(_<<15|_>>>17)+e<<0,t+=(e^(_|~$))+h[1]-2054922799,t=(t<<21|t>>>11)+_<<0,$+=(_^(t|~e))+h[8]+1873313359,$=($<<6|$>>>26)+t<<0,e+=(t^($|~_))+h[15]-30611744,e=(e<<10|e>>>22)+$<<0,_+=($^(e|~t))+h[6]-1560198380,_=(_<<15|_>>>17)+e<<0,t+=(e^(_|~$))+h[13]+1309151649,t=(t<<21|t>>>11)+_<<0,$+=(_^(t|~e))+h[4]-145523070,$=($<<6|$>>>26)+t<<0,e+=(t^($|~_))+h[11]-1120210379,e=(e<<10|e>>>22)+$<<0,_+=($^(e|~t))+h[2]+718787259,_=(_<<15|_>>>17)+e<<0,t+=(e^(_|~$))+h[9]-343485551,t=(t<<21|t>>>11)+_<<0,this.first?(this.h0=$+1732584193<<0,this.h1=t-271733879<<0,this.h2=_-1732584194<<0,this.h3=e+271733878<<0,this.first=!1):(this.h0=this.h0+$<<0,this.h1=this.h1+t<<0,this.h2=this.h2+_<<0,this.h3=this.h3+e<<0)},v.prototype.hex=function(){this.finalize();var $=this.h0,t=this.h1,_=this.h2,e=this.h3;return s[$>>>4&15]+s[15&$]+s[$>>>12&15]+s[$>>>8&15]+s[$>>>20&15]+s[$>>>16&15]+s[$>>>28&15]+s[$>>>24&15]+s[t>>>4&15]+s[15&t]+s[t>>>12&15]+s[t>>>8&15]+s[t>>>20&15]+s[t>>>16&15]+s[t>>>28&15]+s[t>>>24&15]+s[_>>>4&15]+s[15&_]+s[_>>>12&15]+s[_>>>8&15]+s[_>>>20&15]+s[_>>>16&15]+s[_>>>28&15]+s[_>>>24&15]+s[e>>>4&15]+s[15&e]+s[e>>>12&15]+s[e>>>8&15]+s[e>>>20&15]+s[e>>>16&15]+s[e>>>28&15]+s[e>>>24&15]},v.prototype.toString=v.prototype.hex,v.prototype.digest=function(){this.finalize();var $=this.h0,t=this.h1,_=this.h2,e=this.h3;return[255&$,$>>>8&255,$>>>16&255,$>>>24&255,255&t,t>>>8&255,t>>>16&255,t>>>24&255,255&_,_>>>8&255,_>>>16&255,_>>>24&255,255&e,e>>>8&255,e>>>16&255,e>>>24&255]},v.prototype.array=v.prototype.digest,v.prototype.arrayBuffer=function(){this.finalize();var $=new ArrayBuffer(16),t=new Uint32Array($);return t[0]=this.h0,t[1]=this.h1,t[2]=this.h2,t[3]=this.h3,$},v.prototype.buffer=v.prototype.arrayBuffer,v.prototype.base64=function(){for(var $,t,_,e="",i=this.array(),r=0;r<15;)$=i[r++],t=i[r++],_=i[r++],e+=x[$>>>2]+x[($<<4|t>>>4)&63]+x[(t<<2|_>>>6)&63]+x[63&_];return e+(x[($=i[r])>>>2]+x[$<<4&63]+"==")},b.prototype=new v,b.prototype.finalize=function(){if(v.prototype.finalize.call(this),this.inner){this.inner=!1;var $=this.array();v.call(this,this.sharedMemory),this.update(this.oKeyPad),this.update($),v.prototype.finalize.call(this)}};var w=function(){var $=y("hex");i&&($=d($)),$.create=function(){return new v},$.update=function(t){return $.create().update(t)};for(var t=0;t<o.length;++t){var _=o[t];$[_]=y(_)}return $}();w.md5=w,w.md5.hmac=function(){var $=l("hex");$.create=function($){return new b($)},$.update=function(t,_){return $.create(t).update(_)};for(var t=0;t<o.length;++t){var _=o[t];$[_]=l(_)}return $}(),window.md5=w}();