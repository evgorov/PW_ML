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
#import "SocialNetworks.h"

@interface LoginMainViewController ()

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
    [SocialNetworks loginFacebookWithViewController:self andCallback:^{
        [self.navigationController setNavigationBarHidden:NO animated:YES];
        [self.navigationController pushViewController:[PuzzlesViewController new] animated:YES];
    }];
}

- (IBAction)handleVKClick:(UIButton *)sender
{
    [SocialNetworks loginVkontakteWithViewController:self andCallback:^{
        [self.navigationController setNavigationBarHidden:NO animated:YES];
        [self.navigationController pushViewController:[PuzzlesViewController new] animated:YES];
    }];
}

- (IBAction)handleReleaseNotesClick:(UIButton *)sender
{
    [self.navigationController setNavigationBarHidden:NO animated:YES];
    [self.navigationController pushViewController:[ReleaseNotesViewController new] animated:YES];
}


@end
