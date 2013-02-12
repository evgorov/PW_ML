//
//  RootViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PrizeWordSwitchView.h"
#import "ExternalImage.h"

@class PrizeWordNavigationController;

@interface RootViewController : UIViewController
{
    IBOutlet UIScrollView *mainMenuView;
    IBOutlet UIImageView *mainMenuBg;
    IBOutlet PrizeWordSwitchView *mainMenuVKSwitch;
    IBOutlet PrizeWordSwitchView *mainMenuFBSwitch;
    IBOutlet PrizeWordSwitchView *mainMenuNotificationsSwitch;
    IBOutlet UILabel *mainMenuUserName;
    IBOutlet ExternalImage *mainMenuAvatar;
    IBOutlet UILabel *mainMenuMaxScore;
    IBOutlet UILabel *mainMenuYourResult;
    IBOutlet UIButton *btnScore;
    IBOutlet UIButton *btnRating;
    IBOutlet UIView *rulesView;

    PrizeWordNavigationController * navController;
    UIScrollView * rulesScrollView;

    UIBarButtonItem * currentLeftButton;
    UIBarButtonItem * currentRightButton;
    UIView * currentTitleView;
    IBOutlet UIView *overlayContainer;
    IBOutlet UIView *fullscreenOverlayContainer;
}

@property (readonly) BOOL isMenuHidden;
@property (readonly, nonatomic) UIView * currentOverlay;

-(id)initWithNavigationController:(PrizeWordNavigationController *)navigationController;
-(void)showMenuAnimated:(BOOL)animated;
-(void)hideMenuAnimated:(BOOL)animated;

-(void)showOverlay:(UIView *)overlayView;
-(void)showFullscreenOverlay:(UIView *)overlayView;
-(void)hideOverlay;

@end
