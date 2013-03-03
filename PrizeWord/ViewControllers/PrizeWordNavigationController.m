//
//  PrizeWordNavigationController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/5/12.
//
//

#import "PrizeWordNavigationController.h"
#import "PrizeWordNavigationBar.h"
#import "AppDelegate.h"
#import "APIRequest.h"

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
        self.delegate = self;
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
    [APIRequest cancelAll];
    [super pushViewController:viewController animated:animated];
    if (viewController.navigationItem.leftBarButtonItem == nil) {
        [viewController.navigationItem setLeftBarButtonItem:self.backButtonItem animated:animated];
    }
}

-(UIViewController *)popViewControllerAnimated:(BOOL)animated
{
    [APIRequest cancelAll];
    if (UIDeviceOrientationIsLandscape([AppDelegate currentDelegate].viewOrientation))
    {
        [[AppDelegate currentDelegate] setOrientation:UIDeviceOrientationPortrait];
    }
    return [super popViewControllerAnimated:animated];  
}

-(NSArray *)popToRootViewControllerAnimated:(BOOL)animated
{
    [APIRequest cancelAll];
    NSArray * popped = [super popToRootViewControllerAnimated:animated];
    
    if (self.topViewController.navigationItem.leftBarButtonItem == nil)
    {
        [self.topViewController.navigationItem setLeftBarButtonItem:self.backButtonItem animated:animated];
    }
    [self setNavigationBarHidden:YES animated:animated];
    return popped;
}

-(void)navigationController:(UINavigationController *)navigationController didShowViewController:(UIViewController *)viewController animated:(BOOL)animated
{
    if ([[UIDevice currentDevice].systemVersion compare:@"5.0" options:NSNumericSearch] == NSOrderedAscending)
    {
        [viewController viewDidAppear:animated];
    }
}

-(void)navigationController:(UINavigationController *)navigationController willShowViewController:(UIViewController *)viewController animated:(BOOL)animated
{
    if ([[UIDevice currentDevice].systemVersion compare:@"5.0" options:NSNumericSearch] == NSOrderedAscending)
    {
        [viewController viewWillAppear:animated];
    }
    [[AppDelegate currentDelegate] orientationChanged:nil];
}

-(void)handleBackTap:(UIButton *)sender
{
    [self popViewControllerAnimated:YES];
}


-(BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
    return [self.topViewController shouldAutorotateToInterfaceOrientation:toInterfaceOrientation];

}

-(BOOL)shouldAutorotate
{
    return [self.topViewController shouldAutorotate];
}

-(NSUInteger)supportedInterfaceOrientations
{
    return [self.topViewController supportedInterfaceOrientations];
}

@end
