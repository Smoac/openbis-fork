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

@implementation CISDOBAppDelegate

@synthesize rootOpenBisModel = _rootOpenBisModel;
@synthesize serviceManager = _serviceManager;

#pragma mark - User management

- (NSString *)username { return @"admin"; }
- (NSString *)password { return @"password"; }
- (NSURL *)openbisUrl { return [NSURL URLWithString: @"https://localhost:8443"]; }

- (void)configureControllers;
{
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        UISplitViewController *splitViewController = (UISplitViewController *)self.window.rootViewController;
        UINavigationController *navigationController = [splitViewController.viewControllers lastObject];
        splitViewController.delegate = (id)navigationController.topViewController;
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

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // Initialize the controller
    [self configureControllers];
    CISDOBMasterViewController *controller = [self masterViewController];
    controller.openBisModel = self.rootOpenBisModel;

    // Initialize the connection to openBIS
    CISDOBAsyncCall *call = [self.serviceManager loginUser: [self username] password: [self password]];
    call.success = ^(id result) {
        [controller didConnectServiceManager: self.serviceManager];
    };
    [call start];    
    
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
    
    NSURL *storeUrl = [[self applicationDocumentsDirectory] URLByAppendingPathComponent:@"openBISData.sqlite"];
    NSURL *openbisUrl = [self openbisUrl];
    
    NSError *error;
    _serviceManager =
        [[CISDOBIpadServiceManager alloc]
            initWithStoreUrl: storeUrl openbisUrl: openbisUrl trusted: YES error: &error];
    
    if (!_serviceManager) {
        // TODO Implement error handling
        NSLog(@"Unresolved error -- could not create service manager %@, %@", error, [error userInfo]);
        abort();
    }
    
    return _serviceManager;
}

- (CISDOBOpenBisModel *)rootOpenBisModel
{
    if (_rootOpenBisModel != nil) return _rootOpenBisModel;
    
    _rootOpenBisModel = [[CISDOBOpenBisModel alloc] init];
    _rootOpenBisModel.managedObjectContext = self.managedObjectContext;
    _rootOpenBisModel.serviceManager = self.serviceManager;
    
    return _rootOpenBisModel;
}


#pragma mark - Application's Documents directory

// Returns the URL to the application's Documents directory.
- (NSURL *)applicationDocumentsDirectory
{
    return [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
}

@end
