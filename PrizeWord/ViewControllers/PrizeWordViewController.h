//
//  PrizeWordViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/11/12.
//
//

#import <UIKit/UIKit.h>

@interface PrizeWordViewController : UIViewController
{
    UIActivityIndicatorView * activityIndicator;    
}

-(void)showActivityIndicator;
-(void)hideActivityIndicator;

@end
