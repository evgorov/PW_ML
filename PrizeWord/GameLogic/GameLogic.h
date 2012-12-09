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

typedef enum GameState {
    GAMESTATE_NOT_STARTED,
    GAMESTATE_PLAYING,
    GAMESTATE_PAUSED,
    GAMESTATE_FINISHED
} GameState;

@interface GameLogic : NSObject<EventListenerDelegate>
{
    GameField * currentGameField;
    NSTimer * gameTimer;
    GameState gameState;
}

@property (readonly) double gameTime;

+(GameLogic *)sharedLogic;
-(GameField *)gameField;

@end
