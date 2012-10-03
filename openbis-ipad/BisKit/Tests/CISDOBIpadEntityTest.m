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
//  CISDOBIpadEntityTest.m
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 10/2/12.
//

#import "CISDOBIpadEntityTest.h"
#import "CISDOBIpadService.h"
#import "CISDOBConnection.h"
#import "CISDOBIpadEntity.h"

NSManagedObjectContext* GetDatabaseManagedObjectContext(NSURL* storeURL, NSError** error)
{
	NSManagedObjectModel* mom = [NSManagedObjectModel mergedModelFromBundles: nil];
	
	// Explicitly specify which db schema we want to use
//	NSBundle* bundle = [NSBundle bundleForClass: [CISDOBIpadEntity class]];
//	NSString* momPath = [bundle pathForResource: @"persistent-data-model" ofType: @"mom"];
//	NSLog(@"Mom %@", momPath);
//	NSManagedObjectModel* mom = [[NSManagedObjectModel alloc] initWithContentsOfURL: [NSURL fileURLWithPath: momPath]];

	NSManagedObjectContext* moc = [[NSManagedObjectContext alloc] init];
	NSPersistentStoreCoordinator* coordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel: mom];
	[moc setPersistentStoreCoordinator: coordinator];	
	NSPersistentStore* store = 
		[coordinator 
			addPersistentStoreWithType: NSSQLiteStoreType 
			configuration: nil 
			URL: storeURL
			options: nil 
			error: error];
	if (!store) {
		return nil;
	}
	return moc;
}

@implementation CISDOBIpadEntityTest

- (void)setUp
{
    [super setUp];
    NSURL *url = [NSURL URLWithString: @"https://localhost:8443"];
    CISDOBLiveConnection *connection = [[CISDOBLiveConnection alloc] initWithUrl: url trusted: YES];
    self.service = [[CISDOBIpadService alloc] initWithConnection: connection];
    [connection release];
    NSURL *tempDir = [NSURL fileURLWithPath: NSTemporaryDirectory()];
    self.databaseUrl = [tempDir URLByAppendingPathComponent: @"ipad-test.db"];
    NSError* error;
    self.moc = GetDatabaseManagedObjectContext(self.databaseUrl, &error);
    STAssertNil(error, @"DB file could not be created:\n%@", error);
}

- (void)tearDown
{
    self.service = nil;
    self.moc = nil;
    [[NSFileManager defaultManager] removeItemAtURL: self.databaseUrl error: nil];
    self.databaseUrl = nil;
    [super tearDown];
}

@end
