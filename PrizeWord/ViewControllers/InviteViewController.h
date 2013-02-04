//
//  InviteViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import "BlockedViewController.h"
#import "Facebook.h"

@interface InviteViewController : BlockedViewController<UIAlertViewDelegate, FBDialogDelegate>
{
    IBOutlet UIView *vkView;
    IBOutlet UIView *fbView;
    IBOutlet UIImageView *vkHeader;
    IBOutlet UIImageView *fbHeader;
    
    UIBarButtonItem * inviteAllItem;
    NSMutableArray * vkFriends;
    NSMutableArray * fbFriends;
    Facebook * facebook;
}

@end
