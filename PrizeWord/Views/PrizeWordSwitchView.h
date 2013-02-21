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
-(void)switchOnAnimated:(BOOL)animated;
-(void)switchOffAnimated:(BOOL)animated;
+(PrizeWordSwitchView *)switchView;

@end
