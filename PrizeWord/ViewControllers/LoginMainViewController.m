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

-(NSDictionary*)parseURLParams:(NSString *)query;
-(void)finalizeAuthorizationWithToken:(NSString *)accessToken forProvider:(NSString *)provider;

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
    if ([GlobalData globalData].fbSession == nil || ![GlobalData globalData].fbSession.isOpen || [[GlobalData globalData].fbSession.expirationDate compare:[NSDate new]] == NSOrderedDescending)
    {
        // create a fresh session object
        [GlobalData globalData].fbSession = [[FBSession alloc] init];
        [FBSession setActiveSession:[GlobalData globalData].fbSession];
        
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
                    [self finalizeAuthorizationWithToken:session.accessToken forProvider:@"facebook"];
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
    [self finalizeAuthorizationWithToken:[GlobalData globalData].fbSession.accessToken forProvider:@"facebook"];
}

- (IBAction)handleVKClick:(UIButton *)sender
{
    UIWebView * vkWebView = [[UIWebView alloc] initWithFrame:self.view.frame];
    [self.view addSubview:vkWebView];
    NSURLRequest * request = [NSURLRequest requestWithURL:[NSURL URLWithString:[NSString stringWithFormat:@"%@vkontakte/login", SERVER_ENDPOINT]]];
    NSLog(@"request: %@", request.URL.path);
    vkWebView.delegate = self;
    vkWebView.hidden = YES;
    [vkWebView loadRequest:request];
}

- (IBAction)handleReleaseNotesClick:(UIButton *)sender
{
    [self.navigationController setNavigationBarHidden:NO animated:YES];
    [self.navigationController pushViewController:[ReleaseNotesViewController new] animated:YES];
}

-(BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{
    NSLog(@"vkontakte: %@", request.description);
    NSDictionary * params = [self parseURLParams:request.URL.query];
    if ([params objectForKey:@"access_token"])
    {
        [webView removeFromSuperview];
        [self finalizeAuthorizationWithToken:[params objectForKey:@"access_token"] forProvider:@"vkontakte"];
        return NO;
    }
    else if ([params objectForKey:@"act"] != nil && [params objectForKey:@"cancel"] != nil && [(NSString *)[params objectForKey:@"act"] compare:@"grant_access"] == NSOrderedSame && [(NSString *)[params objectForKey:@"cancel"] compare:@"1"] == NSOrderedSame)
    {
        [self hideActivityIndicator];
        [webView removeFromSuperview];
        return NO;
    }
    else if ([request.URL.path compare:@"/vkontakte/authorize"] == NSOrderedSame)
    {
        [webView removeFromSuperview];
        NSLog(@"url: %@", request.URL.query);
        
        [self showActivityIndicator];
        APIRequest * apiRequest = [APIRequest getRequest:@"vkontakte/authorize" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
            [self hideActivityIndicator];
            NSLog(@"vkontakte/authorize: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            if (response.statusCode == 200)
            {
                SBJsonParser * parser = [SBJsonParser new];
                NSDictionary * data = [parser objectWithData:receivedData];
                [GlobalData globalData].sessionKey = [data objectForKey:@"session_key"];
                [GlobalData globalData].loggedInUser = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
                [self.navigationController setNavigationBarHidden:NO animated:YES];
                [self.navigationController pushViewController:[PuzzlesViewController new] animated:YES];
            }
        } failCallback:^(NSError *error) {
            [self hideActivityIndicator];
            NSLog(@"vk error: %@", error.description);
        }];
        [apiRequest.params setObject:@"vkontakte" forKey:@"provider_name"];
        [apiRequest.params setObject:[request.URL.query substringFromIndex:[request.URL.query rangeOfString:@"="].location + 1] forKey:@"code"];
        [apiRequest runSilent];
        
        return NO;
    }
    else
    {
        return YES;
    }
}

-(void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{
    NSLog(@"login failed: %@", error.description);
    [webView removeFromSuperview];
    [self hideActivityIndicator];
}

-(void)webViewDidFinishLoad:(UIWebView *)webView
{
    NSLog(@"vkontakte page loaded");
    webView.hidden = NO;
}

-(void)webViewDidStartLoad:(UIWebView *)webView
{
}



-(NSDictionary*)parseURLParams:(NSString *)query
{
    NSArray *pairs = [query componentsSeparatedByString:@"&"];
    NSMutableDictionary *params = [[NSMutableDictionary alloc] init];
    for (NSString *pair in pairs)
    {
        NSArray *kv = [pair componentsSeparatedByString:@"="];
        NSString *val =
        [[kv objectAtIndex:1]
         stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        
        [params setObject:val forKey:[[kv objectAtIndex:0] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        NSLog(@"params: %@=%@", [[kv objectAtIndex:0] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding], [[kv objectAtIndex:1] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding]);
    }
    return params;
}

-(void)finalizeAuthorizationWithToken:(NSString *)accessToken forProvider:(NSString *)provider
{
    [self showActivityIndicator];
    lastAccessToken = accessToken;
    lastProvider = provider;
    APIRequest * request = [APIRequest getRequest:[NSString stringWithFormat:@"%@/authorize", provider] successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        [self hideActivityIndicator];
        if (response.statusCode == 200)
        {
            NSLog(@"%@/authorize: %@", provider, [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            SBJsonParser * parser = [SBJsonParser new];
            NSDictionary * data = [parser objectWithData:receivedData];
            [GlobalData globalData].sessionKey = [data objectForKey:@"session_key"];
            [GlobalData globalData].loggedInUser = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
            [self.navigationController setNavigationBarHidden:NO animated:YES];
            [self.navigationController pushViewController:[PuzzlesViewController new] animated:YES];
        }
        else
        {
            SBJsonParser * parser = [SBJsonParser new];
            NSDictionary * data = [parser objectWithData:receivedData];
            NSLog(@"error: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка сервера" message:[data objectForKey:@"message"] delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
            alert.tag = 1;
            [alert show];
        }
    } failCallback:^(NSError *error) {
        [self hideActivityIndicator];
        UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка сервера" message:error.description delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
        alert.tag = 1;
        [alert show];
    }];
    [request.params setObject:accessToken forKey:@"access_token"];
    [request runSilent];
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
            [self finalizeAuthorizationWithToken:lastAccessToken forProvider:lastProvider];
        }
    }
}

@end
