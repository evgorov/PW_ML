//
//  PuzzleSetView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/19/12.
//
//

#import "PuzzleSetView.h"
#import "BadgeView.h"
#import "AppDelegate.h"

@interface PuzzleSetView (private)

-(id)initWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price;

-(id)initWithType:(PuzzleSetType)type puzzlesCount:(int)count puzzlesSolved:(int)solved score:(int)score ids:(NSArray *)ids percents:(NSArray *)percents;

-(void)initHeaderWithType:(PuzzleSetType)type;
-(void)initElementsWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price;
-(void)initBoughtElementsWithType:(PuzzleSetType)type puzzlesCount:(int)count puzzlesSolved:(int)solved score:(int)score ids:(NSArray *)ids percents:(NSArray *)percents;
-(NSString *)stringWithScore:(int)score;

@end

@implementation PuzzleSetView

@synthesize imgBar = _imgBar;
@synthesize imgStar = _imgStar;
@synthesize lblCaption = _lblCaption;
@synthesize lblCount = _lblCount;
@synthesize lblScore = _lblScore;
@synthesize lblText1 = _lblText1;
@synthesize lblText2 = _lblText2;
@synthesize btnBuy = _btnBuy;
@synthesize btnShowMore = _btnShowMore;
@synthesize badges = _badges;

@synthesize shortSize = _shortSize;
@synthesize fullSize = _fullSize;

-(id)initWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price
{
    if (self)
    {
        puzzlesCount = count;
        setType = type;
        
        self.autoresizesSubviews = NO;
        self.clipsToBounds = YES;
        
        [self initHeaderWithType:type];
        [self initElementsWithType:type puzzlesCount:count minScore:score price:price];
        _badges = nil;
    }
    return self;
}

-(id)initWithType:(PuzzleSetType)type puzzlesCount:(int)count puzzlesSolved:(int)solved score:(int)score ids:(NSArray *)ids percents:(NSArray *)percents
{
    if (self)
    {
        puzzlesCount = count;
        setType = type;

        self.autoresizesSubviews = NO;
        self.clipsToBounds = YES;

        [self initHeaderWithType:type];
        [self initBoughtElementsWithType:type puzzlesCount:count puzzlesSolved:solved score:score ids:ids percents:percents];
        
        if (_badges.count > 0)
        {
            BadgeView * lastBadge = [_badges lastObject];
            self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, self.frame.size.width, lastBadge.frame.origin.y + lastBadge.frame.size.height * 1.2);
            _fullSize = self.frame.size;
        }
    }
    return self;
}

-(void)initHeaderWithType:(PuzzleSetType)type
{
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
        _imgBar.image = [UIImage imageNamed:barFilename];
    }
    _lblCaption.font = [UIFont fontWithName:@"DINPro-Bold" size:([AppDelegate currentDelegate].isIPad ? 30 : 25)];
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
}

-(void)initElementsWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price
{
    NSString * countString = [NSString stringWithFormat:@"%d", count];
    CGSize countSize = [countString sizeWithFont:_lblCount.font];
    _lblCount.frame = CGRectMake(_lblCount.frame.origin.x, _lblCount.frame.origin.y, countSize.width, countSize.height);
    _lblCount.text = countString;
    
    NSString * text = (type == PUZZLESET_FREE) ? @" сканвордов " : @" сканвордов, минимум ";
    CGSize textSize = [text sizeWithFont:_lblText1.font];
    _lblText1.frame = CGRectMake(_lblCount.frame.origin.x + _lblCount.frame.size.width, _lblCount.frame.origin.y, textSize.width, _lblCount.frame.size.height);
    _lblText1.text = text;
    
    
    _imgStar.frame = CGRectMake(_lblText1.frame.origin.x + _lblText1.frame.size.width, _lblText1.frame.origin.y + _imgStar.frame.size.height / 4, _imgStar.frame.size.width, _imgStar.frame.size.height);
    
    NSString * scoreString = [NSString stringWithFormat:@" %@", [self stringWithScore:score]];
    CGSize scoreSize = [scoreString sizeWithFont:_lblScore.font];
    _lblScore.frame = CGRectMake(_imgStar.frame.origin.x + _imgStar.frame.size.width, _lblCount.frame.origin.y, scoreSize.width, scoreSize.height);
    _lblScore.text = scoreString;
    
    _btnBuy.titleLabel.font = [UIFont fontWithName:@"DINPro-Bold" size:([AppDelegate currentDelegate].isIPad ? 17 : 15)];
    if (type == PUZZLESET_FREE)
    {
        [_btnBuy setTitle:@"Скачать" forState:UIControlStateNormal];
    }
    else
    {
        [_btnBuy setTitle:[NSString stringWithFormat:@"%0.02f$", price] forState:UIControlStateNormal];
    }
    _shortSize = CGSizeMake(self.frame.size.width, self.frame.size.height - _btnBuy.frame.size.height);
    _fullSize = self.frame.size;
}

-(void)initBoughtElementsWithType:(PuzzleSetType)type puzzlesCount:(int)count puzzlesSolved:(int)solved score:(int)score ids:(NSArray *)ids percents:(NSArray *)percents
{
    _btnBuy.hidden = YES;
    _lblText2.hidden = NO;
    _btnShowMore.hidden = NO;
    
    NSString * text = @"Разгадано ";
    CGSize textSize = [text sizeWithFont:_lblText1.font];
    _lblText1.frame = CGRectMake(_lblText2.frame.origin.x, _lblText1.frame.origin.y, textSize.width, textSize.height);
    _lblText1.text = text;
    
    NSString * countString = [NSString stringWithFormat:@"%d/%d ", solved, count];
    CGSize countSize = [countString sizeWithFont:_lblCount.font];
    _lblCount.frame = CGRectMake(_lblText1.frame.origin.x + _lblText1.frame.size.width, _lblText1.frame.origin.y, countSize.width, countSize.height);
    _lblCount.text = countString;
    
    NSString * text2 = @"Набрано ";
    CGSize text2Size = [text2 sizeWithFont:_lblText2.font];
    _lblText2.frame = CGRectMake(_lblText2.frame.origin.x, _lblText2.frame.origin.y, text2Size.width, text2Size.height);
    _lblText2.text = text2;
    
    _imgStar.frame = CGRectMake(_lblText2.frame.origin.x + _lblText2.frame.size.width, _lblText2.frame.origin.y + _imgStar.frame.size.height / 4, _imgStar.frame.size.width, _imgStar.frame.size.height);
    
    NSString * scoreString = [NSString stringWithFormat:@" %@", [self stringWithScore:score]];
    CGSize scoreSize = [scoreString sizeWithFont:_lblScore.font];
    _lblScore.frame = CGRectMake(_imgStar.frame.origin.x + _imgStar.frame.size.width, _lblText2.frame.origin.y, scoreSize.width, scoreSize.height);
    _lblScore.text = scoreString;
    
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
    int badgesPerRow = [AppDelegate currentDelegate].isIPad ? 5 : 4;
    _badges = [NSMutableArray arrayWithCapacity:badgesCount];
    for (int badgeIdx = 0; badgeIdx != badgesCount; ++badgeIdx)
    {
        BadgeView * badgeView = [BadgeView badgeWithType:(type == PUZZLESET_SILVER2 ? BADGE_SILVER : (BadgeType)type) andNumber:[(NSNumber *)[ids objectAtIndex:badgeIdx] intValue] andPercent:[(NSNumber *)[percents objectAtIndex:badgeIdx] floatValue]];
        badgeView.frame = CGRectMake(_lblText1.frame.origin.x + (badgeIdx % badgesPerRow) * badgeView.frame.size.width * 1.2, _btnBuy.frame.origin.y + _btnBuy.frame.size.height / 2 + (badgeIdx / badgesPerRow) * badgeView.frame.size.height * 1.2, badgeView.frame.size.width, badgeView.frame.size.height);
        badgeView.tag = [(NSNumber *)[ids objectAtIndex:badgeIdx] intValue] - 1;
        [self addSubview:badgeView];
        [_badges addObject:badgeView];
    }
    _shortSize = CGSizeMake(self.frame.size.width, self.frame.size.height - _btnBuy.frame.size.height);
    _fullSize = self.frame.size;
}

+(PuzzleSetView *)puzzleSetViewWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price
{
    PuzzleSetView * setView = (PuzzleSetView *)[[[NSBundle mainBundle] loadNibNamed:@"PuzzleSetView" owner:self options:nil] objectAtIndex:0];
    return [setView initWithType:type puzzlesCount:count minScore:score price:price];
}

+(PuzzleSetView *)puzzleSetViewWithType:(PuzzleSetType)type puzzlesCount:(int)count puzzlesSolved:(int)solved score:(int)score ids:(NSArray *)ids percents:(NSArray *)percents
{
    PuzzleSetView * setView = (PuzzleSetView *)[[[NSBundle mainBundle] loadNibNamed:@"PuzzleSetView" owner:self options:nil] objectAtIndex:0];
    return [setView initWithType:type puzzlesCount:count puzzlesSolved:solved score:score ids:ids percents:percents];
}

-(void)switchToBought
{
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
        self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, self.frame.size.width, lastBadge.frame.origin.y + lastBadge.frame.size.height * 1.2);
        _fullSize = self.frame.size;
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
