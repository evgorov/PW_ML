//
//  InviteViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import "InviteViewController.h"
#import "PrizeWordNavigationBar.h"
#import "UserData.h"
#import "GlobalData.h"
#import "SBJsonParser.h"
#import "InviteCellView.h"
#import <FacebookSDK/FacebookSDK.h>
#import "AppDelegate.h"

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

-(void)handleFacebookDialogResult:(FBWebDialogResult)result resultURL:(NSURL *)resultURL error:(NSError *)error;
-(NSDictionary*)parseURLParams:(NSString *)query;

@end

@implementation InviteViewController

NSString * INVITE_MESSAGE = @"Приглашаю тебя поиграть в PrizeWord – увлекательную и полезную игру! Разгадывай сканворды, участвуй в рейтинге, побеждай!";

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
    
    lblPlaceholder.font = [UIFont fontWithName:@"DINPro-Bold" size:[AppDelegate currentDelegate].isIPad ? 18 : 14];

    if ([GlobalData globalData].loggedInUser.vkProvider != nil)
    {
        [self addFramedView:vkView];
    }
    if ([GlobalData globalData].loggedInUser.fbProvider != nil)
    {
        [self addFramedView:fbView];
    }
    if ([GlobalData globalData].loggedInUser.vkProvider == nil && [GlobalData globalData].loggedInUser.fbProvider == nil)
    {
        [self addFramedView:placeholderView];
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
    
    if ([GlobalData globalData].loggedInUser.vkProvider != nil)
    {
        [self updateData:vkFriends withViews:vkFriendsViews container:vkView andProvider:@"vkontakte"];
    }
    if ([GlobalData globalData].loggedInUser.fbProvider != nil)
    {
        [self updateData:fbFriends withViews:fbFriendsViews container:fbView andProvider:@"facebook"];
    }
}

- (void)dealloc
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
    
    placeholderView = nil;
    lblPlaceholder = nil;
    scrollView.delegate = nil;
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
    
    placeholderView = nil;
    lblPlaceholder = nil;
    scrollView.delegate = nil;
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
        
        NSDictionary * params = @{@"session_key": [GlobalData globalData].sessionKey};
        
        [[APIClient sharedClient] getPath:[NSString stringWithFormat:@"%@/friends", provider] parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
            [self hideActivityIndicator];
            [updateInProgress removeObjectForKey:provider];
            float height = headerView.frame.size.height;
            SBJsonParser * parser = [SBJsonParser new];
            NSArray * friendsData = [parser objectWithData:operation.responseData];
            for (NSDictionary * friendDataInmutable in friendsData)
            {
                NSMutableDictionary * friendData = [friendDataInmutable mutableCopy];
                if ([friendData objectForKey:@"first_name"] == nil || [friendData objectForKey:@"first_name"] == [NSNull null])
                {
                    [friendData setObject:@"" forKey:@"first_name"];
                }
                if ([friendData objectForKey:@"last_name"] == nil || [friendData objectForKey:@"last_name"] == [NSNull null])
                {
                    [friendData setObject:@"" forKey:@"last_name"];
                }
                if ([friendData objectForKey:@"status"] == nil || [friendData objectForKey:@"status"] == [NSNull null])
                {
                    [friendData setObject:@"uninvited" forKey:@"status"];
                }
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
                    [userView.imgAvatar clear];
                    if (userpicURL != nil)
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
        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            [self hideActivityIndicator];
        }];
    }
}

-(void)inviteVKUser:(int)idx
{
    [self showActivityIndicator];
    NSDictionary * userData = [vkFriends objectAtIndex:idx];
    NSDictionary * params = @{@"session_key": [GlobalData globalData].sessionKey
                              , @"ids": [userData objectForKey:@"id"]};
    
    [[APIClient sharedClient] postPath:@"vkontakte/invite" parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
        NSLog(@"invite success: %@", operation.responseString);
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
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"invite failed: %@", error.localizedDescription);
        [self hideActivityIndicator];
    }];
}

-(void)inviteFBUser:(int)idx
{
    [self showActivityIndicator];
    
    NSDictionary * userData = [fbFriends objectAtIndex:idx];

    NSMutableDictionary* params = [NSMutableDictionary dictionaryWithObjectsAndKeys:
                                   @"PrizeWord", @"title",
                                   INVITE_MESSAGE,  @"message",
                                   [userData objectForKey:@"id"], @"to",
                                   nil];

  
    [self showActivityIndicator];
    
    // Invoke the dialog
    [FBWebDialogs presentRequestsDialogModallyWithSession:[FBSession activeSession] message:INVITE_MESSAGE title:@"PrizeWord" parameters:params handler:^(FBWebDialogResult result, NSURL *resultURL, NSError *error) {
         [self hideActivityIndicator];
         [self handleFacebookDialogResult:result resultURL:resultURL error:error];
     }];
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
        NSDictionary * params = @{@"session_key": [GlobalData globalData].sessionKey
                                  , @"ids": ids};
        
        [[APIClient sharedClient] postPath:@"vkontakte/invite" parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
            NSLog(@"invite success: %@", operation.responseString);
            [self hideActivityIndicator];
            for (UIView * subview in vkView.subviews) {
                if ([subview isKindOfClass:[InviteCellView class]])
                {
                    InviteCellView * inviteView = (InviteCellView *)subview;
                    inviteView.btnAdd.enabled = NO;
                }
            }
        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            NSLog(@"invite failed: %@", error.localizedDescription);
            [self hideActivityIndicator];
        }];
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
                                       @"PrizeWord", @"title",
                                       INVITE_MESSAGE,  @"message",
                                       usersString, @"to",
                                       nil];

        // Invoke the dialog
        [FBWebDialogs presentRequestsDialogModallyWithSession:[FBSession activeSession] message:INVITE_MESSAGE title:@"PrizeWord" parameters:params handler:^(FBWebDialogResult result, NSURL *resultURL, NSError *error) {
            [self hideActivityIndicator];
            [self handleFacebookDialogResult:result resultURL:resultURL error:error];
        }];
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

-(void)handleFacebookDialogResult:(FBWebDialogResult)result resultURL:(NSURL *)resultURL error:(NSError *)error
{
    if (error) {
        // Case A: Error launching the dialog or publishing story.
        NSLog(@"Error inviting.");
    } else {
        if (result == FBWebDialogResultDialogNotCompleted) {
            // Case B: User clicked the "x" icon
            NSLog(@"User canceled inviting.");
            UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:@"Ошибка при приглашении друга" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
            [alertView show];
        } else {
            // Case C: Dialog shown and the user clicks Cancel or Share
            NSDictionary * params = [self parseURLParams:resultURL.query];
            if ([params objectForKey:@"request"] == nil)
            {
                [self hideActivityIndicator];
            }
            else
            {
                [self showActivityIndicator];
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
                NSDictionary * params = @{@"session_key": [GlobalData globalData].sessionKey
                                          , @"ids": userIds};
                
                [[APIClient sharedClient] postPath:@"facebook/invite" parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
                    NSLog(@"invite success: %@", operation.responseString);
                    [self hideActivityIndicator];
                    for (UIView * subview in fbView.subviews) {
                        if ([subview isKindOfClass:[InviteCellView class]])
                        {
                            InviteCellView * inviteView = (InviteCellView *)subview;
                            if ([userIds rangeOfString:[(NSDictionary *)[fbFriends objectAtIndex:inviteView.tag] objectForKey:@"id"]].location != NSNotFound)
                            {
                                inviteView.btnAdd.enabled = NO;
                            }
                        }
                    }
                } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
                    NSLog(@"invite failed: %@", error.description);
                    [self hideActivityIndicator];
                }];
            }
        }
    }
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
