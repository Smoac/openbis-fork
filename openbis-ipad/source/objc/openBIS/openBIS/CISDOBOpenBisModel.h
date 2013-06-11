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
//  CISDOBModel.h
//  openBIS
//
//  Created by Ramakrishnan  Chandrasekhar on 10/12/12.
//

#import <Foundation/Foundation.h>
#import "CISDOBShared.h"

@class CISDOBIpadEntity, CISDOBIpadServiceManager, CISDOBAppDelegate;

/**
 * \brief A model for the interaction with openBIS.
 */
@interface CISDOBOpenBisModel : NSObject <NSFetchedResultsControllerDelegate> {
    CISDOBIpadEntity *_selectedObject;
}

@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
@property (readonly) NSManagedObjectContext *managedObjectContext;
@property (weak, nonatomic) id <NSFetchedResultsControllerDelegate> delegate;
@property (weak, nonatomic) CISDOBAppDelegate *appDelegate;

@property (weak, nonatomic) CISDOBOpenBisModel *parentModel;
@property (readonly) CISDOBIpadServiceManager *serviceManager;
@property (strong, nonatomic) CISDOBIpadEntity *selectedObject;
@property (readonly, getter=isOnline) BOOL  online;

// Search
@property (strong, nonatomic) NSString *searchString;
@property (readonly) NSArray *searchScopeTitles;
@property (nonatomic) NSInteger selectedSearchScopeIndex;
@property (readonly) id selectedSearchDomain; //!< Return the selected search scope domain or nil if the selected domain is filter
- (BOOL)isSearchSupported;
- (BOOL)isSelectedSearchScopeIndexSearch; //!< Return YES if the searchScopeIndex refers to a search, false if it refers to filter

// Initialize
- (id)initWithParentModel:(CISDOBOpenBisModel *)parentModel; //!< The designated initializer

// Model
- (NSInteger)numberOfSections; //!< Get the number of categories for the current selection
- (NSInteger)numberOfEntitiesInSection:(NSInteger)section;
- (NSString *)titleForHeaderInSection:(NSInteger)section;
- (CISDOBIpadEntity *)objectAtIndexPath:(NSIndexPath *)indexPath;
- (void)refreshResults;

// Selection

//! Select the object and return it
- (CISDOBIpadEntity *)selectObjectAtIndexPath:(NSIndexPath *)indexPath;
- (BOOL)selectionHasChildren; //!< Return YES if the selected object has children in the selection context
- (BOOL)entityHasChildren:(CISDOBIpadEntity *)entity; //!< Return YES if the entity has children in the selection context.

// Actions
- (BOOL)insertNewObjectOrError:(NSError **)error; //!< Return YES if operation succeeded
- (BOOL)deleteObjectAtIndexPath:(NSIndexPath *)indexPath error:(NSError **)error; //!< Return YES if operation succeeded

// Utilities
- (NSURL *)urlFromUrlString:(NSString *)urlString;

// Server Communication
- (void)refreshParentFromServer:(SuccessBlock)success;
- (void)refreshFromServer:(SuccessBlock)success;

- (void)syncRootEntities:(SuccessBlock)success;

//! Get the full selected object from the server and invoke the success block when the data is here
- (void)syncSelectedObjectForDetailOnSuccess:(SuccessBlock)success;

//! Get the data from the selected object necessary for navigation from the server and invoke the success block when the data is here
- (void)syncSelectedObjectForNavigationOnSuccess:(SuccessBlock)success;

//! Invoke the server-side search and call the success block with an array of ipad entity objects when the data is here
- (void)searchServerOnSuccess:(SuccessBlock)success;


@end
