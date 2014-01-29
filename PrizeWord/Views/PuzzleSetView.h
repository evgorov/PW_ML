//
//  PuzzleSetView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/19/12.
//
//

#import <UIKit/UIKit.h>

@class PuzzleSetProxy;
@class SKProduct;

@interface PuzzleSetView : UIView
{
    IBOutlet UIImageView *imgMonthBg;
    IBOutlet UILabel *lblMonth;
}

@property (strong, nonatomic) IBOutlet UIImageView *imgDelimeter;
@property (strong, nonatomic) IBOutlet UIImageView *imgBar;
@property (strong, nonatomic) IBOutlet UIImageView *imgStar;
@property (strong, nonatomic) IBOutlet UIImageView *imgScoreBg;
@property (strong, nonatomic) IBOutlet UILabel *lblCaption;
@property (strong, nonatomic) IBOutlet UILabel *lblCount;
@property (strong, nonatomic) IBOutlet UILabel *lblScore;
@property (strong, nonatomic) IBOutlet UILabel *lblPercent;
@property (strong, nonatomic) IBOutlet UILabel *lblText1;
@property (strong, nonatomic) IBOutlet UILabel *lblText2;
@property (strong, nonatomic) IBOutlet PrizeWordButton *btnBuy;
@property (strong, nonatomic) IBOutlet UIButton *btnShowMore;
@property (nonatomic, readonly) NSMutableArray * badges;
@property (strong, nonatomic) SKProduct * product;

@property (nonatomic, readonly) PuzzleSetProxy * puzzleSetData;
@property (nonatomic, readonly) CGSize shortSize;
@property (nonatomic, readonly) CGSize fullSize;
@property (readonly) int month;

+ (float)fullHeightForPuzzleSet:(PuzzleSetProxy *)puzzleSet;
+ (float)shortHeightForPuzzleSet:(PuzzleSetProxy *)puzzleSet;

+(PuzzleSetView *)puzzleSetViewWithData:(PuzzleSetProxy *)puzzleSetData month:(int)month showSolved:(BOOL)showSolved showUnsolved:(BOOL)showUnsolved;

+(PuzzleSetView *)puzzleSetCompleteViewWithData:(PuzzleSetProxy *)puzzleSetData;


-(void)switchToBought;

@end
