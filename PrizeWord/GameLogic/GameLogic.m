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
#import "PuzzleData.h"
#import "QuestionData.h"
#import "PuzzleSetData.h"
#import "PrizeWordNavigationController.h"
#import "RootViewController.h"

@interface GameLogic (private)

-(void)initGameFieldWithType:(LetterType)type;
-(void)handleTimer:(id)userInfo;

@end

@implementation GameLogic

@synthesize gameTime = _gameTime;

-(id)init
{
    self = [super init];
    if (self)
    {
        currentGameField = nil;
        gameTimer = [NSTimer scheduledTimerWithTimeInterval:(1/4.0) target:self selector:@selector(handleTimer:) userInfo:nil repeats:YES];
        _gameTime = 0;
        gameState = GAMESTATE_NOT_STARTED;
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_START];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_PAUSE];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_RESUME];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_COMPLETE];
    }
    return self;
}

-(void)dealloc
{
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_GAME_REQUEST_START];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_GAME_REQUEST_PAUSE];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_GAME_REQUEST_RESUME];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_GAME_REQUEST_COMPLETE];
    [gameTimer invalidate];
    gameTimer = nil;
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

-(GameField *)gameField
{
    return currentGameField;
}

-(void)handleEvent:(Event *)event
{
    switch (event.type)
    {
        case EVENT_GAME_REQUEST_START:
        {
            PuzzleData * puzzle = (PuzzleData *)event.data;
            currentGameField = [[GameField alloc] initWithData:puzzle];
            [[AppDelegate currentDelegate].rootViewController hideMenuAnimated:YES];
            if ([[AppDelegate currentDelegate].navController.topViewController isKindOfClass:[GameViewController class]])
            {
                [[AppDelegate currentDelegate].navController popViewControllerAnimated:NO];
                [[AppDelegate currentDelegate].navController pushViewController:[[GameViewController alloc] initWithGameField:currentGameField] animated:NO];
            }
            else
            {
                [[AppDelegate currentDelegate].navController pushViewController:[[GameViewController alloc] initWithGameField:currentGameField] animated:YES];
            }
            gameState = GAMESTATE_PLAYING;
            NSLog(@"start game. time given: %d, time left: %d", puzzle.time_given.intValue, puzzle.time_left.intValue);
            _gameTime = puzzle.time_given.intValue - puzzle.time_left.intValue;
        }
            break;

        case EVENT_GAME_REQUEST_PAUSE:
            gameState = GAMESTATE_PAUSED;
            break;

        case EVENT_GAME_REQUEST_RESUME:
            gameState = GAMESTATE_PLAYING;
            break;

        case EVENT_GAME_REQUEST_COMPLETE:
            gameState = GAMESTATE_FINISHED;
            break;

        default:
            break;
    }
}


-(void)handleTimer:(id)userInfo
{
    if (gameState == GAMESTATE_PLAYING)
    {
        _gameTime += gameTimer.timeInterval;
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_TIME_CHANGED]];
    }
}

@end
