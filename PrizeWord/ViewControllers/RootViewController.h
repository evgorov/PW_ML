//
//  RootViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface RootViewController : UIViewController
{
    IBOutlet UIScrollView *mainMenuView;
    IBOutlet UIImageView *mainMenuBg;
    IBOutlet UIButton *btnScore;
    IBOutlet UIButton *btnRating;
    
    UINavigationController * navController;
}

@property (readonly) BOOL isMenuHidden;

-(id)initWithNavigationController:(UINavigationController *)navigationController;
-(void)showMenuAnimated:(BOOL)animated;
-(void)hideMenuAnimated:(BOOL)animated;

@end
