//
//  ScoreInviteCellView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 2/2/13.
//
//

#import <UIKit/UIKit.h>
#import "ExternalImage.h"

@interface ScoreInviteCellView : UIView

@property (strong, nonatomic) IBOutlet ExternalImage *imgAvatar;
@property (strong, nonatomic) IBOutlet UILabel *lblScore;
@property (strong, nonatomic) IBOutlet UILabel *lblName;

@end
