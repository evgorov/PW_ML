//
//  GameLogic.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EventListenerDelegate.h"

@class GameField;

@interface GameLogic : NSObject<EventListenerDelegate>
{
    GameField * currentGameField;
}

+(GameLogic *)sharedLogic;

@end
