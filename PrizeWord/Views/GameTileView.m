//
//  GameTileView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/25/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "GameTileView.h"
#import "EventManager.h"
#import "TileImageHelper.h"
#import "QuestionData.h"
#import "GameLogic.h"
#import "GameField.h"
#import "AppDelegate.h"

#import <QuartzCore/CALayer.h>

@interface GameTileView ()

-(void)onTap;
-(void)initParts;
-(void)showArrow;
-(void)hideArrow;
-(void)updateSourceForArrow;
-(void)animateToCorrect;

@end

@implementation GameTileView

@synthesize arrowTileX;
@synthesize arrowTileY;

- (id)initWithFrame:(CGRect)frame andData:(TileData *)data
{
    self = [super initWithFrame:frame];
    if (self) {
        background = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"tile_letter_empty"]];
        background.contentMode = UIViewContentModeScaleToFill;
        [self addSubview:background];
        questionLabel = [[UILabel alloc] initWithFrame:CGRectMake(frame.size.width * 0.1, 0, frame.size.width * 0.8, frame.size.height)];
        questionLabel.font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:([AppDelegate currentDelegate].isIPad ? 10 : 8)];
        questionLabel.adjustsFontSizeToFitWidth = NO;
        questionLabel.backgroundColor = [UIColor clearColor];
        questionLabel.lineBreakMode = NSLineBreakByWordWrapping;
        questionLabel.numberOfLines = 5;
        questionLabel.textAlignment = NSTextAlignmentCenter;
        questionLabel.layer.shadowColor = [UIColor whiteColor].CGColor;
        questionLabel.layer.shadowOffset = CGSizeMake(0, 1);
        questionLabel.layer.shadowRadius = 0.5;
        questionLabel.layer.shadowOpacity = 1;
        questionLabel.textColor = [UIColor colorWithRed:0.235 green:0.243 blue:0.271 alpha:1];
        questionLabel.hidden = YES;
        [self addSubview:questionLabel];
        
        arrow = nil;
        overlay = nil;
        self.clipsToBounds = YES;

        oldState = TILE_UNUSED;
        tileData = data;

        arrowTileX = -1;
        arrowTileY = -1;
        arrowDone = NO;
        if (tileData.state == TILE_QUESTION_CORRECT || tileData.state == TILE_QUESTION_INPUT || tileData.state == TILE_QUESTION_NEW || tileData.state == TILE_QUESTION_WRONG)
        {
            arrowTileX = tileData.x;
            arrowTileY = tileData.y;
            if ((tileData.answerPosition & kAnswerPositionNorth) != 0)
            {
                arrowTileY = tileData.y - 1;
            }
            if ((tileData.answerPosition & kAnswerPositionSouth) != 0)
            {
                arrowTileY = tileData.y + 1;
            }
            if ((tileData.answerPosition & kAnswerPositionWest) != 0)
            {
                arrowTileX = tileData.x - 1;
            }
            if ((tileData.answerPosition & kAnswerPositionEast) != 0)
            {
                arrowTileX = tileData.x + 1;
            }
        }

        [self initParts];
        
        self.userInteractionEnabled = YES;
        UITapGestureRecognizer * tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTap)];
        [self addGestureRecognizer:tapRecognizer];
    }
    return self;
}

-(void)dealloc
{
    tileData = nil;
    background = nil;
    questionLabel = nil;
    overlay = nil;
    arrow = nil;
}

-(void)handleEvent:(Event *)event
{
    switch (event.type) {
        case EVENT_TILE_CHANGE:
        {
            TileData * newData = (TileData *)event.data;
            if (tileData.x == newData.x && tileData.y == newData.y)
            {
                tileData = newData;
                [self initParts];
            }
            else if (arrowTileX == newData.x && arrowTileY == newData.y)
            {
                if (arrow != nil)
                {
                    if (!arrowDone && (newData.state == TILE_LETTER_CORRECT || newData.state == TILE_LETTER_CORRECT_INPUT || newData.state == TILE_LETTER_INPUT || newData.state == TILE_LETTER_WRONG))
                    {
                        arrowDone = YES;
                        [self updateSourceForArrow];
                    }
                }
            }
            break;
        }
            
        default:
            break;
    }
}

-(void)initParts
{
    if ((oldState == TILE_LETTER_INPUT || oldState == TILE_LETTER_EMPTY_INPUT) && tileData.state == TILE_LETTER_CORRECT)
    {
        [self animateToCorrect];
    }
    else
    {
        switch (tileData.state) {
            case TILE_QUESTION_NEW:
                [background setImage:[UIImage imageNamed:@"tile_question_new"]];
                break;
                
            case TILE_QUESTION_CORRECT:
                [background setImage:[[TileImageHelper sharedHelper] correctQuestionForType:tileData.letterType]];
                break;
                
            case TILE_QUESTION_WRONG:
                [background setImage:[UIImage imageNamed:@"tile_question_wrong"]];
                break;
                
            case TILE_QUESTION_INPUT:
                [background setImage:[UIImage imageNamed:@"tile_question_input"]];
                break;
                
            case TILE_LETTER_EMPTY:
                [background setImage:[UIImage imageNamed:@"tile_letter_empty"]];
                break;
                
            case TILE_LETTER_CORRECT_INPUT:
            case TILE_LETTER_CORRECT:
            {
                [background setImage:[[TileImageHelper sharedHelper] letterForType:tileData.letterType andIndex:tileData.currentLetterIdx]];
            }
                break;
                
            case TILE_LETTER_WRONG:
                [background setImage:[[TileImageHelper sharedHelper] letterForType:LETTER_WRONG andIndex:tileData.currentLetterIdx]];
                break;
                
            case TILE_LETTER_EMPTY_INPUT:
                [background setImage:[UIImage imageNamed:@"tile_letter_empty_input"]];
                break;
                
            case TILE_LETTER_INPUT:
            {
                [background setImage:[[TileImageHelper sharedHelper] letterForType:LETTER_INPUT andIndex:tileData.currentLetterIdx]];
            }
                break;
                
            default:
                break;
        }
    }
    oldState = tileData.state;
    if (tileData.state == TILE_QUESTION_NEW || tileData.state == TILE_QUESTION_CORRECT || tileData.state == TILE_QUESTION_WRONG || tileData.state == TILE_QUESTION_INPUT)
    {
        questionLabel.text = tileData.question;
        if (tileData.state == TILE_QUESTION_CORRECT)
        {
            questionLabel.textColor = [UIColor colorWithRed:0.667 green:0.678 blue:0.71 alpha:1];
        }
        else
        {
            questionLabel.textColor = [UIColor colorWithRed:0.235 green:0.243 blue:0.271 alpha:1];
        }
        questionLabel.hidden = NO;

        if (tileData.state == TILE_QUESTION_NEW)
        {
            [self showArrow];
        }
        else
        {
            [self hideArrow];
        }
    }
    else
    {
        if (tileData.state == TILE_LETTER_CORRECT_INPUT || tileData.state == TILE_LETTER_EMPTY_INPUT || tileData.state == TILE_LETTER_INPUT)
        {
            [self.superview bringSubviewToFront:self];
        }
        else
        {
            [self.superview sendSubviewToBack:self];
        }
        questionLabel.text = @"";
        questionLabel.hidden = YES;
        [self hideArrow];
    }
    
    if (tileData.state == TILE_LETTER_CORRECT_INPUT && overlay == nil)
    {
        overlay = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"tile_letter_correct_input_overlay"]];
        overlay.contentMode = UIViewContentModeScaleToFill;
        [self addSubview:overlay];
    }
    else if (tileData.state != TILE_LETTER_CORRECT_INPUT && overlay != nil)
    {
        [overlay removeFromSuperview];
        overlay = nil;
    }
}

-(void)showArrow
{
    [self.superview bringSubviewToFront:self];
    self.clipsToBounds = NO;
    if (arrow == nil)
    {
        arrow = [UIImageView new];
        [self addSubview: arrow];
    }
    
    [self updateSourceForArrow];
    
    int offsetX = 0;
    int offsetY = 0;
    if ((tileData.answerPosition & kAnswerPositionNorth) != 0)
    {
        offsetY = -self.frame.size.height;
    }
    if ((tileData.answerPosition & kAnswerPositionSouth) != 0)
    {
        offsetY = self.frame.size.height;
    }
    if ((tileData.answerPosition & kAnswerPositionWest) != 0)
    {
        offsetX = -self.frame.size.width;
    }
    if ((tileData.answerPosition & kAnswerPositionEast) != 0)
    {
        offsetX = self.frame.size.width;
    }
    arrow.frame = CGRectMake(offsetX, offsetY, self.frame.size.width, self.frame.size.height);
    
    float rotation = 0;
    float scaleX = 1;
    float scaleY = 1;
    
    switch (tileData.answerPosition)
    {
        case kAnswerPositionWest | kAnswerPositionLeft:
        case kAnswerPositionWest | kAnswerPositionBottom:
        case kAnswerPositionEast | kAnswerPositionBottom:
        case kAnswerPositionSouth | kAnswerPositionWest | kAnswerPositionTop:
        case kAnswerPositionSouth | kAnswerPositionEast | kAnswerPositionTop:
        case kAnswerPositionNorth | kAnswerPositionWest | kAnswerPositionTop:
        case kAnswerPositionSouth | kAnswerPositionWest | kAnswerPositionBottom:
            rotation = -M_PI_2;
            break;

        case kAnswerPositionEast | kAnswerPositionRight:
        case kAnswerPositionEast | kAnswerPositionTop:
        case kAnswerPositionWest | kAnswerPositionTop:
        case kAnswerPositionNorth | kAnswerPositionEast | kAnswerPositionBottom:
        case kAnswerPositionNorth | kAnswerPositionWest | kAnswerPositionBottom:
        case kAnswerPositionSouth | kAnswerPositionEast | kAnswerPositionBottom:
        case kAnswerPositionNorth | kAnswerPositionEast | kAnswerPositionTop:
            rotation = M_PI_2;
            break;
            
        case kAnswerPositionSouth | kAnswerPositionBottom:
        case kAnswerPositionSouth | kAnswerPositionRight:
        case kAnswerPositionSouth | kAnswerPositionLeft:
        case kAnswerPositionSouth | kAnswerPositionEast | kAnswerPositionLeft:
        case kAnswerPositionSouth | kAnswerPositionWest | kAnswerPositionRight:
        case kAnswerPositionSouth | kAnswerPositionEast | kAnswerPositionRight:
        case kAnswerPositionSouth | kAnswerPositionWest | kAnswerPositionLeft:
            rotation = M_PI;
            break;
        default:
            break;
    }
    
    switch (tileData.answerPosition)
    {
        case kAnswerPositionNorth | kAnswerPositionRight:
        case kAnswerPositionEast | kAnswerPositionBottom:
        case kAnswerPositionWest | kAnswerPositionTop:
        case kAnswerPositionSouth | kAnswerPositionLeft:
        case kAnswerPositionNorth | kAnswerPositionEast | kAnswerPositionLeft:
        case kAnswerPositionSouth | kAnswerPositionEast | kAnswerPositionTop:
        case kAnswerPositionNorth | kAnswerPositionWest | kAnswerPositionBottom:
        case kAnswerPositionSouth | kAnswerPositionWest | kAnswerPositionRight:
        case kAnswerPositionNorth | kAnswerPositionEast | kAnswerPositionTop:
        case kAnswerPositionNorth | kAnswerPositionWest | kAnswerPositionLeft:
        case kAnswerPositionSouth | kAnswerPositionEast | kAnswerPositionRight:
        case kAnswerPositionSouth | kAnswerPositionWest | kAnswerPositionBottom:
            
            scaleX = -1;
            break;
    }

    arrow.transform = CGAffineTransformConcat(CGAffineTransformMakeRotation(rotation), CGAffineTransformMakeScale(scaleX, scaleY));
}

-(void)hideArrow
{
    if (arrow != nil)
    {
        [arrow removeFromSuperview];
        arrow = nil;
    }
}

-(void)updateSourceForArrow
{
    if (arrow == nil)
    {
        return;
    }
    switch (tileData.answerPosition)
    {
        case kAnswerPositionNorth | kAnswerPositionTop:
        case kAnswerPositionSouth | kAnswerPositionBottom:
        case kAnswerPositionWest | kAnswerPositionLeft:
        case kAnswerPositionEast | kAnswerPositionRight:
            arrow.image = [UIImage imageNamed:arrowDone ? @"tile_arrow_north_up_done" : @"tile_arrow_north_up"];
            break;
            
        case kAnswerPositionNorth | kAnswerPositionLeft:
        case kAnswerPositionSouth | kAnswerPositionLeft:
        case kAnswerPositionWest | kAnswerPositionTop:
        case kAnswerPositionEast | kAnswerPositionTop:
        case kAnswerPositionNorth | kAnswerPositionRight:
        case kAnswerPositionSouth | kAnswerPositionRight:
        case kAnswerPositionWest | kAnswerPositionBottom:
        case kAnswerPositionEast | kAnswerPositionBottom:
            arrow.image = [UIImage imageNamed:arrowDone ? @"tile_arrow_north_left_done" : @"tile_arrow_north_left"];
            break;
            
        case kAnswerPositionNorth | kAnswerPositionEast | kAnswerPositionRight:
        case kAnswerPositionNorth | kAnswerPositionEast | kAnswerPositionTop:
        case kAnswerPositionNorth | kAnswerPositionWest | kAnswerPositionLeft:
        case kAnswerPositionNorth | kAnswerPositionWest | kAnswerPositionTop:
        case kAnswerPositionSouth | kAnswerPositionEast | kAnswerPositionRight:
        case kAnswerPositionSouth | kAnswerPositionEast | kAnswerPositionBottom:
        case kAnswerPositionSouth | kAnswerPositionWest | kAnswerPositionLeft:
        case kAnswerPositionSouth | kAnswerPositionWest | kAnswerPositionBottom:
            arrow.image = [UIImage imageNamed:arrowDone ? @"tile_arrow_northeast_right_done" : @"tile_arrow_northeast_right"];
            break;
            
        default:
            arrow.image = [UIImage imageNamed:arrowDone ? @"tile_arrow_northwest_right_done" : @"tile_arrow_northwest_right"];
            break;
    }
}

-(void)animateToCorrect
{
    UIImageView * foreground = [[UIImageView alloc] initWithImage:[[TileImageHelper sharedHelper] letterForType:tileData.letterType andIndex:tileData.currentLetterIdx]];
    foreground.alpha = 0;
    foreground.frame = background.frame;
    [self insertSubview:foreground aboveSubview:background];

    CGRect originalFrame = background.frame;
    CGRect zoomedFrame = CGRectMake(originalFrame.origin.x - originalFrame.size.width * 0.1f, originalFrame.origin.y - originalFrame.size.height * 0.1f, originalFrame.size.width * 1.2f, originalFrame.size.height * 1.2f);
    
    [UIView animateWithDuration:0.5f delay:0 options:UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionCurveEaseOut animations:^{
        background.frame = zoomedFrame;
        foreground.frame = zoomedFrame;
    } completion:^(BOOL finished) {
        [UIView animateWithDuration:0.4f delay:0 options:UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionCurveEaseOut animations:^{
            background.frame = originalFrame;
            foreground.frame = originalFrame;
            foreground.alpha = 1;
        } completion:^(BOOL finished) {
            background.image = foreground.image;
            [foreground removeFromSuperview];
        }];
    }];
}

-(void)onTap
{
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_TILE_TAP andData:tileData]];
}

+ (int)tileWidth
{
    return [AppDelegate currentDelegate].isIPad ? 75 : 63;
}

+ (int)tileHeight
{
    return [AppDelegate currentDelegate].isIPad ? 75 : 63;
}

@end
