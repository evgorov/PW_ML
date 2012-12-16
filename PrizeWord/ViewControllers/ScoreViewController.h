//
//  ScoreViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import <UIKit/UIKit.h>

@interface ScoreViewController : UIViewController
{
    IBOutlet UIScrollView *scrollView;
    IBOutlet UIView *contentView;
    
    IBOutlet UIView *puzzlesView;
    IBOutlet UIImageView *puzzlesBorder;
    
    IBOutlet UIView *invitesView;
    IBOutlet UIImageView *invitesBorder;
    IBOutlet UIButton *btnInvite;
    
    NSMutableArray * badges;
    
    UIBarButtonItem * menuItem;
}

@end
