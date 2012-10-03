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
//  CISDOBIpadEntityTest.h
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 10/2/12.
//

#import <SenTestingKit/SenTestingKit.h>

// Test the persistence of iPad entities.
@class CISDOBIpadService;
@interface CISDOBIpadEntityTest : SenTestCase

@property(strong) CISDOBIpadService *service;
@property(strong) NSURL *databaseUrl;
@property(strong) NSManagedObjectContext *moc;


@end
