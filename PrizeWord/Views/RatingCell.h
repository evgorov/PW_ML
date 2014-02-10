//
//  RatingCell.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/26/13.
//
//

#import <UIKit/UIKit.h>
#import "ExternalImage.h"

@interface RatingCell : UITableViewCell
@property (strong, nonatomic) IBOutlet UIView *imgSandBackground;
@property (strong, nonatomic) IBOutlet UIImageView *imgBackground;
@property (strong, nonatomic) IBOutlet UIImageView *imgPlaceBg;
@property (strong, nonatomic) IBOutlet UIImageView *imgMoveNone;
@property (strong, nonatomic) IBOutlet UIImageView *imgMoveUp;
@property (strong, nonatomic) IBOutlet UIImageView *imgMoveDown;
@property (strong, nonatomic) IBOutlet ExternalImage *imgPhoto;
@property (strong, nonatomic) IBOutlet UIImageView *imgBorder;
@property (strong, nonatomic) IBOutlet UILabel *lblSurname;
@property (strong, nonatomic) IBOutlet UILabel *lblName;
@property (strong, nonatomic) IBOutlet UILabel *lblSolved;
@property (strong, nonatomic) IBOutlet UILabel *lblSolvedLabel;
@property (strong, nonatomic) IBOutlet UILabel *lblScore;
@property (strong, nonatomic) IBOutlet UILabel *lblPosition;

@end
