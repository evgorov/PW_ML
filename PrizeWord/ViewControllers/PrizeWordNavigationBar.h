//
//  PrizeWordNavigationBar.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/5/12.
//
//

#import <UIKit/UIKit.h>

@interface PrizeWordNavigationBar : UINavigationBar
{
    UIInterfaceOrientation orientation;
}

+(UIView *)containerWithView:(UIView *)innerView;

@end
