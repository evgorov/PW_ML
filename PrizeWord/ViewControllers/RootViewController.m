//
//  RootViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "RootViewController.h"
#import "GameViewController.h"
#import "ReleaseNotesViewController.h"
#import "EventManager.h"
#import "TileData.h"

@interface RootViewController ()

@end

@implementation RootViewController

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (IBAction)handleBrilliantClick:(UIButton *)sender
{
    [[EventManager sharedManager] dispatchEventWithType:[Event eventWithType:EVENT_GAME_REQUEST_START andData:[NSNumber numberWithInt:LETTER_BRILLIANT]]];
}

- (IBAction)handleGoldClick:(UIButton *)sender
{
    [[EventManager sharedManager] dispatchEventWithType:[Event eventWithType:EVENT_GAME_REQUEST_START andData:[NSNumber numberWithInt:LETTER_GOLD]]];
}

- (IBAction)handleSilverClick:(UIButton *)sender
{
    [[EventManager sharedManager] dispatchEventWithType:[Event eventWithType:EVENT_GAME_REQUEST_START andData:[NSNumber numberWithInt:LETTER_SILVER]]];
}

- (IBAction)handleFreeClick:(UIButton *)sender
{
    [[EventManager sharedManager] dispatchEventWithType:[Event eventWithType:EVENT_GAME_REQUEST_START andData:[NSNumber numberWithInt:LETTER_FREE]]];
}

- (IBAction)handleReleaseNotesClick:(UIButton *)sender
{
    [self.navigationController pushViewController:[[ReleaseNotesViewController alloc] init] animated:YES];
}
@end
