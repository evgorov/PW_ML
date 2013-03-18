//
//  InviteCellView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/31/13.
//
//

#import "InviteCellView.h"
#import "AppDelegate.h"
#import <QuartzCore/QuartzCore.h>

@implementation InviteCellView

@synthesize imgAvatar = _imgAvatar;
@synthesize btnAdd = _btnAdd;
@synthesize lblSurname = _lblSurname;
@synthesize lblName = _lblName;

-(void)awakeFromNib
{
    [super awakeFromNib];
    
    _lblName.font = [UIFont fontWithName:@"DINPro-Bold" size:[AppDelegate currentDelegate].isIPad ? 24 : 20];
    _lblSurname.font = _lblName.font;
    
    CALayer* maskLayer = [CALayer layer];
    maskLayer.frame = CGRectMake(0, 0, _imgAvatar.frame.size.width, _imgAvatar.frame.size.height);
    maskLayer.contents = (__bridge id)[[UIImage imageNamed:@"rating_cell_photo_mask.png"] CGImage];
    _imgAvatar.layer.mask = maskLayer;
}

@end
