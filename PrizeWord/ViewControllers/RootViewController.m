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

@end

NSString * MONTHS3[] = {@"январе", @"феврале", @"марте", @"апреле", @"мае", @"июне", @"июле", @"августе", @"сентябре", @"октябре", @"ноябре", @"декабре"};

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
    }
    return self;
}

-(void)viewDidLoad
{
    self.view.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_black_tile.jpg"]];
    
    mainMenuView.contentSize = mainMenuBg.frame.size;
    mainMenuView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"menu_bg_tile.jpg"]];
    mainMenuYourResult.font = [UIFont fontWithName:@"DINPro-Bold" size:[AppDelegate currentDelegate].isIPad ? 18 : 15];
    mainMenuYourResult.text = [NSString stringWithFormat:@"Ваш результат в %@", MONTHS3[[GlobalData globalData].currentMonth]];

    
    [self.view addSubview:navController.view];
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

    UISwipeGestureRecognizer * swipeLeftGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipeLeft:)];
    swipeLeftGestureRecognizer.direction = UISwipeGestureRecognizerDirectionLeft;
    UISwipeGestureRecognizer * swipeRightGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipeRight:)];
    swipeRightGestureRecognizer.direction = UISwipeGestureRecognizerDirectionRight;
    self.view.gestureRecognizers = [NSArray arrayWithObjects:swipeLeftGestureRecognizer, swipeRightGestureRecognizer, nil];
    
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_ME_UPDATED];
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
    
    rulesCaption = nil;
    [super viewDidUnload];
}

-(void)didReceiveMemoryWarning
{
    /*
    if (_currentOverlay != rulesView)
    {
//        [rulesScrollView removeFromSuperview];
//        rulesScrollView = nil;
        
        NSArray * subviews = [rulesScrollView subviews];
        for (UIView * subview in subviews)
        {
            [subview removeFromSuperview];
        }
    }
    */
}

-(void)handleEvent:(Event *)event
{
    if (event.type == EVENT_ME_UPDATED)
    {
        [self updateUserInfo];
    }
}

-(void)updateUserInfo
{
    NSLog(@"update user info");
    mainMenuMaxScore.text = [NSString stringWithFormat:@"%d очков", [GlobalData globalData].loggedInUser.high_score];
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
        [[btnScore.subviews objectAtIndex:(btnScore.subviews.count - 1)] removeFromSuperview];
    }
    while (btnRating.subviews.count > 1) {
        [[btnRating.subviews objectAtIndex:(btnRating.subviews.count - 1)] removeFromSuperview];
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
    lblScoreSuffix.text = @"очков";
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
    [mainMenuVKSwitch setOn:([GlobalData globalData].loggedInUser.vkProvider != nil) animated:YES];
}

-(void)showMenuAnimated:(BOOL)animated
{
    if (!_isMenuHidden)
        return;
    _isMenuHidden = NO;
    if (animated)
    {
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
        [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
        [UIView animateWithDuration:0.3 animations:^{
            mainMenuView.frame = CGRectMake(-mainMenuView.frame.size.width, 0, mainMenuView.frame.size.width, mainMenuView.frame.size.height);
            navController.view.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
            navController.navigationBar.frame = CGRectMake(0, 0, self.view.frame.size.width, navController.navigationBar.frame.size.height);
        }];
    }
    else
    {
        mainMenuView.frame = CGRectMake(-mainMenuView.frame.size.width, 0, mainMenuView.frame.size.width, mainMenuView.frame.size.height);
        navController.view.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
        navController.navigationBar.frame = CGRectMake(0, 0, self.view.frame.size.width, navController.navigationBar.frame.size.height);
    }
}

-(void)showOverlay:(UIView *)overlayView
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
    [PrizeWordNavigationController setTitleViewForViewController:navController.topViewController];
    
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
    
    _currentOverlay = overlayView;
    [overlayContainer addSubview:_currentOverlay];
    _currentOverlay.frame = CGRectMake((overlayContainer.frame.size.width - _currentOverlay.frame.size.width) / 2, -_currentOverlay.frame.size.height, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
    [UIView setAnimationCurve:UIViewAnimationCurveEaseOut];
    [UIView animateWithDuration:0.5 animations:^{
        _currentOverlay.frame = CGRectMake((overlayContainer.frame.size.width - _currentOverlay.frame.size.width) / 2, 0, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
        overlayContainer.alpha = 1;
    }];
}

-(void)showFullscreenOverlay:(UIView *)overlayView
{
    if (_currentOverlay != nil)
    {
        return;
    }
    
    fullscreenOverlayContainer.alpha = 0;
    fullscreenOverlayContainer.frame = CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height);
    [self.view addSubview:fullscreenOverlayContainer];
    fullscreenOverlayContainer.clipsToBounds = YES;
    
    _currentOverlay = overlayView;
    [fullscreenOverlayContainer addSubview:_currentOverlay];
    overlayContainer.frame = CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height);
    
    _currentOverlay.frame = CGRectMake((fullscreenOverlayContainer.frame.size.width - _currentOverlay.frame.size.width) / 2, (fullscreenOverlayContainer.frame.size.height - _currentOverlay.frame.size.height) / 2 - fullscreenOverlayContainer.frame.size.height, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
    [UIView animateWithDuration:0.5 animations:^{
        _currentOverlay.frame = CGRectMake((fullscreenOverlayContainer.frame.size.width - _currentOverlay.frame.size.width) / 2, (fullscreenOverlayContainer.frame.size.height - _currentOverlay.frame.size.height) / 2, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
        fullscreenOverlayContainer.alpha = 1;
    }];
    
}

-(void)hideOverlay
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
    
    [UIView animateWithDuration:0.5 animations:^{
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
    [GlobalData globalData].loggedInUser = nil;
    [GlobalData globalData].sessionKey = nil;
    if ([GlobalData globalData].fbSession != nil)
    {
        [[GlobalData globalData].fbSession close];
        [GlobalData globalData].fbSession = nil;
    }
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
    int pages = 5;
    if (rulesScrollView.subviews.count < pages)
    {
        //        rulesScrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 0, rulesView.frame.size.width, rulesView.frame.size.height - pagecontrolBgImage.size.height)];
        rulesScrollView.backgroundColor = [UIColor clearColor];
        rulesScrollView.scrollEnabled = NO;
        rulesScrollView.showsHorizontalScrollIndicator = YES;
        rulesScrollView.showsVerticalScrollIndicator = NO;
        rulesScrollView.contentSize = CGSizeMake(pages * rulesScrollView.frame.size.width, rulesScrollView.frame.size.height);
        
        for (int i = 0; i != pages; ++i)
        {
            UIImageView * pageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"rules_page_1"]];
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
        float pagecontrolWidth = 1.5f * pages * paginatorEmptyImage.size.width + pageControlDefaultWidth;
        rulesPageControl.frame = CGRectMake((rulesView.frame.size.width - pagecontrolWidth) / 2, rulesView.frame.size.height - rulesPageControl.frame.size.height, pagecontrolWidth, rulesPageControl.frame.size.height);
        rulesPageControl.userInteractionEnabled = YES;
        [rulesPageControl addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleRulesTap:)]];
        [rulesView addSubview:rulesPageControl];
        for (int i = 0; i != pages; ++i)
        {
            UIButton * pageButton = [UIButton buttonWithType:UIButtonTypeCustom];
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
    }
    if (![AppDelegate currentDelegate].isIPad)
    {
        [self hideMenuAnimated:YES];
    }
    navController.topViewController.title = NSLocalizedString(@"TITLE_RULES", nil);
    [self showOverlay:rulesView];
    UIImage * menuImage = [UIImage imageNamed:@"menu_btn"];
    UIImage * menuHighlightedImage = [UIImage imageNamed:@"menu_btn_down"];
    UIButton * menuButton = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, menuImage.size.width, menuImage.size.height)];
    [menuButton setBackgroundImage:menuImage forState:UIControlStateNormal];
    [menuButton setBackgroundImage:menuHighlightedImage forState:UIControlStateHighlighted];
    [menuButton addTarget:self action:@selector(handleRulesMenuClick:) forControlEvents:UIControlEventTouchUpInside];
    UIBarButtonItem * menuItem = [[UIBarButtonItem alloc] initWithCustomView:
                [PrizeWordNavigationBar containerWithView:menuButton]];
    [navController.topViewController.navigationItem setLeftBarButtonItem:menuItem animated:YES];
    
}

- (IBAction)handleRestoreClick:(id)sender
{
}

- (IBAction)handleVKSwitchChange:(id)sender
{
    NSLog(@"handleVKSwitch");
    if (mainMenuVKSwitch.isOn)
    {
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
        [SocialNetworks loginFacebookWithViewController:self andCallback:^{
            NSLog(@"fb authorization complete");
            [[GlobalData globalData] loadMe];
        }];
    }
}

- (IBAction)handleNotificationSwitchChange:(id)sender
{
    
}

-(void)handleRulesMenuClick:(id)sender
{
    [self hideOverlay];
}

- (IBAction)handleRulesBgClick:(id)sender
{
    [self hideOverlay];
}

-(void)handlePageButtonClick:(id)sender
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
    
    UIButton * selectedButton = (UIButton *)sender;
    for (UIButton * pageButton in pageButtons)
    {
        pageButton.selected = (pageButton == sender);
    }
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
        pageButton.selected = (pageButton.tag == currentPage);
    }
}

-(void)handleSwipeLeft:(id)sender
{
    if (![AppDelegate currentDelegate].isIPad && [navController.topViewController isKindOfClass:[BlockedViewController class]] && !_isMenuHidden)
    {
        [self hideMenuAnimated:YES];
    }
}

-(void)handleSwipeRight:(id)sender
{
    if (![AppDelegate currentDelegate].isIPad && [navController.topViewController isKindOfClass:[BlockedViewController class]] && _isMenuHidden)
    {
        [self showMenuAnimated:YES];
    }
}

-(BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
    return [navController shouldAutorotateToInterfaceOrientation:toInterfaceOrientation];
}

-(BOOL)shouldAutorotate
{
    return [navController shouldAutorotate];
}

-(NSUInteger)supportedInterfaceOrientations
{
    return [navController supportedInterfaceOrientations];
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


@end
