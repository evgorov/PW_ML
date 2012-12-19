//
//  PuzzleSetView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/19/12.
//
//

#import "PuzzleSetView.h"
#import "BadgeView.h"

@interface PuzzleSetView (private)

-(void)initHeaderWithType:(PuzzleSetType)type;
-(void)initElementsWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price;
-(void)initBoughtElementsWithType:(PuzzleSetType)type puzzlesCount:(int)count puzzlesSolved:(int)solved score:(int)score ids:(NSArray *)ids percents:(NSArray *)percents;
-(NSString *)stringWithScore:(int)score;

@end

@implementation PuzzleSetView

@synthesize imgBar = _imgBar;
@synthesize lblCaption = _lblCaption;
@synthesize lblCount = _lblCount;
@synthesize lblScore = _lblScore;
@synthesize btnBuy = _btnBuy;
@synthesize badges = _badges;

-(id)initWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price
{
    self = [super initWithFrame:CGRectMake(0, 0, 303, 130)];
    if (self)
    {
        puzzlesCount = count;
        setType = type;
        
        [self initHeaderWithType:type];
        [self initElementsWithType:type puzzlesCount:count minScore:score price:price];
        _badges = nil;
        
        self.autoresizesSubviews = NO;
        self.clipsToBounds = YES;
    }
    return self;
}

-(id)initWithType:(PuzzleSetType)type puzzlesCount:(int)count puzzlesSolved:(int)solved score:(int)score ids:(NSArray *)ids percents:(NSArray *)percents
{
    self = [super initWithFrame:CGRectMake(0, 0, 303, 105)];
    if (self != nil)
    {
        puzzlesCount = count;
        setType = type;
        
        [self initHeaderWithType:type];
        [self initBoughtElementsWithType:type puzzlesCount:count puzzlesSolved:solved score:score ids:ids percents:percents];
        
        if (_badges.count > 0)
        {
            BadgeView * lastBadge = [_badges lastObject];
            self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, self.frame.size.width, lastBadge.frame.origin.y + 115);
        }
    }
    return self;
}

-(void)initHeaderWithType:(PuzzleSetType)type
{
    UIImageView * delimeter = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"puzzles_set_delimeter"]];
    [self addSubview:delimeter];
    
    NSString * barFilename = nil;
    if (type == PUZZLESET_BRILLIANT) {
        barFilename = @"puzzles_set_br";
    } else if (type == PUZZLESET_GOLD) {
        barFilename = @"puzzles_set_au";
    } else if (type == PUZZLESET_SILVER) {
        barFilename = @"puzzles_set_ag";
    } else if (type == PUZZLESET_SILVER2) {
        barFilename = @"puzzles_set_ag2";
    } else if (type == PUZZLESET_FREE) {
        barFilename = @"puzzles_set_fr";
    }
    if (barFilename != nil)
    {
        _imgBar = [[UIImageView alloc] initWithImage:[UIImage imageNamed:barFilename]];
        _imgBar.frame = CGRectMake(14, 17, _imgBar.frame.size.width, _imgBar.frame.size.height);
        [self addSubview:_imgBar];
    }
    _lblCaption = [[UILabel alloc] initWithFrame:CGRectMake(76, 13, 225, 32)];
    _lblCaption.font = [UIFont fontWithName:@"DINPro-Bold" size:25];
    _lblCaption.textColor = [UIColor colorWithRed:80/255.f green:79/255.f blue:74/255.f alpha:1];
    _lblCaption.shadowColor = [UIColor whiteColor];
    _lblCaption.backgroundColor = [UIColor clearColor];
    _lblCaption.shadowOffset = CGSizeMake(0, 1);
    _lblCaption.textAlignment = NSTextAlignmentLeft;
    if (type == PUZZLESET_BRILLIANT) {
        _lblCaption.text = @"Бриллиантовый";
    } else if (type == PUZZLESET_GOLD) {
        _lblCaption.text = @"Золотой";
    } else if (type == PUZZLESET_SILVER) {
        _lblCaption.text = @"Серебряный";
    } else if (type == PUZZLESET_SILVER2) {
        _lblCaption.text = @"2-й Серебряный";
    } else if (type == PUZZLESET_FREE) {
        _lblCaption.text = @"Бесплатный";
    }
    [self addSubview:_lblCaption];
}

-(void)initElementsWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price
{
    UIFont * helveticaBold12 = [UIFont fontWithName:@"HelveticaNeue-Bold" size:12];
    UIFont * helvetica12 = [UIFont fontWithName:@"HelveticaNeue" size:12];
    NSString * countString = [NSString stringWithFormat:@"%d", count];
    CGSize countSize = [countString sizeWithFont:helveticaBold12];
    _lblCount = [[UILabel alloc] initWithFrame:CGRectMake(16, 50, countSize.width, countSize.height)];
    _lblCount.font = helveticaBold12;
    _lblCount.text = countString;
    _lblCount.textColor = [UIColor colorWithRed:112/255.f green:99/255.f blue:88/255.f alpha:1];
    _lblCount.backgroundColor = [UIColor clearColor];
    _lblCount.shadowColor = [UIColor whiteColor];
    _lblCount.shadowOffset = CGSizeMake(0, 1);
    _lblCount.textAlignment = NSTextAlignmentLeft;
    [self addSubview:_lblCount];
    
    NSString * text = (type == PUZZLESET_FREE) ? @" сканвордов " : @" сканвордов, минимум ";
    CGSize textSize = [text sizeWithFont:helvetica12];
    UILabel * lblText = [[UILabel alloc] initWithFrame:CGRectMake(_lblCount.frame.origin.x + _lblCount.frame.size.width, _lblCount.frame.origin.y, textSize.width, _lblCount.frame.size.height)];
    lblText.font = helvetica12;
    lblText.text = text;
    lblText.textColor = [UIColor colorWithRed:112/255.f green:99/255.f blue:88/255.f alpha:1];
    lblText.shadowColor = [UIColor whiteColor];
    lblText.backgroundColor = [UIColor clearColor];
    lblText.shadowOffset = CGSizeMake(0, 1);
    lblText.textAlignment = NSTextAlignmentLeft;
    [self addSubview:lblText];
    
    UIImageView * star = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"puzzles_badge_star"]];
    star.frame = CGRectMake(lblText.frame.origin.x + lblText.frame.size.width, lblText.frame.origin.y + star.frame.size.height / 4, star.frame.size.width, star.frame.size.height);
    [self addSubview:star];
    
    NSString * scoreString = [NSString stringWithFormat:@" %@", [self stringWithScore:score]];
    CGSize scoreSize = [scoreString sizeWithFont:helveticaBold12];
    _lblScore = [[UILabel alloc] initWithFrame:CGRectMake(star.frame.origin.x + star.frame.size.width, _lblCount.frame.origin.y, scoreSize.width, scoreSize.height)];
    _lblScore.font = helveticaBold12;
    _lblScore.text = scoreString;
    _lblScore.textColor = [UIColor colorWithRed:112/255.f green:99/255.f blue:88/255.f alpha:1];
    _lblScore.shadowColor = [UIColor whiteColor];
    _lblScore.backgroundColor = [UIColor clearColor];
    _lblScore.shadowOffset = CGSizeMake(0, 1);
    _lblScore.textAlignment = NSTextAlignmentLeft;
    [self addSubview:_lblScore];
    
    UIImage * imgBuy = [UIImage imageNamed:@"puzzles_buy_btn"];
    UIImage * imgBuyDown = [UIImage imageNamed:@"puzzles_buy_btn_down"];
    _btnBuy = [[UIButton alloc] initWithFrame:CGRectMake(13, 70, imgBuy.size.width, imgBuy.size.height)];
    [_btnBuy setBackgroundImage:imgBuy forState:UIControlStateNormal];
    [_btnBuy setBackgroundImage:imgBuyDown forState:UIControlStateHighlighted];
    _btnBuy.titleLabel.font = [UIFont fontWithName:@"DINPro-Bold" size:15];
    _btnBuy.titleLabel.shadowOffset = CGSizeMake(0, -1);
    [_btnBuy setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [_btnBuy setTitleShadowColor:[UIColor colorWithRed:0 green:51/255.f blue:13/255.f alpha:1] forState:UIControlStateNormal];
    if (type == PUZZLESET_FREE)
    {
        [_btnBuy setTitle:@"Скачать" forState:UIControlStateNormal];
    }
    else
    {
        [_btnBuy setTitle:[NSString stringWithFormat:@"%0.02f$", price] forState:UIControlStateNormal];
    }
    [self addSubview:_btnBuy];
}

-(void)initBoughtElementsWithType:(PuzzleSetType)type puzzlesCount:(int)count puzzlesSolved:(int)solved score:(int)score ids:(NSArray *)ids percents:(NSArray *)percents
{
    UIFont * helveticaBold12 = [UIFont fontWithName:@"HelveticaNeue-Bold" size:12];
    UIFont * helvetica12 = [UIFont fontWithName:@"HelveticaNeue" size:12];
    
    NSString * text = @"Разгадано ";
    CGSize textSize = [text sizeWithFont:helvetica12];
    UILabel * lblText = [[UILabel alloc] initWithFrame:CGRectMake(16, 50, textSize.width, textSize.height)];
    lblText.font = helvetica12;
    lblText.text = text;
    lblText.textColor = [UIColor colorWithRed:112/255.f green:99/255.f blue:88/255.f alpha:1];
    lblText.shadowColor = [UIColor whiteColor];
    lblText.backgroundColor = [UIColor clearColor];
    lblText.shadowOffset = CGSizeMake(0, 1);
    lblText.textAlignment = NSTextAlignmentLeft;
    [self addSubview:lblText];
    
    NSString * countString = [NSString stringWithFormat:@"%d/%d ", solved, count];
    CGSize countSize = [countString sizeWithFont:helveticaBold12];
    _lblCount = [[UILabel alloc] initWithFrame:CGRectMake(lblText.frame.origin.x + lblText.frame.size.width, lblText.frame.origin.y, countSize.width, countSize.height)];
    _lblCount.font = helveticaBold12;
    _lblCount.text = countString;
    _lblCount.textColor = [UIColor colorWithRed:112/255.f green:99/255.f blue:88/255.f alpha:1];
    _lblCount.backgroundColor = [UIColor clearColor];
    _lblCount.shadowColor = [UIColor whiteColor];
    _lblCount.shadowOffset = CGSizeMake(0, 1);
    _lblCount.textAlignment = NSTextAlignmentLeft;
    [self addSubview:_lblCount];
    
    NSString * text2 = @"Набрано ";
    CGSize text2Size = [text2 sizeWithFont:helvetica12];
    UILabel * lblText2 = [[UILabel alloc] initWithFrame:CGRectMake(16, 64, text2Size.width, text2Size.height)];
    lblText2.font = helvetica12;
    lblText2.text = text2;
    lblText2.textColor = [UIColor colorWithRed:112/255.f green:99/255.f blue:88/255.f alpha:1];
    lblText2.shadowColor = [UIColor whiteColor];
    lblText2.backgroundColor = [UIColor clearColor];
    lblText2.shadowOffset = CGSizeMake(0, 1);
    lblText2.textAlignment = NSTextAlignmentLeft;
    [self addSubview:lblText2];
    
    UIImageView * star = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"puzzles_badge_star"]];
    star.frame = CGRectMake(lblText2.frame.origin.x + lblText2.frame.size.width, lblText2.frame.origin.y + star.frame.size.height / 4, star.frame.size.width, star.frame.size.height);
    [self addSubview:star];
    
    NSString * scoreString = [NSString stringWithFormat:@" %@", [self stringWithScore:score]];
    CGSize scoreSize = [scoreString sizeWithFont:helveticaBold12];
    _lblScore = [[UILabel alloc] initWithFrame:CGRectMake(star.frame.origin.x + star.frame.size.width, lblText2.frame.origin.y, scoreSize.width, scoreSize.height)];
    _lblScore.font = helveticaBold12;
    _lblScore.text = scoreString;
    _lblScore.textColor = [UIColor colorWithRed:112/255.f green:99/255.f blue:88/255.f alpha:1];
    _lblScore.shadowColor = [UIColor whiteColor];
    _lblScore.backgroundColor = [UIColor clearColor];
    _lblScore.shadowOffset = CGSizeMake(0, 1);
    _lblScore.textAlignment = NSTextAlignmentLeft;
    [self addSubview:_lblScore];
    
    UIImageView * progressbarView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"puzzles_set_progressbar_bg"]];
    progressbarView.frame = CGRectMake(_lblCount.frame.origin.x + _lblCount.frame.size.width, _lblCount.frame.origin.y + 4, progressbarView.frame.size.width, progressbarView.frame.size.height);
    [self addSubview:progressbarView];
    
    UIImage * imgProgress = [UIImage imageNamed:@"puzzles_set_progressbar"];
    CGSize imageSize = imgProgress.size;
    if ([imgProgress respondsToSelector:@selector(resizableImageWithCapInsets:)])
    {
        imgProgress = [imgProgress resizableImageWithCapInsets:UIEdgeInsetsMake(imageSize.height / 2 - 1, imageSize.width / 2 - 1, imageSize.height / 2, imageSize.width / 2)];
    }
    else
    {
        imgProgress = [imgProgress stretchableImageWithLeftCapWidth:(imageSize.width / 2) topCapHeight:(imageSize.height / 2)];
    }
    UIImageView * progressbar = [[UIImageView alloc] initWithFrame:CGRectMake(1, 1, (progressbarView.frame.size.width - 2) * solved / count, imageSize.height)];
    progressbar.image = imgProgress;
    [progressbarView addSubview:progressbar];
    
    int badgesCount = ids.count;
    _badges = [NSMutableArray arrayWithCapacity:badgesCount];
    for (int badgeIdx = 0; badgeIdx != badgesCount; ++badgeIdx)
    {
        BadgeView * badgeView = [BadgeView badgeWithType:(type == PUZZLESET_SILVER2 ? BADGE_SILVER : (BadgeType)type) andNumber:[(NSNumber *)[ids objectAtIndex:badgeIdx] intValue] andPercent:[(NSNumber *)[percents objectAtIndex:badgeIdx] floatValue]];
        badgeView.frame = CGRectMake(16 + (badgeIdx % 4) * 70, 95 + (badgeIdx / 4) * 105, badgeView.frame.size.width, badgeView.frame.size.height);
        badgeView.tag = [(NSNumber *)[ids objectAtIndex:badgeIdx] intValue] - 1;
        [self addSubview:badgeView];
        [_badges addObject:badgeView];
    }
}

+(PuzzleSetView *)puzzleSetViewWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price
{
    return [[PuzzleSetView alloc] initWithType:type puzzlesCount:count minScore:score price:price];
}

+(PuzzleSetView *)puzzleSetViewWithType:(PuzzleSetType)type puzzlesCount:(int)count puzzlesSolved:(int)solved score:(int)score ids:(NSArray *)ids percents:(NSArray *)percents
{
    return [[PuzzleSetView alloc] initWithType:type puzzlesCount:count puzzlesSolved:solved score:score ids:ids percents:percents];
}

-(void)switchToBought
{
    while (self.subviews.count > 0)
    {
        [(UIView *)[self.subviews lastObject] removeFromSuperview];
    }
    _btnBuy = nil;
    [self initHeaderWithType:setType];
    NSMutableArray * ids = [NSMutableArray arrayWithCapacity:puzzlesCount];
    NSMutableArray * percents = [NSMutableArray arrayWithCapacity:puzzlesCount];
    for (int i = 0; i != puzzlesCount; ++i)
    {
        [ids addObject:[NSNumber numberWithInt:(i + 1)]];
        [percents addObject:[NSNumber numberWithFloat:0]];
    }
    [self initBoughtElementsWithType:setType puzzlesCount:puzzlesCount puzzlesSolved:0 score:0 ids:ids percents:percents];

    if (_badges.count > 0)
    {
        BadgeView * lastBadge = [_badges lastObject];
        self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, self.frame.size.width, lastBadge.frame.origin.y + 115);
    }
    else
    {
        self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, 303, 105);
    }
}

-(NSString *)stringWithScore:(int)score
{
    if (score < 1000)
    {
        return [NSString stringWithFormat:@"%d", score];
    }
    if (score < 1000000)
    {
        return [NSString stringWithFormat:@"%d %03d", score/1000, score % 1000];
    }
    if (score < 1000000000)
    {
        return [NSString stringWithFormat:@"%d %03d %03d", score/1000000, (score % 1000000) / 1000, score % 1000];
    }
    return [NSString stringWithFormat:@"%d %03d %03d %03d", score/1000000000, (score % 1000000000) / 1000000, (score % 1000000) / 1000, score % 1000];
}


@end
