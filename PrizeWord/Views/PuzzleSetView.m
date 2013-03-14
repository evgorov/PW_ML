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
#import "PuzzleSetData.h"
#import "NSString+Utils.h"

NSString * MONTHS[] = {@"январь", @"февраль", @"март", @"апрель", @"май", @"июнь", @"июль", @"август", @"сентябрь", @"октябрь", @"ноябрь", @"декабрь"};
float PRICES[] = {3.99f, 2.99f, 1.99f, 0, 1.99f};

@interface PuzzleSetView (private)

-(id)initWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price;
-(id)initWithData:(PuzzleSetData *)puzzleSetData month:(int)month showSolved:(BOOL)showSolved showUnsolved:(BOOL)showUnsolved;
-(id)initCompleteWithData:(PuzzleSetData *)puzzleSetData;


-(void)initHeaderWithType:(PuzzleSetType)type month:(int)month;
-(void)initBuySubtitlesWithData:(PuzzleSetData *)puzzleSetData;
-(void)initBougtSubtitlesWithData:(PuzzleSetData *)puzzleSetData;
-(void)initSolvedSubtitlesWithData:(PuzzleSetData *)puzzleSetData;
-(void)initElementsWithData:(PuzzleSetData *)puzzleSetData showSolved:(BOOL)showSolved showUnsolved:(BOOL)showUnsolved;
-(void)initProgressWithPuzzlesCount:(int)count puzzlesSolved:(int)solved;

@end

@implementation PuzzleSetView

@synthesize imgBar = _imgBar;
@synthesize imgStar = _imgStar;
@synthesize imgScoreBg = _imgScoreBg;
@synthesize lblCaption = _lblCaption;
@synthesize lblCount = _lblCount;
@synthesize lblScore = _lblScore;
@synthesize lblPercent = _lblPercent;
@synthesize lblText1 = _lblText1;
@synthesize lblText2 = _lblText2;
@synthesize btnBuy = _btnBuy;
@synthesize btnShowMore = _btnShowMore;
@synthesize badges = _badges;
@synthesize product = _product;

@synthesize puzzleSetData = _puzzleSetData;
@synthesize shortSize = _shortSize;
@synthesize fullSize = _fullSize;

-(id)initWithData:(PuzzleSetData *)puzzleSetData month:(int)month showSolved:(BOOL)showSolved showUnsolved:(BOOL)showUnsolved
{
    if (self)
    {
        _puzzleSetData = puzzleSetData;
        
        self.autoresizesSubviews = NO;
        self.clipsToBounds = YES;
        
        [self initHeaderWithType:puzzleSetData.type.intValue month:month];
        if (puzzleSetData.bought.boolValue)
        {
            [self initBougtSubtitlesWithData:puzzleSetData];
            [self initElementsWithData:puzzleSetData showSolved:showSolved showUnsolved:showUnsolved];
        }
        else
        {
            [self initBuySubtitlesWithData:puzzleSetData];
            _badges = nil;
        }
        
        if (_badges != nil && _badges.count > 0)
        {
            BadgeView * lastBadge = [_badges lastObject];
            self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, self.frame.size.width, lastBadge.frame.origin.y + lastBadge.frame.size.height * 1.2);
            _fullSize = self.frame.size;
        }
    }
    return self;
}

-(id)initCompleteWithData:(PuzzleSetData *)puzzleSetData
{
    if (self)
    {
        _puzzleSetData = puzzleSetData;
        
        self.autoresizesSubviews = NO;
        self.clipsToBounds = YES;
        
        [self initHeaderWithType:puzzleSetData.type.intValue month:0];
        [self initSolvedSubtitlesWithData:puzzleSetData];
        [self initElementsWithData:puzzleSetData showSolved:YES showUnsolved:NO];
        
        if (_badges.count > 0)
        {
            BadgeView * lastBadge = [_badges lastObject];
            self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, self.frame.size.width, lastBadge.frame.origin.y + lastBadge.frame.size.height * 1.2);
            _fullSize = self.frame.size;
        }
    }
    return self;
}

-(void)initHeaderWithType:(PuzzleSetType)type month:(int)month
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
    
    if (month == 0)
    {
        imgMonthBg.hidden = YES;
        lblMonth.hidden = YES;
        imgDelimeter.frame = CGRectMake(0, imgDelimeter.frame.origin.y, self.frame.size.width, imgDelimeter.frame.size.height);
    }
    else
    {
        imgMonthBg.hidden = NO;
        lblMonth.hidden = NO;
        NSString * monthText = MONTHS[month - 1];
        NSString * defaultMonthText = MONTHS[4];
        UIImage * image = [UIImage imageNamed:@"puzzles_set_month_bg"];
        CGSize imageSize = image.size;
        if ([image respondsToSelector:@selector(resizableImageWithCapInsets:)])
        {
            image = [image resizableImageWithCapInsets:UIEdgeInsetsMake(imageSize.height / 2 - 1, imageSize.width / 2 - 1, imageSize.height / 2, imageSize.width / 2)];
        }
        else
        {
            image = [image stretchableImageWithLeftCapWidth:(imageSize.width / 2) topCapHeight:(imageSize.height / 2)];
        }
        imgMonthBg.image = image;
        imgMonthBg.frame = CGRectMake(imgMonthBg.frame.origin.x, imgMonthBg.frame.origin.y, image.size.width + [monthText sizeWithFont:lblMonth.font].width - [defaultMonthText sizeWithFont:lblMonth.font].width, imgMonthBg.frame.size.height);
        lblMonth.text = monthText;
        imgDelimeter.frame = CGRectMake(imgMonthBg.frame.origin.x + imgMonthBg.frame.size.width, imgDelimeter.frame.origin.y, self.frame.size.width - (imgMonthBg.frame.origin.x + imgMonthBg.frame.size.width), imgDelimeter.frame.size.height);
    }
}

-(void)initBuySubtitlesWithData:(PuzzleSetData *)puzzleSetData
{
    int count = puzzleSetData.puzzles.count;
    int minScore = puzzleSetData.minScore;
    NSString * countString = [NSString stringWithFormat:@"%d", count];
    CGSize countSize = [countString sizeWithFont:_lblCount.font];
    _lblCount.frame = CGRectMake(_lblCount.frame.origin.x, _lblCount.frame.origin.y, countSize.width, countSize.height);
    _lblCount.text = countString;
    
    NSString * text = [NSString stringWithFormat:((puzzleSetData.type.intValue == PUZZLESET_FREE) ? @" %@ " : @" %@, минимум "), [NSString declesion:count oneString:@"сканворд" twoString:@"сканворда" fiveString:@"сканвордов"]];
    CGSize textSize = [text sizeWithFont:_lblText1.font];
    _lblText1.frame = CGRectMake(_lblCount.frame.origin.x + _lblCount.frame.size.width, _lblCount.frame.origin.y, textSize.width, _lblCount.frame.size.height);
    _lblText1.text = text;
    
    
    _imgStar.frame = CGRectMake(_lblText1.frame.origin.x + _lblText1.frame.size.width, _lblText1.frame.origin.y + _imgStar.frame.size.height / 4, _imgStar.frame.size.width, _imgStar.frame.size.height);
    
    NSString * scoreString = [NSString stringWithFormat:@" %@", [NSString digitString:minScore]];
    _lblScore.frame = CGRectMake(_imgStar.frame.origin.x + _imgStar.frame.size.width, _lblCount.frame.origin.y, 200, _lblScore.frame.size.height);
    _lblScore.text = scoreString;
    
    _btnBuy.titleLabel.font = [UIFont fontWithName:@"DINPro-Bold" size:([AppDelegate currentDelegate].isIPad ? 17 : 15)];
    if (puzzleSetData.type.intValue == PUZZLESET_FREE)
    {
        [_btnBuy setTitle:@"Скачать" forState:UIControlStateNormal];
    }
    else
    {
        [_btnBuy setTitle:[NSString stringWithFormat:@"%0.02f$", PRICES[puzzleSetData.type.intValue]] forState:UIControlStateNormal];
    }
    _shortSize = CGSizeMake(self.frame.size.width, self.frame.size.height - _btnBuy.frame.size.height);
    _fullSize = self.frame.size;
}

-(void)initBougtSubtitlesWithData:(PuzzleSetData *)puzzleSetData
{
    _btnBuy.hidden = YES;
    _lblText2.hidden = NO;
    _btnShowMore.hidden = NO;
    _lblPercent.hidden = NO;
    
    [self initProgressWithPuzzlesCount:puzzleSetData.total puzzlesSolved:puzzleSetData.solved];
    
    NSString * text2 = @"Набрано ";
    CGSize text2Size = [text2 sizeWithFont:_lblText2.font];
    _lblText2.frame = CGRectMake(_lblText2.frame.origin.x, _lblText2.frame.origin.y, text2Size.width, text2Size.height);
    _lblText2.text = text2;
    
    _imgStar.frame = CGRectMake(_lblText2.frame.origin.x + _lblText2.frame.size.width, _lblText2.frame.origin.y + _imgStar.frame.size.height / 4, _imgStar.frame.size.width, _imgStar.frame.size.height);
    
    NSString * scoreString = [NSString stringWithFormat:@" %@", [NSString digitString:puzzleSetData.score]];
    CGSize scoreSize = [scoreString sizeWithFont:_lblScore.font];
    _lblScore.frame = CGRectMake(_imgStar.frame.origin.x + _imgStar.frame.size.width, _lblText2.frame.origin.y, scoreSize.width, scoreSize.height);
    _lblScore.text = scoreString;
}

-(void)initSolvedSubtitlesWithData:(PuzzleSetData *)puzzleSetData
{
    _btnBuy.hidden = YES;
    _lblText2.hidden = YES;
    _btnShowMore.hidden = YES;
    _lblPercent.hidden = NO;
    _imgScoreBg.hidden = NO;
    
    [self initProgressWithPuzzlesCount:puzzleSetData.total puzzlesSolved:puzzleSetData.solved];
    
    _imgStar.frame = CGRectMake(_imgScoreBg.frame.origin.x + _imgStar.frame.size.width / 2, _imgStar.frame.origin.y, _imgStar.frame.size.width, _imgStar.frame.size.height);
    
    NSString * scoreString = [NSString stringWithFormat:@" %@", [NSString digitString:puzzleSetData.score]];
    CGSize scoreSize = [scoreString sizeWithFont:_lblScore.font];
    _lblScore.frame = CGRectMake(_imgStar.frame.origin.x + _imgStar.frame.size.width, _lblScore.frame.origin.y, scoreSize.width, scoreSize.height);
    _lblScore.text = scoreString;
    _lblScore.textColor = [UIColor whiteColor];
    _lblScore.shadowColor = [UIColor blackColor];
}

-(void)initElementsWithData:(PuzzleSetData *)puzzleSetData showSolved:(BOOL)showSolved showUnsolved:(BOOL)showUnsolved
{
    int badgesPerRow = [AppDelegate currentDelegate].isIPad ? 5 : 4;
    _badges = [NSMutableArray arrayWithCapacity:puzzleSetData.total];
    int badgeIdx = 0;
    NSArray * orderedPuzzles = puzzleSetData.orderedPuzzles;
    for (PuzzleData * puzzleData in orderedPuzzles)
    {
        ++badgeIdx;
        if (puzzleData.progress == 1 && !showSolved)
        {
            continue;
        }
        if (puzzleData.progress != 1 && !showUnsolved)
        {
            continue;
        }
        BadgeView * badgeView;
        badgeView = [BadgeView badgeForPuzzle:puzzleData andNumber:badgeIdx];
        badgeView.frame = CGRectMake(_lblText1.frame.origin.x + (_badges.count % badgesPerRow) * badgeView.frame.size.width * 1.2, _btnBuy.frame.origin.y + _btnBuy.frame.size.height / 2 + (_badges.count / badgesPerRow) * badgeView.frame.size.height * 1.2, badgeView.frame.size.width, badgeView.frame.size.height);
        badgeView.tag = badgeIdx - 1;
        [self addSubview:badgeView];
        [_badges addObject:badgeView];
    }
    _shortSize = CGSizeMake(self.frame.size.width, _lblText1.frame.origin.y + _lblText1.frame.size.height * 3);
    if (_badges.count == 0)
    {
        _fullSize = _shortSize;
        _btnShowMore.hidden = YES;
    }
    else
    {
        _fullSize = self.frame.size;
    }
}

-(void)initProgressWithPuzzlesCount:(int)count puzzlesSolved:(int)solved
{
    NSString * text = @"Разгадано ";
    CGSize textSize = [text sizeWithFont:_lblText1.font];
    _lblText1.frame = CGRectMake(_lblText2.frame.origin.x, _lblText1.frame.origin.y, textSize.width, textSize.height);
    _lblText1.text = text;
    
    NSString * countString = [NSString stringWithFormat:@"%d/%d ", solved, count];
    CGSize countSize = [countString sizeWithFont:_lblCount.font];
    _lblCount.frame = CGRectMake(_lblText1.frame.origin.x + _lblText1.frame.size.width, _lblText1.frame.origin.y, countSize.width, countSize.height);
    _lblCount.text = countString;
    
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
        imgProgress = [imgProgress stretchableImageWithLeftCapWidth:(imageSize.width / 2 - 1) topCapHeight:(imageSize.height / 2 - 1)];
    }
    float progress = 1;
    if (count != 0)
    {
        progress = (float)solved / count;
    }
    UIImageView * progressbar = [[UIImageView alloc] initWithFrame:CGRectMake(1, 0, (progressbarView.frame.size.width - 2) * progress, imageSize.height)];
    progressbar.image = imgProgress;
    [progressbarView addSubview:progressbar];
    
    NSString * percentString = [NSString stringWithFormat:@" %d%%", (int)(100 * progress)];
    CGSize percentSize = [percentString sizeWithFont:_lblPercent.font];
    _lblPercent.frame = CGRectMake(progressbarView.frame.origin.x + progressbarView.frame.size.width, _lblPercent.frame.origin.y, percentSize.width, percentSize.height);
    _lblPercent.text = percentString;
}

+(PuzzleSetView *)puzzleSetViewWithData:(PuzzleSetData *)puzzleSetData month:(int)month showSolved:(BOOL)showSolved showUnsolved:(BOOL)showUnsolved
{
    PuzzleSetView * setView = (PuzzleSetView *)[[[NSBundle mainBundle] loadNibNamed:@"PuzzleSetView" owner:self options:nil] objectAtIndex:0];
    return [setView initWithData:puzzleSetData month:month showSolved:showSolved showUnsolved:showUnsolved];
}

+(PuzzleSetView *)puzzleSetCompleteViewWithData:(PuzzleSetData *)puzzleSetData
{
    PuzzleSetView * setView = (PuzzleSetView *)[[[NSBundle mainBundle] loadNibNamed:@"PuzzleSetView" owner:self options:nil] objectAtIndex:0];
    return [setView initCompleteWithData:puzzleSetData];
}


-(void)switchToBought
{
    [self initHeaderWithType:_puzzleSetData.type.intValue month:0];
    [self initBougtSubtitlesWithData:_puzzleSetData];
    [self initElementsWithData:_puzzleSetData showSolved:NO showUnsolved:YES];

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

@end
