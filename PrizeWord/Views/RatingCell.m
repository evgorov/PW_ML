//
//  RatingCell.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/26/13.
//
//

#import "RatingCell.h"

@implementation RatingCell

@synthesize imgBackground = _imgBackground;
@synthesize imgPlaceBg = _imgPlaceBg;
@synthesize imgMoveNone = _imgMoveNone;
@synthesize imgMoveUp = _imgMoveUp;
@synthesize imgMoveDown = _imgMoveDown;
@synthesize imgPhoto = _imgPhoto;
@synthesize lblName = _lblName;
@synthesize lblSolved = _lblSolved;
@synthesize lblSolvedLabel = _lblSolvedLabel;
@synthesize lblScore = _lblScore;
@synthesize lblPosition = _lblPosition;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
