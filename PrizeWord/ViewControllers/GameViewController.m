//
//  GameViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "GameViewController.h"
#import "GameFieldView.h"
#import "GameField.h"
#import "EventManager.h"

@interface GameViewController ()

@end

@implementation GameViewController

-(id)initWithGameField:(GameField *)gameField_
{
    self = [super init];
    if (self)
    {
        gameField = gameField_;
    }
    return self;
}

-(void)dealloc
{
    NSLog(@"GameViewController dealloc");
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];

    [lblTime setFont:[UIFont fontWithName:@"DINPro-Black" size:22]];
    [btnHint.titleLabel setFont:[UIFont fontWithName:@"DINPro-Black" size:18]];
    
    [gameFieldView setGameField:gameField];
    textField = [UITextField new];
    textField.autocorrectionType = UITextAutocorrectionTypeNo;
    textField.hidden = YES;
    textField.delegate = self;
    [self.view addSubview:textField];
    
    UIBarButtonItem * pauseButtonItem = [[UIBarButtonItem alloc] initWithCustomView:btnPause];
    [self.navigationItem setLeftBarButtonItem:pauseButtonItem animated:animated];
    UIBarButtonItem * hintButtonItem = [[UIBarButtonItem alloc] initWithCustomView:btnHint];
    [self.navigationItem setRightBarButtonItem:hintButtonItem animated:animated];
    
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_BEGIN_INPUT];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_FINISH_INPUT];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_PAUSE];
}

-(void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];

    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_BEGIN_INPUT];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_FINISH_INPUT];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_GAME_REQUEST_PAUSE];
    [textField removeFromSuperview];
    textField = nil;
    gameFieldView = nil;
    btnPause = nil;
    lblTime = nil;
    btnPlay = nil;
    gameFieldView = nil;
    btnHint = nil;
}

-(NSUInteger)supportedInterfaceOrientations
{
    return (UIInterfaceOrientationPortrait | UIInterfaceOrientationPortraitUpsideDown);
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait || interfaceOrientation == UIInterfaceOrientationPortraitUpsideDown);
}

// EventListenerDelegate
-(void)handleEvent:(Event *)event
{
    if (event.type == EVENT_BEGIN_INPUT) {
        textField.text = @"";
        [textField becomeFirstResponder];
    }
    else if (event.type == EVENT_FINISH_INPUT) {
        [textField resignFirstResponder];
    }
    else if (event.type == EVENT_GAME_REQUEST_PAUSE) {
        [self.navigationController popViewControllerAnimated:YES];
    }
}

-(BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    if (range.length == 1 && string.length == 0)
    {
        [[EventManager sharedManager] dispatchEventWithType:[Event eventWithType:EVENT_POP_LETTER]];
    }
    else
    {
        [[EventManager sharedManager] dispatchEventWithType:[Event eventWithType:EVENT_PUSH_LETTER andData:string.lowercaseString]];
    }
    return YES;
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [[EventManager sharedManager] dispatchEventWithType:[Event eventWithType:EVENT_FINISH_INPUT]];
    return YES;
}

- (IBAction)handlePauseClick:(UIButton *)sender
{
    [[EventManager sharedManager] dispatchEventWithType:[Event eventWithType:EVENT_GAME_REQUEST_PAUSE]];
}

@end
