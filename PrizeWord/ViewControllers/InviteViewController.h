//
//  InviteViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import "BlockedViewController.h"

@interface InviteViewController : BlockedViewController<UIAlertViewDelegate, UIScrollViewDelegate>
{
    IBOutlet UIView *vkView;
    IBOutlet UIView *fbView;
    IBOutlet UIImageView *headerView;

    UIBarButtonItem * inviteAllItem;
    NSMutableArray * vkFriends;
    NSMutableArray * fbFriends;
    NSMutableArray * vkFriendsViews;
    NSMutableArray * fbFriendsViews;
    NSMutableArray * viewsForReuse;
    NSMutableDictionary * updateInProgress;
}

@end
