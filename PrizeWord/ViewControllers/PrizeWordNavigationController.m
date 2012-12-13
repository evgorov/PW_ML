//
//  PrizeWordNavigationController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/5/12.
//
//

#import "PrizeWordNavigationController.h"
#import "PrizeWordNavigationBar.h"

@interface PrizeWordNavigationController ()

-(UIBarButtonItem *)backButtonItem;
-(void)handleBackTap:(UIButton *)sender;

@end

@implementation PrizeWordNavigationController

-(id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        currentOverlay = nil;
    }
    return self;
}

-(UIBarButtonItem *)backButtonItem
{
    UIImage * backButtonBg = [UIImage imageNamed:@"nav_back_btn"];
    UIImage * backButtonDownBg = [UIImage imageNamed:@"nav_back_btn_down"];
    UIButton * backButton = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, backButtonBg.size.width, backButtonBg.size.height)];
    [backButton setBackgroundImage:backButtonBg forState:UIControlStateNormal];
    [backButton setBackgroundImage:backButtonDownBg forState:UIControlStateHighlighted];
    [backButton addTarget:self action:@selector(handleBackTap:) forControlEvents:UIControlEventTouchUpInside];
    return [[UIBarButtonItem alloc] initWithCustomView:[PrizeWordNavigationBar containerWithView:backButton]];
}

-(void)pushViewController:(UIViewController *)viewController animated:(BOOL)animated
{
    [super pushViewController:viewController animated:animated];
    if (viewController.navigationItem.leftBarButtonItem == nil) {
        [viewController.navigationItem setLeftBarButtonItem:self.backButtonItem animated:animated];
    }
}

-(void)handleBackTap:(UIButton *)sender
{
    [self popViewControllerAnimated:YES];
}

-(void)showOverlay:(UIView *)overlayView
{
    if (currentOverlay != nil)
    {
        return;
    }
    currentLeftButton = self.topViewController.navigationItem.leftBarButtonItem;
    currentRightButton = self.topViewController.navigationItem.rightBarButtonItem;
    currentTitleView = self.topViewController.navigationItem.titleView;
    
    [self.topViewController.navigationItem setLeftBarButtonItem:[[UIBarButtonItem alloc] initWithCustomView:[UIView new]] animated:YES];
    [self.topViewController.navigationItem setRightBarButtonItem:nil animated:YES];
    [self.topViewController.navigationItem setTitleView:nil];
    
    overlayContainer.alpha = 0;
    overlayContainer.frame = CGRectMake(0, self.view.frame.size.height - overlayContainer.frame.size.height, overlayContainer.frame.size.width, overlayContainer.frame.size.height);
    [self.view addSubview:overlayContainer];
    overlayContainer.clipsToBounds = YES;
    
    currentOverlay = overlayView;
    [overlayContainer addSubview:currentOverlay];
    currentOverlay.frame = CGRectMake(0, -currentOverlay.frame.size.height, currentOverlay.frame.size.width, currentOverlay.frame.size.height);
    [UIView animateWithDuration:0.5 animations:^{
        currentOverlay.frame = CGRectMake(0, 0, currentOverlay.frame.size.width, currentOverlay.frame.size.height);
        overlayContainer.alpha = 1;
    }];
}

-(void)hideOverlay
{
    if (currentOverlay == nil)
    {
        return;
    }
    
    [self.topViewController.navigationItem setLeftBarButtonItem:currentLeftButton animated:YES];
    [self.topViewController.navigationItem setRightBarButtonItem:currentRightButton animated:YES];
    [self.topViewController.navigationItem setTitleView:currentTitleView];

    currentLeftButton = nil;
    currentRightButton = nil;
    currentTitleView = nil;
    
    [UIView animateWithDuration:0.5 animations:^{
        currentOverlay.frame = CGRectMake(0, -currentOverlay.frame.size.height, currentOverlay.frame.size.width, currentOverlay.frame.size.height);
        overlayContainer.alpha = 0;
    } completion:^(BOOL finished) {
        [currentOverlay removeFromSuperview];
        [overlayContainer removeFromSuperview];
        currentOverlay = nil;
    }];
}

@end
