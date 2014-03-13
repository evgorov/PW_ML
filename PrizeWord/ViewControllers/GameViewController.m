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
#import "SBJson.h"
#import "PuzzleProxy.h"
#import "PuzzleSetProxy.h"
#import "FlipNumberView.h"
#import "SocialNetworks.h"
#import "PrizeWordButton.h"
#import "FISoundEngine.h"
#import "NSString+Utils.h"
#import <FacebookSDK/FacebookSDK.h>
#import <StoreKit/StoreKit.h>
#import "UserDataManager.h"

const int TAG_USEHINT = 100;
const int TAG_BUYHINTS = 101;
extern NSString * PRODUCTID_HINTS10;

const int TAG_FINAL_BASE_SCORE = 103;
const int TAG_FINAL_TIME_BONUS = 104;
const int TAG_FINAL_RATE_BONUS = 105;
const int TAG_FINAL_FLIPNUMBER0 = 106;
const int TAG_FINAL_FLIPNUMBER1 = 107;
const int TAG_FINAL_FLIPNUMBER2 = 108;
const int TAG_FINAL_FLIPNUMBER3 = 109;
const int TAG_FINAL_FLIPNUMBER4 = 110;
const int TAG_FINAL_FLIPNUMBER5 = 111;
const int TAG_FINAL_FACEBOOK_SCORE = 112;
const int TAG_FINAL_VKONTAKTE_BONUS = 113;
const int TAG_FINAL_SET_TYPE = 114;
const int TAG_FINAL_MENU = 115;
const int TAG_FINAL_NEXT = 116;
const int TAG_FINAL_CONTINUE = 117;

const int FINAL_OVERVIEW_TYPE_ORDINARY = 1;
const int FINAL_OVERVIEW_TYPE_RATE = 2;
const int FINAL_OVERVIEW_TYPE_RATE_DONE = 3;
const int FINAL_OVERVIEW_TYPE_SET = 4;
const int FINAL_OVERVIEW_TYPE_SET_VK_DONE = 5;
const int FINAL_OVERVIEW_TYPE_SET_FB_DONE = 6;
const int FINAL_OVERVIEW_TYPE_SET_DONE = 7;

NSString *reviewURL = @"itms-apps://ax.itunes.apple.com/WebObjects/MZStore.woa/wa/viewContentsUserReviews?type=Purple+Software&id=725511947";
NSString *reviewURLiOS7 = @"itms-apps://itunes.apple.com/app/id725511947";

@interface GameViewController (private)

-(void)handleKeyboardWillShow:(NSNotification *)aNotification;
-(void)handleKeyboardWillHide:(NSNotification *)aNotification;
-(void)animateFinalScreenAppears:(id)sender;
-(void)showFinalScreenAnimated:(BOOL)animated;

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
    finalFlipNumbers = nil;
    
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
    if ([[UIDevice currentDevice].systemVersion compare:@"7.0" options:NSNumericSearch] != NSOrderedAscending)
    {
        CGRect frame = btnHint.frame;
        frame.origin = CGPointMake(frame.origin.x + 10, frame.origin.y);
        btnHint.frame = frame;
    }
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
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_ME_UPDATED];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    
    BOOL soundMute = [[NSUserDefaults standardUserDefaults] boolForKey:@"sound-mute"];
    BOOL musicMute = ![[NSUserDefaults standardUserDefaults] boolForKey:@"music-unmute"];
    [pauseSwtSound setOn:!soundMute animated:animated];
    [pauseSwtMusic setOn:!musicMute animated:animated];
    
    if (!musicMute && [AppDelegate currentDelegate].backgroundMusicPlayer != nil)
    {
        [[AppDelegate currentDelegate].backgroundMusicPlayer play];
    }
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
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_ME_UPDATED];
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

    [[AppDelegate currentDelegate].backgroundMusicPlayer pause];
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
    if ([AppDelegate currentDelegate].rootViewController.currentOverlay == nil)
    {
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_REQUEST_FINISH_INPUT]];
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_PAUSE]];
    }
}

- (IBAction)handlePlayClick:(id)sender
{
    if ([AppDelegate currentDelegate].rootViewController.currentOverlay != nil)
    {
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_RESUME]];
    }
}

- (IBAction)handleHintClick:(id)sender
{
    int hints = [GlobalData globalData].loggedInUser.hints;
    if ([GameLogic sharedLogic].gameField.activeQuestion != nil || hints == 0)
    {
        [textField resignFirstResponder];

        if (hints > 0)
        {
            UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"TITLE_USE_HINT", nil) message:NSLocalizedString(@"QUESTION_USE_HINT", nil) delegate:self cancelButtonTitle:NSLocalizedString(@"BUTTON_CANCEL", nil) otherButtonTitles:NSLocalizedString(@"BUTTON_USE_HINT", nil), nil];
            alertView.tag = TAG_USEHINT;
            [alertView show];
        }
        else
        {
            UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"TITLE_BUY_HINTS", nil) message:NSLocalizedString(@"QUESTION_BUY_HINTS", nil) delegate:self cancelButtonTitle:NSLocalizedString(@"BUTTON_CANCEL", nil) otherButtonTitles:NSLocalizedString(@"BUTTON_BUY_HINTS", nil), nil];
            alertView.tag = TAG_BUYHINTS;
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_PAUSE]];
            [alertView show];
        }
    }
}

- (IBAction)handlePauseNext:(id)sender
{
    [[AppDelegate currentDelegate].rootViewController hideOverlay];
    [gameField.puzzle synchronize];
    PuzzleProxy * puzzle = gameField.puzzle;
    PuzzleSetProxy * puzzleSet = puzzle.puzzleSet;
    BOOL selectNext = NO;
    PuzzleProxy * nextPuzzle = nil;
    NSArray * orderedPuzzles = puzzleSet.orderedPuzzles;
    for (PuzzleProxy * otherPuzzle in orderedPuzzles)
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
        for (PuzzleProxy * otherPuzzle in orderedPuzzles) {
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

- (IBAction)handlePauseContinue:(id)sender
{
    [[AppDelegate currentDelegate].rootViewController hideOverlayAnimated:YES];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.6 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self prepareFinalOverlayWithType: FINAL_OVERVIEW_TYPE_SET];
        [self showFinalScreenAnimated:YES];
        [[AppDelegate currentDelegate].rootViewController showFullscreenOverlay:finalOverlay animated:YES];
    });
}

- (IBAction)handlePauseMusicSwitch:(id)sender
{
    BOOL mute = !pauseSwtMusic.isOn;
    [[NSUserDefaults standardUserDefaults] setBool:!mute forKey:@"music-unmute"];
    [[NSUserDefaults standardUserDefaults] synchronize];
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
    [[NSUserDefaults standardUserDefaults] synchronize];
    [[FISoundEngine sharedEngine] setMuted:mute];
}

- (IBAction)handleShareClick:(id)sender
{
    UIButton * button = sender;
    NSString * puzzleType = @"";
    switch (puzzleData.puzzleSet.type.intValue) {
        case PUZZLESET_BRILLIANT:
            puzzleType = @"бриллиантовый ";
            break;
            
        case PUZZLESET_FREE:
            puzzleType = @"бесплатный ";
            break;
            
        case PUZZLESET_GOLD:
            puzzleType = @"золотой ";
            break;
            
        case PUZZLESET_SILVER:
        case PUZZLESET_SILVER2:
            puzzleType = @"серебряный ";
            break;

        default:
            break;
    }
    NSString * message = [NSString stringWithFormat:@"Я только что разгадал %@сет и получил за это %d %@!", puzzleType, puzzleData.puzzleSet.score, [NSString declesion:puzzleData.puzzleSet.score oneString:@"очко" twoString:@"очка" fiveString:@"очков"]];
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
                                 [[UserDataManager sharedManager] addScore:[GlobalData globalData].scoreForShare forKey:[NSString stringWithFormat:@"shareset|facebook|%@", puzzleData.puzzleSet.set_id]];

                                 [[AppDelegate currentDelegate].rootViewController hideOverlayAnimated:NO];
                                 if (finalOverlay == finalOverlaySet)
                                 {
                                     [self prepareFinalOverlayWithType: FINAL_OVERVIEW_TYPE_SET_FB_DONE];
                                 }
                                 else
                                 {
                                     [self prepareFinalOverlayWithType: FINAL_OVERVIEW_TYPE_SET_DONE];
                                 }
                                 [self showFinalScreenAnimated:NO];
                                 [[AppDelegate currentDelegate].rootViewController showFullscreenOverlay:finalOverlay animated:NO];
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
            NSDictionary * params = @{@"session_key": [GlobalData globalData].sessionKey
                                      , @"message": message};
            [[APIClient sharedClient] postPath:@"vkontakte/share" parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
                UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:@"PrizeWord" message:@"Ваш результат опубликован!" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
                [alertView show];

                [[UserDataManager sharedManager] addScore:[GlobalData globalData].scoreForShare forKey:[NSString stringWithFormat:@"shareset|vkontakte|%@", puzzleData.puzzleSet.set_id]];
                
                [[AppDelegate currentDelegate].rootViewController hideOverlayAnimated:NO];
                if (finalOverlay == finalOverlaySet)
                {
                    [self prepareFinalOverlayWithType: FINAL_OVERVIEW_TYPE_SET_VK_DONE];
                }
                else
                {
                    [self prepareFinalOverlayWithType: FINAL_OVERVIEW_TYPE_SET_DONE];
                }
                [self showFinalScreenAnimated:NO];
                [[AppDelegate currentDelegate].rootViewController showFullscreenOverlay:finalOverlay animated:NO];
            } failure:nil];
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

- (IBAction)handleRateClick:(id)sender
{
    BOOL isIOS7 = [[UIDevice currentDevice].systemVersion compare:@"7.0" options:NSNumericSearch] != NSOrderedAscending;
    if (![[UIApplication sharedApplication] openURL:[NSURL URLWithString:isIOS7 ? reviewURLiOS7 : reviewURL]])
    {
        [[[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"") message:NSLocalizedString(@"Cannot open AppStore. Please try again later.", @"") delegate:nil cancelButtonTitle:NSLocalizedString(@"OK", @"") otherButtonTitles:nil] show];
        return;
    }
    
    double delayInSeconds = 0.1;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        [[AppDelegate currentDelegate].rootViewController hideOverlayAnimated:NO];
        
        [self prepareFinalOverlayWithType:FINAL_OVERVIEW_TYPE_RATE_DONE];
        [self showFinalScreenAnimated:NO];
        [[AppDelegate currentDelegate].rootViewController showFullscreenOverlay:finalOverlay animated:NO];
    });
    
    [[UserDataManager sharedManager] addScore:[GlobalData globalData].scoreForRate forKey:@"rateapp"];
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
        if (gameField.puzzle.time_left.intValue > gameField.puzzle.time_given.intValue)
        {
            NSLog(@"WARNING: time left is bigger than given time 2");
        }
        
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
        
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 1 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
            BOOL onlyFree = YES;
            NSArray * monthSets = [GlobalData globalData].monthSets;
            for (PuzzleSetProxy * puzzleSet in monthSets) {
                if (puzzleSet.bought.boolValue && puzzleSet.type.intValue != PUZZLESET_FREE)
                {
                    onlyFree = NO;
                    break;
                }
            }
            
            NSString * showRateKey = [NSString stringWithFormat:@"showRate%d%d", [GlobalData globalData].currentYear, [GlobalData globalData].currentMonth];
            puzzleData = event.data;
            // DEBUG :: set YES for test purposes

            if (![GlobalData globalData].loggedInUser.is_app_rated && ![[NSUserDefaults standardUserDefaults] boolForKey:showRateKey] && ((puzzleData.puzzleSet.type.intValue != PUZZLESET_FREE && puzzleData.time_left.intValue > 0) || (puzzleData.puzzleSet.percent >= 0.999999 && onlyFree)))
            {
                [self prepareFinalOverlayWithType:FINAL_OVERVIEW_TYPE_RATE];
            }
            else
            {
                if (gameField.puzzle.puzzleSet.type.intValue == PUZZLESET_FREE)
                {
                    [self.navigationController popViewControllerAnimated:YES];
                    return;
                }
                [self prepareFinalOverlayWithType:FINAL_OVERVIEW_TYPE_ORDINARY];
            }

            
            [NSTimer scheduledTimerWithTimeInterval:0.5f target:self selector:@selector(animateFinalScreenAppears:) userInfo:event.data repeats:NO];
            
            [[AppDelegate currentDelegate].rootViewController showFullscreenOverlay:finalOverlay];
        });
    }
    else if (event.type == EVENT_PRODUCT_ERROR || event.type == EVENT_PRODUCT_FAILED)
    {
        [self hideActivityIndicator];
        [textField becomeFirstResponder];
        if ([AppDelegate currentDelegate].rootViewController.currentOverlay != nil)
        {
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_RESUME]];
        }
    }
    else if (event.type == EVENT_PRODUCT_BOUGHT)
    {
        SKPaymentTransaction * paymentTransaction = event.data;
        NSLog(@"EVENT_PRODUCT_BOUGHT: %@", paymentTransaction.payment.productIdentifier);
        [self hideActivityIndicator];
        if ([AppDelegate currentDelegate].rootViewController.currentOverlay != nil)
        {
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_RESUME]];
        }
    }
    else if (event.type == EVENT_ME_UPDATED)
    {
        UserData * user = event.data;
        if (user != nil)
        {
            [btnHint setTitle:[NSString stringWithFormat:@"%d", user.hints] forState:UIControlStateNormal];
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
        if (typeSounds.count > 0)
        {
            [(FISound *)[typeSounds objectAtIndex:(rand() % typeSounds.count)] play];
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
        [textField becomeFirstResponder];
        if ([AppDelegate currentDelegate].rootViewController.currentOverlay != nil)
        {
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_RESUME]];
        }
        return;
    }
    if (alertView.tag == TAG_USEHINT)
    {
        [[UserDataManager sharedManager] addHints:-1 withKey:[NSString stringWithFormat:@"%lld%04d", (long long)[[NSDate date] timeIntervalSince1970], rand() % 1000]];
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_REQUEST_APPLY_HINT]];
    }
    else if (alertView.tag == TAG_BUYHINTS)
    {
        [self showActivityIndicator];
        SKProductsRequest * productRequest = [[SKProductsRequest alloc] initWithProductIdentifiers:[NSSet setWithObject:PRODUCTID_HINTS10]];
        productRequest.delegate = self;
        [productRequest start];
    }
}

- (void)prepareFinalOverlayWithType:(int)type
{
    switch (type) {
        case FINAL_OVERVIEW_TYPE_ORDINARY:
            finalOverlay = finalOverlayOrdinary;
            break;
            
        case FINAL_OVERVIEW_TYPE_RATE:
            finalOverlay = finalOverlayRate;
            break;
            
        case FINAL_OVERVIEW_TYPE_RATE_DONE:
            finalOverlay = finalOverlayRateDone;
            break;
            
        case FINAL_OVERVIEW_TYPE_SET:
            finalOverlay = finalOverlaySet;
            break;
            
        case FINAL_OVERVIEW_TYPE_SET_FB_DONE:
            finalOverlay = finalOverlaySetFBDone;
            break;
            
        case FINAL_OVERVIEW_TYPE_SET_VK_DONE:
            finalOverlay = finalOverlaySetVKDone;
            break;
            
        case FINAL_OVERVIEW_TYPE_SET_DONE:
            finalOverlay = finalOverlaySetDone;
            break;
            
        default:
            break;
    }
    
    if (type == FINAL_OVERVIEW_TYPE_RATE)
    {
        NSString * showRateKey = [NSString stringWithFormat:@"showRate%d%d", [GlobalData globalData].currentYear, [GlobalData globalData].currentMonth];
        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:showRateKey];
    }

    lblFinalBaseScore = (UILabel *)[finalOverlay viewWithTag:TAG_FINAL_BASE_SCORE];
    lblFinalTimeBonus = (UILabel *)[finalOverlay viewWithTag:TAG_FINAL_TIME_BONUS];
    lblFinalRateBonus = (UILabel *)[finalOverlay viewWithTag:TAG_FINAL_RATE_BONUS];
    lblFinalFacebookBonus = (UILabel *)[finalOverlay viewWithTag:TAG_FINAL_FACEBOOK_SCORE];
    lblFinalVkontakteBonus = (UILabel *)[finalOverlay viewWithTag:TAG_FINAL_VKONTAKTE_BONUS];
    finalFlipNumbers = [NSMutableArray new];
    for (int i = 0; i < 6; ++i)
    {
        FlipNumberView * view = (FlipNumberView *)[finalOverlay viewWithTag:TAG_FINAL_FLIPNUMBER0 + i];
        if (view != nil)
        {
            [finalFlipNumbers addObject:view];
            [view reset];
        }
    }

    int baseScore = [[GlobalData globalData] baseScoreForType:puzzleData.puzzleSet.type.intValue];
    int fullScore = [puzzleData.score unsignedIntValue];
    if (finalOverlay != finalOverlayOrdinary && finalOverlay != finalOverlayRateDone && finalOverlay != finalOverlayRate)
    {
        baseScore *= puzzleData.puzzleSet.total;
        fullScore = puzzleData.puzzleSet.score;
    }
    lblFinalBaseScore.text = [NSString stringWithFormat:@"%d", fullScore < baseScore ? 0 : baseScore];
    lblFinalTimeBonus.text = [NSString stringWithFormat:@"%d", fullScore < baseScore ? fullScore : (fullScore - baseScore)];
    lblFinalRateBonus.text = [NSString stringWithFormat:@"+%d", [GlobalData globalData].scoreForRate];
    lblFinalFacebookBonus.text = [NSString stringWithFormat:@"+%d", [GlobalData globalData].scoreForShare];
    lblFinalVkontakteBonus.text = [NSString stringWithFormat:@"+%d", [GlobalData globalData].scoreForShare];
    
    UIImage * setTypeImage = nil;
    switch (puzzleData.puzzleSet.type.intValue) {
        case PUZZLESET_BRILLIANT:
            setTypeImage = [UIImage imageNamed:@"puzzles_set_br.png"];
            break;

        case PUZZLESET_FREE:
            setTypeImage = [UIImage imageNamed:@"puzzles_set_fr.png"];
            break;

        case PUZZLESET_GOLD:
            setTypeImage = [UIImage imageNamed:@"puzzles_set_au.png"];
            break;

        case PUZZLESET_SILVER:
            setTypeImage = [UIImage imageNamed:@"puzzles_set_ag.png"];
            break;
            
        case PUZZLESET_SILVER2:
            setTypeImage = [UIImage imageNamed:@"puzzles_set_ag2.png"];
            break;
            
        default:
            break;
    }
    imgFinalSetType = (UIImageView *)[finalOverlay viewWithTag:TAG_FINAL_SET_TYPE];
    [imgFinalSetType setImage:setTypeImage];
    
    btnFinalMenu = (UIButton *)[finalOverlay viewWithTag:TAG_FINAL_MENU];
    btnFinalNext = (UIButton *)[finalOverlay viewWithTag:TAG_FINAL_NEXT];
    btnFinalContinue = (UIButton *)[finalOverlay viewWithTag:TAG_FINAL_CONTINUE];
    // DEBUG :: invert conditions for test purposes
    btnFinalMenu.hidden = puzzleData.puzzleSet.type.intValue != PUZZLESET_FREE && puzzleData.puzzleSet.percent >= 0.99999;
    btnFinalNext.hidden = btnFinalMenu.hidden;
    btnFinalContinue.hidden = !btnFinalMenu.hidden;
}

-(void)animateFinalScreenAppears:(id)sender
{
    NSTimer * timer = sender;
    if (timer != nil && timer.userInfo != nil) {
        puzzleData = timer.userInfo;
    }
    [self showFinalScreenAnimated:YES];
}

- (void)showFinalScreenAnimated:(BOOL)animated
{
    lblFinalBaseScore.frame = CGRectMake(lblFinalBaseScore.frame.origin.x, lblFinalBaseScore.frame.origin.y, [lblFinalBaseScore.text sizeWithFont:lblFinalBaseScore.font].width, lblFinalBaseScore.frame.size.height);
    lblFinalTimeBonus.frame = CGRectMake(lblFinalTimeBonus.frame.origin.x, lblFinalTimeBonus.frame.origin.y, [lblFinalTimeBonus.text sizeWithFont:lblFinalTimeBonus.font].width, lblFinalTimeBonus.frame.size.height);
    
    __block uint score = finalFlipNumbers.count == 5 ? [puzzleData.score unsignedIntValue] : puzzleData.puzzleSet.score;
    if (finalOverlay == finalOverlayRateDone)
    {
        score += [GlobalData globalData].scoreForRate;
    }
    if (finalOverlay == finalOverlaySetDone)
    {
        score += [GlobalData globalData].scoreForShare * 2;
    }
    if (finalOverlay == finalOverlaySetVKDone)
    {
        score += [GlobalData globalData].scoreForShare;
    }
    if (finalOverlay == finalOverlaySetFBDone)
    {
        score += [GlobalData globalData].scoreForShare;
    }
    if (animated)
    {
        lblFinalBaseScore.transform = CGAffineTransformMakeScale(0.01f, 0.01f);
        lblFinalBaseScore.hidden = YES;
        lblFinalTimeBonus.transform = CGAffineTransformMakeScale(0.01f, 0.01f);
        lblFinalTimeBonus.hidden = YES;
        lblFinalBaseScore.hidden = NO;
        lblFinalRateBonus.hidden = YES;
        if (finalOverlay == finalOverlayRate)
        {
            lblFinalRateBonus.hidden = NO;
        }
        
        
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
                // TODO :: loop counting sound
                [countingSound play];
                int lastI = 0;
                for (int i = 0; i < finalFlipNumbers.count; ++i)
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
        
        if (finalOverlay == finalOverlayRateDone)
        {
            [UIView animateWithDuration:0.25f delay:1 options:UIViewAnimationOptionCurveEaseIn animations:^{
                lblFinalRateBonus.transform = CGAffineTransformMakeScale(2.0f, 2.0f);
            } completion:^(BOOL finished) {
                [secondaryDingSound play];
                [UIView animateWithDuration:0.25f delay:0 options:UIViewAnimationOptionCurveEaseOut animations:^{
                    lblFinalRateBonus.transform = CGAffineTransformMakeScale(1, 1);
                } completion:nil];
            }];
        }
    }
    else
    {
        lblFinalTimeBonus.hidden = NO;
        lblFinalBaseScore.hidden = NO;
        lblFinalRateBonus.hidden = NO;
        lblFinalBaseScore.transform = CGAffineTransformMakeScale(1, 1);
        lblFinalTimeBonus.transform = CGAffineTransformMakeScale(1, 1);
        lblFinalRateBonus.transform = CGAffineTransformMakeScale(1, 1);
        
        // TODO :: loop counting sound
        [countingSound play];
        int lastI = 0;
        for (int i = 0; i < finalFlipNumbers.count; ++i)
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
    }
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
