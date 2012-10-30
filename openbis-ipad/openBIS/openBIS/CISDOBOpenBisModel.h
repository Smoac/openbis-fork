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

@class CISDOBIpadEntity, CISDOBIpadServiceManager;

/**
 * \brief A model for the interaction with openBIS.
 */
@interface CISDOBOpenBisModel : NSObject <NSFetchedResultsControllerDelegate> {
    CISDOBIpadEntity *_selectedObject;
}

@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (weak, nonatomic) id <NSFetchedResultsControllerDelegate> delegate;

@property (weak, nonatomic) CISDOBOpenBisModel *parentModel;
@property (weak, nonatomic) CISDOBIpadServiceManager *serviceManager;

// Initialize
- (id)initWithParentModel:(CISDOBOpenBisModel *)parentModel; //!< The designated initializer

// Model
- (NSInteger)numberOfSections; //!< Get the number of categories for the current selection
- (NSInteger)numberOfEntitiesInSection:(NSInteger)section;
- (NSString *)titleForHeaderInSection:(NSInteger)section;
- (CISDOBIpadEntity *)objectAtIndexPath:(NSIndexPath *)indexPath;

// Selection
@property (readonly) CISDOBIpadEntity *selectedObject;

//! Select the object and return it
- (CISDOBIpadEntity *)selectObjectAtIndexPath:(NSIndexPath *)indexPath;
- (BOOL)selectionHasChildren; //!< Return YES if the selected object has children in the selection context
- (BOOL)entityHasChildren:(CISDOBIpadEntity *)entity; //!< Return YES if the entity has children in the selection context.

// Actions
- (BOOL)insertNewObjectOrError:(NSError **)error; //!< Return YES if operation succeeded
- (BOOL)deleteObjectAtIndexPath:(NSIndexPath *)indexPath error:(NSError **)error; //!< Return YES if operation succeeded

// Server Communication
- (void)syncRootEntities:(SuccessBlock)success;

//! Get the full selected object from the server and invoke the success block when the data is here
- (void)syncSelectedObjectForDetailOnSuccess:(SuccessBlock)success;

//! Get the data from the selected object necessary for navigation from the server and invoke the success block when the data is here
- (void)syncSelectedObjectForNavigationOnSuccess:(SuccessBlock)success;


@end
