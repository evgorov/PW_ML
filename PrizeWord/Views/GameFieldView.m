//
//  GameFieldView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/26/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "GameFieldView.h"
#import "GameField.h"
#import "TileData.h"
#import "EventManager.h"
#import "TileImageHelper.h"
#import "AppDelegate.h"
#import "QuestionData.h"

#define kTileOffset 20

int tileWidth = 63;
int tileHeight = 63;

@interface GameFieldContentView : UIView
{
    GameField * gameField;
    UIImageView * borderTopLeft;
    UIImageView * borderBottomLeft;
    UIImageView * borderTopRight;
    UIImageView * borderBottomRight;
    UILabel * questionLabel;
    BOOL hideQuestions;
}

- (id)initWithGameField:(GameField *)gameField;
- (void)setGameField:(GameField *)gameField;
- (void)handleEvent:(Event *)event;
- (void)drawTile:(TileData *)tileData inRect:(CGRect)rect;
- (void)drawArrowForQuestionTile:(TileData *)tileData inBounds:(CGRect)rect;
- (UIImage *)arrowImageForQuestionTile:(TileData *)tileData empty:(BOOL)empty;
- (void)animateTileToCorrect:(TileData *)tileData;
- (void)invalidateTile:(TileData *)tileData;

- (IBAction)onTap:(id)sender;
@end

@interface GameFieldView ()
{
    GameFieldContentView * fieldView;
}

-(void)switchFocusToTile:(TileData *)tile;
-(void)scrollTo:(CGPoint)targetPosition;
-(void)handlePinch:(id)sender;
-(void)handleTap:(id)sender;

@end

@implementation GameFieldView

+(void)initialize
{
    tileWidth = [AppDelegate currentDelegate].isIPad ? 75 : 63;
    tileHeight = [AppDelegate currentDelegate].isIPad ? 75 : 63;
}

-(id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 0, self.frame.size.width, self.frame.size.height)];
        scrollView.bounces = YES;
        [self addSubview:scrollView];
        self.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_dark_tile.jpg"]];
        scrollView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_dark_tile.jpg"]];
        fieldView = [GameFieldContentView new];
        [scrollView addSubview:fieldView];
        scrollView.delegate = self;
        focusedTile = nil;
        
        pinchGestureRecognizer = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(handlePinch:)];
        pinchGestureRecognizer.delegate = self;
        tapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTap:)];
        tapGestureRecognizer.delegate = self;
        tapGestureRecognizer.numberOfTapsRequired = 1;
        tapGestureRecognizer.numberOfTouchesRequired = 1;
        [scrollView addGestureRecognizer:pinchGestureRecognizer];
        [scrollView addGestureRecognizer:tapGestureRecognizer];
        scrollView.userInteractionEnabled = YES;
        scrollView.multipleTouchEnabled = YES;
        fieldView.userInteractionEnabled = YES;
        fieldView.multipleTouchEnabled = YES;
        
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_FOCUS_CHANGE];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_TILE_CHANGE];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_TILE_INVALIDATE];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_PAUSE];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_RESUME];
    }
    return self;
}

-(void)setGameField:(GameField *)gameField;
{
    [fieldView setGameField:gameField];

    scrollView.contentSize = fieldView.bounds.size;
}

-(void)dealloc
{
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_FOCUS_CHANGE];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_TILE_CHANGE];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_TILE_INVALIDATE];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_GAME_REQUEST_PAUSE];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_GAME_REQUEST_RESUME];
    [fieldView removeFromSuperview];
    fieldView = nil;
    [scrollView removeFromSuperview];
    scrollView = nil;
}

// EventListener
-(void)handleEvent:(Event *)event
{
    switch (event.type)
    {
        case EVENT_FOCUS_CHANGE:
            [self switchFocusToTile:event.data];
            break;
            
        case EVENT_TILE_CHANGE:
        case EVENT_TILE_INVALIDATE:
        case EVENT_GAME_REQUEST_PAUSE:
        case EVENT_GAME_REQUEST_RESUME:
            [fieldView handleEvent:event];
            break;
            
        default:
            break;
    }
}

-(void)setFrame:(CGRect)frame
{
    [super setFrame:frame];
    [scrollView setFrame:CGRectMake(0, 0, frame.size.width, frame.size.height)];
    float dx = scrollView.frame.size.width - scrollView.contentSize.width;
    float dy = scrollView.frame.size.height - scrollView.contentSize.height;
    scrollView.contentInset = UIEdgeInsetsMake(dy > 0 ? dy / 2 : 0, dx > 0 ? dx / 2 : 0, dy > 0 ? dy / 2 : 0, dx > 0 ? dx / 2 : 0);
}

-(void)switchFocusToTile:(TileData *)tile
{
    focusedTile = tile;
    [self refreshFocus];
}

-(void)scrollTo:(CGPoint)targetPosition
{
    if (targetPosition.x > scrollView.contentSize.width + scrollView.contentInset.right - scrollView.frame.size.width)
        targetPosition.x = scrollView.contentSize.width + scrollView.contentInset.right - scrollView.frame.size.width;
    if (targetPosition.y > scrollView.contentSize.height + scrollView.contentInset.bottom - scrollView.frame.size.height)
        targetPosition.y = scrollView.contentSize.height + scrollView.contentInset.bottom - scrollView.frame.size.height;
    if (targetPosition.x < -scrollView.contentInset.left)
        targetPosition.x = -scrollView.contentInset.left;
    if (targetPosition.y < -scrollView.contentInset.top)
        targetPosition.y = -scrollView.contentInset.top;
    [scrollView setContentOffset:scrollView.contentOffset animated:NO];
    [scrollView setContentOffset:targetPosition animated:YES];
}

-(void)refreshFocus
{
    if (focusedTile != nil) {
        int offsetX = kTileOffset + focusedTile.x * tileWidth + tileWidth / 2 - scrollView.frame.size.width / 2;
        int offsetY = kTileOffset + focusedTile.y * tileHeight + tileHeight / 2 - scrollView.frame.size.height / 2;
        [self scrollTo:CGPointMake(offsetX, offsetY)];
    }
}

-(UIView *)viewForZoomingInScrollView:(UIScrollView *)scrollView
{
    return fieldView;
}

-(void)handlePinch:(id)sender
{
    if (pinchGestureRecognizer.state == UIGestureRecognizerStateBegan)
    {
        if (pinchGestureRecognizer.scale > 1)
        {
            return;
        }
        float targetZoom = scrollView.frame.size.width / scrollView.contentSize.width;
        if (scrollView.frame.size.height / scrollView.contentSize.height < targetZoom)
        {
            targetZoom = scrollView.frame.size.height / scrollView.contentSize.height;
        }
        if (targetZoom < 1)
        {
            scrollView.minimumZoomScale = targetZoom;
            scrollView.maximumZoomScale = targetZoom;
            [scrollView setZoomScale:targetZoom animated:YES];
            NSLog(@"%f %f %f %f", scrollView.frame.size.width, scrollView.frame.size.height, scrollView.contentSize.width, scrollView.contentSize.height);
            float dx = scrollView.frame.size.width - scrollView.contentSize.width;
            float dy = scrollView.frame.size.height - scrollView.contentSize.height;
            if (dx > 0 || dy > 0)
            {
                [scrollView setContentOffset:CGPointMake(dx > 0 ? (-dx) / 2 : 0, dy > 0 ? (-dy) / 2 : 0) animated:YES];
            }
            pinchGestureRecognizer.enabled = NO;
            tapGestureRecognizer.enabled = YES;
        }
    }
}

-(BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldReceiveTouch:(UITouch *)touch
{
    if ([gestureRecognizer isKindOfClass:[UITapGestureRecognizer class]] && scrollView.zoomScale >= 1)
    {
        return NO;
    }

    if ([gestureRecognizer isKindOfClass:[UITapGestureRecognizer class]])
    {
        if (scrollView.zoomScale < 1)
        {
            CGPoint touchPosition = [touch locationInView:fieldView];
            scrollView.minimumZoomScale = 1;
            scrollView.maximumZoomScale = 1;
            [scrollView setZoomScale:1 animated:YES];
            touchPosition.x -= scrollView.frame.size.width / 2;
            touchPosition.y -= scrollView.frame.size.height / 2;
            [self scrollTo:touchPosition];
            /*
            if (touchPosition.x < 0) touchPosition.x = 0;
            if (touchPosition.y < 0) touchPosition.y = 0;
            if (touchPosition.x > scrollView.contentSize.width - scrollView.frame.size.width) touchPosition.x = scrollView.contentSize.width - scrollView.frame.size.width;
            if (touchPosition.y > scrollView.contentSize.height - scrollView.frame.size.height) touchPosition.y = scrollView.contentSize.height - scrollView.frame.size.height;
            [scrollView setContentOffset:touchPosition];
             */
            pinchGestureRecognizer.enabled = YES;
            tapGestureRecognizer.enabled = NO;
        }
    }
    return YES;
}

-(void)handleTap:(id)sender
{
}

-(void)invalidateTile:(TileData *)tile
{
    [fieldView invalidateTile:tile];
}

@end

@implementation GameFieldContentView

- (id)init
{
    self = [super init];
    if (self)
    {
        gameField = nil;
        self.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_sand_tile.jpg"]];

        UIImage * stretchableBorder = [[UIImage imageNamed:@"bg_border"] stretchableImageWithLeftCapWidth:40 topCapHeight:40];
        borderTopLeft = [[UIImageView alloc] initWithImage:stretchableBorder];
        borderBottomLeft = [[UIImageView alloc] initWithImage:stretchableBorder];
        borderBottomLeft.transform = CGAffineTransformMakeRotation(-M_PI_2);
        borderTopRight = [[UIImageView alloc] initWithImage:stretchableBorder];
        borderTopRight.transform = CGAffineTransformMakeRotation(M_PI_2);
        borderBottomRight = [[UIImageView alloc] initWithImage:stretchableBorder];
        borderBottomRight.transform = CGAffineTransformMakeRotation(M_PI);
        [self addSubview:borderTopLeft];
        [self addSubview:borderTopRight];
        [self addSubview:borderBottomLeft];
        [self addSubview:borderBottomRight];
        
        questionLabel = [[UILabel alloc] initWithFrame:CGRectMake(tileWidth * 0.1, 0, tileWidth * 0.8, tileHeight)];
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
        hideQuestions = NO;
        
        self.userInteractionEnabled = YES;
        self.multipleTouchEnabled = YES;
        
        UITapGestureRecognizer * tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTap:)];
        [self addGestureRecognizer:tapRecognizer];
        
    }
    return self;
}

- (id)initWithGameField:(GameField *)gameField_
{
    self = [self init];
    if (self)
    {
        [self setGameField:gameField_];
    }
    return self;
}

- (void)dealloc
{
    [borderTopLeft removeFromSuperview];
    [borderTopRight removeFromSuperview];
    [borderBottomLeft removeFromSuperview];
    [borderBottomRight removeFromSuperview];
    borderTopLeft = nil;
    borderTopRight = nil;
    borderBottomLeft = nil;
    borderBottomRight = nil;
    questionLabel = nil;
}

- (void)setGameField:(GameField *)gameField_
{
    gameField = gameField_;
    
    int width = gameField.tilesPerRow * tileWidth + 2 * kTileOffset;
    int height = gameField.tilesPerCol * tileHeight + 2 * kTileOffset;
    self.frame = CGRectMake(0, 0, width, height);
    borderTopLeft.frame = CGRectMake(0, 0, width / 2, height / 2);
    borderTopRight.frame = CGRectMake(width / 2, 0, width - width / 2, height / 2);
    borderBottomLeft.frame = CGRectMake(0, height / 2, width / 2, height - height / 2);
    borderBottomRight.frame = CGRectMake(width / 2, height / 2, width - width / 2, height - height / 2);

    [self setNeedsDisplayInRect:self.bounds];
}

-(void)handleEvent:(Event *)event
{
    switch (event.type)
    {
        case EVENT_TILE_CHANGE:
        {
            TileData * tileData = event.data;
            
            [self invalidateTile:tileData];
            
            if (tileData.state == TILE_LETTER_CORRECT && tileData.prevState == TILE_LETTER_INPUT)
            {
                [self animateTileToCorrect:tileData];
            }
        }
            break;
            
        case EVENT_TILE_INVALIDATE:
        {
            TileData * tileData = event.data;

            [self invalidateTile:tileData];
        }
            break;
            
        case EVENT_GAME_REQUEST_PAUSE:
            hideQuestions = YES;
            [self setNeedsDisplay];
            break;
            
        case EVENT_GAME_REQUEST_RESUME:
            hideQuestions = NO;
            [self setNeedsDisplay];
            break;
            
        default:
            break;
    }
}

- (void)drawRect:(CGRect)rect
{
    if (gameField == nil)
    {
        return;
    }
    int minCol = (rect.origin.x - kTileOffset) / tileWidth;
    int maxCol = (rect.origin.x + rect.size.width - kTileOffset) / tileWidth;
    int minRow = (rect.origin.y - kTileOffset) / tileHeight;
    int maxRow = (rect.origin.y + rect.size.height - kTileOffset) / tileHeight;
    minCol = MAX(0, minCol);
    minRow = MAX(0, minRow);
    maxCol = MIN(gameField.tilesPerRow - 1, maxCol);
    maxRow = MIN(gameField.tilesPerCol - 1, maxRow);
    
    // draw tiles (letters and questions)
    for (int col = minCol; col <= maxCol; ++col)
    {
        for (int row = minRow; row <= maxRow; ++row)
        {
            CGRect tileRect = CGRectMake(tileWidth * col + kTileOffset, tileHeight * row + kTileOffset, tileWidth, tileHeight);
            [self drawTile:[gameField dataForPositionX:col y:row] inRect:tileRect];
        }
    }
    
    // draw arrows
    maxCol = MIN(gameField.tilesPerRow - 1, maxCol + 1);
    maxRow = MIN(gameField.tilesPerCol - 1, maxRow + 1);
    minCol = MAX(0, minCol - 1);
    minRow = MAX(0, minRow - 1);
    for (int col = minCol; col <= maxCol; ++col)
    {
        for (int row = minRow; row <= maxRow; ++row)
        {
            TileData * tileData = [gameField dataForPositionX:col y:row];
            if (tileData.state != TILE_QUESTION_NEW && tileData.state != TILE_QUESTION_WRONG)
            {
                continue;
            }
            
            [self drawArrowForQuestionTile:tileData inBounds:rect];
        }
    }
}

- (void)drawTile:(TileData *)tileData inRect:(CGRect)rect
{
    if (tileData == nil)
    {
        return;
    }
    switch (tileData.state) {
        case TILE_QUESTION_NEW:
            [[UIImage imageNamed:@"tile_question_new"] drawInRect:rect];
            break;
            
        case TILE_QUESTION_CORRECT:
            [[[TileImageHelper sharedHelper] correctQuestionForType:tileData.letterType] drawInRect:rect];
            break;
            
        case TILE_QUESTION_WRONG:
            [[UIImage imageNamed:@"tile_question_wrong"] drawInRect:rect];
            break;
            
        case TILE_QUESTION_INPUT:
            [[UIImage imageNamed:@"tile_question_input"] drawInRect:rect];
            break;
            
        case TILE_LETTER_EMPTY:
            [[UIImage imageNamed:@"tile_letter_empty"] drawInRect:rect];
            break;
            
        case TILE_LETTER_CORRECT_INPUT:
        case TILE_LETTER_CORRECT:
        {
            [[[TileImageHelper sharedHelper] letterForType:tileData.letterType andIndex:tileData.currentLetterIdx] drawInRect:rect];
        }
            break;
            
        case TILE_LETTER_WRONG:
            [[[TileImageHelper sharedHelper] letterForType:LETTER_WRONG andIndex:tileData.currentLetterIdx] drawInRect:rect];
            break;
            
        case TILE_LETTER_EMPTY_INPUT:
            [[UIImage imageNamed:@"tile_letter_empty_input"] drawInRect:rect];
            break;
            
        case TILE_LETTER_INPUT:
        {
            [[[TileImageHelper sharedHelper] letterForType:LETTER_INPUT andIndex:tileData.currentLetterIdx] drawInRect:rect];
        }
            break;
            
        default:
            break;
    }

    if (tileData.state == TILE_LETTER_CORRECT_INPUT)
    {
        [[UIImage imageNamed:@"tile_letter_correct_input_overlay"] drawInRect: rect];
    }
    
    if (!hideQuestions && (tileData.state == TILE_QUESTION_CORRECT
        || tileData.state == TILE_QUESTION_INPUT
        || tileData.state == TILE_QUESTION_NEW
        || tileData.state == TILE_QUESTION_WRONG))
    {
        CGRect labelRect = rect;
        labelRect.origin.x += tileWidth * 0.1;
        labelRect.size.width -= tileWidth * 0.2;
        if (tileData.state == TILE_QUESTION_CORRECT)
        {
            questionLabel.textColor = [UIColor colorWithRed:0.667 green:0.678 blue:0.71 alpha:1];
        }
        else
        {
            questionLabel.textColor = [UIColor colorWithRed:0.235 green:0.243 blue:0.271 alpha:1];
        }
        questionLabel.text = tileData.question;
        [questionLabel drawTextInRect:labelRect];
    }
}

- (void)drawArrowForQuestionTile:(TileData *)tileData inBounds:(CGRect)rect
{
    int arrowCol = tileData.x;
    int arrowRow = tileData.y;

    if ((tileData.answerPosition & kAnswerPositionNorth) != 0)
    {
        --arrowRow;
    }
    if ((tileData.answerPosition & kAnswerPositionSouth) != 0)
    {
        ++arrowRow;
    }
    if ((tileData.answerPosition & kAnswerPositionWest) != 0)
    {
        --arrowCol;
    }
    if ((tileData.answerPosition & kAnswerPositionEast) != 0)
    {
        ++arrowCol;
    }

    CGRect arrowRect = CGRectMake(tileWidth * arrowCol + kTileOffset, tileHeight * arrowRow + kTileOffset, tileWidth, tileHeight);

    if (CGRectGetMinX(arrowRect) >= CGRectGetMaxX(rect)
        || CGRectGetMinY(arrowRect) >= CGRectGetMaxY(rect)
        || CGRectGetMaxX(arrowRect) <= CGRectGetMinX(rect)
        || CGRectGetMaxY(arrowRect) <= CGRectGetMinY(rect))
    {
        return;
    }
    
    TileData * arrowBackgroundTile = [gameField dataForPositionX:arrowCol y:arrowRow];
    
    if (arrowBackgroundTile.state == TILE_LETTER_INPUT || arrowBackgroundTile.state == TILE_LETTER_EMPTY_INPUT || arrowBackgroundTile.state == TILE_LETTER_CORRECT_INPUT)
    {
        return;
    }

    UIImage * arrowImage = [self arrowImageForQuestionTile:tileData empty:(arrowBackgroundTile.state == TILE_LETTER_EMPTY)];
    
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
        case kAnswerPositionNorth | kAnswerPositionEast | kAnswerPositionTop:
            rotation = -M_PI_2;
            break;
            
        case kAnswerPositionEast | kAnswerPositionRight:
        case kAnswerPositionEast | kAnswerPositionTop:
        case kAnswerPositionWest | kAnswerPositionTop:
        case kAnswerPositionNorth | kAnswerPositionEast | kAnswerPositionBottom:
        case kAnswerPositionNorth | kAnswerPositionWest | kAnswerPositionBottom:
        case kAnswerPositionSouth | kAnswerPositionEast | kAnswerPositionBottom:
        case kAnswerPositionSouth | kAnswerPositionWest | kAnswerPositionBottom:
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

    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSaveGState(context);
    CGContextTranslateCTM(context, arrowRect.origin.x + arrowRect.size.width / 2, arrowRect.origin.y + arrowRect.size.height / 2);
    CGContextScaleCTM(context, scaleX, scaleY);
    CGContextRotateCTM(context, rotation);
    CGContextTranslateCTM(context, -arrowRect.size.width / 2, -arrowRect.size.height / 2);
    arrowRect.origin = CGPointZero;
    [arrowImage drawInRect:arrowRect];
    CGContextRestoreGState(context);
}

- (UIImage *)arrowImageForQuestionTile:(TileData *)tileData empty:(BOOL)empty
{
    switch (tileData.answerPosition)
    {
        case kAnswerPositionNorth | kAnswerPositionTop:
        case kAnswerPositionSouth | kAnswerPositionBottom:
        case kAnswerPositionWest | kAnswerPositionLeft:
        case kAnswerPositionEast | kAnswerPositionRight:
            return [UIImage imageNamed:empty ? @"tile_arrow_north_up" : @"tile_arrow_north_up_done"];
            break;
            
        case kAnswerPositionNorth | kAnswerPositionLeft:
        case kAnswerPositionSouth | kAnswerPositionLeft:
        case kAnswerPositionWest | kAnswerPositionTop:
        case kAnswerPositionEast | kAnswerPositionTop:
        case kAnswerPositionNorth | kAnswerPositionRight:
        case kAnswerPositionSouth | kAnswerPositionRight:
        case kAnswerPositionWest | kAnswerPositionBottom:
        case kAnswerPositionEast | kAnswerPositionBottom:
            return [UIImage imageNamed:empty ? @"tile_arrow_north_left" : @"tile_arrow_north_left_done"];
            break;
            
        case kAnswerPositionNorth | kAnswerPositionEast | kAnswerPositionRight:
        case kAnswerPositionNorth | kAnswerPositionEast | kAnswerPositionTop:
        case kAnswerPositionNorth | kAnswerPositionWest | kAnswerPositionLeft:
        case kAnswerPositionNorth | kAnswerPositionWest | kAnswerPositionTop:
        case kAnswerPositionSouth | kAnswerPositionEast | kAnswerPositionRight:
        case kAnswerPositionSouth | kAnswerPositionEast | kAnswerPositionBottom:
        case kAnswerPositionSouth | kAnswerPositionWest | kAnswerPositionLeft:
        case kAnswerPositionSouth | kAnswerPositionWest | kAnswerPositionBottom:
            return [UIImage imageNamed:empty ? @"tile_arrow_northeast_right" : @"tile_arrow_northeast_right_done"];
            break;
            
        default:
            return [UIImage imageNamed:empty ? @"tile_arrow_northwest_right" : @"tile_arrow_northwest_right_done"];
            break;
    }
}

- (void)animateTileToCorrect:(TileData *)tileData
{
    CGRect tileRect = CGRectMake(tileWidth * tileData.x + kTileOffset, tileHeight * tileData.y + kTileOffset, tileWidth, tileHeight);
    UIImageView * background = [[UIImageView alloc] initWithImage:[[TileImageHelper sharedHelper] letterForType:LETTER_INPUT andIndex:tileData.currentLetterIdx]];
    UIImageView * foreground = [[UIImageView alloc] initWithImage:[[TileImageHelper sharedHelper] letterForType:tileData.letterType andIndex:tileData.currentLetterIdx]];
    background.alpha = 1;
    background.frame = tileRect;
    foreground.alpha = 0;
    foreground.frame = tileRect;
    [self addSubview:background];
    [self addSubview:foreground];
    
    CGRect originalFrame = tileRect;
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
            [background removeFromSuperview];
            [UIView animateWithDuration:0.3f delay:0 options:UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionCurveEaseOut animations:^{
                foreground.alpha = 0;
            } completion:^(BOOL finished) {
                [foreground removeFromSuperview];
            }];
        }];
    }];
}

- (void)invalidateTile:(TileData *)tileData
{
    CGRect tileRect = CGRectMake(tileWidth * tileData.x + kTileOffset, tileHeight * tileData.y + kTileOffset, tileWidth, tileHeight);
    [self setNeedsDisplayInRect:tileRect];
}

- (void)onTap:(id)sender
{
    UITapGestureRecognizer * tapGestureRecognizer = sender;
    CGPoint tapPosition = [tapGestureRecognizer locationInView:self];
    
    int col = (tapPosition.x - kTileOffset) / tileWidth;
    int row = (tapPosition.y - kTileOffset) / tileHeight;
    
    TileData * tileData = [gameField dataForPositionX:col y:row];
    
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_TILE_TAP andData:tileData]];
}

@end
