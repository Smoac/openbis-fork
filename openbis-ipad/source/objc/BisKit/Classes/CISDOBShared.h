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
//  CISDOBShared.h
//  BisMac
//
//  Created by cramakri on 28.08.12.
//
//

//
// Shared declarations.
//

//! A block that is invoked when the call succeeds
typedef void (^SuccessBlock)(id result);

//! A block that is invoked when the call fails
typedef void (^FailBlock)(NSError* error);

//
// Errors
// 

//! The error domain for errors in the JSON-RPC layer
FOUNDATION_EXPORT NSString *const CISOBJsonRpcErrorDomain;

//! The key in the error userInfo dictionary that contains the JSON-RPC error object
FOUNDATION_EXPORT NSString *const CISOBJsonRpcErrorObjectKey;

//! The key in the error userInfo dictionary that contains the JSON-RPC response object
FOUNDATION_EXPORT NSString *const CISOBJsonRpcResponseObjectKey;

enum CISOBJsonRpcErrorCode {
    kCISOBJsonRpcError_CouldNotConnectToServer = 1,
    kCISOBJsonRpcError_CouldNotSerializeRequestToJson = 2,    
    kCISOBJsonRpcError_CouldNotParseResponse = 3,
    
    //! The userInfo dictionary of the error includes the key
    //!   CISOBJsonRpcResponseObjectKey -> the entire response object
    kCISOBJsonRpcError_UnknownResponse = 4,

    //! The userInfo dictionary of the error includes the keys 
    //!   CISOBJsonRpcResponseObjectKey -> the entire response object 
    //!   CISOBJsonRpcErrorObjectKey -> the error object
    kCISOBJsonRpcError_CallReturnedError = 5,
};

//! Return true if the error is "Could not connect to server" (Domain=NSURLErrorDomain Code=-1004
BOOL IsCouldNotConnectToServerError(NSError *error);

//! Return true if the error is "Could not connect to server" (Domain=NSURLErrorDomain Code=-1009
BOOL IsInternetConnectionOfflineError(NSError *error);

//! Return true either IsCouldNotConnectToServerError or IsInternetConnectionOfflineError
BOOL IsSomeKindOfNetworkConnectionError(NSError *error);

//
// Shared preprocessor macros
//
#define SHOULD_CALL_DELEGATE_SELECTOR(_selector) (self.delegate != nil && [self.delegate respondsToSelector: @selector(_selector)])