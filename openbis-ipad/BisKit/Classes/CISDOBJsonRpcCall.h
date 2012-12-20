/*
 * Copyright 2012 ETH Zuerich, CISD
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
//
//  CISDOBJsonRpcCall.h
//  BisMac
//
//  Created by cramakri on 27.08.12.
//
//

#import <Foundation/Foundation.h>
#import "CISDOBShared.h"
#import "CISDOBAsyncCall.h"


/**
 *  \brief A call to a JSON-RPC service. 
 * 
 *  The design is based on the jquery ajaxRequest object.
 *  This object makes a call to a JSON-RPC service and invokes
 *  the success or fail block as a result as appropriate.
 * 
 *  The fail block is called when there are problems either 
 *  with the invocation/transport or if the json-rpc call resulted 
 *  in an error. If the problem was with the transport, the error 
 *  will probably be in the NSURLErrorDomain. If the call returned
 *  an error, the error will be in the CISOBJsonRpcErrorDomain domain.
 *  See the documentation of the CISOBJsonRpcErrorCode enum to see what is
 *  in the userInfo dictionary.
 * 
 *  If the call was successful and returned a non-error result, the success block is called.
 *
 *  All properties are expected to be non-nil before start is called, 
 *  unless it is nil values are explicitly allowed. In particular, success, and fail are expected to be non-nil.
 */
@interface CISDOBJsonRpcCall : CISDOBAsyncCall {
@private
    // Exposed state
    NSURL           *_url;
    NSString        *_method;
    NSArray         *_params;
    NSTimeInterval  _timeoutInterval;
    id              _delegate;

    // Internal state
    NSMutableData   *_responseData;
    NSURLConnection *_connection;
}

// JSON-RPC
@property(strong) NSURL *url;           //!< The URL that implements the JSON-RPC service
@property(strong) NSString *method;     //!< The method to call
@property(strong) NSArray *params;      //!< The method parameters

// Configuration
@property(strong) id delegate;                      //!< The delegate to receive progress notifications. Can be nil.

// Actions
- (void)start;  //!< Make the JSON-RPC call (asynchronously).

@end

//
//! The interface that delegates implement
//
@interface NSObject (CISDOBJsonRpcCallDelegate)

//! Called when the call is sent over https to a server with a self-signed certificate. 
//! If the host can be trusted, the call will continue, otherwise it will fail
- (BOOL)jsonRpcCall:(CISDOBJsonRpcCall *)call canTrustHost:(NSString *)host;

@end
