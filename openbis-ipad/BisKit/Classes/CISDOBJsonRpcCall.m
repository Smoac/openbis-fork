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
//  CISDOBJsonRpcCall.m
//  BisMac
//
//  Created by cramakri on 27.08.12.
//
//

#import "CISDOBJsonRpcCall.h"

NSString *const CISOBJsonRpcErrorDomain = @"CISOBJsonRpcErrorDomain";
NSString *const CISOBJsonRpcErrorObjectKey = @"CISOBJsonRpcErrorObjectKey";
NSString *const CISOBJsonRpcResponseObjectKey = @"CISOBJsonRpcResponseObjectKey";

@interface CISDOBJsonRpcCall (CISDOBJsonRpcCallPrivate)

//! Call the failure block with an error that the connection could not be created
- (void)couldNotCreateConnection;

//! Take the parameters and convert them into the body of the http invocation
- (NSData *)httpBody;

@end


@implementation CISDOBJsonRpcCall

- (id)init
{ 
    if (!(self = [super init])) return nil;
    
    // Default timeout interval to 60s
    _timeoutInterval = 60.0;
    _responseData = [[NSMutableData alloc] init];
    
    return self;
}

- (void)start
{
    // TODO: Validate the parameters and invoke the fail block if the parameters are not properly filled in.
    NSMutableURLRequest *request = 
        [NSMutableURLRequest requestWithURL: self.url cachePolicy: NSURLRequestReloadIgnoringLocalCacheData timeoutInterval: self.timeoutInterval];
        
    // Prepare the request
    NSData* httpBody = [self httpBody];
    if (!httpBody) {
        // The fail block has already been called
        return;
    }
    
    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [request setHTTPMethod:@"POST"];
#if TARGET_OS_IPHONE
    [request setValue: [NSString stringWithFormat:@"%i", [httpBody length]]  forHTTPHeaderField:@"Content-Length"];
#else
    [request setValue: [NSString stringWithFormat:@"%li", [httpBody length]]  forHTTPHeaderField:@"Content-Length"];
#endif
    
    [request setHTTPBody: httpBody];
        
    // Check that the connection can be created
    if (![NSURLConnection canHandleRequest: request]) {
        [self couldNotCreateConnection];
        return;
    } 
    
    _connection =
        [NSURLConnection connectionWithRequest: request delegate: self];
        
    if (!_connection) {
        [self couldNotCreateConnection];
        return;
    }
    
}

- (void)couldNotCreateConnection
{
    NSDictionary *userInfo =
        [NSDictionary dictionaryWithObjectsAndKeys: @"Could not connect to server", NSLocalizedDescriptionKey, nil];
    NSError *error = [NSError errorWithDomain: CISOBJsonRpcErrorDomain code: kCISOBJsonRpcError_CouldNotConnectToServer userInfo: userInfo];
    _fail(error);
}

- (NSData *)httpBody
{
    NSDictionary *bodyDict = 
        [NSDictionary dictionaryWithObjectsAndKeys: 
            _method, @"method",
            _params, @"params",
            [NSNumber numberWithInt: 1], @"id",
            @"2.0", @"version",
            nil];
    NSError *error;
    NSData *body = [NSJSONSerialization dataWithJSONObject: bodyDict options: 0 error: &error];
    if (!body) {
        _fail(error);
    }
    
    return body;
}

@end

@implementation CISDOBJsonRpcCall (NSURLConnectionDelegate)

- (BOOL)connection:(NSURLConnection *)connection canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *)protectionSpace
{
    return [protectionSpace.authenticationMethod isEqualToString: NSURLAuthenticationMethodServerTrust];
}

- (void)connection:(NSURLConnection *)connection didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge
{
    if ([challenge.protectionSpace.authenticationMethod isEqualToString: NSURLAuthenticationMethodServerTrust])
    {
        if (SHOULD_CALL_DELEGATE_SELECTOR(asyncCall:didReceiveAuthenticationChallenge:))
        {
            [_delegate asyncCall: self didReceiveAuthenticationChallenge: challenge];
            return;
        }
    }
    [challenge.sender continueWithoutCredentialForAuthenticationChallenge: challenge]; 
}

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
    [_responseData setLength: 0];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    [_responseData appendData: data];
}


- (void)handleResponseDictionary:(NSDictionary *)response
{
    // Extract the error or the result
    NSDictionary *errorDict = [response objectForKey: @"error"];
    id result = [response objectForKey: @"result"];
    if (errorDict) {
        NSString *errorMessage = [errorDict objectForKey: @"message"];
        NSDictionary *userInfo =
            [NSDictionary dictionaryWithObjectsAndKeys: 
                errorMessage, NSLocalizedDescriptionKey, 
                errorDict, CISOBJsonRpcErrorObjectKey,
                response, CISOBJsonRpcResponseObjectKey,
                nil];    
        NSError *error = [NSError errorWithDomain: CISOBJsonRpcErrorDomain code: kCISOBJsonRpcError_CallReturnedError userInfo: userInfo];
        _fail(error);
    } else if (result && _success) {
        _success(result);
    } else {
        NSDictionary *userInfo =
            [NSDictionary dictionaryWithObjectsAndKeys: 
                @"The JSON-RPC response does not match the expected structure", NSLocalizedDescriptionKey,
                response, CISOBJsonRpcResponseObjectKey,
                nil];
        NSError *error = [NSError errorWithDomain: CISOBJsonRpcErrorDomain code: kCISOBJsonRpcError_UnknownResponse userInfo: userInfo];        
        _fail(error);
    }

}
- (void)connectionDidFinishLoading:(NSURLConnection *)aConnection
{
    // Parse the data and call the appropriate block
    NSError *error;
    id response = [NSJSONSerialization JSONObjectWithData: _responseData options: 0 error: &error];
    if (!response && _fail) {
        // Oops, couldn't parse the data -- call the fail block
        _fail(error);
    } else {
        [self handleResponseDictionary: response];
    }

    _connection = nil;
}


- (void)connection:(NSURLConnection *)aConnection didFailWithError:(NSError *)error
{
    if (_fail) _fail(error);
    _connection = nil;
}


@end
