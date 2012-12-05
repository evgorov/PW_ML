//
//  LoginMainViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/5/12.
//
//

#import "LoginMainViewController.h"
#import "RootViewController.h"

@interface LoginMainViewController ()

@end

@implementation LoginMainViewController

-(void)viewWillAppear:(BOOL)animated
{
    [self.navigationController setNavigationBarHidden:YES animated:animated];
}

- (IBAction)handleEnterClick:(UIButton *)sender
{
    [self.navigationController pushViewController:[RootViewController new] animated:YES];
}

- (IBAction)handleRegisterClick:(UIButton *)sender
{
    [self.navigationController pushViewController:[RootViewController new] animated:YES];
}

- (IBAction)handleFacebookClick:(UIButton *)sender
{
    [self.navigationController pushViewController:[RootViewController new] animated:YES];
}

- (IBAction)handleVKClick:(UIButton *)sender
{
    [self.navigationController pushViewController:[RootViewController new] animated:YES];
}

@end
