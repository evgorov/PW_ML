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

#import <QuartzCore/CALayer.h>

@interface GameTileView ()

-(void)onTap;
-(void)initParts;

@end

@implementation GameTileView

- (id)initWithFrame:(CGRect)frame andData:(TileData *)data
{
    self = [super initWithFrame:frame];
    if (self) {
        background = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"tile_letter_empty"]];
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
        
        letterLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, frame.size.width, frame.size.height)];
        letterLabel.font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:24];
        letterLabel.lineBreakMode = NSLineBreakByTruncatingTail;
        letterLabel.textAlignment = NSTextAlignmentCenter;
        letterLabel.textColor = [UIColor colorWithRed:0.235 green:0.243 blue:0.271 alpha:1];
        letterLabel.backgroundColor = [UIColor clearColor];
        letterLabel.hidden = YES;
        [self addSubview:letterLabel];
        
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
    NSLog(@"TileView dealloc");
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_TILE_CHANGE];
    tileData = nil;
    background = nil;
    questionLabel = nil;
    letterLabel = nil;
}

-(void)handleEvent:(Event *)event
{
    switch (event.type) {
        case EVENT_TILE_CHANGE:
        {
            TileData * newData = (TileData *)event.data;
            if (tileData.x == newData.x && tileData.y == newData.y)
            {
                // TODO :: play animation
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
            
        case TILE_LETTER_CORRECT:
            [background setImage:[UIImage imageNamed:@"tile_letter_correct"]];
            break;
            
        case TILE_LETTER_WRONG:
            [background setImage:[UIImage imageNamed:@"tile_letter_wrong"]];
            break;
            
        case TILE_LETTER_EMPTY_INPUT:
            [background setImage:[UIImage imageNamed:@"tile_letter_empty_input"]];
            break;
            
        case TILE_LETTER_CORRECT_INPUT:
            [background setImage:[UIImage imageNamed:@"tile_letter_correct_input"]];
            break;
            
        case TILE_LETTER_INPUT:
            [background setImage:[UIImage imageNamed:@"tile_letter_input"]];
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
    }
    else
    {
        questionLabel.text = @"";
        questionLabel.hidden = YES;
    }
    if (tileData.state == TILE_LETTER_CORRECT || tileData.state == TILE_LETTER_CORRECT_INPUT || tileData.state == TILE_LETTER_INPUT || tileData.state == TILE_LETTER_WRONG)
    {
        letterLabel.text = [tileData.currentLetter uppercaseString];
        letterLabel.hidden = NO;
    }
    else
    {
        letterLabel.text = @"";
        letterLabel.hidden = YES;
    }
}

-(void)onTap
{
    [[EventManager sharedManager] dispatchEventWithType:[Event eventWithType:EVENT_TILE_TAP andData:tileData]];
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
