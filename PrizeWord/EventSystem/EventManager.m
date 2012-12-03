//
//  EventManager.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "EventManager.h"

@implementation EventManager

+(EventManager *)sharedManager
{
    static EventManager * _sharedManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _sharedManager = [[EventManager alloc] init];
    });
    return _sharedManager;
}

-(id)init
{
    self = [super init];
    if (self)
    {
        for (EventType type = EVENT_FIRST; type < EVENTS_COUNT; ++type) {
            listeners[type] = [NSMutableSet new];
        }
    }
    return self;
}

-(void)registerListener:(id<EventListenerDelegate> __unsafe_unretained)listener forEventType:(EventType)type
{
    [listeners[type] addObject:[NSValue valueWithNonretainedObject:listener]];
}

-(void)unregisterListener:(id<EventListenerDelegate> __unsafe_unretained)listener forEventType:(EventType)type
{
    [listeners[type] removeObject:[NSValue valueWithNonretainedObject:listener]];
}

-(void)dispatchEventWithType:(Event *)event
{
    for (NSValue * listenerValue in listeners[event.type]) {
        [[listenerValue nonretainedObjectValue] handleEvent:event];
    }
}

@end
