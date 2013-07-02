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
#import <StoreKit/StoreKit.h>

const int TAG_USEHINT = 100;
const int TAG_BUYHINTS = 101;
extern NSString * PRODUCTID_HINTS10;

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
    [super viewDidLoad];
    activityIndicator.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleTopMargin;
    
    PrizeWordSwitchView * switchView = [PrizeWordSwitchView switchView];
    switchView.frame = pauseSwtMusic.frame;
    [pauseSwtMusic.superview addSubview:switchView];
    [pauseSwtMusic removeFromSuperview];
    pauseSwtMusic = switchView;
    [pauseSwtMusic addTarget:self action:@selector(handlePauseMusicSwitch:) forControlEvents:UIControlEventValueChanged];
    
    switchView = [PrizeWordSwitchView switchView];
    switchView.frame = pauseSwtSound.frame;
    [pauseSwtSound.superview addSubview:switchView];
    [pauseSwtSound removeFromSuperview];
    pauseSwtSound = switchView;
    [pauseSwtSound addTarget:self action:@selector(handlePauseSoundSwitch:) forControlEvents:UIControlEventValueChanged];
    
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
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_PRODUCT_BOUGHT];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_PRODUCT_ERROR];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_PRODUCT_FAILED];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    
    BOOL soundMute = [[NSUserDefaults standardUserDefaults] boolForKey:@"sound-mute"];
    BOOL musicMute = [[NSUserDefaults standardUserDefaults] boolForKey:@"music-mute"];
    [pauseSwtSound setOn:!soundMute animated:animated];
    [pauseSwtMusic setOn:!musicMute animated:animated];
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
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_PRODUCT_BOUGHT];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_PRODUCT_ERROR];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_PRODUCT_FAILED];
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
            alertView.tag = TAG_USEHINT;
            [alertView show];
        }
        else
        {
            UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"TITLE_BUY_HINTS", nil) message:NSLocalizedString(@"QUESTION_BUY_HINTS", nil) delegate:self cancelButtonTitle:NSLocalizedString(@"BUTTON_CANCEL", nil) otherButtonTitles:NSLocalizedString(@"BUTTON_BUY_HINTS", nil), nil];
            alertView.tag = TAG_BUYHINTS;
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

- (IBAction)handlePauseMusicSwitch:(id)sender
{
    BOOL mute = !pauseSwtMusic.isOn;
    [[NSUserDefaults standardUserDefaults] setBool:mute forKey:@"music-mute"];
    if (mute)
    {
        [[AppDelegate currentDelegate].backgroundMusicPlayer pause];
    }
    else
    {
        [[AppDelegate currentDelegate].backgroundMusicPlayer play];
    }
}

- (IBAction)handlePauseSoundSwitch:(id)sender
{
    BOOL mute = !pauseSwtSound.isOn;
    [[NSUserDefaults standardUserDefaults] setBool:mute forKey:@"sound-mute"];
    [[FISoundEngine sharedEngine] setMuted:mute];
}

- (IBAction)handleShareClick:(id)sender
{
    UIButton * button = sender;
    int mins = (int)([GameLogic sharedLogic].gameTime / 60);
    NSString * message = [NSString stringWithFormat:@"Я только что разгадал сканворд %@ за %d %@ и получил за это %d %@!", gameField.puzzle.name, mins, [NSString declesion:mins oneString:@"минуту" twoString:@"минуты" fiveString:@"минут"], gameField.puzzle.score.intValue, [NSString declesion:gameField.puzzle.score.intValue oneString:@"очко" twoString:@"очка" fiveString:@"очков"]];
    // facebook
    if (button.tag == 0)
    {
        // Put together the dialog parameters
        NSMutableDictionary *params =
        [NSMutableDictionary dictionaryWithObjectsAndKeys:
         @"PrizeWord", @"name",
         message, @"caption",
         @"http://prize-word.com", @"link",
         nil];
        
        void (^publishHandler)(FBSession *session, NSError *error) = ^(FBSession *session, NSError *error) {
            [self hideActivityIndicator];
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
        };
        
        void (^loginHandler)(FBSession *session, FBSessionState state, NSError *error) = ^(FBSession *session, FBSessionState state, NSError *error) {
            [self hideActivityIndicator];
            if (error == nil && (state == FBSessionStateOpen || state == FBSessionStateOpenTokenExtended))
            {
                publishHandler(session, error);
                return;
            }
            if (error != nil)
            {
                UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка facebook" message:error.localizedDescription delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
                [alert show];
            }
        };
        
        [self showActivityIndicator];
        if ([[FBSession activeSession] isOpen])
        {
            [[FBSession activeSession] requestNewPublishPermissions:[NSArray arrayWithObjects:@"publish_actions", @"publish_stream", nil] defaultAudience:FBSessionDefaultAudienceEveryone completionHandler:publishHandler];
        }
        else
        {
            [FBSession openActiveSessionWithPublishPermissions:[NSArray arrayWithObjects:@"publish_actions", @"publish_stream", nil] defaultAudience:FBSessionDefaultAudienceEveryone allowLoginUI:YES completionHandler:loginHandler];
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
    else if (event.type == EVENT_PRODUCT_ERROR || event.type == EVENT_PRODUCT_FAILED)
    {
        [self hideActivityIndicator];
        [textField becomeFirstResponder];
    }
    else if (event.type == EVENT_PRODUCT_BOUGHT)
    {
        SKPaymentTransaction * paymentTransaction = event.data;
        NSLog(@"EVENT_PRODUCT_BOUGHT: %@", paymentTransaction.payment.productIdentifier);
        
        if ([paymentTransaction.payment.productIdentifier compare:PRODUCTID_HINTS10] == NSOrderedSame)
        {
            APIRequest * request = [APIRequest postRequest:@"hints" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
                NSLog(@"hints: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
                SBJsonParser * parser = [SBJsonParser new];
                NSDictionary * dict = [parser objectWithData:receivedData];
                [GlobalData globalData].loggedInUser = [UserData userDataWithDictionary:[dict objectForKey:@"me"]];
                [btnHint setTitle:[NSString stringWithFormat:@"%d", [GlobalData globalData].loggedInUser.hints] forState:UIControlStateNormal];
                [self hideActivityIndicator];
                [self handleHintClick:nil];
            } failCallback:^(NSError *error) {
                [self hideActivityIndicator];
                [textField becomeFirstResponder];
                NSLog(@"hints error: %@", error.description);
            }];
            [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
            [request.params setObject:[NSString stringWithFormat:@"%d", 10] forKey:@"hints_change"];
            [request runUsingCache:NO silentMode:YES];
        }
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
    if (alertView.tag == TAG_USEHINT)
    {
        APIRequest * request = [APIRequest postRequest:@"hints" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
            SBJsonParser * parser = [SBJsonParser new];
            NSDictionary * data = [parser objectWithData:receivedData];
            [GlobalData globalData].loggedInUser = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
            [btnHint setTitle:[NSString stringWithFormat:@"%d", [GlobalData globalData].loggedInUser.hints] forState:UIControlStateNormal];
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_REQUEST_APPLY_HINT]];
        } failCallback:^(NSError *error) {
            UserData * loggedInUser = [GlobalData globalData].loggedInUser;
            loggedInUser.hints--;
            [GlobalData globalData].loggedInUser = loggedInUser;
            [btnHint setTitle:[NSString stringWithFormat:@"%d", [GlobalData globalData].loggedInUser.hints] forState:UIControlStateNormal];
            NSString * savedHintsKey = [NSString stringWithFormat:@"savedHints%@", [GlobalData globalData].loggedInUser.user_id];
            int savedHints = [[NSUserDefaults standardUserDefaults] integerForKey:savedHintsKey];
            savedHints--;
            [[NSUserDefaults standardUserDefaults] setInteger:savedHints forKey:savedHintsKey];
            
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_REQUEST_APPLY_HINT]];
        }];
        [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
        [request.params setObject:@"-1" forKey:@"hints_change"];
        [request runUsingCache:NO silentMode:YES];
    }
    else if (alertView.tag == TAG_BUYHINTS)
    {
        [self showActivityIndicator];
        SKProductsRequest * productRequest = [[SKProductsRequest alloc] initWithProductIdentifiers:[NSSet setWithObject:PRODUCTID_HINTS10]];
        productRequest.delegate = self;
        [productRequest start];
    }
}

-(void)animateFinalScreenAppears:(id)sender
{
    NSTimer * timer = sender;
    PuzzleData * puzzleData = timer.userInfo;
    
    CGRect shareFrame = finalShareView.frame;
    shareFrame.origin.y = [AppDelegate currentDelegate].isIPad ? 402 : 308;
    
    int baseScore = [[GlobalData globalData] baseScoreForType:puzzleData.puzzleSet.type.intValue];
    lblFinalBaseScore.text = [NSString stringWithFormat:@"%d", [puzzleData.score unsignedIntValue] < baseScore ? 0 : baseScore];
    lblFinalTimeBonus.text = [NSString stringWithFormat:@"%d", [puzzleData.score unsignedIntValue] < baseScore ? [puzzleData.score unsignedIntValue] : ([puzzleData.score unsignedIntValue] - baseScore)];
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

#pragma mark SKProductRequestDelegate

- (void)request:(SKRequest *)request didFailWithError:(NSError *)error
{
    [self hideActivityIndicator];
    [textField becomeFirstResponder];
    [[[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:NSLocalizedString(@"Connection error", @"Connection error") delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] show];
}

- (void)productsRequest:(SKProductsRequest *)request didReceiveResponse:(SKProductsResponse *)response
{
    if (response.products.count > 0)
    {
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_REQUEST_PRODUCT andData:[response.products lastObject]]];
    }
    else
    {
        [self hideActivityIndicator];
        [textField becomeFirstResponder];
    }
}

@end
