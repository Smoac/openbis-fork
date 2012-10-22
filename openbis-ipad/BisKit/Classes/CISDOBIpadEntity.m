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
//  CISDOBIpadEntity.m
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 10/1/12.
//
//

#import "CISDOBIpadEntity.h"
#import "CISDOBIpadService.h"


@implementation CISDOBIpadEntity

@dynamic summaryHeader;
@dynamic summary;
@dynamic identifier;
@dynamic permId;
@dynamic refcon;
@dynamic category;
@dynamic imageUrl;
@dynamic childrenPermIdsJson;
@dynamic propertiesJson;

@synthesize image;

- (NSArray *)properties
{
    [self willAccessValueForKey: @"properties"];
    NSMutableArray *properties = [self primitiveValueForKey: @"properties"];
    [self didAccessValueForKey: @"properties"];
    
    if (nil == properties) {
        NSError *error;
        NSDictionary *propertiesDict = [NSJSONSerialization JSONObjectWithData: [self.propertiesJson dataUsingEncoding: NSASCIIStringEncoding] options: 0 error: &error];
        properties = [[NSMutableArray alloc] init];
        for (NSString *key in [propertiesDict allKeys]) {
            NSDictionary *property = [NSDictionary dictionaryWithObjectsAndKeys:
                key, @"key",
                [propertiesDict valueForKey: key], @"value", nil];
            [properties addObject: property];
        }
        if (error) {
            NSLog(@"Could not deserialize properties %@", error);
        }
        [self setPrimitiveValue: properties forKey: @"properties"];
    }
    
    return properties;
}


- (NSArray *)childrenPermIds
{
    [self willAccessValueForKey: @"childrenPermIds"];
    NSArray *childrenPermIds = [self primitiveValueForKey: @"childrenPermIds"];
    [self didAccessValueForKey: @"childrenPermIds"];
    
    if (nil == childrenPermIds) {
            // This value has not yet been initialized from the server.
            // Leave it as nil
        if (nil == self.childrenPermIdsJson) return nil;

        NSError *error;
        childrenPermIds = [NSJSONSerialization JSONObjectWithData: [self.childrenPermIdsJson dataUsingEncoding: NSASCIIStringEncoding] options: 0 error: &error];
        if (error) {
            NSLog(@"Could not deserialize childrenPermIds %@", error);
        }
        [self setPrimitiveValue: childrenPermIds forKey: @"childrenPermIds"];
    }
    
    return childrenPermIds;
}

- (void)initializeFromRawEntity:(CISDOBIpadRawEntity *)rawEntity
{
    self.permId = rawEntity.permId;
    self.refcon = rawEntity.refcon;
    self.category = rawEntity.category;
    self.summaryHeader = rawEntity.summaryHeader;
    self.summary = rawEntity.summary;
    self.childrenPermIdsJson = rawEntity.children;
//    self.identifier = rawEntity.identifier;
//    self.imageUrl = rawEntity.imageUrl;
//    self.propertiesJson = rawEntity.properties;
}

@end
