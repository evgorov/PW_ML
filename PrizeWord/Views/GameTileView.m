//
//  GameTileView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/25/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "GameTileView.h"
#import "EventManager.h"
#import "TileData.h"
#import "TileImageHelper.h"
#import "QuestionData.h"
#import "GameLogic.h"
#import "GameField.h"

#import <QuartzCore/CALayer.h>

@interface GameTileView ()

-(void)onTap;
-(void)initParts;
-(void)showArrow;
-(void)hideArrow;

@end

@implementation GameTileView

- (id)initWithFrame:(CGRect)frame andData:(TileData *)data
{
    self = [super initWithFrame:frame];
    if (self) {
        background = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"tile_letter_empty"]];
        background.contentMode = UIViewContentModeScaleToFill;
        [self addSubview:background];
        questionLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, frame.size.width, frame.size.height)];
        questionLabel.font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:8];
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
        self.clipsToBounds = YES;
        
        tileData = data;
        [self initParts];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_TILE_CHANGE];
        
        self.userInteractionEnabled = YES;
        UITapGestureRecognizer * tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTap)];
        [self addGestureRecognizer:tapRecognizer];
    }
    return self;
}

-(void)dealloc
{
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_TILE_CHANGE];
    tileData = nil;
    background = nil;
    questionLabel = nil;
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
            break;
        }
            
        default:
            break;
    }
}

-(void)initParts
{
    switch (tileData.state) {
        case TILE_QUESTION_NEW:
            [background setImage:[UIImage imageNamed:@"tile_question_new"]];
            break;
            
        case TILE_QUESTION_CORRECT:
            [background setImage:[UIImage imageNamed:@"tile_question_correct"]];
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
        
        if (tileData.state != TILE_QUESTION_CORRECT && tileData != [GameLogic sharedLogic].gameField.activeQuestion)
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
    
    switch (tileData.answerPosition)
    {
        case kAnswerPositionNorth | kAnswerPositionTop:
        case kAnswerPositionSouth | kAnswerPositionBottom:
        case kAnswerPositionWest | kAnswerPositionLeft:
        case kAnswerPositionEast | kAnswerPositionRight:
            arrow.image = [UIImage imageNamed:@"tile_arrow_north_up"];
            break;
            
        case kAnswerPositionNorth | kAnswerPositionLeft:
        case kAnswerPositionSouth | kAnswerPositionLeft:
        case kAnswerPositionWest | kAnswerPositionTop:
        case kAnswerPositionEast | kAnswerPositionTop:
        case kAnswerPositionNorth | kAnswerPositionRight:
        case kAnswerPositionSouth | kAnswerPositionRight:
        case kAnswerPositionWest | kAnswerPositionBottom:
        case kAnswerPositionEast | kAnswerPositionBottom:
            arrow.image = [UIImage imageNamed:@"tile_arrow_north_left"];
            break;
            
        default:
            arrow.image = [UIImage imageNamed:@"tile_arrow_northwest_right"];
            break;
    }
    
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
        case kAnswerPositionWest | kAnswerPositionTop:
        case kAnswerPositionSouth | kAnswerPositionWest | kAnswerPositionTop:
        case kAnswerPositionSouth | kAnswerPositionEast | kAnswerPositionTop:
            rotation = -M_PI_2;
            break;

        case kAnswerPositionEast | kAnswerPositionRight:
        case kAnswerPositionEast | kAnswerPositionTop:
        case kAnswerPositionEast | kAnswerPositionBottom:
        case kAnswerPositionNorth | kAnswerPositionEast | kAnswerPositionBottom:
        case kAnswerPositionNorth | kAnswerPositionWest | kAnswerPositionBottom:
            rotation = M_PI_2;
            break;
            
        case kAnswerPositionSouth | kAnswerPositionBottom:
        case kAnswerPositionSouth | kAnswerPositionRight:
        case kAnswerPositionSouth | kAnswerPositionLeft:
        case kAnswerPositionSouth | kAnswerPositionEast | kAnswerPositionLeft:
        case kAnswerPositionSouth | kAnswerPositionWest | kAnswerPositionRight:
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

-(void)onTap
{
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_TILE_TAP andData:tileData]];
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

@end
