//
//  FlipNumberView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 2/9/13.
//
//

#import <UIKit/UIKit.h>

@interface FlipNumberView : UIView
{
    NSMutableArray * topViews;
    NSMutableArray * bottomViews;
}

-(void)reset;
-(void)flipNTimes:(uint)times;

@end
