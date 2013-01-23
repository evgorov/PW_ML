//
//  PuzzleSetView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/19/12.
//
//

#import <UIKit/UIKit.h>
#import "PuzzleSetData.h"

@interface PuzzleSetView : UIView
{
    PuzzleSetType setType;
    int puzzlesCount;
    IBOutlet UIImageView *imgDelimeter;
    IBOutlet UIImageView *imgMonthBg;
    IBOutlet UILabel *lblMonth;
}


@property (strong, nonatomic) IBOutlet UIImageView *imgBar;
@property (strong, nonatomic) IBOutlet UIImageView *imgStar;
@property (strong, nonatomic) IBOutlet UIImageView *imgScoreBg;
@property (strong, nonatomic) IBOutlet UILabel * lblCaption;
@property (strong, nonatomic) IBOutlet UILabel *lblCount;
@property (strong, nonatomic) IBOutlet UILabel *lblScore;
@property (strong, nonatomic) IBOutlet UILabel *lblPercent;
@property (strong, nonatomic) IBOutlet UILabel *lblText1;
@property (strong, nonatomic) IBOutlet UILabel *lblText2;
@property (strong, nonatomic) IBOutlet UIButton *btnBuy;
@property (strong, nonatomic) IBOutlet UIButton *btnShowMore;
@property (nonatomic, readonly) NSMutableArray * badges;

@property (nonatomic, readonly) CGSize shortSize;
@property (nonatomic, readonly) CGSize fullSize;

@property (nonatomic, strong) PuzzleSetData * puzzleSetData;

+(PuzzleSetView *)puzzleSetViewWithType:(PuzzleSetType)type puzzlesCount:(int)count minScore:(int)score price:(float)price;

+(PuzzleSetView *)puzzleSetViewWithType:(PuzzleSetType)type month:(int)month puzzlesCount:(int)count puzzlesSolved:(int)solved score:(int)score ids:(NSArray *)ids percents:(NSArray *)percents scores:(NSArray *)scores;

+(PuzzleSetView *)puzzleSetCompleteViewWithType:(PuzzleSetType)type puzzlesCount:(int)count puzzlesSolved:(int)solved score:(int)score ids:(NSArray *)ids scores:(NSArray *)scores;



-(void)switchToBought;

@end
