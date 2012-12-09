//
//  EventManager.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Event.h"
#import "EventListenerDelegate.h"

@interface EventManager : NSObject
{
    NSMutableSet * listeners[EVENTS_COUNT];
}

+(EventManager *)sharedManager;

-(void)registerListener:(id<EventListenerDelegate> __unsafe_unretained)listener forEventType:(EventType)type;
-(void)unregisterListener:(id<EventListenerDelegate> __unsafe_unretained)listener forEventType:(EventType)type;
-(void)dispatchEvent:(Event *)event;

@end
