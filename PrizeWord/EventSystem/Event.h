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
    EVENT_GAME_REQUEST_RESUME,
    EVENT_GAME_REQUEST_PAUSE,
    EVENT_GAME_REQUEST_COMPLETE,
    EVENT_GAME_TIME_CHANGED,
    EVENT_BEGIN_INPUT,
    EVENT_PUSH_LETTER,
    EVENT_POP_LETTER,
    EVENT_FINISH_INPUT,
    EVENT_REQUEST_FINISH_INPUT,
    EVENT_REQUEST_APPLY_HINT,
    EVENT_FOCUS_CHANGE,
    EVENT_TILE_CHANGE,
    EVENT_TILE_INVALIDATE,
    EVENT_TILE_TAP,
    
    EVENT_PUZZLE_SYNCHRONIZED,
    EVENT_ME_UPDATED,
    EVENT_USER_LOGGED_IN,
    EVENT_MONTH_SETS_UPDATED,
    EVENT_COEFFICIENTS_UPDATED,
    EVENT_SESSION_ENDED,
    
    EVENT_REQUEST_PRODUCT,
    EVENT_PRODUCT_BOUGHT,
    EVENT_PRODUCT_FAILED,
    EVENT_PRODUCT_ERROR,
    EVENT_SET_BOUGHT,
    
    EVENT_ALL_REQUESTS_CANCELED,
    
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
