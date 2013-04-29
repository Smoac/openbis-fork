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
//  CISDOBAuthenticationChallengeConfirmationDialog.m
//  openBIS
//
//  Created by Ramakrishnan  Chandrasekhar on 1/9/13.
//

#import "CISDOBAuthenticationChallengeConfirmationDialog.h"
#import "CISDOBShared.h"
#import "CISDOBAsyncCall.h"

@implementation CISDOBAuthenticationChallengeConfirmationDialog

- (id)initWithCall:(CISDOBAsyncCall *)call challenge:(NSURLAuthenticationChallenge *)challenge
{
    if (!(self = [super init])) return nil;
    
    _call = call;
    _challenge = challenge;
    _challenges = [NSMutableArray array];
    [_challenges addObject: challenge];
    
    [self initializeAlertView];
    
    return self;
}

- (void)initializeAlertView
{
    NSString *message =
        [NSString stringWithFormat: @"The certificate for this server is untrusted. If you trust the host %@, select Trust to continue. Selecting Cancel will abort the connection to the server.", _challenge.protectionSpace.host];
    _alertView =
        [[UIAlertView alloc]
            initWithTitle: @"Untrusted Certificate"
            message: message
            delegate: self
            cancelButtonTitle: @"Cancel"
            otherButtonTitles: @"Trust", nil
        ];
}

- (void)addChallenge:(NSURLAuthenticationChallenge *)challenge
{
    [_challenges addObject: challenge];
}

- (void)start
{
    [_alertView show];
}


// UIAlertViewDelegate
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    BOOL didTrust = buttonIndex != alertView.cancelButtonIndex;
    if (didTrust) {
        for (NSURLAuthenticationChallenge *challenge in _challenges)
            [_call trustProtectionSpaceForAuthenticationChallenge: challenge];
    } else {
        for (NSURLAuthenticationChallenge *challenge in _challenges)
            [challenge.sender continueWithoutCredentialForAuthenticationChallenge: challenge];
    }
    
    if (SHOULD_CALL_DELEGATE_SELECTOR(didDismissConfirmationDialog:trusting:)) {
        [self.delegate didDismissConfirmationDialog: self trusting: didTrust];
    }
}

@end
