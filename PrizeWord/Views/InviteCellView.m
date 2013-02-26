//
//  InviteCellView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/31/13.
//
//

#import "InviteCellView.h"
#import "AppDelegate.h"

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
}

@end