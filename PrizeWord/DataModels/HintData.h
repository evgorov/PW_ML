//
//  HintData.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/28/12.
//
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class PuzzleData;

#define kAnswerPositionNorth    1
#define kAnswerPositionSouth    2
#define kAnswerPositionEast     4
#define kAnswerPositionWest     8
#define kAnswerPositionTop      16
#define kAnswerPositionBottom   32
#define kAnswerPositionLeft     64
#define kAnswerPositionRight    128

@interface HintData : NSManagedObject

@property (nonatomic, retain) NSNumber * column;
@property (nonatomic, retain) NSNumber * row;
@property (nonatomic, retain) NSNumber * answer_position;
@property (nonatomic, retain) NSString * hint_text;
@property (nonatomic, retain) NSString * answer;
@property (nonatomic, retain) PuzzleData *puzzle;

@property (nonatomic) uint columnAsUint;
@property (nonatomic) uint rowAsUint;
@property (nonatomic) uint answer_positionAsUint;
@property (nonatomic, retain) NSString * answer_positionAsString;

@end
