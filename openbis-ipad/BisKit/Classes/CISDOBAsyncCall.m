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
//  CISDOBAsyncCall.m
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 9/25/12.
//
//

#import "CISDOBAsyncCall.h"

@implementation CISDOBAsyncCall

- (void)subclassResponsibility
{
    NSException* exception = [NSException exceptionWithName: NSInvalidArgumentException reason: @"Subclass Responsibility" userInfo: nil];
    @throw exception;
}

- (void)start { [self subclassResponsibility]; }


- (void)trustProtectionSpaceForAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge
{
    // Tell the connection to trust this host
    NSURLCredential *credential = [NSURLCredential credentialForTrust: challenge.protectionSpace.serverTrust];
    [challenge.sender useCredential: credential forAuthenticationChallenge: challenge];
    return;
}

@end
