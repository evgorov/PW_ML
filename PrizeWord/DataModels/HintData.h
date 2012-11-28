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

@interface HintData : NSManagedObject

@property (nonatomic, retain) NSNumber * column;
@property (nonatomic, retain) NSNumber * row;
@property (nonatomic, retain) NSString * hint_text;
@property (nonatomic, retain) NSString * answer;
@property (nonatomic, retain) NSString * answer_position;
@property (nonatomic, retain) PuzzleData *puzzle;

@end
