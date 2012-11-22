//
//  Event.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "Event.h"

@implementation Event

@synthesize type = _type;
@synthesize data = _data;

-(id) initWithType:(EventType) type
{
    return [self initWithType:type andData:nil];
}

-(id) initWithType:(EventType) type andData:(id) data
{
    self = [super init];
    if (self)
    {
        _type = type;
        _data = data;
    }
    return self;
}


+(Event *) eventWithType:(EventType) type
{
    return [[Event alloc] initWithType:type];
}

+(Event *) eventWithType:(EventType) type andData:(id) data
{
    return [[Event alloc] initWithType:type andData:data];
}

@end
