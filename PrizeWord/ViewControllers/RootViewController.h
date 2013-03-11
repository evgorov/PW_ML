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
/*
@class GPUImageUIElement;
@class GPUImageView;
@class GPUImageGaussianBlurFilter;
@class GPUImageFastBlurFilter;
*/

@interface RootViewController : PrizeWordViewController<EventListenerDelegate, UIPopoverControllerDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate, UIActionSheetDelegate>
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
/*
    GPUImageUIElement * uiElementInput;
    GPUImageView * gpuImageView;
    GPUImageGaussianBlurFilter * blurFilter;
*/
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
-(void)showRules;

@end
