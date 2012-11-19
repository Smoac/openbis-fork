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
//  CISDOBLoginViewController.m
//  openBIS
//
//  Created by Ramakrishnan  Chandrasekhar on 11/15/12.
//

#import "CISDOBLoginViewController.h"
#import "CISDOBDetailViewController.h"
#import "CISDOBOpenBisModel.h"
#import "CISDOBAppDelegate.h"

@implementation CISDOBLoginViewController

- (void)initializeField:(UITextField *)field value:(NSString *)value
{
    if (nil != value && [value length] > 0)
        [field setText: value];
}

- (NSString *)valueFromTextField:(UITextField *)field
{
    NSString *value = [field text];
    if (nil != value && [value length] > 0) return value;
    return nil;
}


- (void)viewDidLoad
{
    [super viewDidLoad];
    
    CISDOBAppDelegate *appDelegate = self.appDelegate;
    
    NSString *username = appDelegate.username;
    NSString *password = appDelegate.password;
    NSURL *openbisUrl = appDelegate.openbisUrl;
    
    [self initializeField: self.usernameTextField value: username];
    [self initializeField: self.passwordTextField value: password];
    [self initializeField: self.urlTextField value: [openbisUrl absoluteString]];

}

- (IBAction)demoClicked:(id)sender
{
    [self.appDelegate loginControllerDidComplete: self];
}

- (IBAction)loginClicked:(id)sender
{
    NSString *username = [self valueFromTextField: self.usernameTextField];
    NSString *password = [self valueFromTextField: self.passwordTextField];
    NSString *urlString = [self valueFromTextField: self.urlTextField];
    NSURL *openbisUrl = (urlString) ? [NSURL URLWithString: urlString] : nil;
    
    CISDOBAppDelegate *appDelegate = self.appDelegate;
    [appDelegate verifyLoginURL: openbisUrl username: username password: password sender: self];
}

- (void)showError:(NSError *)error
{
    NSString *errorText = [[error userInfo] valueForKey: NSLocalizedDescriptionKey];
    self.errorLabel.text = errorText;
}


@end
