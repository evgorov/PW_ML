//
//  GameLogic.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "GameLogic.h"
#import "AppDelegate.h"
#import "GameViewController.h"
#import "EventManager.h"
#import "GameField.h"

@interface GameLogic ()

-(void)initGameField;

@end

@implementation GameLogic

-(id)init
{
    self = [super init];
    if (self)
    {
        currentGameField = nil;
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_START];
    }
    return self;
}

+(GameLogic *)sharedLogic
{
    static GameLogic * _sharedLogic = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _sharedLogic = [[GameLogic alloc] init];
    });
    return _sharedLogic;
}

-(void)handleEvent:(Event *)event
{
    switch (event.type)
    {
        case EVENT_GAME_REQUEST_START:
            [self initGameField];
            [[AppDelegate currentDelegate].navController pushViewController:[[GameViewController alloc] initWithGameField:currentGameField] animated:YES];

            break;
            
        case EVENTS_COUNT:
        default:
            break;
    }
}

-(void)initGameField
{
    currentGameField = [[GameField alloc] initWithTilesPerRow:10 andTilesPerCol:10];
}

@end
