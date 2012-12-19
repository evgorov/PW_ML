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
    UIViewController * prevViewController = self.topViewController;
    [prevViewController viewWillDisappear:animated];
    [super pushViewController:viewController animated:animated];
    if (viewController.navigationItem.leftBarButtonItem == nil) {
        [viewController.navigationItem setLeftBarButtonItem:self.backButtonItem animated:animated];
    }
}

-(UIViewController *)popViewControllerAnimated:(BOOL)animated
{
    UIViewController * popped = [super popViewControllerAnimated:animated];
    [popped viewWillDisappear:animated];
    if (self.topViewController.navigationItem.leftBarButtonItem == nil)
    {
        [self.topViewController.navigationItem setLeftBarButtonItem:self.backButtonItem animated:animated];
    }
    return popped;
}

-(NSArray *)popToRootViewControllerAnimated:(BOOL)animated
{
    NSArray * popped = [super popToRootViewControllerAnimated:animated];
    UIViewController * top = popped.count > 0 ? [popped objectAtIndex:0] : nil;
    
    if (top != nil)
    {
        [top viewWillDisappear:animated];
    }
    if (self.topViewController.navigationItem.leftBarButtonItem == nil)
    {
        [self.topViewController.navigationItem setLeftBarButtonItem:self.backButtonItem animated:animated];
    }
    return popped;
}

-(void)navigationController:(UINavigationController *)navigationController didShowViewController:(UIViewController *)viewController animated:(BOOL)animated
{
    [viewController viewDidAppear:animated];
}

-(void)navigationController:(UINavigationController *)navigationController willShowViewController:(UIViewController *)viewController animated:(BOOL)animated
{
    [viewController viewWillAppear:animated];
}

-(void)handleBackTap:(UIButton *)sender
{
    [self popViewControllerAnimated:YES];
}

@end