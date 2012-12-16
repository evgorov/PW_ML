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
    
    NSMutableArray * blockViews;
    
    UIBarButtonItem * menuItem;
}

-(void)addFramedView:(UIView *)view;
-(void)addSimpleView:(UIView *)view;

@end
