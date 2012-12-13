//
//  PrizeWordNavigationController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/5/12.
//
//

#import <UIKit/UIKit.h>

@interface PrizeWordNavigationController : UINavigationController
{
    UIView * currentOverlay;
    UIBarButtonItem * currentLeftButton;
    UIBarButtonItem * currentRightButton;
    UIView * currentTitleView;
    IBOutlet UIView *overlayContainer;
}

-(void)showOverlay:(UIView *)overlayView;
-(void)hideOverlay;

@end
