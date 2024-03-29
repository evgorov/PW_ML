//
//  RootViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "RootViewController.h"
#import "GameViewController.h"
#import "PuzzlesViewController.h"
#import "ScoreViewController.h"
#import "InviteViewController.h"
#import "RatingViewController.h"
#import "PrizeWordNavigationController.h"
#import "PrizeWordNavigationBar.h"
#import "AppDelegate.h"
#import "GlobalData.h"
#import "UserData.h"
#import "EventManager.h"
#import "SocialNetworks.h"
#import <MobileCoreServices/UTCoreTypes.h>
#import "SBJsonParser.h"
#import "FISoundEngine.h"
#import "NSString+Utils.h"
#import <QuartzCore/CALayer.h>
#import "UserDataManager.h"

@interface RootViewController (private)

-(void)updateUserInfo;

-(void)handlePageButtonClick:(id)sender;
-(void)handleRulesMenuClick:(id)sender;

-(void)handleRulesPrev:(id)sender;
-(void)handleRulesNext:(id)sender;
-(void)handleRulesTap:(id)sender;
-(void)updatePageButtons:(int)currentPage;

-(void)handleSwipeLeft:(id)sender;
-(void)handleSwipeRight:(id)sender;

-(void)startCameraControllerWithSourceType:(UIImagePickerControllerSourceType)sourceType;

@end

NSString * MONTHS3[] = {@"январе", @"феврале", @"марте", @"апреле", @"мае", @"июне", @"июле", @"августе", @"сентябре", @"октябре", @"ноябре", @"декабре"};

const int RULES_PAGES = 12;
NSString * RULES_TEXTS[RULES_PAGES] = {@"Разгадывайте и участвуйте в рейтинге! Каждый месяц лидеры рейтинга получают денежный приз! Все подробности на сайте\nwww.prize-word.com"
    , @"Чтобы разгадать сканворд, нажмите на иконку в меню «Мои сканворды»."
    , @"Нажатие на клетку с вопросом выделит слово. Вписывайте буквы с помощью клавиатуры."
    , @"Регулируйте размер поля."
    , @"Правильное слово будет выделено фоном и звуковым сигналом. Если слово набрано неверно, клетка с вопросом будет выделена красным цветом. Буквы в пересечениях набирать не нужно."
    , @"В главном меню вы видите все сканворды месяца. Бриллиантовые сканворды самые сложные, но они приносят больше всего баллов – минимум 8 999 за каждый!"
    , @"Золотые сканворды – принесут не меньше 5 999 за каждый. Серебряные – не меньше 3 999 за один разгаданный сканворд."
    , @"Бесплатные сканворды баллов не дают!\nДалее расположены пакеты Подсказок. Одна подсказка – одно выбранное слово, которое будет вписано автоматически."
    , @"Разгадайте сканворд быстрее, чем за 15 минут, и получите дополнительные баллы! Нажав на Паузу слева, вы приостановите разгадывание и отсчет времени."
    , @"Нажатие на кнопку Меню покажет ваш текущий рейтинг, лучший результат месяца и позволит пригласить в игру друзей.\nКаждый скачавший игру друг принесет вам дополнительные баллы!"
    , @"Рейтинг - это сумма баллов, набранных за месяц. Он фиксируется первого числа каждого месяца, в момент появления новых сканвордов. Лидеры рейтинга получают денежный приз! В новом месяце – новый рейтинг!"
    , @"PrizeWord – увлекательно, полезно и выгодно!\nОрганизатором конкурса является Faisode limited. Все призы выплачивает Faisode limited."
};


@implementation RootViewController

@synthesize currentOverlay = _currentOverlay;
@synthesize isMenuHidden = _isMenuHidden;

-(id)initWithNavigationController:(PrizeWordNavigationController *)navigationController
{
    self = [super init];
    if (self)
    {
        navController = navigationController;
        _isMenuHidden = YES;
        sidebarSound = [[FISoundEngine sharedEngine] soundNamed:@"sidebar.caf" error:nil];
    }
    return self;
}

-(void)viewDidLoad
{
    self.view.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_black_tile.jpg"]];
    
    mainMenuView.contentSize = mainMenuBg.frame.size;
    mainMenuView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"menu_bg_tile.jpg"]];
    mainMenuYourResult.font = [UIFont fontWithName:@"DINPro-Bold" size:[AppDelegate currentDelegate].isIPad ? 18 : 15];
    mainMenuYourResult.text = [NSString stringWithFormat:@"Ваш результат в %@", MONTHS3[[GlobalData globalData].currentMonth - 1]];
    
    // iPhone 5
    if ([UIScreen mainScreen].bounds.size.height == 568)
    {
        CGRect frameRect = overlayContainer.frame;
        frameRect.size.height = 568 - (480 - frameRect.size.height);
        overlayContainer.frame = frameRect;
    }

    
    [self.view addSubview:navController.view];
    navController.view.frame = self.view.frame;
    if (_isMenuHidden)
    {
        [self hideMenuAnimated:NO];
    }
    else
    {
        [self showMenuAnimated:NO];
    }
    
    PrizeWordSwitchView * switchView = [PrizeWordSwitchView switchView];
    switchView.frame = mainMenuVKSwitch.frame;
    [mainMenuVKSwitch.superview addSubview:switchView];
    [mainMenuVKSwitch removeFromSuperview];
    [switchView addTarget:self action:@selector(handleVKSwitchChange:) forControlEvents:UIControlEventValueChanged];
    mainMenuVKSwitch = switchView;
    
    switchView = [PrizeWordSwitchView switchView];
    switchView.frame = mainMenuFBSwitch.frame;
    [mainMenuFBSwitch.superview addSubview:switchView];
    [mainMenuFBSwitch removeFromSuperview];
    [switchView addTarget:self action:@selector(handleFBSwitchChange:) forControlEvents:UIControlEventValueChanged];
    mainMenuFBSwitch = switchView;
    
    switchView = [PrizeWordSwitchView switchView];
    switchView.frame = mainMenuNotificationsSwitch.frame;
    [mainMenuNotificationsSwitch.superview addSubview:switchView];
    [mainMenuNotificationsSwitch removeFromSuperview];
    [switchView addTarget:self action:@selector(handleNotificationSwitchChange:) forControlEvents:UIControlEventValueChanged];
    mainMenuNotificationsSwitch = switchView;
    [mainMenuNotificationsSwitch setOn:![[NSUserDefaults standardUserDefaults] boolForKey:@"remote-notifications-disabled"] animated:YES];

    UISwipeGestureRecognizer * swipeLeftGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipeLeft:)];
    swipeLeftGestureRecognizer.direction = UISwipeGestureRecognizerDirectionLeft;
    UISwipeGestureRecognizer * swipeRightGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipeRight:)];
    swipeRightGestureRecognizer.direction = UISwipeGestureRecognizerDirectionRight;
    self.view.gestureRecognizers = [NSArray arrayWithObjects:swipeLeftGestureRecognizer, swipeRightGestureRecognizer, nil];
    
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_ME_UPDATED];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_SESSION_ENDED];
}

- (void)viewDidUnload
{
    btnScore = nil;
    btnRating = nil;
    mainMenuView = nil;
    mainMenuBg = nil;
    rulesView = nil;
    fullscreenOverlayContainer = nil;
    mainMenuVKSwitch = nil;
    mainMenuFBSwitch = nil;
    mainMenuNotificationsSwitch = nil;
    mainMenuAvatar = nil;
    mainMenuMaxScore = nil;
    mainMenuUserName = nil;
    mainMenuYourResult = nil;
    
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_ME_UPDATED];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_SESSION_ENDED];
    
    rulesCaption = nil;

    mainMenuAvatarActivityIndicator = nil;
    [super viewDidUnload];
}

-(void)orientationChanged:(UIDeviceOrientation)orientation
{
    
        if (_currentOverlay != nil && _currentOverlay.superview == overlayContainer)
        {
            [UIView animateWithDuration:0.5f animations:^{
                if ([AppDelegate currentDelegate].isIPad)
                {
                    overlayContainer.frame = CGRectMake(0, 70, self.view.bounds.size.width, self.view.bounds.size.height - 70);
                }
                else
                {
                    overlayContainer.frame = CGRectMake(0, self.view.frame.size.height - overlayContainer.frame.size.height, overlayContainer.frame.size.width, overlayContainer.frame.size.height);
                }
                _currentOverlay.frame = CGRectMake((overlayContainer.frame.size.width - _currentOverlay.frame.size.width) / 2, 0, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
            } completion:^(BOOL finished) {
                /*
                gpuImageView.frame = CGRectMake(0, -overlayContainer.frame.origin.y, self.view.frame.size.width, self.view.frame.size.height);
                [uiElementInput update];
                */
            }];
        }
        else if (_currentOverlay != nil && _currentOverlay.superview == fullscreenOverlayContainer)
        {
            [UIView animateWithDuration:0.5f animations:^{
                fullscreenOverlayContainer.frame = CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height);
                _currentOverlay.frame = CGRectMake((fullscreenOverlayContainer.frame.size.width - _currentOverlay.frame.size.width) / 2, (fullscreenOverlayContainer.frame.size.height - _currentOverlay.frame.size.height) / 2, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
            } completion:^(BOOL finished) {
                /*
                gpuImageView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
                [uiElementInput update];
                */
            }];
        }
}

-(void)handleEvent:(Event *)event
{
    if (event.type == EVENT_ME_UPDATED)
    {
        [self updateUserInfo];
    }
    else if (event.type == EVENT_SESSION_ENDED)
    {
        [self handleSwitchUserClick:self];
    }
}

-(void)updateUserInfo
{
    NSLog(@"update user info");
    mainMenuMaxScore.text = [NSString stringWithFormat:@"%d %@", [GlobalData globalData].loggedInUser.high_score, [NSString declesion:[GlobalData globalData].loggedInUser.high_score oneString:@"очко" twoString:@"очка" fiveString:@"очков"]];
    mainMenuUserName.text = [NSString stringWithFormat:@"%@ %@", [GlobalData globalData].loggedInUser.first_name, [GlobalData globalData].loggedInUser.last_name];
    [mainMenuAvatar clear];
    if ([GlobalData globalData].loggedInUser.userpic != nil) {
        mainMenuAvatar.image = [GlobalData globalData].loggedInUser.userpic;
    }
    else if ([GlobalData globalData].loggedInUser.userpic_url != nil)
    {
        [mainMenuAvatar loadImageFromURL:[NSURL URLWithString:[GlobalData globalData].loggedInUser.userpic_url]];
    }

    UIFont * dinFont = [UIFont fontWithName:@"DINPro-Bold" size:([AppDelegate currentDelegate].isIPad ? 16 : 13)];
    [mainMenuUserName setFont:dinFont];
    
    UIFont * font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:([AppDelegate currentDelegate].isIPad ? 18 : 13)];
    while (btnScore.subviews.count > 1) {
        [[btnScore.subviews lastObject] removeFromSuperview];
    }
    while (btnRating.subviews.count > 1) {
        [[btnRating.subviews lastObject] removeFromSuperview];
    }
    NSString * score = [NSString stringWithFormat:@"%d ", [GlobalData globalData].loggedInUser.month_score];
    CGSize scoreSize = [score sizeWithFont:font];
    UILabel * lblScoreNumber = [[UILabel alloc] initWithFrame:CGRectMake(40, 15, scoreSize.width, scoreSize.height)];
    lblScoreNumber.font = font;
    lblScoreNumber.textAlignment = UITextAlignmentLeft;
    lblScoreNumber.textColor = [UIColor colorWithRed:228/255.f green:179/255.f blue:55/255.f alpha:1];
    lblScoreNumber.highlightedTextColor = [UIColor colorWithRed:228/400.f green:179/400.f blue:55/400.f alpha:1];
    lblScoreNumber.shadowColor = [UIColor colorWithRed:36/255.f green:31/255.f blue:26/255.f alpha:1];
    lblScoreNumber.backgroundColor = [UIColor clearColor];
    lblScoreNumber.shadowOffset = CGSizeMake(0, -1);
    lblScoreNumber.text = score;
    [btnScore addSubview:lblScoreNumber];
    
    UILabel * lblScoreSuffix = [[UILabel alloc] initWithFrame:CGRectMake(40 + scoreSize.width, 15, 100, scoreSize.height)];
    lblScoreSuffix.font = font;
    lblScoreSuffix.textAlignment = UITextAlignmentLeft;
    lblScoreSuffix.textColor = [UIColor colorWithRed:158/255.f green:146/255.f blue:135/255.f alpha:1];
    lblScoreSuffix.highlightedTextColor = [UIColor colorWithRed:158/400.f green:146/400.f blue:135/400.f alpha:1];
    lblScoreSuffix.shadowColor = [UIColor colorWithRed:36/255.f green:31/255.f blue:26/255.f alpha:1];
    lblScoreSuffix.backgroundColor = [UIColor clearColor];
    lblScoreSuffix.shadowOffset = CGSizeMake(0, -1);
    lblScoreSuffix.text = [NSString declesion:[GlobalData globalData].loggedInUser.month_score oneString:@"очко" twoString:@"очка" fiveString:@"очков"];
    [btnScore addSubview:lblScoreSuffix];
    
    NSString * rating = [NSString stringWithFormat:@"%d-й ", [GlobalData globalData].loggedInUser.position];
    CGSize ratingSize = [rating sizeWithFont:font];
    UILabel * lblRatingNumber = [[UILabel alloc] initWithFrame:CGRectMake(40, 15, ratingSize.width, ratingSize.height)];
    lblRatingNumber.font = font;
    lblRatingNumber.textAlignment = UITextAlignmentLeft;
    lblRatingNumber.textColor = [UIColor colorWithRed:115/255.f green:189/255.f blue:69/255.f alpha:1];
    lblRatingNumber.highlightedTextColor = [UIColor colorWithRed:115/400.f green:189/400.f blue:69/400.f alpha:1];
    lblRatingNumber.shadowColor = [UIColor colorWithRed:36/255.f green:31/255.f blue:26/255.f alpha:1];
    lblRatingNumber.backgroundColor = [UIColor clearColor];
    lblRatingNumber.shadowOffset = CGSizeMake(0, -1);
    lblRatingNumber.text = rating;
    [btnRating addSubview:lblRatingNumber];
    
    UILabel * lblRatingSuffix = [[UILabel alloc] initWithFrame:CGRectMake(40 + ratingSize.width, 15, 100, ratingSize.height)];
    lblRatingSuffix.font = font;
    lblRatingSuffix.textAlignment = UITextAlignmentLeft;
    lblRatingSuffix.textColor = [UIColor colorWithRed:158/255.f green:146/255.f blue:135/255.f alpha:1];
    lblRatingSuffix.highlightedTextColor = [UIColor colorWithRed:158/400.f green:146/400.f blue:135/400.f alpha:1];
    lblRatingSuffix.shadowColor = [UIColor colorWithRed:36/255.f green:31/255.f blue:26/255.f alpha:1];
    lblRatingSuffix.backgroundColor = [UIColor clearColor];
    lblRatingSuffix.shadowOffset = CGSizeMake(0, -1);
    lblRatingSuffix.text = @"в рейтинге";
    [btnRating addSubview:lblRatingSuffix];
    
    [mainMenuFBSwitch setOn:([GlobalData globalData].loggedInUser.fbProvider != nil) animated:YES];
    [mainMenuFBSwitch setEnabled:([GlobalData globalData].loggedInUser.fbProvider == nil)];
    [mainMenuVKSwitch setOn:([GlobalData globalData].loggedInUser.vkProvider != nil) animated:YES];
    [mainMenuVKSwitch setEnabled:([GlobalData globalData].loggedInUser.vkProvider == nil)];
    
    [[UserDataManager sharedManager] restoreUnfinishedScoreQueries];
    [[UserDataManager sharedManager] restoreUnfinishedHintsQueries];
}

-(void)showMenuAnimated:(BOOL)animated
{
    if (!_isMenuHidden)
        return;
    _isMenuHidden = NO;
    if (animated)
    {
        [sidebarSound play];
        
        [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
        [UIView animateWithDuration:0.3 animations:^{
            mainMenuView.frame = CGRectMake(0, 0, mainMenuView.frame.size.width, mainMenuView.frame.size.height);
            navController.view.frame = CGRectMake(mainMenuView.frame.size.width, 0, self.view.frame.size.width, self.view.frame.size.height);
            if ([AppDelegate currentDelegate].isIPad)
            {
                navController.navigationBar.frame = CGRectMake(0, 0, self.view.frame.size.width - mainMenuView.frame.size.width, navController.navigationBar.frame.size.height);
            }
        }];
    }
    else
    {
        mainMenuView.frame = CGRectMake(0, 0, mainMenuView.frame.size.width, mainMenuView.frame.size.height);
        navController.view.frame = CGRectMake(mainMenuView.frame.size.width, 0, self.view.frame.size.width, self.view.frame.size.height);
        if ([AppDelegate currentDelegate].isIPad)
        {
            navController.navigationBar.frame = CGRectMake(0, 0, self.view.frame.size.width - mainMenuView.frame.size.width, navController.navigationBar.frame.size.height);
        }
    }

    [self updateUserInfo];
}

-(void)hideMenuAnimated:(BOOL)animated
{
    if (_isMenuHidden)
        return;
    _isMenuHidden = YES;
    if (animated)
    {
        [sidebarSound play];
        [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
        [UIView animateWithDuration:0.3 animations:^{
//            mainMenuView.frame = CGRectMake(-mainMenuView.frame.size.width, 0, mainMenuView.frame.size.width, mainMenuView.frame.size.height);
            navController.view.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
            navController.navigationBar.frame = CGRectMake(0, 0, self.view.frame.size.width, navController.navigationBar.frame.size.height);
        }];
    }
    else
    {
//        mainMenuView.frame = CGRectMake(-mainMenuView.frame.size.width, 0, mainMenuView.frame.size.width, mainMenuView.frame.size.height);
        navController.view.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
        navController.navigationBar.frame = CGRectMake(0, 0, self.view.frame.size.width, navController.navigationBar.frame.size.height);
    }
}

-(void)showOverlay:(UIView *)overlayView
{
    [self showOverlay:overlayView animated:YES];
}

-(void)showOverlay:(UIView *)overlayView animated:(BOOL)animated
{
    if (_currentOverlay != nil)
    {
        return;
    }
    currentLeftButton = navController.topViewController.navigationItem.leftBarButtonItem;
    currentRightButton = navController.topViewController.navigationItem.rightBarButtonItem;
    currentTitleView = navController.topViewController.navigationItem.titleView;
    
    [navController.topViewController.navigationItem setLeftBarButtonItem:[[UIBarButtonItem alloc] initWithCustomView:[UIView new]]];
    [navController.topViewController.navigationItem setRightBarButtonItem:nil];
    [navController.topViewController.navigationItem setTitleView:nil];
    navController.topViewController.title = navController.topViewController.title;
    
    overlayContainer.alpha = 0;
    if ([AppDelegate currentDelegate].isIPad)
    {
        overlayContainer.frame = CGRectMake(0, 70, self.view.bounds.size.width, self.view.bounds.size.height - 70);
    }
    else
    {
        overlayContainer.frame = CGRectMake(0, self.view.frame.size.height - overlayContainer.frame.size.height, overlayContainer.frame.size.width, overlayContainer.frame.size.height);
    }
    [self.view addSubview:overlayContainer];
    overlayContainer.clipsToBounds = YES;
    
/*
    gpuImageView.frame = CGRectMake(0, -overlayContainer.frame.origin.y, self.view.frame.size.width, self.view.frame.size.height);
    [overlayContainer insertSubview:gpuImageView atIndex:0];
    [uiElementInput update];
*/    
    _currentOverlay = overlayView;
    [overlayContainer addSubview:_currentOverlay];
    _currentOverlay.frame = CGRectMake((overlayContainer.frame.size.width - _currentOverlay.frame.size.width) / 2, -_currentOverlay.frame.size.height, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
    [UIView setAnimationCurve:UIViewAnimationCurveEaseOut];
    [UIView animateWithDuration:animated ? 0.5 : 0 delay:0 options:UIViewAnimationOptionBeginFromCurrentState animations:^{
        _currentOverlay.frame = CGRectMake((overlayContainer.frame.size.width - _currentOverlay.frame.size.width) / 2, 0, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
        overlayContainer.alpha = 1;
    } completion:nil];
}

-(void)showFullscreenOverlay:(UIView *)overlayView
{
    [self showFullscreenOverlay:overlayView animated:YES];
}

-(void)showFullscreenOverlay:(UIView *)overlayView animated:(BOOL)animated
{
    if (_currentOverlay != nil)
    {
        return;
    }
    
    fullscreenOverlayContainer.alpha = 0;
    fullscreenOverlayContainer.frame = CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height);
    [self.view addSubview:fullscreenOverlayContainer];
    fullscreenOverlayContainer.clipsToBounds = YES;

/*
    gpuImageView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
    [fullscreenOverlayContainer insertSubview:gpuImageView atIndex:0];
    [uiElementInput update];
*/
    
    _currentOverlay = overlayView;
    [fullscreenOverlayContainer addSubview:_currentOverlay];
    
    _currentOverlay.frame = CGRectMake((fullscreenOverlayContainer.frame.size.width - _currentOverlay.frame.size.width) / 2, (fullscreenOverlayContainer.frame.size.height - _currentOverlay.frame.size.height) / 2 - fullscreenOverlayContainer.frame.size.height, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
    [UIView animateWithDuration:animated ? 0.5 : 0 delay:0 options:UIViewAnimationOptionBeginFromCurrentState animations:^{
        _currentOverlay.frame = CGRectMake((fullscreenOverlayContainer.frame.size.width - _currentOverlay.frame.size.width) / 2, (fullscreenOverlayContainer.frame.size.height - _currentOverlay.frame.size.height) / 2, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
        fullscreenOverlayContainer.alpha = 1;
    } completion:nil];
    
}

-(void)hideOverlay
{
    [self hideOverlayAnimated:YES];
}

-(void)hideOverlayAnimated:(BOOL)animated
{
    if (_currentOverlay == nil)
    {
        return;
    }
    
    if (overlayContainer.superview != nil)
    {
        [navController.topViewController.navigationItem setLeftBarButtonItem:currentLeftButton];
        [navController.topViewController.navigationItem setRightBarButtonItem:currentRightButton];
        [navController.topViewController.navigationItem setTitleView:currentTitleView];
        
        currentLeftButton = nil;
        currentRightButton = nil;
        currentTitleView = nil;
    }
    
    if (animated)
    {
        [UIView animateWithDuration:0.5 delay:0 options:UIViewAnimationOptionBeginFromCurrentState animations:^{
            _currentOverlay.frame = CGRectMake((overlayContainer.frame.size.width - _currentOverlay.frame.size.width) / 2, -_currentOverlay.frame.size.height, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
            overlayContainer.alpha = 0;
            fullscreenOverlayContainer.alpha = 0;
        } completion:^(BOOL finished) {
            [_currentOverlay removeFromSuperview];
            [overlayContainer removeFromSuperview];
            [fullscreenOverlayContainer removeFromSuperview];
            _currentOverlay = nil;
        }];
    }
    else
    {
        _currentOverlay.frame = CGRectMake((overlayContainer.frame.size.width - _currentOverlay.frame.size.width) / 2, -_currentOverlay.frame.size.height, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
        overlayContainer.alpha = 0;
        fullscreenOverlayContainer.alpha = 0;
        [_currentOverlay removeFromSuperview];
        [overlayContainer removeFromSuperview];
        [fullscreenOverlayContainer removeFromSuperview];
        _currentOverlay = nil;
    }
}

-(void)showRules
{
    if (rulesScrollView.subviews.count < RULES_PAGES)
    {
        //        rulesScrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 0, rulesView.frame.size.width, rulesView.frame.size.height - pagecontrolBgImage.size.height)];
        rulesScrollView.backgroundColor = [UIColor clearColor];
        rulesScrollView.scrollEnabled = NO;
        rulesScrollView.showsHorizontalScrollIndicator = YES;
        rulesScrollView.showsVerticalScrollIndicator = NO;
        rulesScrollView.contentSize = CGSizeMake(RULES_PAGES * rulesScrollView.frame.size.width, rulesScrollView.frame.size.height);
        
        for (int i = 0; i != RULES_PAGES; ++i)
        {
            UIImageView * pageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:[NSString stringWithFormat:@"rules_page_%d", i + 1]]];
            pageView.frame = CGRectMake(i * pageView.frame.size.width, 0, pageView.frame.size.width, pageView.frame.size.height);
            [rulesScrollView addSubview:pageView];
        }
        
    }
    if (rulesPageControl == nil)
    {
        UIImage * paginatorEmptyImage = [UIImage imageNamed:@"rules_pagecontrol_empty"];
        UIImage * paginatorFullImage = [UIImage imageNamed:@"rules_pagecontrol_full"];
        UIImage * pagecontrolBgImage = [UIImage imageNamed:@"rules_pagecontrol_bg"];
        
        UISwipeGestureRecognizer * rulesRightGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleRulesPrev:)];
        rulesRightGestureRecognizer.direction = UISwipeGestureRecognizerDirectionRight;
        UISwipeGestureRecognizer * rulesLeftGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleRulesNext:)];
        rulesLeftGestureRecognizer.direction = UISwipeGestureRecognizerDirectionLeft;
        UITapGestureRecognizer * rulesTapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleRulesTap:)];
        [rulesScrollView setGestureRecognizers:[NSArray arrayWithObjects:rulesLeftGestureRecognizer, rulesRightGestureRecognizer, rulesTapGestureRecognizer, nil]];
        
        //        [rulesView addSubview:rulesScrollView];
        
        if ([pagecontrolBgImage respondsToSelector:@selector(resizableImageWithCapInsets:)])
        {
            pagecontrolBgImage = [pagecontrolBgImage resizableImageWithCapInsets:UIEdgeInsetsMake(pagecontrolBgImage.size.height / 2 - 1, pagecontrolBgImage.size.width / 2 - 1, pagecontrolBgImage.size.height / 2, pagecontrolBgImage.size.width / 2)];
        }
        else
        {
            pagecontrolBgImage = [pagecontrolBgImage stretchableImageWithLeftCapWidth:(pagecontrolBgImage.size.width / 2 - 1) topCapHeight:(pagecontrolBgImage.size.height / 2 - 1)];
        }
        rulesPageControl = [[UIImageView alloc] initWithImage:pagecontrolBgImage];
        float pageControlDefaultWidth = rulesPageControl.frame.size.width;
        float pagecontrolWidth = 1.5f * RULES_PAGES * paginatorEmptyImage.size.width + pageControlDefaultWidth;
        rulesPageControl.frame = CGRectMake((rulesView.frame.size.width - pagecontrolWidth) / 2, rulesView.frame.size.height - rulesPageControl.frame.size.height, pagecontrolWidth, rulesPageControl.frame.size.height);
        rulesPageControl.userInteractionEnabled = YES;
        [rulesPageControl addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleRulesTap:)]];
        [rulesView addSubview:rulesPageControl];
        for (int i = 0; i != RULES_PAGES; ++i)
        {
            PrizeWordButton * pageButton = [PrizeWordButton buttonWithType:UIButtonTypeCustom];
            pageButton.selected = (i == 0);
            pageButton.adjustsImageWhenHighlighted = NO;
            pageButton.enabled = YES;
            pageButton.userInteractionEnabled = YES;
            [pageButton setBackgroundImage:paginatorEmptyImage forState:UIControlStateNormal];
            [pageButton setBackgroundImage:paginatorFullImage forState:UIControlStateSelected];
            [pageButton addTarget:self action:@selector(handlePageButtonClick:) forControlEvents:UIControlEventTouchUpInside];
            pageButton.frame = CGRectMake(pageControlDefaultWidth / 2 + paginatorEmptyImage.size.width / 4 + 1.5f * i * paginatorEmptyImage.size.width, (rulesPageControl.frame.size.height - paginatorEmptyImage.size.height) / 2, paginatorEmptyImage.size.width, paginatorEmptyImage.size.height);
            pageButton.tag = i;
            [rulesPageControl addSubview:pageButton];
        }
        [rulesCaption setText:RULES_TEXTS[0]];
    }
    [self showFullscreenOverlay:rulesView];
}


#pragma mark handlers

- (IBAction)handleAvatarClick:(id)sender
{
    if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera] && [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypePhotoLibrary])
    {
        UIActionSheet * actionSheet = [[UIActionSheet alloc] initWithTitle:NSLocalizedString(@"Select source", @"Select source for avatar") delegate:self cancelButtonTitle:NSLocalizedString(@"Cancel", @"Cancel") destructiveButtonTitle:nil otherButtonTitles:NSLocalizedString(@"Camera", @"Camera source for avatar"), NSLocalizedString(@"Gallery", @"Gallery source for avatar"), nil];
        [actionSheet showInView:self.view];
    }
    else if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
    {
        [self startCameraControllerWithSourceType:UIImagePickerControllerSourceTypeCamera];
    }
    else if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypePhotoLibrary])
    {
        [self startCameraControllerWithSourceType:UIImagePickerControllerSourceTypePhotoLibrary];
    }
}

- (IBAction)handleMyPuzzlesClick:(id)sender
{
    if (![navController.topViewController isKindOfClass:[PuzzlesViewController class]])
    {
        [navController popViewControllerAnimated:NO];
        [navController pushViewController:[PuzzlesViewController new] animated:YES];
    }
    if (![AppDelegate currentDelegate].isIPad)
    {
        [self hideMenuAnimated:YES];
    }
}

- (IBAction)handleSwitchUserClick:(id)sender
{
    [self hideMenuAnimated:YES];
    [GlobalData globalData].monthSets = [NSMutableArray new];
    [SocialNetworks logout];
    
    [navController popToRootViewControllerAnimated:YES];
}

- (IBAction)handleScoreClick:(id)sender
{
    if (![navController.topViewController isKindOfClass:[ScoreViewController class]])
    {
        [navController popViewControllerAnimated:NO];
        [navController pushViewController:[ScoreViewController new] animated:YES];
    }
    if (![AppDelegate currentDelegate].isIPad)
    {
        [self hideMenuAnimated:YES];
    }
}

- (IBAction)handleRatingClick:(id)sender
{
    if (![navController.topViewController isKindOfClass:[RatingViewController class]])
    {
        [navController popViewControllerAnimated:NO];
        [navController pushViewController:[RatingViewController new] animated:YES];
    }
    if (![AppDelegate currentDelegate].isIPad)
    {
        [self hideMenuAnimated:YES];
    }
}

- (IBAction)handleInviteClick:(id)sender
{
    if (![navController.topViewController isKindOfClass:[InviteViewController class]])
    {
        [navController popViewControllerAnimated:NO];
        [navController pushViewController:[InviteViewController new] animated:YES];
    }
    if (![AppDelegate currentDelegate].isIPad)
    {
        [self hideMenuAnimated:YES];
    }
}

- (IBAction)handleRulesClick:(id)sender
{
    [self showRules];
}

- (IBAction)handleRestoreClick:(id)sender
{
    [[SKPaymentQueue defaultQueue] restoreCompletedTransactions];
    [[GlobalData globalData] loadMe];
}

- (IBAction)handleVKSwitchChange:(id)sender
{
    NSLog(@"handleVKSwitch");
    if (mainMenuVKSwitch.isOn)
    {
        mainMenuVKSwitch.enabled = NO;
        [SocialNetworks loginVkontakteWithViewController:self andCallback:^{
            NSLog(@"vk authorization complete");
            [[GlobalData globalData] loadMe];
        }];
    }
}

- (IBAction)handleFBSwitchChange:(id)sender
{
    NSLog(@"handleFBSwitch");
    if (mainMenuFBSwitch.isOn)
    {
        mainMenuFBSwitch.enabled = NO;
        [SocialNetworks loginFacebookWithViewController:self andCallback:^{
            NSLog(@"fb authorization complete");
            [[GlobalData globalData] loadMe];
        }];
    }
}

- (IBAction)handleNotificationSwitchChange:(id)sender
{
    if (mainMenuNotificationsSwitch.isOn)
    {
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:UIRemoteNotificationTypeAlert|UIRemoteNotificationTypeBadge|UIRemoteNotificationTypeSound];
    }
    else
    {
        [[UIApplication sharedApplication] unregisterForRemoteNotifications];
        [GlobalData globalData].deviceToken = nil;
    }
    [[NSUserDefaults standardUserDefaults] setBool:!mainMenuNotificationsSwitch.isOn forKey:@"remote-notifications-disabled"];
}

-(void)handleRulesMenuClick:(id)sender
{
    [self hideOverlay];
}

- (IBAction)handleRulesBgClick:(id)sender
{
    [self hideOverlay];
}

- (IBAction)handleRulesCloseClick:(id)sender
{
    [self hideOverlay];
}

-(void)handlePageButtonClick:(id)sender
{
    UIButton * selectedButton = (UIButton *)sender;
    [self updatePageButtons:selectedButton.tag];
    
    [rulesScrollView setContentOffset:CGPointMake(selectedButton.tag * rulesScrollView.frame.size.width, 0) animated:YES];
}

-(void)handleRulesPrev:(id)sender
{
    int page = rulesScrollView.contentOffset.x / rulesScrollView.frame.size.width + 0.5f;
    if (page == 0)
    {
        return;
    }
    --page;
    [rulesScrollView setContentOffset:CGPointMake(page * rulesScrollView.frame.size.width, 0) animated:YES];
    [self updatePageButtons:page];
}

-(void)handleRulesNext:(id)sender
{
    int page = rulesScrollView.contentOffset.x / rulesScrollView.frame.size.width + 0.5f;
    if (page == (int)(rulesScrollView.contentSize.width / rulesScrollView.frame.size.width + 0.5f) - 1)
    {
        return;
    }
    ++page;
    [rulesScrollView setContentOffset:CGPointMake(page * rulesScrollView.frame.size.width, 0) animated:YES];
    [self updatePageButtons:page];
}

-(void)handleRulesTap:(id)sender
{
    int page = rulesScrollView.contentOffset.x / rulesScrollView.frame.size.width + 0.5f;
    if (page == (int)(rulesScrollView.contentSize.width / rulesScrollView.frame.size.width + 0.5f) - 1)
    {
        page = 0;
    }
    else
    {
        ++page;
    }
    [rulesScrollView setContentOffset:CGPointMake(page * rulesScrollView.frame.size.width, 0) animated:YES];
    [self updatePageButtons:page];
}

-(void)updatePageButtons:(int)currentPage
{
    NSArray * pageButtons = nil;
    for (UIView * subview in rulesView.subviews)
    {
        if ([subview isKindOfClass:[UIImageView class]])
        {
            pageButtons = subview.subviews;
            break;
        }
    }
    
    if (pageButtons == nil)
    {
        return;
    }
    
    for (UIButton * pageButton in pageButtons)
    {
        if (pageButton.tag == currentPage && pageButton.selected)
        {
            return;
        }
        pageButton.selected = (pageButton.tag == currentPage);
    }
    
    [rulesCaption.layer removeAllAnimations];
    [UIView animateWithDuration:0.3 delay:0 options:UIViewAnimationOptionAllowAnimatedContent|UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionBeginFromCurrentState animations:^{
        rulesCaption.alpha = 0;
    } completion:^(BOOL finished) {
        if (finished)
        {
            rulesCaption.text = RULES_TEXTS[currentPage];
            [UIView animateWithDuration:0.3 delay:0 options:UIViewAnimationOptionAllowAnimatedContent|UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionBeginFromCurrentState animations:^{
                rulesCaption.alpha = 1;
            } completion:nil];
        }
        
    }];
}

-(void)handleSwipeLeft:(id)sender
{
    if (_currentOverlay == nil && ![AppDelegate currentDelegate].isIPad && [navController.topViewController isKindOfClass:[BlockedViewController class]] && !_isMenuHidden)
    {
        [self hideMenuAnimated:YES];
    }
}

-(void)handleSwipeRight:(id)sender
{
    if (_currentOverlay == nil && ![AppDelegate currentDelegate].isIPad && [navController.topViewController isKindOfClass:[BlockedViewController class]] && _isMenuHidden)
    {
        [self showMenuAnimated:YES];
    }
}

-(BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
    NSLog(@"rootViewController shouldAutorotateToInterfaceOrientation %d", toInterfaceOrientation);
    return [navController shouldAutorotateToInterfaceOrientation:toInterfaceOrientation];
}

-(BOOL)shouldAutorotate
{
    NSLog(@"rootViewController shouldAutorotate");
    return [navController shouldAutorotate];
}

-(NSUInteger)supportedInterfaceOrientations
{
    NSLog(@"rootViewController supportedInterfaceOrientations");
    return [navController supportedInterfaceOrientations];
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation
{
    NSLog(@"rootViewController preferredInterfaceOrientationForPresentation");
    return [navController preferredInterfaceOrientationForPresentation];
}

-(void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    if (_currentOverlay != nil)
    {
        // we grab the screen frame first off; these are always
        // in portrait mode
        CGRect bounds = [[UIScreen mainScreen] applicationFrame];
        CGSize size = bounds.size;
        
        // let's figure out if width/height must be swapped
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            // we're going to landscape, which means we gotta swap them
            size.width = bounds.size.height;
            size.height = bounds.size.width;
        }
        if (_currentOverlay.superview == overlayContainer)
        {
            [UIView animateWithDuration:duration animations:^{
                if ([AppDelegate currentDelegate].isIPad)
                {
                    overlayContainer.frame = CGRectMake(0, 70, size.width, size.height - 70);
                }
                else
                {
                    overlayContainer.frame = CGRectMake(0, size.height - overlayContainer.frame.size.height, overlayContainer.frame.size.width, overlayContainer.frame.size.height);
                }
                _currentOverlay.frame = CGRectMake((overlayContainer.frame.size.width - _currentOverlay.frame.size.width) / 2, 0, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
            }];
        }
        else
        {
            [UIView animateWithDuration:duration animations:^{
                fullscreenOverlayContainer.frame = CGRectMake(0, 0, size.width, size.height);
                _currentOverlay.frame = CGRectMake((fullscreenOverlayContainer.frame.size.width - _currentOverlay.frame.size.width) / 2, 0, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
            }];
        }
    }
}

#pragma mark select image source and start UIImagePickerController

-(void)actionSheet:(UIActionSheet *)actionSheet willDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (buttonIndex != actionSheet.cancelButtonIndex)
    {
        if (buttonIndex == actionSheet.firstOtherButtonIndex)
        {
            [self startCameraControllerWithSourceType:UIImagePickerControllerSourceTypeCamera];
        }
        else
        {
            [self startCameraControllerWithSourceType:UIImagePickerControllerSourceTypePhotoLibrary];
        }
    }
}

// Открывает ImagePicker с выбранным ресурсов (камера или галерея)
-(void)startCameraControllerWithSourceType:(UIImagePickerControllerSourceType)sourceType
{
    if (![UIImagePickerController isSourceTypeAvailable:sourceType])
    {
        return;
    }
    
    UIImagePickerController * imagePickerController = [[UIImagePickerController alloc] init];
    imagePickerController.sourceType = sourceType;
    imagePickerController.delegate = self;
    imagePickerController.allowsEditing = YES;
    imagePickerController.mediaTypes = [NSArray arrayWithObject:(NSString *)kUTTypeImage];
    if ([AppDelegate currentDelegate].isIPad && imagePickerController.sourceType == UIImagePickerControllerSourceTypePhotoLibrary)
    {
        avatarPopover = [[UIPopoverController alloc] initWithContentViewController:imagePickerController];
        avatarPopover.delegate = self;
        [avatarPopover presentPopoverFromRect:CGRectMake(mainMenuAvatar.frame.origin.x, mainMenuAvatar.frame.origin.y - mainMenuView.contentOffset.y, mainMenuAvatar.frame.size.width, mainMenuAvatar.frame.size.height) inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
    }
    else
    {
        [self presentViewController:imagePickerController animated:YES completion:nil];
    }
}

#pragma mark UIImagePickerControllerDelegate

-(void)imagePickerControllerDidCancel:(UIImagePickerController *)picker
{
    if ([AppDelegate currentDelegate].isIPad && picker.sourceType == UIImagePickerControllerSourceTypePhotoLibrary)
    {
        [(UIPopoverController *)picker.parentViewController dismissPopoverAnimated:YES];
    }
    else
    {
        [picker.presentingViewController dismissViewControllerAnimated:YES completion:nil];
    }
}

-(void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
    UIImage *originalImage, *editedImage, *imageToSave;
    
    // Handle a still image capture
    editedImage = (UIImage *) [info objectForKey:UIImagePickerControllerEditedImage];
    originalImage = (UIImage *) [info objectForKey:UIImagePickerControllerOriginalImage];
    if (editedImage) {
        imageToSave = editedImage;
    } else {
        imageToSave = originalImage;
    }
//    UIImageWriteToSavedPhotosAlbum (imageToSave, nil, nil, nil);
    int width = imageToSave.size.width;
    int height = imageToSave.size.height;
    int minDimension = width < height ? width : height;
    CGRect subrect = CGRectMake((width - minDimension) / 2, (height - minDimension) / 2, minDimension, minDimension);
    [mainMenuAvatar cancelLoading];
    CGImageRef cgImage = CGImageCreateWithImageInRect(imageToSave.CGImage, subrect);
    UIImage * image = [UIImage imageWithCGImage:cgImage];
    CGImageRelease(cgImage);
    mainMenuAvatar.image = image;
    
//    [self showActivityIndicator];
    [mainMenuAvatarActivityIndicator startAnimating];
    
    NSDictionary * params = @{@"session_key": [GlobalData globalData].sessionKey};
    NSMutableURLRequest * request = [[APIClient sharedClient] multipartFormRequestWithMethod:@"POST" path:@"me" parameters:params constructingBodyWithBlock:^(id<AFMultipartFormData> formData) {
        NSData * imageData = UIImageJPEGRepresentation(image, 1.0);
        [formData appendPartWithFileData:imageData name:@"userpic" fileName:@"image1.jpg" mimeType:@"image/jpeg"];
    }];
    AFHTTPRequestOperation * requestOperation = [[APIClient sharedClient] HTTPRequestOperationWithRequest:request success:^(AFHTTPRequestOperation *operation, id responseObject) {
        NSLog(@"update me result: %d %@", operation.response.statusCode, operation.responseString);
        if (operation.response.statusCode == 200)
        {
            SBJsonParser * parser = [SBJsonParser new];
            NSDictionary * data = [parser objectWithData:operation.responseData];
            UserData * newUser = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
            if (newUser != nil)
            {
                [GlobalData globalData].loggedInUser = newUser;
            }
            [self updateUserInfo];
            [mainMenuAvatarActivityIndicator stopAnimating];
        }
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"update me error: %@", error.description);
        [mainMenuAvatarActivityIndicator stopAnimating];
    }];
    [[APIClient sharedClient] enqueueHTTPRequestOperation:requestOperation];
    
    [self imagePickerControllerDidCancel:picker];
}

- (BOOL)prefersStatusBarHidden
{
    return YES;
}

@end
