//
//  PrizeWordSwitchView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/25/12.
//
//

#import <UIKit/UIKit.h>

@interface PrizeWordSwitchView : UIControl
{
    IBOutlet UIView *offView;
    IBOutlet UIView *onView;
    IBOutlet UIImageView *imgSlider;
    IBOutlet UIImageView *imgDisabled;
}

@property (readonly) BOOL isOn;

-(void)setOn:(BOOL)on animated:(BOOL)animated;
-(void)switchOnAnimated:(BOOL)animated;
-(void)switchOffAnimated:(BOOL)animated;
+(PrizeWordSwitchView *)switchView;

@end
