//
//  InviteViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import "InviteViewController.h"
#import "PrizeWordNavigationBar.h"
#import "APIRequest.h"
#import "UserData.h"
#import "GlobalData.h"
#import "SBJsonParser.h"
#import "InviteCellView.h"
#import "Facebook.h"

@interface InviteViewController (private)

-(void)handleAddClick:(id)sender;
-(void)handleInviteAllClick:(id)sender;

-(void)updateVK;
-(void)updateFB;
-(void)inviteVKUser:(int)idx;
-(void)inviteFBUser:(int)idx;
-(void)inviteAllVKUsers;
-(void)inviteAllFBUsers;

-(NSDictionary*)parseURLParams:(NSString *)query;

@end

@implementation InviteViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.title = NSLocalizedString(@"TITLE_INVITE", nil);
    
    [self addFramedView:vkView];
    [self addFramedView:fbView];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    UIImage * inviteAllImage = [UIImage imageNamed:@"invite_invite_all_btn"];
    UIImage * inviteAllHighlightedImage = [UIImage imageNamed:@"invite_invite_all_btn_down"];
    UIButton * inviteAllButton = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, inviteAllImage.size.width, inviteAllImage.size.height)];
    [inviteAllButton setBackgroundImage:inviteAllImage forState:UIControlStateNormal];
    [inviteAllButton setBackgroundImage:inviteAllHighlightedImage forState:UIControlStateHighlighted];
    [inviteAllButton addTarget:self action:@selector(handleInviteAllClick:) forControlEvents:UIControlEventTouchUpInside];
    inviteAllItem = [[UIBarButtonItem alloc] initWithCustomView:
                [PrizeWordNavigationBar containerWithView:inviteAllButton]];
    [self.navigationItem setRightBarButtonItem:inviteAllItem animated:animated];
    
    [self updateVK];
    [self updateFB];
}

- (void)viewDidUnload {
    vkView = nil;
    fbView = nil;
    vkHeader = nil;
    fbHeader = nil;
    [super viewDidUnload];
}

-(void)handleAddClick:(id)sender
{
    UIButton * btn = sender;
    if (btn.superview.superview == vkView)
    {
        [self inviteVKUser:btn.tag];
    }
    else if (btn.superview.superview == fbView)
    {
        [self inviteFBUser:btn.tag];
    }
}

-(void)handleInviteAllClick:(id)sender
{
    [self inviteAllVKUsers];
    [self inviteAllFBUsers];
}

-(void)updateVK
{
    if ([[GlobalData globalData].loggedInUser.provider compare:@"vkontakte"] == NSOrderedSame)
    {
        [self showActivityIndicator];
        vkFriends = [NSMutableArray new];
        for (int idx = 0; idx < vkView.subviews.count; ++idx)
        {
            UIView * view = [vkView.subviews objectAtIndex:idx];
            if ([view isKindOfClass:[InviteCellView class]])
            {
                [view removeFromSuperview];
                --idx;
            }
        }
        [self resizeView:vkView newHeight:vkHeader.frame.size.height animated:YES];
        
        APIRequest * request = [APIRequest getRequest:@"vkontakte/friends" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
            [self hideActivityIndicator];
            if (response.statusCode == 200)
            {
                NSLog(@"vkontakte/friends: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
                float height = vkHeader.frame.size.height;
                SBJsonParser * parser = [SBJsonParser new];
                NSArray * friendsData = [parser objectWithData:receivedData];
                for (NSDictionary * friendData in friendsData)
                {
                    //UserData * user = [UserData userDataWithDictionary:friendData];
                    BOOL invited = [(NSNumber *)[friendData objectForKey:@"invite_sent"] boolValue] || [(NSNumber *)[friendData objectForKey:@"invite_used"] boolValue];
                    InviteCellView * userView = [[[NSBundle mainBundle] loadNibNamed:@"InviteCellView" owner:self options:nil] objectAtIndex:0];
                    userView.lblName.text = [friendData objectForKey:@"first_name"];
                    userView.lblSurname.text = [friendData objectForKey:@"last_name"];
                    userView.btnAdd.enabled = !invited;
                    userView.btnAdd.tag = vkFriends.count;
                    [userView.btnAdd addTarget:self action:@selector(handleAddClick:) forControlEvents:UIControlEventTouchUpInside];
                    userView.frame = CGRectMake(0, height, userView.frame.size.width, userView.frame.size.height);
                    [vkView insertSubview:userView atIndex:0];
                    height += userView.frame.size.height;
                    [vkFriends addObject:friendData];
                }
                [self resizeView:vkView newHeight:height animated:YES];
            }
            else
            {
                SBJsonParser * parser = [SBJsonParser new];
                NSDictionary * data = [parser objectWithData:receivedData];
                NSString * message = data == nil ? ([[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]) : [data objectForKey:@"message"];
                UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка сервера" message:message delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
                alert.tag = 1;
                [alert show];
            }
        } failCallback:^(NSError *error) {
            [self hideActivityIndicator];
            UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка сервера" message:error.description delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
            alert.tag = 1;
            [alert show];
        }];
        [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
        [request runSilent];
    }
}

-(void)updateFB
{
    if ([[GlobalData globalData].loggedInUser.provider compare:@"facebook"] == NSOrderedSame)
    {
        [self showActivityIndicator];
        fbFriends = [NSMutableArray new];
        for (int idx = 0; idx < fbView.subviews.count; ++idx)
        {
            UIView * view = [fbView.subviews objectAtIndex:idx];
            if ([view isKindOfClass:[InviteCellView class]])
            {
                [view removeFromSuperview];
                --idx;
            }
        }
        [self resizeView:fbView newHeight:fbHeader.frame.size.height animated:YES];
        
        APIRequest * request = [APIRequest getRequest:@"facebook/friends" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
            [self hideActivityIndicator];
            if (response.statusCode == 200)
            {
                NSLog(@"facebook/friends: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
                float height = fbHeader.frame.size.height;
                SBJsonParser * parser = [SBJsonParser new];
                NSArray * friendsData = [parser objectWithData:receivedData];
                for (NSDictionary * friendData in friendsData)
                {
                    //UserData * user = [UserData userDataWithDictionary:friendData];
                    NSString * name = [friendData objectForKey:@"name"];
                    BOOL invited = [(NSNumber *)[friendData objectForKey:@"invite_sent"] boolValue] || [(NSNumber *)[friendData objectForKey:@"invite_used"] boolValue];
                    InviteCellView * userView = [[[NSBundle mainBundle] loadNibNamed:@"InviteCellView" owner:self options:nil] objectAtIndex:0];
                    userView.lblName.text = [name substringToIndex:[name rangeOfString:@" "].location];
                    userView.lblSurname.text = [name substringFromIndex:[name rangeOfString:@" "].location + 1];
                    userView.btnAdd.enabled = !invited;
                    userView.btnAdd.tag = fbFriends.count;
                    [userView.btnAdd addTarget:self action:@selector(handleAddClick:) forControlEvents:UIControlEventTouchUpInside];
                    userView.frame = CGRectMake(0, height, userView.frame.size.width, userView.frame.size.height);
                    [fbView insertSubview:userView atIndex:0];
                    height += userView.frame.size.height;
                    [fbFriends addObject:friendData];
                }
                [self resizeView:fbView newHeight:height animated:YES];
            }
            else
            {
                SBJsonParser * parser = [SBJsonParser new];
                NSDictionary * data = [parser objectWithData:receivedData];
                NSString * message = data == nil ? ([[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]) : [data objectForKey:@"message"];
                UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка сервера" message:message delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
                alert.tag = 2;
                [alert show];
            }
        } failCallback:^(NSError *error) {
            [self hideActivityIndicator];
            UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка сервера" message:error.description delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
            alert.tag = 2;
            [alert show];
        }];
        [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
        [request runSilent];
    }
}


-(void)inviteVKUser:(int)idx
{
    [self showActivityIndicator];
    APIRequest * request = [APIRequest postRequest:@"vkontakte/invite" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        NSLog(@"invite success: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
        [self hideActivityIndicator];
        for (UIView * subview in vkView.subviews) {
            if ([subview isKindOfClass:[InviteCellView class]])
            {
                InviteCellView * inviteView = (InviteCellView *)subview;
                if (inviteView.btnAdd.tag == idx) {
                    inviteView.btnAdd.enabled = NO;
                    break;
                }
            }
        }
    } failCallback:^(NSError *error) {
        NSLog(@"invite failed: %@", error.description);
        [self hideActivityIndicator];
    }];
    NSDictionary * userData = [vkFriends objectAtIndex:idx];
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    [request.params setObject:[userData objectForKey:@"id"] forKey:@"ids"];
    [request runSilent];
}

-(void)inviteFBUser:(int)idx
{
    [self showActivityIndicator];
    
    NSDictionary * userData = [fbFriends objectAtIndex:idx];
    NSMutableDictionary* params = [NSMutableDictionary dictionaryWithObjectsAndKeys:
                                   @"PrizeWord Hello!", @"title",
                                   @"Come check out PrizeWord.",  @"message",
                                   [userData objectForKey:@"id"], @"to",
                                   nil];
    
    facebook = [[Facebook alloc]
                     initWithAppId:FBSession.activeSession.appID
                     andDelegate:nil];
    
    // Store the Facebook session information
    facebook.accessToken = FBSession.activeSession.accessToken;
    facebook.expirationDate = FBSession.activeSession.expirationDate;
    
    [facebook dialog:@"apprequests" andParams:params andDelegate:self];
}

-(void)inviteAllVKUsers
{
    if (vkFriends != nil && vkFriends.count > 0)
    {
        [self showActivityIndicator];
        NSString * ids = @"";
        for (NSDictionary * friendData in vkFriends)
        {
            if (!([(NSNumber *)[friendData objectForKey:@"invite_sent"] boolValue] || [(NSNumber *)[friendData objectForKey:@"invite_used"] boolValue]))
            {
                ids = ids.length > 0 ? [ids stringByAppendingFormat:@",%@", [friendData objectForKey:@"id"]] : [friendData objectForKey:@"id"];
            }
        }
        APIRequest * request = [APIRequest postRequest:@"vkontakte/invite" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
            NSLog(@"invite success: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            [self hideActivityIndicator];
            for (UIView * subview in vkView.subviews) {
                if ([subview isKindOfClass:[InviteCellView class]])
                {
                    InviteCellView * inviteView = (InviteCellView *)subview;
                    inviteView.btnAdd.enabled = NO;
                }
            }
        } failCallback:^(NSError *error) {
            NSLog(@"invite failed: %@", error.description);
            [self hideActivityIndicator];
        }];
        [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
        [request.params setObject:ids forKey:@"ids"];
        [request runSilent];
    }
}

-(void)inviteAllFBUsers
{
    if (fbFriends != nil && fbFriends.count > 0)
    {
        [self showActivityIndicator];
        
        NSMutableString * usersString = [NSMutableString new];
        for (NSDictionary * userData in fbFriends)
        {
            if (!([(NSNumber *)[userData objectForKey:@"invite_sent"] boolValue] || [(NSNumber *)[userData objectForKey:@"invite_used"] boolValue]))
            {
                if (usersString.length != 0)
                {
                    [usersString appendString:@","];
                }
                [usersString appendFormat:@"%@", [userData objectForKey:@"id"]];
            }
        }
        NSMutableDictionary* params = [NSMutableDictionary dictionaryWithObjectsAndKeys:
                                       @"PrizeWord Hello!", @"title",
                                       @"Come check out PrizeWord.",  @"message",
                                       usersString, @"to",
                                       nil];
        
        facebook = [[Facebook alloc]
                    initWithAppId:FBSession.activeSession.appID
                    andDelegate:nil];
        
        // Store the Facebook session information
        facebook.accessToken = FBSession.activeSession.accessToken;
        facebook.expirationDate = FBSession.activeSession.expirationDate;
        
        [facebook dialog:@"apprequests" andParams:params andDelegate:self];
    }
}


-(void)alertView:(UIAlertView *)alertView willDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (buttonIndex != alertView.cancelButtonIndex)
    {
        if (alertView.tag == 1)
        {
            [self updateVK];
        }
        else if (alertView.tag == 2)
        {
            [self updateFB];
        }
    }
}

-(void)dialog:(FBDialog *)dialog didFailWithError:(NSError *)error
{
    [self hideActivityIndicator];
    NSLog(@"dialog error: %@", error.description);
}

-(void)dialogCompleteWithUrl:(NSURL *)url
{
    NSLog(@"dialog complete with URL: %@", url.description);
    
    NSDictionary * params = [self parseURLParams:url.query];
    if ([params objectForKey:@"request"] == nil)
    {
        [self hideActivityIndicator];
    }
    else
    {
        NSMutableString * userIds = [NSMutableString new];
        [params enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
            if ([(NSString *)key rangeOfString:@"to"].location == 0)
            {
                if (userIds.length != 0)
                {
                    [userIds appendString:@","];
                }
                [userIds appendFormat:@"%@", obj];
            }
        }];
        APIRequest * request = [APIRequest postRequest:@"facebook/invite" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
            NSLog(@"invite success: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            [self hideActivityIndicator];
            for (UIView * subview in fbView.subviews) {
                if ([subview isKindOfClass:[InviteCellView class]])
                {
                    InviteCellView * inviteView = (InviteCellView *)subview;
                    if ([userIds rangeOfString:[(NSDictionary *)[fbFriends objectAtIndex:inviteView.btnAdd.tag] objectForKey:@"id"]].location != NSNotFound)
                    {
                        inviteView.btnAdd.enabled = NO;
                    }
                }
            }
        } failCallback:^(NSError *error) {
            NSLog(@"invite failed: %@", error.description);
            [self hideActivityIndicator];
        }];
        [request.params setObject:userIds forKey:@"ids"];
        [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
        [request runSilent];
    }
}

-(void)dialogDidNotComplete:(FBDialog *)dialog
{
    [self hideActivityIndicator];
    NSLog(@"dialog did not complete: %@", dialog.description);
}

-(BOOL)dialog:(FBDialog *)dialog shouldOpenURLInExternalBrowser:(NSURL *)url
{
    NSLog(@"dialog should open external url: %@", url.description);
    return YES;
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

@end
