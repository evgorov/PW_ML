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
#import "GlobalData.h"
#import "APIRequest.h"
#import "SBJsonParser.h"
#import "UserData.h"

@interface LoginMainViewController ()

-(void)gotoRoot:(id)sender;
-(void)handleFacebookLoggedIn;

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
    if ([GlobalData globalData].fbSession == nil || ![GlobalData globalData].fbSession.isOpen)
    {
        // create a fresh session object
        [GlobalData globalData].fbSession = [[FBSession alloc] init];
        
        // if we don't have a cached token, a call to open here would cause UX for login to
        // occur; we don't want that to happen unless the user clicks the login button, and so
        // we check here to make sure we have a token before calling open
        if ([GlobalData globalData].fbSession.state != FBSessionStateCreatedTokenLoaded)
        {
            // even though we had a cached token, we need to login to make the session usable
            [[GlobalData globalData].fbSession openWithCompletionHandler:^(FBSession *session,
                                                             FBSessionState status,
                                                             NSError *error) {
                [self hideActivityIndicator];
                if (error == nil)
                {
                    [GlobalData globalData].fbSession = session;
                    [self handleFacebookLoggedIn];
                }
                else
                {
                    UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка facebook" message:error.description delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
                    alert.tag = 0;
                    [alert show];
                }
            }];

            [self showActivityIndicator];
            return;
        }
    }
    [self handleFacebookLoggedIn];
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

-(void)handleFacebookLoggedIn
{
    [self showActivityIndicator];
    APIRequest * request = [APIRequest getRequest:@"facebook/authorize" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        [self hideActivityIndicator];
        SBJsonParser * parser = [SBJsonParser new];
        NSDictionary * data = [parser objectWithData:receivedData];
        [GlobalData globalData].sessionKey = [data objectForKey:@"session_key"];
        [GlobalData globalData].loggedInUser = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
        [self.navigationController setNavigationBarHidden:NO animated:YES];
        [self.navigationController pushViewController:[PuzzlesViewController new] animated:YES];
    } failCallback:^(NSError *error) {
        [self hideActivityIndicator];
        UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка сервера" message:error.description delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
        alert.tag = 1;
        [alert show];
    }];
    [request.params setObject:@"facebook" forKey:@"provider_name"];
    [request.params setObject:[GlobalData globalData].fbSession.accessToken forKey:@"code"];
    [request runSilent];
}

-(void)gotoRoot:(id)sender
{
    [self hideActivityIndicator];
//    [self.navigationController setNavigationBarHidden:NO animated:YES];
//    [self.navigationController pushViewController:[PuzzlesViewController new] animated:YES];
}


-(void)alertView:(UIAlertView *)alertView willDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (buttonIndex != alertView.cancelButtonIndex)
    {
        if (alertView.tag == 0)
        {
            [self handleFacebookClick:nil];
        }
        else if (alertView.tag == 1)
        {
            [self handleFacebookLoggedIn];
        }
    }
}

@end
