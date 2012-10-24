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
        
        // Make sure that the children collection is parsable
        if (rawEntity.children && [rawEntity.children length] > 2) {
            STAssertTrue([entity.childrenPermIds count] > 0, @"%@ should result in an entity with children", rawEntity.children);
        }
    }
    
    NSError *error;
	STAssertTrue([_moc save: &error], @"Could not save data %@", error);
}

- (void)checkUpdateRawEntities:(NSArray *)rawEntities
{
    STAssertTrue([rawEntities count] > 0, @"The Pad service should have returned some entities.");

    NSEntityDescription *entity = [NSEntityDescription entityForName: @"CISDOBIpadEntity" inManagedObjectContext: _moc];
    NSManagedObjectModel *model = [entity managedObjectModel];
    NSError *error = nil;

    for (CISDOBIpadRawEntity *rawEntity in rawEntities) {
        // Find existing entities in the moc, creating if necessary, and store them.
        CISDOBIpadEntity *entity;
        NSDictionary *fetchVariables = [NSDictionary dictionaryWithObject: [NSArray arrayWithObject: rawEntity.permId] forKey: @"PERM_IDS"];
        NSFetchRequest *request = [model fetchRequestFromTemplateWithName: @"EntitiesByPermIds" substitutionVariables: fetchVariables];
        NSArray *matchedEntities = [_moc executeFetchRequest: request error: &error];
        STAssertNil(error, @"Encountered error retreiving entities %@", error);
        if ([matchedEntities count] > 0) {
            entity = [matchedEntities objectAtIndex: 0];
            [entity updateFromRawEntity: rawEntity];
        } else {
            entity = [NSEntityDescription insertNewObjectForEntityForName: @"CISDOBIpadEntity" inManagedObjectContext: _moc];
            [entity initializeFromRawEntity: rawEntity];
        }

        // Make sure that the children collection is parsable
        if (rawEntity.children && [rawEntity.children length] > 2) {
            STAssertTrue([entity.childrenPermIds count] > 0, @"%@ should result in an entity with children", rawEntity.children);
        }
    }

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

- (void)checkFindingChildren
{
	NSError* error;
	NSFetchRequest* request = [[NSFetchRequest alloc] init];
	NSEntityDescription* entity = [NSEntityDescription entityForName: @"CISDOBIpadEntity" inManagedObjectContext: _moc];
	[request setEntity: entity];
	NSArray* allEntities = [_moc executeFetchRequest: request error: &error];
    
    NSManagedObjectModel *model = [entity managedObjectModel];
    for (CISDOBIpadEntity *entity in allEntities) {
        if ([entity.childrenPermIds count] > 0) {
            NSDictionary *fetchVariables = [NSDictionary dictionaryWithObject: entity.childrenPermIds forKey: @"PERM_IDS"];
            request = [model fetchRequestFromTemplateWithName: @"EntitiesByPermIds" substitutionVariables: fetchVariables];
            NSArray *children = [_moc executeFetchRequest: request error: &error];
            STAssertTrue([entity.childrenPermIds count] == [children count], @"Entity children %@ should resolve correctly. Found instead the following %@", entity.childrenPermIds, children);
        }
    }

}

- (void)collectAllPermIds:(NSMutableArray *)permIds refcons:(NSMutableArray *)refcons
{
	NSError* error;
	NSFetchRequest* request = [[NSFetchRequest alloc] init];
	NSEntityDescription* entity = [NSEntityDescription entityForName: @"CISDOBIpadEntity" inManagedObjectContext: _moc];
    [request setEntity: entity];
	NSArray* elements = [_moc executeFetchRequest: request error: &error];
    for (CISDOBIpadEntity *entity in elements) {
        [permIds addObject: entity.permId];
        [refcons addObject: entity.refcon];
    }
}

- (void)collectAllDrillablePermIds:(NSMutableArray *)permIds refcons:(NSMutableArray *)refcons
{
	NSError* error;
	NSFetchRequest* request = [[NSFetchRequest alloc] init];
	NSEntityDescription* entity = [NSEntityDescription entityForName: @"CISDOBIpadEntity" inManagedObjectContext: _moc];
    [request setEntity: entity];
	NSArray* elements = [_moc executeFetchRequest: request error: &error];
    for (CISDOBIpadEntity *entity in elements) {
        if ([entity.childrenPermIds count] > 0) {
            [permIds addObject: entity.permId];
            [refcons addObject: entity.refcon];
        }
    }
}

- (void)testPersistEntities
{
    CISDOBAsyncCall *call;
    call = [_service loginUser: GetDefaultUserName() password: GetDefaultUserPassword()];
    [self configureAndRunCallSynchronously: call];
    
    call = [_service listRootLevelEntities];
    [self configureAndRunCallSynchronously: call];
    
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    
    // Check that we can persist the entities
    [self checkPersistRawEntities: _callResult];
    
    // Check that the cardnality is equal
    [self checkEntityCardnalityEquals: [_callResult count]];

    // Get drill information on all entities
    NSMutableArray *permIds = [NSMutableArray array];
    NSMutableArray *refcons = [NSMutableArray array];
    [self collectAllDrillablePermIds: permIds refcons: refcons];
    call = [_service drillOnEntities: permIds refcons: refcons];
    [self configureAndRunCallSynchronously: call];
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    [self checkUpdateRawEntities: _callResult];
    
    
    
    // Check that the children could be found
    [self checkFindingChildren];
}

@end
