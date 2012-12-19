//
//  PuzzleSetView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/19/12.
//
//

#import "PuzzleSetView.h"

@interface PuzzleSetView (private)

-(NSString *)stringWithScore:(int)score;

@end

@implementation PuzzleSetView

@synthesize imgBar = _imgBar;
@synthesize lblCaption = _lblCaption;
@synthesize lblCount = _lblCount;
@synthesize lblScore = _lblScore;
@synthesize btnBuy = _btnBuy;

-(id)initWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price
{
    self = [super initWithFrame:CGRectMake(0, 0, 303, 130)];
    if (self)
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
        
        self.autoresizesSubviews = NO;
        self.clipsToBounds = NO;
    }
    return self;
}

+(PuzzleSetView *)puzzleSetViewWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price
{
    return [[PuzzleSetView alloc] initWithType:type puzzlesCount:count minScore:score price:price];
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
