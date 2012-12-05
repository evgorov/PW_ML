//
//  PrizeWordNavigationController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/5/12.
//
//

#import "PrizeWordNavigationController.h"

@interface PrizeWordNavigationController ()

-(void)handleBackTap:(UIButton *)sender;

@end

@implementation PrizeWordNavigationController

-(void)pushViewController:(UIViewController *)viewController animated:(BOOL)animated
{
    [super pushViewController:viewController animated:animated];
    UIImage * backButtonBg = [UIImage imageNamed:@"nav_back_btn"];
    UIImage * backButtonDownBg = [UIImage imageNamed:@"nav_back_btn_down"];
    UIButton * backButton = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, backButtonBg.size.width, backButtonBg.size.height)];
    [backButton setBackgroundImage:backButtonBg forState:UIControlStateNormal];
    [backButton setBackgroundImage:backButtonDownBg forState:UIControlStateHighlighted];
    [backButton addTarget:self action:@selector(handleBackTap:) forControlEvents:UIControlEventTouchUpInside];
    UIBarButtonItem * backButtonItem = [[UIBarButtonItem alloc] initWithCustomView:backButton];
    [self.topViewController.navigationItem setLeftBarButtonItem:backButtonItem animated:animated];
}

-(void)handleBackTap:(UIButton *)sender
{
    [self.navigationItem setLeftBarButtonItem:nil animated:YES];
    [self popViewControllerAnimated:YES];
}

@end
