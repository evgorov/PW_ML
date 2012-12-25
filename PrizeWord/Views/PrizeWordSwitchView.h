//
//  PrizeWordSwitchView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/25/12.
//
//

#import <UIKit/UIKit.h>

@interface PrizeWordSwitchView : UIView
{
    IBOutlet UIView *offView;
    IBOutlet UIView *onView;
    IBOutlet UIImageView *imgSlider;
}

-(BOOL)isOn;
+(PrizeWordSwitchView *)switchView;

@end
