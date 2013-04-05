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
#import "FISoundEngine.h"

@interface LoginMainViewController ()

@end

@implementation LoginMainViewController

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    NSLog(@"login main frame size: %f %f", self.view.bounds.size.width, self.view.bounds.size.height);
    [self.navigationController setNavigationBarHidden:YES animated:animated];
}

- (IBAction)handleEnterClick:(id)sender
{
    [self.navigationController setNavigationBarHidden:NO animated:YES];
    [self.navigationController pushViewController:[LoginEnterViewController new] animated:YES];
}

- (IBAction)handleRegisterClick:(id)sender
{
    [self.navigationController setNavigationBarHidden:NO animated:YES];
    [self.navigationController pushViewController:[LoginRegisterViewController new] animated:YES];
}

- (IBAction)handleFacebookClick:(id)sender
{
    [SocialNetworks loginFacebookWithViewController:self andCallback:^{
        [self.navigationController setNavigationBarHidden:NO animated:YES];
        [self.navigationController pushViewController:[PuzzlesViewController new] animated:YES];
    }];
}

- (IBAction)handleVKClick:(id)sender
{
    [SocialNetworks loginVkontakteWithViewController:self andCallback:^{
        [self.navigationController setNavigationBarHidden:NO animated:YES];
        [self.navigationController pushViewController:[PuzzlesViewController new] animated:YES];
    }];
}

- (IBAction)handleReleaseNotesClick:(id)sender
{
    [self.navigationController setNavigationBarHidden:NO animated:YES];
    [self.navigationController pushViewController:[ReleaseNotesViewController new] animated:YES];
}


@end
