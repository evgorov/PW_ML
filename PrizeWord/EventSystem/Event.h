//
//  Event.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef enum EventType {
    EVENT_FIRST = 0,
    EVENT_GAME_REQUEST_START = 0,
    EVENT_BEGIN_INPUT,
    EVENT_PUSH_LETTER,
    EVENT_POP_LETTER,
    EVENT_FINISH_INPUT,
    EVENT_FOCUS_CHANGE,
    EVENT_TILE_CHANGE,
    EVENT_TILE_TAP,
    
    EVENTS_COUNT
} EventType;

@interface Event : NSObject
{
}

@property (readonly) EventType type;
@property (readonly, nonatomic) id data;

-(id) initWithType:(EventType) type;
-(id) initWithType:(EventType) type andData:(id) data;


+(Event *) eventWithType:(EventType) type;
+(Event *) eventWithType:(EventType) type andData:(id) data;

@end
