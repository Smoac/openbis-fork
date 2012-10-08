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
@dynamic entityKind;
@dynamic entityType;
@dynamic imageUrl;
@dynamic propertiesJson;

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

- (void)initializeFromRawEntity:(CISDOBIpadRawEntity *)rawEntity
{
    self.summaryHeader = rawEntity.summaryHeader;
    self.summary = rawEntity.summary;
    self.identifier = rawEntity.identifier;
    self.permId = rawEntity.permId;
    self.entityKind = rawEntity.entityKind;
    self.entityType = rawEntity.entityType;
    self.imageUrl = rawEntity.imageUrl;
    self.propertiesJson = rawEntity.properties;
}

@end
