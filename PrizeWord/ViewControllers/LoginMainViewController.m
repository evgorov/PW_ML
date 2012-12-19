//
//  LoginMainViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/5/12.
//
//

#import "LoginMainViewController.h"
#import "PuzzlesViewController.h"
#import "LoginRegisterViewController.h"
#import "LoginEnterViewController.h"
#import "ReleaseNotesViewController.h"

@interface LoginMainViewController ()

-(void)gotoRoot:(id)sender;

@end

@implementation LoginMainViewController

-(void)viewWillAppear:(BOOL)animated
{
    [self.navigationController setNavigationBarHidden:YES animated:animated];
}

- (IBAction)handleEnterClick:(UIButton *)sender
{
    [self.navigationController setNavigationBarHidden:NO animated:YES];
    [self.navigationController pushViewController:[LoginEnterViewController new] animated:YES];
}

- (IBAction)handleRegisterClick:(UIButton *)sender
{
    [self.navigationController setNavigationBarHidden:NO animated:YES];
    [self.navigationController pushViewController:[LoginRegisterViewController new] animated:YES];
}

- (IBAction)handleFacebookClick:(UIButton *)sender
{
    [self showActivityIndicator];
    [NSTimer scheduledTimerWithTimeInterval:2 target:self selector:@selector(gotoRoot:) userInfo:nil repeats:NO];
}

- (IBAction)handleVKClick:(UIButton *)sender
{
    [self showActivityIndicator];
    [NSTimer scheduledTimerWithTimeInterval:2 target:self selector:@selector(gotoRoot:) userInfo:nil repeats:NO];
}

- (IBAction)handleReleaseNotesClick:(UIButton *)sender
{
    [self.navigationController setNavigationBarHidden:NO animated:YES];
    [self.navigationController pushViewController:[ReleaseNotesViewController new] animated:YES];
}

-(void)gotoRoot:(id)sender
{
    [self hideActivityIndicator];
    [self.navigationController setNavigationBarHidden:NO animated:YES];
    [self.navigationController pushViewController:[PuzzlesViewController new] animated:YES];
}

@end