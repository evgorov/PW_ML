//
//  RootViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "RootViewController.h"
#import "GameViewController.h"
#import "EventManager.h"
#import "TileData.h"

@interface RootViewController ()

@end

@implementation RootViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
}

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
@end
