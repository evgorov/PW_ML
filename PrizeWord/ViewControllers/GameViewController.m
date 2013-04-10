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
#import "GlobalData.h"
#import "UserData.h"
#import "APIRequest.h"
#import "SBJson.h"
#import "PuzzleData.h"
#import "PuzzleSetData.h"
#import "FlipNumberView.h"
#import "SocialNetworks.h"
#import "PrizeWordButton.h"
#import "FISoundEngine.h"
#import "NSString+Utils.h"
#import <FacebookSDK/FacebookSDK.h>

@interface GameViewController (private)

-(void)handleKeyboardWillShow:(NSNotification *)aNotification;
-(void)handleKeyboardWillHide:(NSNotification *)aNotification;
-(void)animateFinalScreenAppears:(id)sender;

-(NSDictionary*)parseURLParams:(NSString *)query;

@end

@implementation GameViewController

-(id)initWithGameField:(GameField *)gameField_
{
    self = [super init];
    if (self)
    {
        gameField = gameField_;
        FISoundEngine * se = [FISoundEngine sharedEngine];
        puzzleSolvedSound = [se soundNamed:@"puzzle_solved.caf" error:nil];
        typeSounds = [NSArray arrayWithObjects:[se soundNamed:@"type_1.caf" error:nil], [se soundNamed:@"type_2.caf" error:nil], [se soundNamed:@"type_3.caf" error:nil], nil];
        countingSound = [se soundNamed:@"counting.caf" error:nil];
        secondaryDingSound = [se soundNamed:@"secondary_ding.caf" error:nil];
    }
    return self;
}

-(void)viewDidLoad
{
    PrizeWordSwitchView * switchView = [PrizeWordSwitchView switchView];
    switchView.frame = pauseSwtMusic.frame;
    [pauseSwtMusic.superview addSubview:switchView];
    [pauseSwtMusic removeFromSuperview];
    pauseSwtMusic = switchView;
    
    switchView = [PrizeWordSwitchView switchView];
    switchView.frame = pauseSwtSound.frame;
    [pauseSwtSound.superview addSubview:switchView];
    [pauseSwtSound removeFromSuperview];
    pauseSwtSound = switchView;
    
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
    finalFlipNumbers = [NSArray arrayWithObjects:finalFlipNumber0, finalFlipNumber1, finalFlipNumber2, finalFlipNumber3, finalFlipNumber4, nil];
    
    self.view.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_dark_tile.jpg"]];

    [self.navigationItem setTitleView:[PrizeWordNavigationBar containerWithView:viewTime]];
}

- (void)viewDidUnload {
    viewTime = nil;
    pauseOverlay = nil;
    pauseSwtMusic = nil;
    pauseSwtSound = nil;
    pauseImgProgressbar = nil;
    pauseTxtProgress = nil;
    finalOverlay = nil;
    pauseSwtMusic = nil;
    pauseSwtSound = nil;
    lblFinalBaseScore = nil;
    lblFinalTimeBonus = nil;
    finalFlipNumber4 = nil;
    finalFlipNumber3 = nil;
    finalFlipNumber2 = nil;
    finalFlipNumber1 = nil;
    finalFlipNumber0 = nil;
    finalFlipNumbers = nil;
    finalShareView = nil;
    
    [super viewDidUnload];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];

    [lblTime setFont:[UIFont fontWithName:@"DINPro-Black" size:22]];
    [btnHint.titleLabel setFont:[UIFont fontWithName:@"DINPro-Black" size:18]];
    
    [gameFieldView setGameField:gameField];
    gameFieldView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
    textField = [UITextField new];
    textField.autocorrectionType = UITextAutocorrectionTypeNo;
    textField.hidden = YES;
    textField.delegate = self;
    [self.view addSubview:textField];
    
    UIView * playPauseView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, btnPause.frame.size.width, btnPause.frame.size.height)];
    [playPauseView addSubview:btnPlay];
    [playPauseView addSubview:btnPause];
    [btnPlay addTarget:self action:@selector(handlePlayClick:) forControlEvents:UIControlEventTouchUpInside];
    [btnPause addTarget:self action:@selector(handlePauseClick:) forControlEvents:UIControlEventTouchUpInside];
    playPauseItem = [[UIBarButtonItem alloc] initWithCustomView:
                                       [PrizeWordNavigationBar containerWithView:playPauseView]];
    [self.navigationItem setLeftBarButtonItem:playPauseItem animated:animated];
    hintButtonItem = [[UIBarButtonItem alloc] initWithCustomView:[PrizeWordNavigationBar containerWithView:btnHint]];
    [self.navigationItem setRightBarButtonItem:hintButtonItem animated:animated];
    
    [btnHint setTitle:[NSString stringWithFormat:@"%d", [GlobalData globalData].loggedInUser.hints] forState:UIControlStateNormal];
    
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

-(void)orientationChanged:(UIDeviceOrientation)orientation
{
    NSLog(@"nav bar: %f %f", self.navigationController.navigationBar.frame.size.width, self.navigationController.navigationBar.frame.size.height);
    NSLog(@"nav con: %f %f", self.navigationController.view.frame.size.width, self.navigationController.view.frame.size.height);
    if ([textField isFirstResponder])
    {
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_FOCUS_CHANGE andData:nil]];
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_REQUEST_FINISH_INPUT]];
        [textField resignFirstResponder];
    }
}

-(NSUInteger)supportedInterfaceOrientations
{
    if ([AppDelegate currentDelegate].isIPad)
    {
        return UIInterfaceOrientationMaskAll;
    }
    return UIInterfaceOrientationMaskPortrait | UIInterfaceOrientationMaskPortraitUpsideDown;
}



- (IBAction)handlePauseClick:(id)sender
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
        int hints = [GlobalData globalData].loggedInUser.hints;
        if (hints > 0)
        {
            [textField resignFirstResponder];
            
            UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"TITLE_USE_HINT", nil) message:NSLocalizedString(@"QUESTION_USE_HINT", nil) delegate:self cancelButtonTitle:NSLocalizedString(@"BUTTON_CANCEL", nil) otherButtonTitles:NSLocalizedString(@"BUTTON_USE_HINT", nil), nil];
            [alertView show];
        }
    }
}

- (IBAction)handlePauseNext:(id)sender
{
    [[AppDelegate currentDelegate].rootViewController hideOverlay];
    [gameField.puzzle synchronize];
    PuzzleData * puzzle = gameField.puzzle;
    PuzzleSetData * puzzleSet = puzzle.puzzleSet;
    BOOL selectNext = NO;
    PuzzleData * nextPuzzle = nil;
    NSArray * orderedPuzzles = puzzleSet.orderedPuzzles;
    for (PuzzleData * otherPuzzle in orderedPuzzles)
    {
        if (selectNext && otherPuzzle.progress != 1)
        {
            nextPuzzle = otherPuzzle;
            break;
        }
        if ([otherPuzzle.puzzle_id isEqualToString:puzzle.puzzle_id])
        {
            selectNext = YES;
        }
    }
    if (nextPuzzle == nil)
    {
        for (PuzzleData * otherPuzzle in orderedPuzzles) {
            if (otherPuzzle.progress != 1)
            {
                nextPuzzle = otherPuzzle;
                break;
            }
        }
    }
    if (nextPuzzle != nil)
    {
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_START andData:nextPuzzle]];
    }
    else
    {
        [[AppDelegate currentDelegate].rootViewController hideOverlay];
        [self.navigationController popViewControllerAnimated:YES];
    }
}

- (IBAction)handlePauseMenu:(id)sender
{
    [gameField.puzzle synchronize];
    [[AppDelegate currentDelegate].rootViewController hideOverlay];
    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)handleShareClick:(id)sender
{
    UIButton * button = sender;
    int mins = (int)([GameLogic sharedLogic].gameTime / 60);
    NSString * message = [NSString stringWithFormat:@"Я только что разгадал сканворд %@ за %d %@ и получил за это %d %@!", gameField.puzzle.name, mins, [NSString declesion:mins oneString:@"минуту" twoString:@"минуты" fiveString:@"минут"], gameField.puzzle.score.intValue, [NSString declesion:gameField.puzzle.score.intValue oneString:@"очко" twoString:@"очка" fiveString:@"очков"]];
    // facebook
    if (button.tag == 0)
    {
        if ([[FBSession activeSession] isOpen])
        {
            // Put together the dialog parameters
            NSMutableDictionary *params =
            [NSMutableDictionary dictionaryWithObjectsAndKeys:
             @"PrizeWord", @"name",
             message, @"caption",
             @"http://prize-word.com", @"link",
             nil];
            [self showActivityIndicator];
            
            [[FBSession activeSession] reauthorizeWithPublishPermissions:[NSArray arrayWithObjects:@"publish_actions", @"publish_stream", nil] defaultAudience:FBSessionDefaultAudienceEveryone completionHandler:^(FBSession *session, NSError *error) {
                if (error == nil)
                {
                    NSLog(@"reauthorizeWithPublishPermissions success");
                    // Invoke the dialog
                    [FBWebDialogs presentFeedDialogModallyWithSession:session
                                                           parameters:params
                                                              handler:
                     ^(FBWebDialogResult result, NSURL *resultURL, NSError *error) {
                         [self hideActivityIndicator];
                         
                         if (error) {
                             // Case A: Error launching the dialog or publishing story.
                             NSLog(@"Error publishing story.");
                         } else {
                             if (result == FBWebDialogResultDialogNotCompleted) {
                                 // Case B: User clicked the "x" icon
                                 NSLog(@"User canceled story publishing.");
                                 UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:@"Ошибка при публикации" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
                                 [alertView show];
                             } else {
                                 // Case C: Dialog shown and the user clicks Cancel or Share
                                 NSDictionary *urlParams = [self parseURLParams:[resultURL query]];
                                 if (![urlParams valueForKey:@"post_id"]) {
                                     // User clicked the Cancel button
                                     NSLog(@"User canceled story publishing.");
                                 } else {
                                     // User clicked the Share button
                                     NSString *postID = [urlParams valueForKey:@"post_id"];
                                     
                                     UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:@"PrizeWord" message:@"Ваш результат опубликован!" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
                                     [alertView show];
                                     
                                     NSLog(@"Posted story, id: %@", postID);
                                 }
                             }
                         }
                     }];
                }
                else
                {
                    NSLog(@"facebook publish stream openning error: %@", error);
                    UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка facebook" message:error.localizedDescription delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
                    [alert show];
                }
            }];
        }
        else
        {
            [SocialNetworks loginFacebookWithViewController:[AppDelegate currentDelegate].rootViewController andCallback:^{
                if ([FBSession activeSession] != nil)
                {
                    [self handleShareClick:sender];
                }
                else
                {
                    NSLog(@"Error while facebook login");
                }
            }];
        }
    }
    // vkontakte
    else
    {
        if ([GlobalData globalData].loggedInUser.vkProvider != nil)
        {
            APIRequest * request = [APIRequest postRequest:@"vkontakte/share" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
                UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:@"PrizeWord" message:@"Ваш результат опубликован!" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
                [alertView show];
            } failCallback:nil];
            [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
            [request.params setObject:message forKey:@"message"];
            [request runUsingCache:NO silentMode:NO];
        }
        else
        {
            [SocialNetworks loginVkontakteWithViewController:[AppDelegate currentDelegate].rootViewController andCallback:^{
                if ([GlobalData globalData].loggedInUser.vkProvider != nil)
                {
                    [self handleShareClick:sender];
                }
                else
                {
                    NSLog(@"Error while vkontakte login");
                }
            }];
        }
    }
}

-(void)handleKeyboardWillShow:(NSNotification *)aNotification
{
    NSDictionary * userInfo = aNotification.userInfo;
    CGRect endFrame = [(NSValue *)[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
    endFrame = [self.view convertRect:endFrame toView:nil];
    if (UIDeviceOrientationIsLandscape([AppDelegate currentDelegate].viewOrientation))
    {
        endFrame = CGRectMake(endFrame.origin.y, endFrame.origin.y, endFrame.size.height, endFrame.size.width);
    }
    
    UIViewAnimationCurve animationCurve = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey] intValue];
    double animationDuration = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] doubleValue];
    
    NSLog(@"animation frame size: %f %f %f %f", self.view.frame.size.width, self.view.frame.size.height, endFrame.size.height, endFrame.origin.y);
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

#pragma mark EventListenerDelegate

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
        gameField.puzzle.time_left = [NSNumber numberWithInt:gameField.puzzle.time_given.intValue - gameTime];
    }
    else if (event.type == EVENT_GAME_REQUEST_PAUSE)
    {
        btnPause.hidden = YES;
        btnPlay.hidden = NO;
        float progress = (float)[GameLogic sharedLogic].gameField.questionsComplete / [GameLogic sharedLogic].gameField.questionsTotal;
        pauseImgProgressbar.frame = CGRectMake(pauseImgProgressbar.frame.origin.x, pauseImgProgressbar.frame.origin.y, pauseMaxProgress * progress, pauseImgProgressbar.frame.size.height);
        [pauseTxtProgress setText:[NSString stringWithFormat:@"%d%%", (int)(100 * progress)]];
        [[AppDelegate currentDelegate].rootViewController showOverlay:pauseOverlay];
        [self.navigationItem setLeftBarButtonItem:playPauseItem];
        [self.navigationItem setRightBarButtonItem:hintButtonItem];
        [self.navigationItem setTitleView:[PrizeWordNavigationBar containerWithView:viewTime]];
        [gameField.puzzle synchronize];
    }
    else if (event.type == EVENT_GAME_REQUEST_RESUME)
    {
        btnPause.hidden = NO;
        btnPlay.hidden = YES;
        [[AppDelegate currentDelegate].rootViewController hideOverlay];
        [self.navigationItem setLeftBarButtonItem:playPauseItem];
        [self.navigationItem setRightBarButtonItem:hintButtonItem];
        [self.navigationItem setTitleView:[PrizeWordNavigationBar containerWithView:viewTime]];
    }
    else if (event.type == EVENT_GAME_REQUEST_COMPLETE)
    {
        [puzzleSolvedSound play];
        
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 1 * NSEC_PER_SEC), dispatch_get_current_queue(), ^{
            if (gameField.puzzle.puzzleSet.type.intValue == PUZZLESET_FREE)
            {
                [self.navigationController popViewControllerAnimated:YES];
                return;
            }
            for (int i = 0; i < 5; ++i)
            {
                [[finalFlipNumbers objectAtIndex:i] reset];
            }
            finalShareView.frame = CGRectMake(finalShareView.frame.origin.x, [AppDelegate currentDelegate].isIPad ? 242 : 190, finalShareView.frame.size.width, finalShareView.frame.size.height);
            lblFinalBaseScore.text = @"0";
            lblFinalTimeBonus.text = @"0";
            [NSTimer scheduledTimerWithTimeInterval:0.5f target:self selector:@selector(animateFinalScreenAppears:) userInfo:event.data repeats:NO];
            
            [[AppDelegate currentDelegate].rootViewController showFullscreenOverlay:finalOverlay];
        });
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
        [(FISound *)[typeSounds objectAtIndex:(rand() % 3)] play];
        
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
        [textField becomeFirstResponder];
        return;
    }
    APIRequest * request = [APIRequest postRequest:@"hints" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        SBJsonParser * parser = [SBJsonParser new];
        NSDictionary * data = [parser objectWithData:receivedData];
        [GlobalData globalData].loggedInUser = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
        [btnHint setTitle:[NSString stringWithFormat:@"%d", [GlobalData globalData].loggedInUser.hints] forState:UIControlStateNormal];
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_REQUEST_APPLY_HINT]];
    } failCallback:nil];
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    [request.params setObject:@"-1" forKey:@"hints_change"];
    [request runUsingCache:NO silentMode:YES];
}

-(void)animateFinalScreenAppears:(id)sender
{
    NSTimer * timer = sender;
    PuzzleData * puzzleData = timer.userInfo;
    
    CGRect shareFrame = finalShareView.frame;
    shareFrame.origin.y = [AppDelegate currentDelegate].isIPad ? 402 : 308;
    
    int baseScore = [[GlobalData globalData] baseScoreForType:puzzleData.puzzleSet.type.intValue];
    lblFinalBaseScore.text = [NSString stringWithFormat:@"%d", baseScore];
    lblFinalTimeBonus.text = [NSString stringWithFormat:@"%d", [puzzleData.score unsignedIntValue] - baseScore];
    lblFinalBaseScore.frame = CGRectMake(lblFinalBaseScore.frame.origin.x, lblFinalBaseScore.frame.origin.y, [lblFinalBaseScore.text sizeWithFont:lblFinalBaseScore.font].width, lblFinalBaseScore.frame.size.height);
    lblFinalTimeBonus.frame = CGRectMake(lblFinalTimeBonus.frame.origin.x, lblFinalTimeBonus.frame.origin.y, [lblFinalTimeBonus.text sizeWithFont:lblFinalTimeBonus.font].width, lblFinalTimeBonus.frame.size.height);
    
    lblFinalBaseScore.transform = CGAffineTransformMakeScale(0.01f, 0.01f);
    lblFinalBaseScore.hidden = YES;
    lblFinalTimeBonus.transform = CGAffineTransformMakeScale(0.01f, 0.01f);
    lblFinalTimeBonus.hidden = YES;
    [UIView animateWithDuration:0.3f delay:0 options:UIViewAnimationOptionCurveEaseOut animations:^{
        finalShareView.frame = shareFrame;
    } completion:^(BOOL finished) {
        
        lblFinalBaseScore.hidden = NO;
        [secondaryDingSound play];
        [UIView animateWithDuration:0.25f delay:0 options:UIViewAnimationOptionCurveEaseIn animations:^{
            lblFinalBaseScore.transform = CGAffineTransformMakeScale(2.0f, 2.0f);
        } completion:^(BOOL finished) {
            [secondaryDingSound play];
            [UIView animateWithDuration:0.25f delay:0 options:UIViewAnimationOptionCurveEaseOut animations:^{
                lblFinalBaseScore.transform = CGAffineTransformMakeScale(1, 1);
            } completion:nil];
        }];

        [UIView animateWithDuration:0.25f delay:0.5f options:UIViewAnimationOptionCurveEaseIn animations:^{
            lblFinalTimeBonus.hidden = NO;
            lblFinalTimeBonus.transform = CGAffineTransformMakeScale(2.0f, 2.0f);
        } completion:^(BOOL finished) {
            [UIView animateWithDuration:0.25f delay:0 options:UIViewAnimationOptionCurveEaseOut animations:^{
                lblFinalTimeBonus.transform = CGAffineTransformMakeScale(1, 1);
            } completion:^(BOOL finished) {
                uint score = [puzzleData.score unsignedIntValue];
                // TODO :: loop counting
                [countingSound play];
                int lastI = 0;
                for (int i = 0; i < 5; ++i)
                {
                    if (score > 0)
                    {
                        [[finalFlipNumbers objectAtIndex:i] flipNTimes:(10 + 10 * i + score % 10)];
                        lastI = i;
                        score /= 10;
                    }
                    else
                    {
                        [[finalFlipNumbers objectAtIndex:i] flipNTimes:(10 + 10 * lastI)];
                    }
                }
            }];
        }];
    }];
}

#pragma mark helpers

-(NSDictionary*)parseURLParams:(NSString *)query
{
    NSArray *pairs = [query componentsSeparatedByString:@"&"];
    NSMutableDictionary *params = [[NSMutableDictionary alloc] init];
    for (NSString *pair in pairs)
    {
        NSArray *kv = [pair componentsSeparatedByString:@"="];
        NSString *val =
        [[kv objectAtIndex:1]
         stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        
        [params setObject:val forKey:[[kv objectAtIndex:0] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        NSLog(@"params: %@=%@", [[kv objectAtIndex:0] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding], [[kv objectAtIndex:1] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding]);
    }
    return params;
}


@end
