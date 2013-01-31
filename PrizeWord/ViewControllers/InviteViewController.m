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

@interface InviteViewController (private)

-(void)handleAddClick:(id)sender;
-(void)handleInviteAllClick:(id)sender;

-(void)updateVK;
-(void)updateFB;
-(void)inviteVKUser:(int)idx;
-(void)inviteFBUser:(int)idx;
-(void)inviteAllVKUsers;
-(void)inviteAllFBUsers;

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
    if (btn.superview == vkView)
    {
        [self inviteVKUser:btn.tag];
    }
    else if (btn.superview == fbView)
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
                float height = vkHeader.frame.size.height;
                SBJsonParser * parser = [SBJsonParser new];
                NSDictionary * data = [parser objectWithData:receivedData];
                NSArray * friendsData = [data objectForKey:@"friends"];
                for (NSDictionary * friendData in friendsData)
                {
                    UserData * user = [UserData userDataWithDictionary:friendData];
                    InviteCellView * userView = [[[NSBundle mainBundle] loadNibNamed:@"InviteCellView" owner:self options:nil] objectAtIndex:0];
                    userView.lblName.text = user.first_name;
                    userView.lblSurname.text = user.last_name;
                    userView.btnAdd.enabled = user.invited;
                    userView.btnAdd.tag = vkFriends.count;
                    [userView.btnAdd addTarget:self action:@selector(handleAddClick:) forControlEvents:UIControlEventTouchUpInside];
                    userView.frame = CGRectMake(0, height, userView.frame.size.width, userView.frame.size.height);
                    [vkView addSubview:userView];
                    height += userView.frame.size.height;
                    [vkFriends addObject:user];
                }
                [self resizeView:vkView newHeight:height animated:YES];
            }
            else
            {
                SBJsonParser * parser = [SBJsonParser new];
                NSDictionary * data = [parser objectWithData:receivedData];
                NSString * message = data == nil ? ([NSString stringWithUTF8String:receivedData.bytes]) : [data objectForKey:@"message"];
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
        [request.params setObject:@"vkontakte" forKey:@"provider_name"];
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
                float height = fbHeader.frame.size.height;
                SBJsonParser * parser = [SBJsonParser new];
                NSDictionary * data = [parser objectWithData:receivedData];
                NSArray * friendsData = [data objectForKey:@"friends"];
                for (NSDictionary * friendData in friendsData)
                {
                    UserData * user = [UserData userDataWithDictionary:friendData];
                    InviteCellView * userView = [[[NSBundle mainBundle] loadNibNamed:@"InviteCellView" owner:self options:nil] objectAtIndex:0];
                    userView.lblName.text = user.first_name;
                    userView.lblSurname.text = user.last_name;
                    userView.btnAdd.enabled = user.invited;
                    userView.btnAdd.tag = fbFriends.count;
                    [userView.btnAdd addTarget:self action:@selector(handleAddClick:) forControlEvents:UIControlEventTouchUpInside];
                    userView.frame = CGRectMake(0, height, userView.frame.size.width, userView.frame.size.height);
                    [fbView addSubview:userView];
                    height += userView.frame.size.height;
                    [fbFriends addObject:user];
                }
                [self resizeView:fbView newHeight:height animated:YES];
            }
            else
            {
                SBJsonParser * parser = [SBJsonParser new];
                NSDictionary * data = [parser objectWithData:receivedData];
                NSString * message = data == nil ? ([NSString stringWithUTF8String:receivedData.bytes]) : [data objectForKey:@"message"];
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
        [request.params setObject:@"facebook" forKey:@"provider_name"];
        [request runSilent];
    }
}


-(void)inviteVKUser:(int)idx
{
    [self showActivityIndicator];
    APIRequest * request = [APIRequest putRequest:@"vkontakte/invite" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        NSLog(@"invite success: %@", [NSString stringWithUTF8String:receivedData.bytes]);
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
    UserData * user = [vkFriends objectAtIndex:idx];
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    [request.params setObject:@"vkontakte" forKey:@"provider_name"];
    [request.params setObject:user.provider_id forKey:@"ids"];
    [request runSilent];
}

-(void)inviteFBUser:(int)idx
{
    [self showActivityIndicator];
    APIRequest * request = [APIRequest putRequest:@"facebook/invite" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        NSLog(@"invite success: %@", [NSString stringWithUTF8String:receivedData.bytes]);
        [self hideActivityIndicator];
        for (UIView * subview in fbView.subviews) {
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
    UserData * user = [fbFriends objectAtIndex:idx];
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    [request.params setObject:@"facebook" forKey:@"provider_name"];
    [request.params setObject:user.provider_id forKey:@"ids"];
    [request runSilent];
}

-(void)inviteAllVKUsers
{
    if (vkFriends != nil && vkFriends.count > 0)
    {
        [self showActivityIndicator];
        NSString * ids = @"";
        for (UserData * friendData in vkFriends)
        {
            if (!friendData.invited)
            {
                ids = ids.length > 0 ? [ids stringByAppendingFormat:@",%@", friendData.provider_id] : friendData.provider_id;
            }
        }
        APIRequest * request = [APIRequest putRequest:@"vkontakte/invite" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
            NSLog(@"invite success: %@", [NSString stringWithUTF8String:receivedData.bytes]);
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
        [request.params setObject:@"vkontakte" forKey:@"provider_name"];
        [request.params setObject:ids forKey:@"ids"];
        [request runSilent];
    }
}

-(void)inviteAllFBUsers
{
    if (fbFriends != nil && fbFriends.count > 0)
    {
        [self showActivityIndicator];
        NSString * ids = @"";
        for (UserData * friendData in fbFriends)
        {
            if (!friendData.invited)
            {
                ids = ids.length > 0 ? [ids stringByAppendingFormat:@",%@", friendData.provider_id] : friendData.provider_id;
            }
        }
        APIRequest * request = [APIRequest putRequest:@"facebook/invite" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
            NSLog(@"invite success: %@", [NSString stringWithUTF8String:receivedData.bytes]);
            [self hideActivityIndicator];
            for (UIView * subview in fbView.subviews) {
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
        [request.params setObject:@"facebook" forKey:@"provider_name"];
        [request.params setObject:ids forKey:@"ids"];
        [request runSilent];
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

@end
