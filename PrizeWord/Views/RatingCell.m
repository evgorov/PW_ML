//
//  RatingCell.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/26/13.
//
//

#import "RatingCell.h"
#import "AppDelegate.h"

@implementation RatingCell

@synthesize imgBackground = _imgBackground;
@synthesize imgPlaceBg = _imgPlaceBg;
@synthesize imgMoveNone = _imgMoveNone;
@synthesize imgMoveUp = _imgMoveUp;
@synthesize imgMoveDown = _imgMoveDown;
@synthesize imgPhoto = _imgPhoto;
@synthesize imgBorder = _imgBorder;
@synthesize lblSurname = _lblSurname;
@synthesize lblName = _lblName;
@synthesize lblSolved = _lblSolved;
@synthesize lblSolvedLabel = _lblSolvedLabel;
@synthesize lblScore = _lblScore;
@synthesize lblPosition = _lblPosition;

-(void)awakeFromNib
{
    [super awakeFromNib];
    _lblName.font = [UIFont fontWithName:@"DINPro-Bold" size:[[AppDelegate currentDelegate] isIPad] ? 20 : 17];
    _lblSurname.font = _lblName.font;
    
    CALayer* maskLayer = [CALayer layer];
    maskLayer.frame = CGRectMake(0, 0, _imgPhoto.frame.size.width, _imgPhoto.frame.size.height);
    maskLayer.contents = (__bridge id)[[UIImage imageNamed:@"rating_cell_photo_mask.png"] CGImage];
    _imgPhoto.layer.mask = maskLayer;
}

@end
