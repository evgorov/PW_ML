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
#import "EventListenerDelegate.h"
#import "PrizeWordViewController.h"

@class PrizeWordNavigationController;
@class FISound;

@interface RootViewController : PrizeWordViewController<EventListenerDelegate, UIPopoverControllerDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate, UIActionSheetDelegate>
{
    IBOutlet UIScrollView *mainMenuView;
    IBOutlet UIImageView *mainMenuBg;
    IBOutlet PrizeWordSwitchView *mainMenuVKSwitch;
    IBOutlet PrizeWordSwitchView *mainMenuFBSwitch;
    IBOutlet PrizeWordSwitchView *mainMenuNotificationsSwitch;
    IBOutlet UILabel *mainMenuUserName;
    IBOutlet ExternalImage *mainMenuAvatar;
    IBOutlet UIActivityIndicatorView *mainMenuAvatarActivityIndicator;
    IBOutlet UILabel *mainMenuMaxScore;
    IBOutlet UILabel *mainMenuYourResult;
    IBOutlet PrizeWordButton *btnScore;
    IBOutlet PrizeWordButton *btnRating;
    IBOutlet UIView *rulesView;

    PrizeWordNavigationController * navController;
    IBOutlet UIScrollView * rulesScrollView;
    IBOutlet UILabel *rulesCaption;
    UIImageView * rulesPageControl;

    UIBarButtonItem * currentLeftButton;
    UIBarButtonItem * currentRightButton;
    UIView * currentTitleView;
    IBOutlet UIView *overlayContainer;
    IBOutlet UIView *fullscreenOverlayContainer;
    
    UIPopoverController * avatarPopover;

    FISound * sidebarSound;
}

@property (readonly) BOOL isMenuHidden;
@property (readonly, nonatomic) UIView * currentOverlay;

-(id)initWithNavigationController:(PrizeWordNavigationController *)navigationController;
-(void)showMenuAnimated:(BOOL)animated;
-(void)hideMenuAnimated:(BOOL)animated;

-(void)showOverlay:(UIView *)overlayView;
-(void)showFullscreenOverlay:(UIView *)overlayView;
-(void)hideOverlay;
-(void)showOverlay:(UIView *)overlayView animated:(BOOL)animated;
-(void)showFullscreenOverlay:(UIView *)overlayView animated:(BOOL)animated;
-(void)hideOverlayAnimated:(BOOL)animated;
-(void)showRules;

@end
