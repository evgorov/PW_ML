//
//  SocialNetworks.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 2/23/13.
//
//

#import "SocialNetworks.h"
#import "GlobalData.h"
#import "PrizeWordViewController.h"
#import "APIRequest.h"
#import <FacebookSDK/FacebookSDK.h>
#import "SBJsonParser.h"
#import "UserData.h"

@interface SocialNetworks (private)

-(void)loginFacebookWithViewController:(PrizeWordViewController *)viewController andCallback:(void (^)())callback;
-(void)loginVkontakteWithViewController:(PrizeWordViewController *)viewController andCallback:(void (^)())callback;
-(void)logout;
-(NSDictionary*)parseURLParams:(NSString *)query;
-(void)finalizeAuthorizationWithToken:(NSString *)accessToken forProvider:(NSString *)provider andViewController:(PrizeWordViewController *)viewController;

@end

@implementation SocialNetworks

+(SocialNetworks *)socialNetworks
{
    static SocialNetworks * _socialNetwokrs = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _socialNetwokrs = [[SocialNetworks alloc] init];
    });
    return _socialNetwokrs;
}

+(void)loginFacebookWithViewController:(PrizeWordViewController *)viewController andCallback:(void (^)())callback
{
    [[SocialNetworks socialNetworks] loginFacebookWithViewController:viewController andCallback:callback];
}

+(void)loginVkontakteWithViewController:(PrizeWordViewController *)viewController andCallback:(void (^)())callback
{
    [[SocialNetworks socialNetworks] loginVkontakteWithViewController:viewController andCallback:callback];
}

+(void)logout
{
    [[SocialNetworks socialNetworks] logout];
}



#pragma mark private

-(void)loginFacebookWithViewController:(PrizeWordViewController *)viewController andCallback:(void (^)())callback
{
    lastViewController = viewController;
    successCallback = callback;
    NSLog(@"create new facebook session");
    if ([[FBSession activeSession] isOpen])
    {
        [[FBSession activeSession] close];
    }
    [FBSession setActiveSession:[[FBSession alloc] init]];
    [viewController showActivityIndicator];
    // create a fresh session object
    [FBSession openActiveSessionWithReadPermissions:[NSArray arrayWithObjects:@"read_stream", nil] allowLoginUI:YES completionHandler:^(FBSession *session, FBSessionState status, NSError *error) {
        NSLog(@"facebook completion handler");
        [lastViewController hideActivityIndicator];
        if (error == nil && (status == FBSessionStateOpen || status == FBSessionStateOpenTokenExtended) && session.accessTokenData.accessToken != nil)
        {
            [self finalizeAuthorizationWithToken:session.accessTokenData.accessToken forProvider:@"facebook" andViewController:viewController];
            return;
        }
        
        if (error != nil)
        {
            UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка facebook" message:error.localizedDescription delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
            alert.tag = 0;
            [alert show];
        }
        if (successCallback != nil)
        {
            successCallback();
        }
        [FBSession setActiveSession:nil];
    }];
}

-(void)loginVkontakteWithViewController:(PrizeWordViewController *)viewController andCallback:(void (^)())callback
{
    lastViewController = viewController;
    successCallback = callback;
    
    UIWebView * vkWebView = [[UIWebView alloc] initWithFrame:viewController.view.frame];
    [viewController.view addSubview:vkWebView];
    NSURLRequest * request = [NSURLRequest requestWithURL:[NSURL URLWithString:[NSString stringWithFormat:@"%@vkontakte/login", SERVER_ENDPOINT]] cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData timeoutInterval:20];
    NSLog(@"request: %@", request.URL.path);
    vkWebView.delegate = self;
    vkWebView.hidden = YES;
    [vkWebView loadRequest:request];
    
}

-(void)logout
{
    if ([FBSession activeSession] != nil)
    {
        [[FBSession activeSession] closeAndClearTokenInformation];
        [FBSession setActiveSession:nil];
    }
    [GlobalData globalData].loggedInUser = nil;
    [GlobalData globalData].sessionKey = nil;
    
    // vkontakte logout
    NSHTTPCookie *cookie;
    NSHTTPCookieStorage *storage = [NSHTTPCookieStorage sharedHTTPCookieStorage];
    for (cookie in [storage cookies]) {
        [storage deleteCookie:cookie];
    }
    [[NSUserDefaults standardUserDefaults] synchronize];
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

-(void)finalizeAuthorizationWithToken:(NSString *)accessToken forProvider:(NSString *)provider andViewController:(PrizeWordViewController *)viewController;
{
    [viewController showActivityIndicator];
    lastAccessToken = accessToken;
    lastProvider = provider;
    APIRequest * request = [APIRequest getRequest:[NSString stringWithFormat:@"%@/authorize", provider] successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        [viewController hideActivityIndicator];
        [[GlobalData globalData] parseDateFromResponse:response];
        NSLog(@"%@/authorize: %@", provider, [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
        SBJsonParser * parser = [SBJsonParser new];
        NSDictionary * data = [parser objectWithData:receivedData];
        if ([GlobalData globalData].sessionKey == nil)
        {
            [GlobalData globalData].sessionKey = [data objectForKey:@"session_key"];
            [GlobalData globalData].loggedInUser = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
            successCallback();
        }
        else
        {
            [viewController showActivityIndicator];
            APIRequest * linkRequest = [APIRequest postRequest:@"link_accounts" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
                NSLog(@"link account success: %d %@", response.statusCode, [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
                [viewController hideActivityIndicator];
                successCallback();
            } failCallback:^(NSError *error) {
                [viewController hideActivityIndicator];
                NSLog(@"link accounts failed: %@", error.description);
                [[GlobalData globalData] loadMe];
            }];
            [linkRequest.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key1"];
            [linkRequest.params setObject:[data objectForKey:@"session_key"] forKey:@"session_key2"];
            [linkRequest runUsingCache:NO silentMode:NO];
        }
    } failCallback:^(NSError *error) {
        [viewController hideActivityIndicator];
    }];
    [request.params setObject:accessToken forKey:@"access_token"];
    [request runUsingCache:NO silentMode:NO];
}

#pragma mark UIAlertViewDelegate

-(void)alertView:(UIAlertView *)alertView willDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (buttonIndex != alertView.cancelButtonIndex)
    {
        if (alertView.tag == 0)
        {
            [self loginFacebookWithViewController:lastViewController andCallback:successCallback];
        }
        else if (alertView.tag == 1)
        {
            [self finalizeAuthorizationWithToken:lastAccessToken forProvider:lastProvider andViewController:lastViewController];
        }
    }
}

#pragma mark UIWebViewDelegate

-(BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{
    NSLog(@"vkontakte: %@", request.description);
    NSString * urlString = request.URL.absoluteString;
    NSString * query = request.URL.query;
    if ([urlString rangeOfString:@"html#"].location != NSNotFound)
    {
        query = [urlString substringFromIndex:[urlString rangeOfString:@"html#"].location + 5];
    }
    NSLog(@"query: %@", query);
    NSDictionary * params = [self parseURLParams:query];
    if ([params objectForKey:@"access_token"])
    {
        [webView removeFromSuperview];
        [self finalizeAuthorizationWithToken:[params objectForKey:@"access_token"] forProvider:@"vkontakte" andViewController:lastViewController];
        return NO;
    }
    else if ([params objectForKey:@"act"] != nil && [params objectForKey:@"cancel"] != nil && [(NSString *)[params objectForKey:@"act"] compare:@"grant_access"] == NSOrderedSame && [(NSString *)[params objectForKey:@"cancel"] compare:@"1"] == NSOrderedSame)
    {
        [lastViewController hideActivityIndicator];
        [webView removeFromSuperview];
        if (successCallback != nil)
        {
            successCallback();
        }
        return NO;
    }
    else if ([request.URL.path compare:@"/vkontakte/authorize"] == NSOrderedSame)
    {
        [webView removeFromSuperview];
        NSLog(@"url: %@", request.URL.query);
        
        [lastViewController showActivityIndicator];
        APIRequest * apiRequest = [APIRequest getRequest:@"vkontakte/authorize" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
            [lastViewController hideActivityIndicator];
            NSLog(@"vkontakte/authorize: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            if (response.statusCode == 200)
            {
                SBJsonParser * parser = [SBJsonParser new];
                NSDictionary * data = [parser objectWithData:receivedData];
                [GlobalData globalData].sessionKey = [data objectForKey:@"session_key"];
                [GlobalData globalData].loggedInUser = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
                successCallback();
            }
        } failCallback:^(NSError *error) {
            [lastViewController hideActivityIndicator];
            NSLog(@"vk error: %@", error.description);
        }];
        [apiRequest.params setObject:@"vkontakte" forKey:@"provider_name"];
        [apiRequest.params setObject:[request.URL.query substringFromIndex:[request.URL.query rangeOfString:@"="].location + 1] forKey:@"code"];
        [apiRequest runUsingCache:NO silentMode:YES];
        
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
    [lastViewController hideActivityIndicator];
    UIAlertView * alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:error.localizedDescription delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
    [alert show];
}

-(void)webViewDidFinishLoad:(UIWebView *)webView
{
    NSLog(@"vkontakte page loaded");
    webView.hidden = NO;
}

-(void)webViewDidStartLoad:(UIWebView *)webView
{
}

@end
