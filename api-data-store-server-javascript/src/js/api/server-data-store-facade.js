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
	this.datastoreUrl = this.normalizeUrl(datastoreUrlOrNull, httpServerUri);
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
							reject("Client error HTTP response. Unsupported content-type received.");
							break;
						}
					}
				} else if (status >= 400 && status < 600) {
					if (response.size > 0) {
						response.text().then((blobResponse) => reject(JSON.parse(blobResponse).error[1].message))
							.catch((error) => reject(error));
					} else {
						reject(xhr.statusText);
					}
				} else {
					reject("ERROR: " + xhr.responseText);
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
			reject(response.error[1].message);
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

const encodeParams = p =>  Object.entries(p).map(kv => kv.map(encodeURIComponent).join("=")).join("&");

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
 * @param {int} offset offset from whoch to start reading
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

/** Helper function to convert string md5Hash into an array. */
function hex2a(hexx) {
    var hex = hexx.toString(); //force conversion
    var str = '';
    for (var i = 0; i < hex.length; i += 2)
        str += String.fromCharCode(parseInt(hex.substr(i, 2), 16));
    return str;
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
		"data":  btoa(data),
		"md5Hash":  btoa(hex2a(md5(data))),
	});

	return this._internal.sendHttpRequest(
		"POST",
		"application/octet-stream",
		this._internal.datastoreUrl,
		encodeParams(params)
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

/**
 * ==================================================================================
 * MD5
 * ==================================================================================
 */

var md5 = (function(){

    /**
     * Add integers, wrapping at 2^32.
     * This uses 16-bit operations internally to work around bugs in interpreters.
     *
     * @param {number} x First integer
     * @param {number} y Second integer
     * @returns {number} Sum
     */
    function safeAdd(x, y) {
      var lsw = (x & 0xffff) + (y & 0xffff)
      var msw = (x >> 16) + (y >> 16) + (lsw >> 16)
      return (msw << 16) | (lsw & 0xffff)
    }

    /**
     * Bitwise rotate a 32-bit number to the left.
     *
     * @param {number} num 32-bit number
     * @param {number} cnt Rotation count
     * @returns {number} Rotated number
     */
    function bitRotateLeft(num, cnt) {
      return (num << cnt) | (num >>> (32 - cnt))
    }

    /**
     * Basic operation the algorithm uses.
     *
     * @param {number} q q
     * @param {number} a a
     * @param {number} b b
     * @param {number} x x
     * @param {number} s s
     * @param {number} t t
     * @returns {number} Result
     */
    function md5cmn(q, a, b, x, s, t) {
      return safeAdd(bitRotateLeft(safeAdd(safeAdd(a, q), safeAdd(x, t)), s), b)
    }
    /**
     * Basic operation the algorithm uses.
     *
     * @param {number} a a
     * @param {number} b b
     * @param {number} c c
     * @param {number} d d
     * @param {number} x x
     * @param {number} s s
     * @param {number} t t
     * @returns {number} Result
     */
    function md5ff(a, b, c, d, x, s, t) {
      return md5cmn((b & c) | (~b & d), a, b, x, s, t)
    }
    /**
     * Basic operation the algorithm uses.
     *
     * @param {number} a a
     * @param {number} b b
     * @param {number} c c
     * @param {number} d d
     * @param {number} x x
     * @param {number} s s
     * @param {number} t t
     * @returns {number} Result
     */
    function md5gg(a, b, c, d, x, s, t) {
      return md5cmn((b & d) | (c & ~d), a, b, x, s, t)
    }
    /**
     * Basic operation the algorithm uses.
     *
     * @param {number} a a
     * @param {number} b b
     * @param {number} c c
     * @param {number} d d
     * @param {number} x x
     * @param {number} s s
     * @param {number} t t
     * @returns {number} Result
     */
    function md5hh(a, b, c, d, x, s, t) {
      return md5cmn(b ^ c ^ d, a, b, x, s, t)
    }
    /**
     * Basic operation the algorithm uses.
     *
     * @param {number} a a
     * @param {number} b b
     * @param {number} c c
     * @param {number} d d
     * @param {number} x x
     * @param {number} s s
     * @param {number} t t
     * @returns {number} Result
     */
    function md5ii(a, b, c, d, x, s, t) {
      return md5cmn(c ^ (b | ~d), a, b, x, s, t)
    }

    /**
     * Calculate the MD5 of an array of little-endian words, and a bit length.
     *
     * @param {Array} x Array of little-endian words
     * @param {number} len Bit length
     * @returns {Array<number>} MD5 Array
     */
    function binlMD5(x, len) {
      /* append padding */
      x[len >> 5] |= 0x80 << len % 32
      x[(((len + 64) >>> 9) << 4) + 14] = len

      var i
      var olda
      var oldb
      var oldc
      var oldd
      var a = 1732584193
      var b = -271733879
      var c = -1732584194
      var d = 271733878

      for (i = 0; i < x.length; i += 16) {
        olda = a
        oldb = b
        oldc = c
        oldd = d

        a = md5ff(a, b, c, d, x[i], 7, -680876936)
        d = md5ff(d, a, b, c, x[i + 1], 12, -389564586)
        c = md5ff(c, d, a, b, x[i + 2], 17, 606105819)
        b = md5ff(b, c, d, a, x[i + 3], 22, -1044525330)
        a = md5ff(a, b, c, d, x[i + 4], 7, -176418897)
        d = md5ff(d, a, b, c, x[i + 5], 12, 1200080426)
        c = md5ff(c, d, a, b, x[i + 6], 17, -1473231341)
        b = md5ff(b, c, d, a, x[i + 7], 22, -45705983)
        a = md5ff(a, b, c, d, x[i + 8], 7, 1770035416)
        d = md5ff(d, a, b, c, x[i + 9], 12, -1958414417)
        c = md5ff(c, d, a, b, x[i + 10], 17, -42063)
        b = md5ff(b, c, d, a, x[i + 11], 22, -1990404162)
        a = md5ff(a, b, c, d, x[i + 12], 7, 1804603682)
        d = md5ff(d, a, b, c, x[i + 13], 12, -40341101)
        c = md5ff(c, d, a, b, x[i + 14], 17, -1502002290)
        b = md5ff(b, c, d, a, x[i + 15], 22, 1236535329)

        a = md5gg(a, b, c, d, x[i + 1], 5, -165796510)
        d = md5gg(d, a, b, c, x[i + 6], 9, -1069501632)
        c = md5gg(c, d, a, b, x[i + 11], 14, 643717713)
        b = md5gg(b, c, d, a, x[i], 20, -373897302)
        a = md5gg(a, b, c, d, x[i + 5], 5, -701558691)
        d = md5gg(d, a, b, c, x[i + 10], 9, 38016083)
        c = md5gg(c, d, a, b, x[i + 15], 14, -660478335)
        b = md5gg(b, c, d, a, x[i + 4], 20, -405537848)
        a = md5gg(a, b, c, d, x[i + 9], 5, 568446438)
        d = md5gg(d, a, b, c, x[i + 14], 9, -1019803690)
        c = md5gg(c, d, a, b, x[i + 3], 14, -187363961)
        b = md5gg(b, c, d, a, x[i + 8], 20, 1163531501)
        a = md5gg(a, b, c, d, x[i + 13], 5, -1444681467)
        d = md5gg(d, a, b, c, x[i + 2], 9, -51403784)
        c = md5gg(c, d, a, b, x[i + 7], 14, 1735328473)
        b = md5gg(b, c, d, a, x[i + 12], 20, -1926607734)

        a = md5hh(a, b, c, d, x[i + 5], 4, -378558)
        d = md5hh(d, a, b, c, x[i + 8], 11, -2022574463)
        c = md5hh(c, d, a, b, x[i + 11], 16, 1839030562)
        b = md5hh(b, c, d, a, x[i + 14], 23, -35309556)
        a = md5hh(a, b, c, d, x[i + 1], 4, -1530992060)
        d = md5hh(d, a, b, c, x[i + 4], 11, 1272893353)
        c = md5hh(c, d, a, b, x[i + 7], 16, -155497632)
        b = md5hh(b, c, d, a, x[i + 10], 23, -1094730640)
        a = md5hh(a, b, c, d, x[i + 13], 4, 681279174)
        d = md5hh(d, a, b, c, x[i], 11, -358537222)
        c = md5hh(c, d, a, b, x[i + 3], 16, -722521979)
        b = md5hh(b, c, d, a, x[i + 6], 23, 76029189)
        a = md5hh(a, b, c, d, x[i + 9], 4, -640364487)
        d = md5hh(d, a, b, c, x[i + 12], 11, -421815835)
        c = md5hh(c, d, a, b, x[i + 15], 16, 530742520)
        b = md5hh(b, c, d, a, x[i + 2], 23, -995338651)

        a = md5ii(a, b, c, d, x[i], 6, -198630844)
        d = md5ii(d, a, b, c, x[i + 7], 10, 1126891415)
        c = md5ii(c, d, a, b, x[i + 14], 15, -1416354905)
        b = md5ii(b, c, d, a, x[i + 5], 21, -57434055)
        a = md5ii(a, b, c, d, x[i + 12], 6, 1700485571)
        d = md5ii(d, a, b, c, x[i + 3], 10, -1894986606)
        c = md5ii(c, d, a, b, x[i + 10], 15, -1051523)
        b = md5ii(b, c, d, a, x[i + 1], 21, -2054922799)
        a = md5ii(a, b, c, d, x[i + 8], 6, 1873313359)
        d = md5ii(d, a, b, c, x[i + 15], 10, -30611744)
        c = md5ii(c, d, a, b, x[i + 6], 15, -1560198380)
        b = md5ii(b, c, d, a, x[i + 13], 21, 1309151649)
        a = md5ii(a, b, c, d, x[i + 4], 6, -145523070)
        d = md5ii(d, a, b, c, x[i + 11], 10, -1120210379)
        c = md5ii(c, d, a, b, x[i + 2], 15, 718787259)
        b = md5ii(b, c, d, a, x[i + 9], 21, -343485551)

        a = safeAdd(a, olda)
        b = safeAdd(b, oldb)
        c = safeAdd(c, oldc)
        d = safeAdd(d, oldd)
      }
      return [a, b, c, d]
    }

    /**
     * Convert an array of little-endian words to a string
     *
     * @param {Array<number>} input MD5 Array
     * @returns {string} MD5 string
     */
    function binl2rstr(input) {
      var i
      var output = ''
      var length32 = input.length * 32
      for (i = 0; i < length32; i += 8) {
        output += String.fromCharCode((input[i >> 5] >>> i % 32) & 0xff)
      }
      return output
    }

    /**
     * Convert a raw string to an array of little-endian words
     * Characters >255 have their high-byte silently ignored.
     *
     * @param {string} input Raw input string
     * @returns {Array<number>} Array of little-endian words
     */
    function rstr2binl(input) {
      var i
      var output = []
      output[(input.length >> 2) - 1] = undefined
      for (i = 0; i < output.length; i += 1) {
        output[i] = 0
      }
      var length8 = input.length * 8
      for (i = 0; i < length8; i += 8) {
        output[i >> 5] |= (input.charCodeAt(i / 8) & 0xff) << i % 32
      }
      return output
    }

    /**
     * Calculate the MD5 of a raw string
     *
     * @param {string} s Input string
     * @returns {string} Raw MD5 string
     */
    function rstrMD5(s) {
      return binl2rstr(binlMD5(rstr2binl(s), s.length * 8))
    }

    /**
     * Calculates the HMAC-MD5 of a key and some data (raw strings)
     *
     * @param {string} key HMAC key
     * @param {string} data Raw input string
     * @returns {string} Raw MD5 string
     */
    function rstrHMACMD5(key, data) {
      var i
      var bkey = rstr2binl(key)
      var ipad = []
      var opad = []
      var hash
      ipad[15] = opad[15] = undefined
      if (bkey.length > 16) {
        bkey = binlMD5(bkey, key.length * 8)
      }
      for (i = 0; i < 16; i += 1) {
        ipad[i] = bkey[i] ^ 0x36363636
        opad[i] = bkey[i] ^ 0x5c5c5c5c
      }
      hash = binlMD5(ipad.concat(rstr2binl(data)), 512 + data.length * 8)
      return binl2rstr(binlMD5(opad.concat(hash), 512 + 128))
    }

    /**
     * Convert a raw string to a hex string
     *
     * @param {string} input Raw input string
     * @returns {string} Hex encoded string
     */
    function rstr2hex(input) {
      var hexTab = '0123456789abcdef'
      var output = ''
      var x
      var i
      for (i = 0; i < input.length; i += 1) {
        x = input.charCodeAt(i)
        output += hexTab.charAt((x >>> 4) & 0x0f) + hexTab.charAt(x & 0x0f)
      }
      return output
    }

    /**
     * Encode a string as UTF-8
     *
     * @param {string} input Input string
     * @returns {string} UTF8 string
     */
    function str2rstrUTF8(input) {
      return unescape(encodeURIComponent(input))
    }

    /**
     * Encodes input string as raw MD5 string
     *
     * @param {string} s Input string
     * @returns {string} Raw MD5 string
     */
    function rawMD5(s) {
      return rstrMD5(str2rstrUTF8(s))
    }
    /**
     * Encodes input string as Hex encoded string
     *
     * @param {string} s Input string
     * @returns {string} Hex encoded string
     */
    function hexMD5(s) {
      return rstr2hex(rawMD5(s))
    }
    /**
     * Calculates the raw HMAC-MD5 for the given key and data
     *
     * @param {string} k HMAC key
     * @param {string} d Input string
     * @returns {string} Raw MD5 string
     */
    function rawHMACMD5(k, d) {
      return rstrHMACMD5(str2rstrUTF8(k), str2rstrUTF8(d))
    }
    /**
     * Calculates the Hex encoded HMAC-MD5 for the given key and data
     *
     * @param {string} k HMAC key
     * @param {string} d Input string
     * @returns {string} Raw MD5 string
     */
    function hexHMACMD5(k, d) {
      return rstr2hex(rawHMACMD5(k, d))
    }

    /**
     * Calculates MD5 value for a given string.
     * If a key is provided, calculates the HMAC-MD5 value.
     * Returns a Hex encoded string unless the raw argument is given.
     *
     * @param {string} string Input string
     * @param {string} [key] HMAC key
     * @param {boolean} [raw] Raw output switch
     * @returns {string} MD5 output
     */
    function md5(string, key, raw) {
      if (!key) {
        if (!raw) {
          return hexMD5(string)
        }
        return rawMD5(string)
      }
      if (!raw) {
        return hexHMACMD5(key, string)
      }
      return rawHMACMD5(key, string)
    }

    return md5;
})();

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