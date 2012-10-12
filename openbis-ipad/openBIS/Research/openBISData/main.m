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
//  main.m
//  openBISData
//
//  Created by Ramakrishnan  Chandrasekhar on 10/3/12.
//  Copyright (c) 2012 ETHZ, CISD. All rights reserved.
//
//  Make a database for use in the iPad app. This is just for testing purposes.
//

#import "CISDOBIpadEntity.h"
#import "CISDOBConnection.h"
#import "CISDOBAsyncCall.h"
#import "CISDOBIpadService.h"

static NSManagedObjectModel *managedObjectModel()
{
    static NSManagedObjectModel *model = nil;
    if (model != nil) {
        return model;
    }
    
    NSString *path = @"openBISData";
    path = [path stringByDeletingPathExtension];
    NSURL *modelURL = [NSURL fileURLWithPath:[path stringByAppendingPathExtension:@"momd"]];
    model = [[NSManagedObjectModel alloc] initWithContentsOfURL:modelURL];
    
    return model;
}

static NSManagedObjectContext *managedObjectContext()
{
    static NSManagedObjectContext *context = nil;
    if (context != nil) {
        return context;
    }

    @autoreleasepool {
        context = [[NSManagedObjectContext alloc] init];
        
        NSPersistentStoreCoordinator *coordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:managedObjectModel()];
        [context setPersistentStoreCoordinator:coordinator];
        
        NSString *STORE_TYPE = NSSQLiteStoreType;
        
        NSString *path = [[NSProcessInfo processInfo] arguments][0];
        path = [path stringByDeletingPathExtension];
        NSURL *url = [NSURL fileURLWithPath:[path stringByAppendingPathExtension:@"sqlite"]];
        
        NSError *error;
        NSPersistentStore *newStore = [coordinator addPersistentStoreWithType:STORE_TYPE configuration:nil URL:url options:nil error:&error];
        
        if (newStore == nil) {
            NSLog(@"Store Configuration Failure %@", ([error localizedDescription] != nil) ? [error localizedDescription] : @"Unknown Error");
        }
    }
    return context;
}

NSManagedObjectContext* GetDatabaseManagedObjectContext(NSURL* storeURL, NSError** error)
{
	// Explicitly specify which db schema we want to use
	NSBundle* bundle = [NSBundle bundleForClass: [CISDOBIpadEntity class]];
	NSString* momPath = [bundle pathForResource: @"persistent-data-model" ofType: @"momd"];
	NSManagedObjectModel* mom = [[NSManagedObjectModel alloc] initWithContentsOfURL: [NSURL fileURLWithPath: momPath]];

	NSManagedObjectContext* moc = [[NSManagedObjectContext alloc] init];
	NSPersistentStoreCoordinator* coordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel: mom];
	[moc setPersistentStoreCoordinator: coordinator];	
	NSPersistentStore* store = [coordinator addPersistentStoreWithType: NSSQLiteStoreType configuration: nil URL: storeURL options: nil error: error];
	if (!store) {
		return nil;
	}
	return moc;
}

id RunCallSynchronously(CISDOBAsyncCall *call, int seconds)
{
    BOOL __block callCompleted = NO;
    id __block callResult = nil;
    SuccessBlock success = ^(id result) {
        callCompleted = YES;
        callResult = result;
    };
    
    FailBlock fail = ^(NSError *error) {
        callCompleted = YES;
        callResult = nil;
    };
    call.success = success;
    call.fail = fail;
    
    [call start];
    
    for(int i = 0; i < seconds && !callCompleted; ++i) {
        // Run the runloop until an answer is returned
        CFRunLoopRunInMode(kCFRunLoopDefaultMode, 1, 0);
    }
    
    return callResult;
}

void InitializeDatabase(NSManagedObjectContext *moc, NSError **error)
{
    NSURL *url = [NSURL URLWithString: @"https://localhost:8443"];
    CISDOBLiveConnection *connection = [[CISDOBLiveConnection alloc] initWithUrl: url trusted: YES];
    CISDOBIpadService *service = [[CISDOBIpadService alloc] initWithConnection: connection];
    int waitTime = ((int) service.connection.timeoutInterval) * 2;
    
    CISDOBAsyncCall *call;
    call = [service loginUser: @"admin" password: @"password"];
    RunCallSynchronously(call, waitTime);
    
    call = [service listAllEntities];
    NSArray *rawEntities = RunCallSynchronously(call, waitTime);
    
       for (CISDOBIpadRawEntity *rawEntity in rawEntities) {
        // Create new entities in the moc, and store them.
       	CISDOBIpadEntity *entity = [NSEntityDescription insertNewObjectForEntityForName: @"CISDOBIpadEntity" inManagedObjectContext: moc];
        [entity initializeFromRawEntity: rawEntity];
    }
}

int main(int argc, const char * argv[])
{

    @autoreleasepool {
        // Create the managed object context
        NSError *error;
        // This is just hard-coded
        NSURL *databaseUrl = [NSURL fileURLWithPath: @"/Users/cramakri/_local/git/openbis-ipad/openBIS/Research/openBISData.sqlite"];
        NSManagedObjectContext *moc = GetDatabaseManagedObjectContext(databaseUrl, &error);
        if (!moc) {
            NSLog(@"Could not create database %@", ([error localizedDescription] != nil) ? [error localizedDescription] : error);
            exit(1);
        }
    
        NSLog(@"START Init DB");
        NSDate *start = [NSDate date];
        InitializeDatabase(moc, &error);
        
        // Save the managed object context
        if (![moc save:&error]) {
            NSLog(@"Error while saving %@", ([error localizedDescription] != nil) ? [error localizedDescription] : @"Unknown Error");
            exit(1);
        }
        NSDate *end = [NSDate date];
        NSLog(@"END  Init DB (%.2f sec) %@", [end timeIntervalSinceDate: start], databaseUrl);
    }
    return 0;
}

