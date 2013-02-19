//
//  BlockedViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import <UIKit/UIKit.h>
#import "PrizeWordViewController.h"

@interface BlockedViewController : PrizeWordViewController
{
    UIScrollView * scrollView;
    UIView * contentView;
    
    UIBarButtonItem * menuItem;
}

-(void)addFramedView:(UIView *)view;
-(void)addSimpleView:(UIView *)view;
-(void)removeFramedView:(UIView *)view;
-(void)removeSimpleView:(UIView *)view;
-(void)resizeView:(UIView *)view newHeight:(float)height;
-(void)resizeView:(UIView *)view newHeight:(float)height animated:(BOOL)animated;

@end
