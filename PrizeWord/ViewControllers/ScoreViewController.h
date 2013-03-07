//
//  ScoreViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import <UIKit/UIKit.h>
#import "BlockedViewController.h"

@interface ScoreViewController : BlockedViewController
{
    IBOutlet UIView *puzzlesView;
    
    IBOutlet UIView *invitesView;
    IBOutlet PrizeWordButton *btnInvite;
    IBOutlet UILabel *lblInvitesScore;
    IBOutlet UILabel *lblInvitesFriendsCount;
    IBOutlet UILabel *lblInvitesFriendsLabel;

    NSMutableDictionary * updateInProgress;
    int invitedFriends;
}

@end
