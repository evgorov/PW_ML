//
//  InviteViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import "BlockedViewController.h"

@interface InviteViewController : BlockedViewController<UIAlertViewDelegate>
{
    IBOutlet UIView *vkView;
    IBOutlet UIView *fbView;
    IBOutlet UIImageView *vkHeader;
    IBOutlet UIImageView *fbHeader;
    
    UIBarButtonItem * inviteAllItem;
    NSMutableArray * vkFriends;
    NSMutableArray * fbFriends;
}

@end
