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
//  CISDOBAppDelegate.m
//  openBIS
//
//  Created by Ramakrishnan  Chandrasekhar on 10/3/12.
//  Copyright (c) 2012 ETHZ, CISD. All rights reserved.
//

#import "CISDOBAppDelegate.h"

#import "CISDOBMasterViewController.h"
#import "CISDOBOpenBisModel.h"
#import "CISDOBIpadServiceManager.h"
#import "CISDOBAsyncCall.h"
#import "CISDOBDetailViewController.h"
#import "CISDOBLoginViewController.h"
#import "CISDOBAuthenticationChallengeConfirmationDialog.h"
#import "CISDOBIpadEntity.h"

NSURL *StoreUrlFromOpenbisUrl(NSURL *applicationDocumentsDirectory, NSURL *openbisUrl)
{
    NSString *host = [openbisUrl host];
    NSNumber *port = [openbisUrl port];
    
    NSString *pathString = (port) ? [NSString stringWithFormat: @"%@_%@-data.sqlite", host, port] : [NSString stringWithFormat: @"%@-data.sqlite", host];
    NSURL *storeUrl = [applicationDocumentsDirectory URLByAppendingPathComponent: pathString];
    return storeUrl;
}

@implementation CISDOBAppDelegate

@synthesize rootOpenBisModel = _rootOpenBisModel;
@synthesize serviceManager = _serviceManager;

#pragma mark - User Settings

//- (NSString *)username { return @"admin"; }
//- (NSString *)password { return @"password"; }
//- (NSURL *)openbisUrl { return [NSURL URLWithString: @"https://localhost:8443"]; }

- (NSString *)username { return [[NSUserDefaults standardUserDefaults] stringForKey: @"openbis_username"]; }
- (NSString *)password { return [[NSUserDefaults standardUserDefaults] stringForKey: @"openbis_password"]; }
- (NSDate *)lastUpdatedDate { return [[NSUserDefaults standardUserDefaults] objectForKey: @"last_updated_date"]; }

- (NSURL *)openbisUrl {
    NSString *urlString = [[NSUserDefaults standardUserDefaults] stringForKey: @"openbis_server_url"];
    return [NSURL URLWithString: urlString];
}

- (void)setUsername:(NSString *)username
{
    return [[NSUserDefaults standardUserDefaults] setObject: username forKey: @"openbis_username"];
}
- (void)setPassword:(NSString *)password
{
    return [[NSUserDefaults standardUserDefaults] setObject: password forKey: @"openbis_password"];
}
- (void)setLastUpdatedDate:(NSDate *)lastUpdatedDate
{
    return [[NSUserDefaults standardUserDefaults] setObject: lastUpdatedDate forKey: @"last_updated_date"];
}

- (void)setOpenbisUrl:(NSURL *)openbisUrl
{
    return [[NSUserDefaults standardUserDefaults] setObject: [openbisUrl absoluteString] forKey: @"openbis_server_url"];
}

- (BOOL)synchronizeUserSettings { return [[NSUserDefaults standardUserDefaults] synchronize]; }

- (void)verifyLoginURL:(NSURL *)openbisUrl username:(NSString *)username password:(NSString *)password sender:(CISDOBLoginViewController *)controller
{
    
    NSError *error;
    if (![self initializeServiceManager: openbisUrl error: &error]) {
        [controller showError: error];
        return;
    }
    
    // Initialize the connection to openBIS
    CISDOBAsyncCall *call = [self.serviceManager loginUser: username password: password];
    __weak CISDOBAppDelegate *weakSelf = self;
    call.success = ^(id result) {
        weakSelf.username = username;
        weakSelf.password = password;
        weakSelf.openbisUrl = openbisUrl;
        [weakSelf synchronizeUserSettings];
        [weakSelf loginControllerDidComplete: controller];
        [weakSelf didConnectToServer];
    };
    call.fail = ^(NSError *error) {
        [controller showError: error];
    };
    [call start];
}

- (void)loginControllerDidComplete:(CISDOBLoginViewController *)controller
{
    [controller dismissViewControllerAnimated: YES completion: nil];
}

#pragma mark - Online Status
- (BOOL)isOnline { return self.serviceManager.online; }
- (void)goOnline { [self initializeOpenBisConnection]; }

#pragma mark - App Startup

- (void)configureControllers;
{
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        UISplitViewController *splitViewController = (UISplitViewController *)self.window.rootViewController;
        UINavigationController *navigationController = [splitViewController.viewControllers lastObject];
        splitViewController.delegate = (id)navigationController.topViewController;
        [self detailViewController].appDelegate = self;
        [self masterViewController].openBisModel = self.rootOpenBisModel;
    } else {

    }
}

- (CISDOBMasterViewController *)masterViewController
{
    CISDOBMasterViewController *controller;
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        UISplitViewController *splitViewController = (UISplitViewController *)self.window.rootViewController;
        UINavigationController *masterNavigationController = splitViewController.viewControllers[0];
        controller = (CISDOBMasterViewController *)masterNavigationController.topViewController;
    } else {
        UINavigationController *navigationController = (UINavigationController *)self.window.rootViewController;
       controller = (CISDOBMasterViewController *)navigationController.topViewController;
    }
    
    return controller;
}

- (CISDOBDetailViewController *)detailViewController
{
    UISplitViewController *splitViewController = (UISplitViewController *)self.window.rootViewController;
    return (CISDOBDetailViewController *)[[splitViewController.viewControllers lastObject] topViewController];
}

- (void)didConnectToServer
{
    CISDOBMasterViewController *controller = [self masterViewController];
    [controller didConnectServiceManager: self.serviceManager];
}

- (void)serverOffline
{
    CISDOBMasterViewController *controller = [self masterViewController];
    [controller didConnectServiceManager: self.serviceManager];
}

- (void)initializeOpenBisConnection
{
    // Initialize the connection to openBIS
    CISDOBAsyncCall *call = [self.serviceManager loginUser: [self username] password: [self password]];
    __weak CISDOBAppDelegate *weakSelf = self;
    call.success = ^(id result) {
        [weakSelf didConnectToServer];
    };
    call.fail = ^(NSError *error) {
        if (IsSomeKindOfNetworkConnectionError(error)) {
            [self serverOffline];
        } else {
            [[weakSelf detailViewController] performSegueWithIdentifier: @"ShowLoginDialog" sender: self];
        }
    };
    [call start];
}

// There is a deadlock that happens initializing the cookie storage, so do it now before
// multiple threads are running. This might only be a problem in the simulator...
- (void)cookieStorageDeadlockWorkaround { [NSHTTPCookieStorage sharedHTTPCookieStorage]; }

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    [self cookieStorageDeadlockWorkaround];
    
    // Initialize the controller
    [self configureControllers];
    
    return YES;
}
							
- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    [self initializeOpenBisConnection];
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Saves changes in the application's managed object context before the application terminates.
    [self saveContext];
}

- (void)saveContext
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    if (managedObjectContext != nil) {
        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error]) {
            // TODO Implement error handling
            NSLog(@"Unresolved error -- could not save %@, %@", error, [error userInfo]);
            abort();
        } 
    }
}

#pragma mark - Core Data stack
- (NSManagedObjectContext *)managedObjectContext { return self.serviceManager.managedObjectContext; }

- (NSManagedObjectModel *)managedObjectModel { return self.serviceManager.managedObjectModel; }

- (NSPersistentStoreCoordinator *)persistentStoreCoordinator { return self.serviceManager.persistentStoreCoordinator; }

- (CISDOBIpadServiceManager *)serviceManager
{
    if (_serviceManager) return _serviceManager;
    
    NSURL *openbisUrl = [self openbisUrl];
    
    NSError *error;
    if (![self initializeServiceManager: openbisUrl error: &error]) {
        // TODO Implement error handling
        NSLog(@"Unresolved error -- could not create service manager %@, %@", error, [error userInfo]);
        abort();
    }
    
    return _serviceManager;
}

- (BOOL)initializeServiceManager:(NSURL *)openbisUrl error:(NSError **)error
{
    
    NSURL *storeUrl = StoreUrlFromOpenbisUrl([self applicationDocumentsDirectory], openbisUrl);
        
    _serviceManager =
        [[CISDOBIpadServiceManager alloc]
            initWithStoreUrl: storeUrl openbisUrl: openbisUrl trusted: YES error: error];
    
    if (!_serviceManager) {
        // We couldn't open the store, probably because we changed the database model. Remove the old cache and create the service manager again.
        [[NSFileManager defaultManager] removeItemAtURL: storeUrl error: nil];
        
        _serviceManager =
            [[CISDOBIpadServiceManager alloc]
                initWithStoreUrl: storeUrl openbisUrl: openbisUrl trusted: YES error: error];
        if (!_serviceManager) return NO;
    }
    
    // Initialize the lastUpdateDate of the service manager from the stored state
    NSDate* lastUpdatedDate = [self lastUpdatedDate];
    _serviceManager.lastRootSetUpdateDate = lastUpdatedDate;
    
    // Use a weak reference to self in blocks to avoid retain cycles
    __weak CISDOBAppDelegate *weakSelf = self;
    _serviceManager.authenticationChallengeBlock = ^(CISDOBAsyncCall *call, NSURLAuthenticationChallenge *challenge) {
        [weakSelf presetDialogForCall: call challenge: challenge];
    };
    _serviceManager.mocSaveBlock = ^(CISDOBIpadServiceManager *serviceManager, NSArray *deletedEntityPermIds) {
        [weakSelf serviceManager: serviceManager willSaveDeletingEntities: deletedEntityPermIds];
    };
    _serviceManager.mocPostSaveBlock = ^(CISDOBIpadServiceManager *serviceManager) {
        // Update the lastUpdateDate to the time the service manager was created
        [weakSelf setLastUpdatedDate: serviceManager.lastRootSetUpdateDate];
    };
    
    return YES;
}

- (CISDOBOpenBisModel *)rootOpenBisModel
{
    if (_rootOpenBisModel != nil) return _rootOpenBisModel;
    
    _rootOpenBisModel = [[CISDOBOpenBisModel alloc] init];
    _rootOpenBisModel.appDelegate = self;
    
    return _rootOpenBisModel;
}

#pragma mark - Authentication Challenges
- (void)presetDialogForCall:(CISDOBAsyncCall *)call challenge:(NSURLAuthenticationChallenge *)challenge
{
    // We only handle server trust challenges
    if (![NSURLAuthenticationMethodServerTrust isEqualToString: challenge.protectionSpace.authenticationMethod]) {
        [challenge.sender continueWithoutCredentialForAuthenticationChallenge: challenge];
        return;
    }
    // Evalute the server trust. If it evaluates to proceed, just proceed
    SecTrustRef serverTrust = challenge.protectionSpace.serverTrust;
    SecTrustResultType serverTrustResult = -1;
    OSStatus err = SecTrustEvaluate(serverTrust, &serverTrustResult);
    if (errSecSuccess != err) {
        [challenge.sender continueWithoutCredentialForAuthenticationChallenge: challenge];
        return;
    }

    // Only the recoverable trust failure requires user confirmation
    if (kSecTrustResultRecoverableTrustFailure != serverTrustResult) {
        [challenge.sender continueWithoutCredentialForAuthenticationChallenge: challenge];
        return;
    }

    // A dialog is already being shown
    if (_challengeDialog != nil) {
        if (call == _challengeDialog.call) {
            // If it relates to the same call, bundle this challenge together with the other one
            [_challengeDialog addChallenge: challenge];
        } else {
            [challenge.sender continueWithoutCredentialForAuthenticationChallenge: challenge];
        }
        return;
    }
    
    _challengeDialog =
        [[CISDOBAuthenticationChallengeConfirmationDialog alloc]
            initWithCall: call challenge: challenge];
    _challengeDialog.delegate = self;
    [_challengeDialog start];
}

// CISDOBAuthenticationChallengeConfirmationDialogDelegate

- (void)didDismissConfirmationDialog:(CISDOBAuthenticationChallengeConfirmationDialog *)dialog trusting:(BOOL)didTrust
{
    _challengeDialog = nil;
}

#pragma mark - MOC Save Handling
- (void)serviceManager:(CISDOBIpadServiceManager *)serviceManager willSaveDeletingEntities:(NSArray *)deletedEntityPermIds
{
    // Nothing to do if nothing was deleted
    if ([deletedEntityPermIds count] < 1) return;
    
    // Check if any of the deleted entities are currently open, if so, return to the root view to avoid any problems caused by looking at zombie entities
    BOOL returnToRoot = NO;
    NSSet *permIdSet = [NSSet setWithArray: deletedEntityPermIds];
    CISDOBOpenBisModel *model;
    for (model = [self detailViewController].openBisModel; model != nil; model = model.parentModel) {
        if ([permIdSet containsObject: model.selectedObject.permId]) {
            returnToRoot = YES;
            break;
        }
    }
    
    if (returnToRoot) {
        UINavigationController *navigationController = self.masterViewController.navigationController;
        [navigationController popToRootViewControllerAnimated: YES];

        [self.masterViewController refreshTable];
        self.detailViewController.openBisModel = self.rootOpenBisModel;
        [self.detailViewController selectionDidChange];
    }
}

#pragma mark - Application's Documents directory

// Returns the URL to the application's Documents directory.
- (NSURL *)applicationDocumentsDirectory
{
    return [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
}

@end
