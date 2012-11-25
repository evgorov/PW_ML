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

@implementation GameLogic

-(id)init
{
    self = [super init];
    if (self)
    {
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
            [[AppDelegate currentDelegate].navController pushViewController:[GameViewController new] animated:YES];

            break;
            
        case EVENTS_COUNT:
        default:
            break;
    }
}

@end
