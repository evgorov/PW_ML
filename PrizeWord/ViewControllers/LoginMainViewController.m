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
#import "GlobalData.h"
#import "UserData.h"
#import "EventManager.h"
#import "Event.h"

@interface LoginMainViewController ()

@end

@implementation LoginMainViewController

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    NSString * sessionKey = [[NSUserDefaults standardUserDefaults] stringForKey:@"session-key"];
    NSDictionary * userData = [[NSUserDefaults standardUserDefaults] dictionaryForKey:@"user-data"];
    if (sessionKey != nil && userData != nil)
    {
        [self showActivityIndicator];
        double delayInSeconds = 1.0;
        dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
        dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
            [self hideActivityIndicator];
            [GlobalData globalData].sessionKey = sessionKey;
            [GlobalData globalData].loggedInUser = [UserData userDataWithDictionary:userData];
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_ME_UPDATED andData:[GlobalData globalData].loggedInUser]];
            [self.navigationController setNavigationBarHidden:NO animated:YES];
            [self.navigationController pushViewController:[PuzzlesViewController new] animated:YES];
        });
    }
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
        if ([GlobalData globalData].sessionKey != nil)
        {
            [self.navigationController setNavigationBarHidden:NO animated:YES];
            [self.navigationController pushViewController:[PuzzlesViewController new] animated:YES];
        }
    }];
}

- (IBAction)handleVKClick:(id)sender
{
    [SocialNetworks loginVkontakteWithViewController:self andCallback:^{
        if ([GlobalData globalData].sessionKey != nil)
        {
            [self.navigationController setNavigationBarHidden:NO animated:YES];
            [self.navigationController pushViewController:[PuzzlesViewController new] animated:YES];
        }
    }];
}

- (IBAction)handleReleaseNotesClick:(id)sender
{
    [self.navigationController setNavigationBarHidden:NO animated:YES];
    [self.navigationController pushViewController:[ReleaseNotesViewController new] animated:YES];
}


@end
