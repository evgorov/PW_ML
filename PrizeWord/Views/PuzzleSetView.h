//
//  PuzzleSetView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/19/12.
//
//

#import <UIKit/UIKit.h>

typedef enum PuzzleSetType
{
    PUZZLESET_BRILLIANT = 0,
    PUZZLESET_GOLD,
    PUZZLESET_SILVER,
    PUZZLESET_FREE,
    PUZZLESET_SILVER2,
}
PuzzleSetType;

@interface PuzzleSetView : UIView
{
    PuzzleSetType setType;
    int puzzlesCount;
}

@property (nonatomic, readonly) UIImageView * imgBar;
@property (nonatomic, readonly) UILabel * lblCaption;
@property (nonatomic, readonly) UILabel * lblCount;
@property (nonatomic, readonly) UILabel * lblScore;
@property (nonatomic, readonly) UIButton * btnBuy;
@property (nonatomic, readonly) NSMutableArray * badges;

-(id)initWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price;

-(id)initWithType:(PuzzleSetType)type puzzlesCount:(int)count puzzlesSolved:(int)solved score:(int)score ids:(NSArray *)ids percents:(NSArray *)percents;

+(PuzzleSetView *)puzzleSetViewWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price;

+(PuzzleSetView *)puzzleSetViewWithType:(PuzzleSetType)type puzzlesCount:(int)count puzzlesSolved:(int)solved score:(int)score ids:(NSArray *)ids percents:(NSArray *)percents;

-(void)switchToBought;

@end
