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

#define TAG_VKONTAKTE 1
#define TAG_FACEBOOK 2

@interface InviteViewController (private)

-(void)handleAddClick:(id)sender;
-(void)handleInviteAllClick:(id)sender;
-(void)updateData:(NSMutableArray *)data withViews:(NSMutableArray *)views container:(UIView *)container andProvider:(NSString *)provider;
-(void)updateContainer:(UIView *)container withViews:(NSMutableArray *)views andData:(NSMutableArray *)data;
-(void)inviteVKUser:(int)idx;
-(void)inviteFBUser:(int)idx;
-(void)inviteAllVKUsers;
-(void)inviteAllFBUsers;

-(NSString *)userpicForData:(NSDictionary *)userData;

-(NSDictionary*)parseURLParams:(NSString *)query;

@end

@implementation InviteViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.title = NSLocalizedString(@"TITLE_INVITE", nil);
    scrollView.delegate = self;
    viewsForReuse = [NSMutableArray new];
    updateInProgress = [NSMutableDictionary new];
    
    vkFriends = [NSMutableArray new];
    fbFriends = [NSMutableArray new];
    vkFriendsViews = [NSMutableArray new];
    fbFriendsViews = [NSMutableArray new];

    if ([GlobalData globalData].loggedInUser.vkProvider != nil)
    {
        [self addFramedView:vkView];
    }
    if ([GlobalData globalData].loggedInUser.fbProvider != nil)
    {
        [self addFramedView:fbView];
    }
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    UIImage * inviteAllImage = [UIImage imageNamed:@"invite_invite_all_btn"];
    UIImage * inviteAllHighlightedImage = [UIImage imageNamed:@"invite_invite_all_btn_down"];
    PrizeWordButton * inviteAllButton = [[PrizeWordButton alloc] initWithFrame:CGRectMake(0, 0, inviteAllImage.size.width, inviteAllImage.size.height)];
    [inviteAllButton setBackgroundImage:inviteAllImage forState:UIControlStateNormal];
    [inviteAllButton setBackgroundImage:inviteAllHighlightedImage forState:UIControlStateHighlighted];
    [inviteAllButton addTarget:self action:@selector(handleInviteAllClick:) forControlEvents:UIControlEventTouchUpInside];
    inviteAllItem = [[UIBarButtonItem alloc] initWithCustomView:
                [PrizeWordNavigationBar containerWithView:inviteAllButton]];
    [self.navigationItem setRightBarButtonItem:inviteAllItem animated:animated];
    
    [self updateData:vkFriends withViews:vkFriendsViews container:vkView andProvider:@"vkontakte"];
    [self updateData:fbFriends withViews:fbFriendsViews container:fbView andProvider:@"facebook"];
}

- (void)viewDidUnload
{
    vkView = nil;
    fbView = nil;
    headerView = nil;
    viewsForReuse = nil;
    updateInProgress = nil;
    vkFriends = nil;
    fbFriends = nil;
    vkFriendsViews = nil;
    fbFriendsViews = nil;
    
    facebook = nil;
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

-(void)updateData:(NSMutableArray *)data withViews:(NSMutableArray *)views container:(UIView *)container andProvider:(NSString *)provider
{
    if ([updateInProgress objectForKey:provider] == nil)
    {
        [updateInProgress setObject:[NSNumber numberWithBool:YES] forKey:provider];
        [self showActivityIndicator];
        [data removeAllObjects];
        [views removeAllObjects];
        for (int idx = 0; idx < container.subviews.count; ++idx)
        {
            UIView * view = [container.subviews objectAtIndex:idx];
            if ([view isKindOfClass:[InviteCellView class]])
            {
                [view removeFromSuperview];
                --idx;
            }
        }
        [self resizeView:container newHeight:headerView.frame.size.height animated:YES];
        
        APIRequest * request = [APIRequest getRequest:[NSString stringWithFormat:@"%@/friends", provider] successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
            [self hideActivityIndicator];
            [updateInProgress removeObjectForKey:provider];
            if (response.statusCode == 200)
            {
                NSLog(@"%@/friends: %@", provider, [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
                float height = headerView.frame.size.height;
                SBJsonParser * parser = [SBJsonParser new];
                NSArray * friendsData = [parser objectWithData:receivedData];
                for (NSDictionary * friendData in friendsData)
                {
                    //UserData * user = [UserData userDataWithDictionary:friendData];
                    if (views.count == 0)
                    {
                        InviteCellView * userView = [[[NSBundle mainBundle] loadNibNamed:@"InviteCellView" owner:self options:nil] objectAtIndex:0];
                        userView.lblName.text = [friendData objectForKey:@"first_name"];
                        userView.lblSurname.text = [friendData objectForKey:@"last_name"];
                        userView.btnAdd.enabled = [(NSString *)[friendData objectForKey:@"status"] compare:@"uninvited"] == NSOrderedSame;
                        userView.btnAdd.tag = 0;
                        [userView.btnAdd addTarget:self action:@selector(handleAddClick:) forControlEvents:UIControlEventTouchUpInside];
                        userView.frame = CGRectMake(0, height, userView.frame.size.width, userView.frame.size.height);
                        NSString * userpicURL = [self userpicForData:friendData];
                        if (userpicURL == nil)
                        {
                            [userView.imgAvatar clear];
                        }
                        else
                        {
                            [userView.imgAvatar loadImageFromURL:[NSURL URLWithString:userpicURL]];
                        }
                        [container insertSubview:userView atIndex:0];
                        height += userView.frame.size.height * friendsData.count;
                        userView.tag = 0;
                        [views addObject:userView];
                    }
                    [data addObject:friendData];
                }
                [self resizeView:container newHeight:height animated:YES];
                [self updateContainer:container withViews:views andData:data];
            }
            else
            {
                SBJsonParser * parser = [SBJsonParser new];
                NSDictionary * data = [parser objectWithData:receivedData];
                NSString * message = data == nil ? ([[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]) : [data objectForKey:@"message"];
                UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка сервера" message:message delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
                alert.tag = [provider compare:@"facebook"] == NSOrderedSame ? TAG_FACEBOOK : TAG_VKONTAKTE;
                [alert show];
            }
        } failCallback:^(NSError *error) {
            [self hideActivityIndicator];
            [updateInProgress removeObjectForKey:provider];
            UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка сервера" message:error.description delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
            alert.tag = [provider compare:@"facebook"] == NSOrderedSame ? TAG_FACEBOOK : TAG_VKONTAKTE;
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
                if (inviteView.tag == idx) {
                    inviteView.btnAdd.enabled = NO;
                    break;
                }
            }
        }
        
        APIRequest * request = [APIRequest postRequest:@"score" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
            NSLog(@"score success! %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            [[GlobalData globalData] loadMe];
        } failCallback:^(NSError *error) {
            NSLog(@"score error! %@", error.description);
        }];
        
        NSDictionary * userData = [vkFriends objectAtIndex:idx];
        [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
        [request.params setObject:[NSString stringWithFormat:@"%d", [GlobalData globalData].scoreForFriend] forKey:@"score"];
        [request.params setObject:[NSString stringWithFormat:@"friend_vk#%@", [userData objectForKey:@"id"]] forKey:@"source"];
        [request runSilent];
        
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
        NSMutableString * ids = [NSMutableString new];
        for (NSDictionary * friendData in vkFriends)
        {
            if (!([(NSNumber *)[friendData objectForKey:@"invite_sent"] boolValue] || [(NSNumber *)[friendData objectForKey:@"invite_used"] boolValue]))
            {
                [ids appendFormat:(ids.length > 0 ? @",%@" : @"%@"), [friendData objectForKey:@"id"]];
            }
        }
        APIRequest * request = [APIRequest postRequest:@"vkontakte/invite" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
            NSLog(@"invite success: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            // TODO :: add score
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
        int count = 0;
        for (NSDictionary * userData in fbFriends)
        {
            if ([(NSString *)[userData objectForKey:@"status"] compare:@"uninvited"] == NSOrderedSame)
            {
                [usersString appendFormat:@"%@,", [userData objectForKey:@"id"]];
                ++count;
            }
            if (count >= 50)
            {
                break;
            }
        }
        NSLog(@"invite fb friends: %@", usersString);
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

-(void)updateContainer:(UIView *)container withViews:(NSMutableArray *)views andData:(NSMutableArray *)data
{
    while (views.count > 1 && [(InviteCellView *)[views objectAtIndex:0] frame].origin.y + [(InviteCellView *)[views objectAtIndex:0] frame].size.height + container.frame.origin.y < scrollView.contentOffset.y)
    {
        [[(InviteCellView *)[views objectAtIndex:0] imgAvatar] clear];
        [viewsForReuse addObject:[views objectAtIndex:0]];
        [views removeObjectAtIndex:0];
    }
    while (views.count > 1 && [(InviteCellView *)[views lastObject] frame].origin.y + container.frame.origin.y > scrollView.contentOffset.y + scrollView.frame.size.height)
    {
        [[(InviteCellView *)views.lastObject imgAvatar] clear];
        [viewsForReuse addObject:[views lastObject]];
        [views removeLastObject];
    }
    
    while (views.count != 0 && [(InviteCellView *)[views objectAtIndex:0] frame].origin.y + container.frame.origin.y >= scrollView.contentOffset.y && [(InviteCellView *)[views objectAtIndex:0] tag] > 0)
    {
        InviteCellView * firstView = [views objectAtIndex:0];
        InviteCellView * newView = nil;
        if (viewsForReuse.count > 0)
        {
            newView = viewsForReuse.lastObject;
            [viewsForReuse removeLastObject];
        }
        else
        {
            newView = [[[NSBundle mainBundle] loadNibNamed:@"InviteCellView" owner:self options:nil] objectAtIndex:0];
            [newView.btnAdd addTarget:self action:@selector(handleAddClick:) forControlEvents:UIControlEventTouchUpInside];
        }
        newView.tag = firstView.tag - 1;
        newView.btnAdd.tag = newView.tag;
        NSDictionary * userData = [data objectAtIndex:newView.tag];
        newView.lblName.text = [userData objectForKey:@"first_name"];
        newView.lblSurname.text = [userData objectForKey:@"last_name"];
        newView.btnAdd.enabled = [(NSString *)[userData objectForKey:@"status"] compare:@"uninvited"] == NSOrderedSame;
        NSString * userpicURL = [self userpicForData:userData];
        if (userpicURL == nil)
        {
            [newView.imgAvatar clear];
        }
        else
        {
            [newView.imgAvatar loadImageFromURL:[NSURL URLWithString:userpicURL]];
        }

        newView.frame = CGRectMake(0, firstView.frame.origin.y - newView.frame.size.height, newView.frame.size.width, newView.frame.size.height);
        [container insertSubview:newView atIndex:0];
        [views insertObject:newView atIndex:0];
    }

    while (views.count != 0 && [(InviteCellView *)[views lastObject] frame].origin.y + [(InviteCellView *)[views lastObject] frame].size.height + container.frame.origin.y <= scrollView.contentOffset.y + scrollView.frame.size.height && [(InviteCellView *)[views lastObject] tag] < data.count - 1)
    {
        InviteCellView * lastView = [views lastObject];
        InviteCellView * newView = nil;
        if (viewsForReuse.count > 0)
        {
            newView = viewsForReuse.lastObject;
            [viewsForReuse removeLastObject];
        }
        else
        {
            newView = [[[NSBundle mainBundle] loadNibNamed:@"InviteCellView" owner:self options:nil] objectAtIndex:0];
            [newView.btnAdd addTarget:self action:@selector(handleAddClick:) forControlEvents:UIControlEventTouchUpInside];
        }
        newView.tag = lastView.tag + 1;
        newView.btnAdd.tag = newView.tag;
        NSDictionary * userData = [data objectAtIndex:newView.tag];
        newView.lblName.text = [userData objectForKey:@"first_name"];
        newView.lblSurname.text = [userData objectForKey:@"last_name"];
        newView.btnAdd.enabled = [(NSString *)[userData objectForKey:@"status"] compare:@"uninvited"] == NSOrderedSame;
        NSString * userpicURL = [self userpicForData:userData];
        if (userpicURL == nil)
        {
            [newView.imgAvatar clear];
        }
        else
        {
            [newView.imgAvatar loadImageFromURL:[NSURL URLWithString:userpicURL]];
        }
        newView.frame = CGRectMake(0, lastView.frame.origin.y + lastView.frame.size.height, newView.frame.size.width, newView.frame.size.height);
        [container insertSubview:newView atIndex:0];
        [views addObject:newView];
    }
}

-(void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    [self updateContainer:vkView withViews:vkFriendsViews andData:vkFriends];
    [self updateContainer:fbView withViews:fbFriendsViews andData:fbFriends];
}

-(void)alertView:(UIAlertView *)alertView willDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (buttonIndex != alertView.cancelButtonIndex)
    {
        if (alertView.tag == TAG_VKONTAKTE)
        {
            [self updateData:vkFriends withViews:vkFriendsViews container:vkView andProvider:@"vkontakte"];
        }
        else if (alertView.tag == TAG_FACEBOOK)
        {
            [self updateData:fbFriends withViews:fbFriendsViews container:fbView andProvider:@"facebook"];
        }
    }
}

-(void)dialog:(FBDialog *)dialog didFailWithError:(NSError *)error
{
    [self hideActivityIndicator];
    NSLog(@"dialog error: %@", error.description);
}

-(void)dialogDidComplete:(FBDialog *)dialog
{
    NSLog(@"dialogDidComplete");
}

-(void)dialogDidNotCompleteWithUrl:(NSURL *)url
{
    NSLog(@"dialogDidNotCompleteWithURL: %@", url.description);
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
        NSLog(@"userIds: %@", userIds);
        APIRequest * request = [APIRequest postRequest:@"facebook/invite" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
            NSLog(@"invite success: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            [self hideActivityIndicator];
            for (UIView * subview in fbView.subviews) {
                if ([subview isKindOfClass:[InviteCellView class]])
                {
                    InviteCellView * inviteView = (InviteCellView *)subview;
                    if ([userIds rangeOfString:[(NSDictionary *)[fbFriends objectAtIndex:inviteView.tag] objectForKey:@"id"]].location != NSNotFound)
                    {
                        inviteView.btnAdd.enabled = NO;
                        APIRequest * request = [APIRequest postRequest:@"score" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
                            NSLog(@"score success! %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
                            [[GlobalData globalData] loadMe];
                        } failCallback:^(NSError *error) {
                            NSLog(@"score error! %@", error.description);
                        }];
                        
                        NSDictionary * userData = [fbFriends objectAtIndex:inviteView.tag];
                        [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
                        [request.params setObject:[NSString stringWithFormat:@"%d", [GlobalData globalData].scoreForFriend] forKey:@"score"];
                        [request.params setObject:[NSString stringWithFormat:@"friend_fb#%@", [userData objectForKey:@"id"]] forKey:@"source"];
                        [request runSilent];
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


-(NSString *)userpicForData:(NSDictionary *)userData
{
    NSString * userpicString = [userData objectForKey:@"userpic"];
    if (userpicString == nil || userpicString == (id)[NSNull null])
    {
        userpicString = [userData objectForKey:@"photo_medium"];
    }
    if (userpicString == (id)[NSNull null])
    {
        userpicString = nil;
    }
    return userpicString;
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
