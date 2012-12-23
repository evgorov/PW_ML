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
#import "GameLogic.h"
#import "EventManager.h"
#import "PrizeWordNavigationBar.h"
#import "RootViewController.h"
#import "AppDelegate.h"

@interface GameViewController (private)

-(void)handleKeyboardWillShow:(NSNotification *)aNotification;
-(void)handleKeyboardWillHide:(NSNotification *)aNotification;

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

-(void)viewDidLoad
{
    pauseMaxProgress = pauseImgProgressbar.frame.size.width;
    UIImage * imgProgress = [UIImage imageNamed:@"pause_progressbar"];
    CGSize imageSize = imgProgress.size;
    if ([imgProgress respondsToSelector:@selector(resizableImageWithCapInsets:)])
    {
        imgProgress = [imgProgress resizableImageWithCapInsets:UIEdgeInsetsMake(imageSize.height / 2 - 1, imageSize.width / 2 - 1, imageSize.height / 2, imageSize.width / 2)];
    }
    else
    {
        imgProgress = [imgProgress stretchableImageWithLeftCapWidth:(imageSize.width / 2) topCapHeight:(imageSize.height / 2)];
    }
    pauseImgProgressbar.image = imgProgress;
    pauseImgProgressbar.frame = CGRectMake(pauseImgProgressbar.frame.origin.x, pauseImgProgressbar.frame.origin.y, pauseMaxProgress, pauseImgProgressbar.frame.size.height);
    [pauseTxtProgress setText:@"100%"];
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
    
    UIView * playPauseView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, btnPause.frame.size.width, btnPause.frame.size.height)];
    [playPauseView addSubview:btnPlay];
    [playPauseView addSubview:btnPause];
    playPauseItem = [[UIBarButtonItem alloc] initWithCustomView:
                                       [PrizeWordNavigationBar containerWithView:playPauseView]];
    [self.navigationItem setLeftBarButtonItem:playPauseItem animated:animated];
    hintButtonItem = [[UIBarButtonItem alloc] initWithCustomView:[PrizeWordNavigationBar containerWithView:btnHint]];
    [self.navigationItem setRightBarButtonItem:hintButtonItem animated:animated];
    [self.navigationItem setTitleView:[PrizeWordNavigationBar containerWithView:viewTime]];
    
    
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_BEGIN_INPUT];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_FINISH_INPUT];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_PAUSE];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_RESUME];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_COMPLETE];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_TIME_CHANGED];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillHideNotification object:nil];

    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_BEGIN_INPUT];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_FINISH_INPUT];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_GAME_REQUEST_PAUSE];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_GAME_REQUEST_RESUME];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_GAME_REQUEST_COMPLETE];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_GAME_TIME_CHANGED];
    [textField removeFromSuperview];
    textField = nil;
    gameFieldView = nil;
    btnPause = nil;
    lblTime = nil;
    btnPlay = nil;
    gameFieldView = nil;
    btnHint = nil;
    playPauseItem = nil;
    hintButtonItem = nil;
}

-(NSUInteger)supportedInterfaceOrientations
{
    return (UIInterfaceOrientationPortrait | UIInterfaceOrientationPortraitUpsideDown);
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait || interfaceOrientation == UIInterfaceOrientationPortraitUpsideDown);
}

- (IBAction)handlePauseClick:(UIButton *)sender
{
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_REQUEST_FINISH_INPUT]];
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_PAUSE]];
}

- (IBAction)handlePlayClick:(id)sender
{
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_RESUME]];
}

- (IBAction)handleHintClick:(id)sender
{
    if ([GameLogic sharedLogic].gameField.activeQuestion != nil)
    {
        int hints = [[btnHint titleForState:UIControlStateNormal] integerValue];
        if (hints > 0)
        {
            UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"TITLE_USE_HINT", nil) message:NSLocalizedString(@"QUESTION_USE_HINT", nil) delegate:self cancelButtonTitle:NSLocalizedString(@"BUTTON_CANCEL", nil) otherButtonTitles:NSLocalizedString(@"BUTTON_USE_HINT", nil), nil];
            [alertView show];
        }
    }
}

- (IBAction)handlePauseNext:(id)sender
{
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_RESUME]];
}

- (IBAction)handlePauseMenu:(id)sender
{
    [[AppDelegate currentDelegate].rootViewController hideOverlay];
    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)handleFBClick:(id)sender
{
}

- (IBAction)handleVKClick:(id)sender
{
}

-(void)handleKeyboardWillShow:(NSNotification *)aNotification
{
    NSDictionary * userInfo = aNotification.userInfo;
    CGRect endFrame = [(NSValue *)[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
    
    UIViewAnimationCurve animationCurve = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey] intValue];
    double animationDuration = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] doubleValue];
    
    [UIView setAnimationCurve:animationCurve];
    [UIView animateWithDuration:animationDuration animations:^{
        gameFieldView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height - endFrame.size.height);
    } completion:^(BOOL finished) {
        [gameFieldView refreshFocus];
    }];
}

-(void)handleKeyboardWillHide:(NSNotification *)aNotification
{
    NSDictionary * userInfo = aNotification.userInfo;
        
    UIViewAnimationCurve animationCurve = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey] intValue];
    double animationDuration = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] doubleValue];
        
    [UIView setAnimationCurve:animationCurve];
    [UIView animateWithDuration:animationDuration animations:^{
        gameFieldView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
    }];
}

// EventListenerDelegate
-(void)handleEvent:(Event *)event
{
    if (event.type == EVENT_BEGIN_INPUT)
    {
        textField.text = @"";
        [textField becomeFirstResponder];
    }
    else if (event.type == EVENT_FINISH_INPUT)
    {
        [textField resignFirstResponder];
    }
    else if (event.type == EVENT_GAME_TIME_CHANGED)
    {
        int gameTime = [GameLogic sharedLogic].gameTime;
        lblTime.text = [NSString stringWithFormat:@"%02d:%02d", gameTime / 60, gameTime % 60];
    }
    else if (event.type == EVENT_GAME_REQUEST_PAUSE)
    {
        btnPause.hidden = YES;
        btnPlay.hidden = NO;
        [[AppDelegate currentDelegate].rootViewController showOverlay:pauseOverlay];
        [self.navigationItem setLeftBarButtonItem:playPauseItem];
        [self.navigationItem setRightBarButtonItem:hintButtonItem];
        [self.navigationItem setTitleView:viewTime];
    }
    else if (event.type == EVENT_GAME_REQUEST_RESUME)
    {
        btnPause.hidden = NO;
        btnPlay.hidden = YES;
        [[AppDelegate currentDelegate].rootViewController hideOverlay];
        [self.navigationItem setLeftBarButtonItem:playPauseItem];
        [self.navigationItem setRightBarButtonItem:hintButtonItem];
        [self.navigationItem setTitleView:viewTime];
    }
    else if (event.type == EVENT_GAME_REQUEST_COMPLETE)
    {
        [[AppDelegate currentDelegate].rootViewController showFullscreenOverlay:finalOverlay];
    }
}

-(BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    if (range.length == 1 && string.length == 0)
    {
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_POP_LETTER]];
    }
    else
    {
        NSString * letter = string.lowercaseString;
        if ([letter compare:@"ё"] == NSOrderedSame)
        {
            letter = @"е";
        }
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_PUSH_LETTER andData:letter]];
    }
    return YES;
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_FOCUS_CHANGE andData:nil]];
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_REQUEST_FINISH_INPUT]];
    return YES;
}

-(void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (alertView.cancelButtonIndex == buttonIndex) {
        return;
    }
    int hints = [[btnHint titleForState:UIControlStateNormal] integerValue];
    hints--;
    [btnHint setTitle:[NSString stringWithFormat:@"%d", hints] forState:UIControlStateNormal];
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_REQUEST_APPLY_HINT]];
}

- (void)viewDidUnload {
    viewTime = nil;
    pauseOverlay = nil;
    pauseSwtMusic = nil;
    pauseSwtSound = nil;
    pauseImgProgressbar = nil;
    pauseTxtProgress = nil;
    finalOverlay = nil;
    [super viewDidUnload];
}


@end
