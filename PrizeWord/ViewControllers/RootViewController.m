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

@interface RootViewController (private)

-(void)handlePageButtonClick:(id)sender;
-(void)handleRulesMenuClick:(id)sender;

@end

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
    mainMenuView.contentSize = mainMenuBg.frame.size;
    
    UIFont * font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:13];
    NSString * score = @"9 001 ";
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

    NSString * rating = @"123-й ";
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
    
    [self.view addSubview:navController.view];
    if (_isMenuHidden)
    {
        [self hideMenuAnimated:NO];
    }
    else
    {
        [self showMenuAnimated:NO];
    }
}

- (void)viewDidUnload
{
    btnScore = nil;
    btnRating = nil;
    mainMenuView = nil;
    mainMenuBg = nil;
    rulesView = nil;
    fullscreenOverlayContainer = nil;
    [super viewDidUnload];
}

-(void)didReceiveMemoryWarning
{
    if (_currentOverlay != rulesView)
    {
        [rulesScrollView removeFromSuperview];
        rulesScrollView = nil;
        
        NSArray * subviews = [rulesView subviews];
        for (UIView * subview in subviews)
        {
            [subview removeFromSuperview];
        }
    }
}

-(void)showMenuAnimated:(BOOL)animated
{
    _isMenuHidden = NO;
    if (animated)
    {
        [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
        [UIView animateWithDuration:0.3 animations:^{
            mainMenuView.frame = CGRectMake(0, 0, mainMenuView.frame.size.width, mainMenuView.frame.size.height);
            navController.view.frame = CGRectMake(mainMenuView.frame.size.width, 0, self.view.frame.size.width, self.view.frame.size.height);
        }];
    }
    else
    {
        mainMenuView.frame = CGRectMake(0, 0, mainMenuView.frame.size.width, mainMenuView.frame.size.height);
        navController.view.frame = CGRectMake(mainMenuView.frame.size.width, 0, self.view.frame.size.width, self.view.frame.size.height);
    }
}

-(void)hideMenuAnimated:(BOOL)animated
{
    _isMenuHidden = YES;
    if (animated)
    {
        [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
        [UIView animateWithDuration:0.3 animations:^{
            mainMenuView.frame = CGRectMake(-mainMenuView.frame.size.width, 0, mainMenuView.frame.size.width, mainMenuView.frame.size.height);
            navController.view.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
        }];
    }
    else
    {
        mainMenuView.frame = CGRectMake(-mainMenuView.frame.size.width, 0, mainMenuView.frame.size.width, mainMenuView.frame.size.height);
        navController.view.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
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
    
    overlayContainer.alpha = 0;
    overlayContainer.frame = CGRectMake(0, self.view.frame.size.height - overlayContainer.frame.size.height, overlayContainer.frame.size.width, overlayContainer.frame.size.height);
    [self.view addSubview:overlayContainer];
    overlayContainer.clipsToBounds = YES;
    
    _currentOverlay = overlayView;
    [overlayContainer addSubview:_currentOverlay];
    _currentOverlay.frame = CGRectMake(0, -_currentOverlay.frame.size.height, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
    [UIView animateWithDuration:0.5 animations:^{
        _currentOverlay.frame = CGRectMake(0, 0, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
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
    fullscreenOverlayContainer.frame = CGRectMake(0, 0, fullscreenOverlayContainer.frame.size.width, fullscreenOverlayContainer.frame.size.height);
    [self.view addSubview:fullscreenOverlayContainer];
    fullscreenOverlayContainer.clipsToBounds = YES;
    
    _currentOverlay = overlayView;
    [fullscreenOverlayContainer addSubview:_currentOverlay];
    _currentOverlay.frame = CGRectMake(0, -_currentOverlay.frame.size.height, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
    [UIView animateWithDuration:0.5 animations:^{
        _currentOverlay.frame = CGRectMake(0, 0, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
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
        _currentOverlay.frame = CGRectMake(0, -_currentOverlay.frame.size.height, _currentOverlay.frame.size.width, _currentOverlay.frame.size.height);
        overlayContainer.alpha = 0;
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
    [self hideMenuAnimated:YES];
}

- (IBAction)handleSwitchUserClick:(id)sender
{
    [navController popToRootViewControllerAnimated:YES];
    [self hideMenuAnimated:YES];
}

- (IBAction)handleScoreClick:(id)sender
{
    if (![navController.topViewController isKindOfClass:[ScoreViewController class]])
    {
        [navController popViewControllerAnimated:NO];
        [navController pushViewController:[ScoreViewController new] animated:YES];
    }
    [self hideMenuAnimated:YES];
}

- (IBAction)handleRatingClick:(id)sender
{
    if (![navController.topViewController isKindOfClass:[RatingViewController class]])
    {
        [navController popViewControllerAnimated:NO];
        [navController pushViewController:[RatingViewController new] animated:YES];
    }
    [self hideMenuAnimated:YES];
}

- (IBAction)handleInviteClick:(id)sender
{
    if (![navController.topViewController isKindOfClass:[InviteViewController class]])
    {
        [navController popViewControllerAnimated:NO];
        [navController pushViewController:[InviteViewController new] animated:YES];
    }
    [self hideMenuAnimated:YES];
}

- (IBAction)handleRulesClick:(id)sender
{
    if (rulesScrollView == nil)
    {
        UIImage * paginatorEmptyImage = [UIImage imageNamed:@"rules_pagecontrol_empty"];
        UIImage * paginatorFullImage = [UIImage imageNamed:@"rules_pagecontrol_full"];
        UIImage * pagecontrolBgImage = [UIImage imageNamed:@"rules_pagecontrol_bg"];

        int pages = 5;
        rulesScrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 0, rulesView.frame.size.width, rulesView.frame.size.height - pagecontrolBgImage.size.height)];
        rulesScrollView.delegate = self;
        rulesScrollView.backgroundColor = [UIColor clearColor];
        rulesScrollView.scrollEnabled = YES;
        rulesScrollView.showsHorizontalScrollIndicator = YES;
        rulesScrollView.showsVerticalScrollIndicator = NO;
        rulesScrollView.contentSize = CGSizeMake(pages * rulesScrollView.frame.size.width, rulesScrollView.frame.size.height);
        [rulesView addSubview:rulesScrollView];

        if ([pagecontrolBgImage respondsToSelector:@selector(resizableImageWithCapInsets:)])
        {
            pagecontrolBgImage = [pagecontrolBgImage resizableImageWithCapInsets:UIEdgeInsetsMake(pagecontrolBgImage.size.height / 2 - 1, pagecontrolBgImage.size.width / 2 - 1, pagecontrolBgImage.size.height / 2, pagecontrolBgImage.size.width / 2)];
        }
        else
        {
            pagecontrolBgImage = [pagecontrolBgImage stretchableImageWithLeftCapWidth:(pagecontrolBgImage.size.width / 2 - 1) topCapHeight:(pagecontrolBgImage.size.height / 2 - 1)];
        }
        UIImageView * pagecontrol = [[UIImageView alloc] initWithImage:pagecontrolBgImage];
        float pageControlDefaultWidth = pagecontrol.frame.size.width;
        float pagecontrolWidth = 1.5f * pages * paginatorEmptyImage.size.width + pageControlDefaultWidth;
        pagecontrol.frame = CGRectMake((rulesView.frame.size.width - pagecontrolWidth) / 2, rulesView.frame.size.height - pagecontrol.frame.size.height, pagecontrolWidth, pagecontrol.frame.size.height);
        pagecontrol.userInteractionEnabled = YES;
        [rulesView addSubview:pagecontrol];
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
            pageButton.frame = CGRectMake(pageControlDefaultWidth / 2 + paginatorEmptyImage.size.width / 4 + 1.5f * i * paginatorEmptyImage.size.width, (pagecontrol.frame.size.height - paginatorEmptyImage.size.height) / 2, paginatorEmptyImage.size.width, paginatorEmptyImage.size.height);
            pageButton.tag = i;
            [pagecontrol addSubview:pageButton];
            
            UIImageView * pageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"rules_page_1"]];
            pageView.frame = CGRectMake(i * pageView.frame.size.width, 0, pageView.frame.size.width, pageView.frame.size.height);
            [rulesScrollView addSubview:pageView];
        }
    }
    [self hideMenuAnimated:YES];
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
}

- (IBAction)handleFBSwitchChange:(id)sender
{
}

- (IBAction)handleNotificationSwitchChange:(id)sender
{
    
}

-(void)handleRulesMenuClick:(id)sender
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

-(void)scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate
{
    int page = scrollView.contentOffset.x / scrollView.frame.size.width + 0.5f;
    
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
    if (page >= pageButtons.count)
    {
        page = pageButtons.count - 1;
    }
    if (page < 0)
    {
        page = 0;
    }
    
    for (UIButton * pageButton in pageButtons)
    {
        pageButton.selected = (page == pageButton.tag);
    }
    if (decelerate)
    {
        [rulesScrollView setContentOffset:rulesScrollView.contentOffset animated:NO];
    }
    [rulesScrollView setContentOffset:CGPointMake(page * rulesScrollView.frame.size.width, 0) animated:YES];
}

-(void)scrollViewWillBeginDecelerating:(UIScrollView *)scrollView
{
    if ([[UIDevice currentDevice].systemVersion compare:@"5.0" options:NSNumericSearch] == NSOrderedAscending)
    {
        NSLog(@"old flow");
        [rulesScrollView setContentOffset:rulesScrollView.contentOffset animated:NO];
    }
}

-(void)scrollViewWillEndDragging:(UIScrollView *)scrollView withVelocity:(CGPoint)velocity targetContentOffset:(inout CGPoint *)targetContentOffset
{
    int page = targetContentOffset->x / scrollView.frame.size.width + 0.5f;
    
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
    if (page >= pageButtons.count)
    {
        page = pageButtons.count - 1;
    }
    if (page < 0)
    {
        page = 0;
    }
    
    for (UIButton * pageButton in pageButtons)
    {
        pageButton.selected = (page == pageButton.tag);
    }
    targetContentOffset->x = page * rulesScrollView.frame.size.width;
    [rulesScrollView setContentOffset:CGPointMake(page * rulesScrollView.frame.size.width, 0) animated:YES];
}

@end