//
//  FramedBlockCell.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/7/13.
//
//

#import "FramedBlockCell.h"
#import "AppDelegate.h"

@interface FramedBlockCell ()
{
    BOOL isFirst;
    BOOL isLast;
    
    UIImageView * frameView;
    UIView * sandBackgroundView;
}

- (void)updateBackground;

@end

@implementation FramedBlockCell

+ (float)height
{
    return [AppDelegate currentDelegate].isIPad ? 116 : 84;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    return;
}

-(void)setupBackgroundForIndexPath:(NSIndexPath *)indexPath inTableView:(UITableView *)tableView
{
    isFirst = indexPath.row == 0;
    isLast = indexPath.row == ([tableView numberOfRowsInSection:indexPath.section] - 1);
    [self updateBackground];
}

- (void)setFrame:(CGRect)frame
{
    [super setFrame:frame];
    [self updateBackground];
}

- (void)setBounds:(CGRect)bounds
{
    [super setBounds:bounds];
    [self updateBackground];
}

- (void)updateBackground
{
    if (sandBackgroundView == nil)
    {
        sandBackgroundView = [UIView new];
        sandBackgroundView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_sand_tile.jpg"]];
        [self insertSubview:sandBackgroundView atIndex:0];
    }
    if (frameView == nil)
    {
        frameView = [UIImageView new];
        [self insertSubview:frameView atIndex:1];
    }
    
    CGRect sandBackgroundFrame;
    if (isFirst && isLast)
    {
        UIImage * baseImage = [UIImage imageNamed:@"frame_border"];
        frameView.image = [baseImage resizableImageWithCapInsets:UIEdgeInsetsMake(baseImage.size.height / 2 - 1, baseImage.size.width / 2 - 1, baseImage.size.height / 2 - 1, baseImage.size.width / 2 - 1) resizingMode:UIImageResizingModeTile];
        sandBackgroundFrame = CGRectInset(self.bounds, [AppDelegate currentDelegate].isIPad ? 18 : 8, [AppDelegate currentDelegate].isIPad ? 22 : 10);
    }
    else if (isFirst)
    {
        UIImage * baseImage = [UIImage imageNamed:@"frame_border_top"];
        frameView.image = [baseImage resizableImageWithCapInsets:UIEdgeInsetsMake(baseImage.size.height - 1, baseImage.size.width / 2 - 1, 0, baseImage.size.width / 2 - 1) resizingMode:UIImageResizingModeTile];
        sandBackgroundFrame = CGRectInset(self.bounds, [AppDelegate currentDelegate].isIPad ? 18 : 8, [AppDelegate currentDelegate].isIPad ? 11 : 5);
        sandBackgroundFrame.origin.y += [AppDelegate currentDelegate].isIPad ? 11 : 5;
    }
    else if (isLast)
    {
        UIImage * baseImage = [UIImage imageNamed:@"frame_border_bottom"];
        frameView.image = [baseImage resizableImageWithCapInsets:UIEdgeInsetsMake(0, baseImage.size.width / 2 - 1, baseImage.size.height - 1, baseImage.size.width / 2 - 1) resizingMode:UIImageResizingModeTile];
        sandBackgroundFrame = CGRectInset(self.bounds, [AppDelegate currentDelegate].isIPad ? 18 : 8, [AppDelegate currentDelegate].isIPad ? 11 : 5);
        sandBackgroundFrame.origin.y -= [AppDelegate currentDelegate].isIPad ? 11 : 5;
    }
    else
    {
        UIImage * baseImage = [UIImage imageNamed:@"frame_border_middle"];
        frameView.image = [baseImage resizableImageWithCapInsets:UIEdgeInsetsMake(0, baseImage.size.width / 2 - 1, 0, baseImage.size.width / 2 - 1) resizingMode:UIImageResizingModeTile];
        sandBackgroundFrame = CGRectInset(self.bounds, [AppDelegate currentDelegate].isIPad ? 18 : 8, 0);
    }
    frameView.frame = self.bounds;
    sandBackgroundView.frame = sandBackgroundFrame;
}

@end
