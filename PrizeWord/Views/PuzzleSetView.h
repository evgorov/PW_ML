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
    PUZZLESET_SILVER2,
    PUZZLESET_FREE
}
PuzzleSetType;

@interface PuzzleSetView : UIView

@property (nonatomic, readonly) UIImageView * imgBar;
@property (nonatomic, readonly) UILabel * lblCaption;
@property (nonatomic, readonly) UILabel * lblCount;
@property (nonatomic, readonly) UILabel * lblScore;
@property (nonatomic, readonly) UIButton * btnBuy;

-(id)initWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price;

+(PuzzleSetView *)puzzleSetViewWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price;

@end
