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
	// Explicitly specify which db schema we want to use
	NSBundle* bundle = [NSBundle bundleForClass: [CISDOBIpadEntity class]];
	NSString* momPath = [bundle pathForResource: @"persistent-data-model" ofType: @"momd"];
	NSManagedObjectModel* mom = [[NSManagedObjectModel alloc] initWithContentsOfURL: [NSURL fileURLWithPath: momPath]];

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
    NSError* error = nil;
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

- (void)configureAndRunCallSynchronously:(CISDOBAsyncCall *)call
{
    [self configureCall: call];
    
    // The ipad service may make multiple calls, so take that into account.
    int waitTime = ((int) _service.connection.timeoutInterval) * 2;
    [self waitSeconds: waitTime forCallToComplete: call];
}

- (void)checkPersistRawEntities:(NSArray *)rawEntities
{
    STAssertTrue([rawEntities count] > 0, @"The Pad service should have returned some entities.");
    
    for (CISDOBIpadRawEntity *rawEntity in rawEntities) {
        // Create new entities in the moc, and store them.
       	CISDOBIpadEntity *entity = [NSEntityDescription insertNewObjectForEntityForName: @"CISDOBIpadEntity" inManagedObjectContext: _moc];
        [entity initializeFromRawEntity: rawEntity];
    }
    
    NSError *error;
	STAssertTrue([_moc save: &error], @"Could not save data %@", error);
}

- (void)checkEntityCardnalityEquals:(NSUInteger)count
{
	NSError* error;
	NSFetchRequest* request = [[NSFetchRequest alloc] init];
	NSEntityDescription* entity = [NSEntityDescription entityForName: @"CISDOBIpadEntity" inManagedObjectContext: _moc];
	[request setEntity: entity];
	NSArray* elements = [_moc executeFetchRequest: request error: &error];
    STAssertEquals([elements count], count, @"%llu in db != %llu from server", [elements count], count);
}

- (void)testPersistEntities
{
    CISDOBAsyncCall *call;
    call = [_service loginUser: GetDefaultUserName() password: GetDefaultUserPassword()];
    [self configureAndRunCallSynchronously: call];
    
    call = [_service listAllEntities];
    [self configureAndRunCallSynchronously: call];
    
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    
    // Check that we can persist the entities
    [self checkPersistRawEntities: _callResult];
    
    // Check that the cardnality is equal
    [self checkEntityCardnalityEquals: [_callResult count]];
}

@end
