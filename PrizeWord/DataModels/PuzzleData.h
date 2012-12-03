//
//  PuzzleData.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/28/12.
//
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class QuestionData;
@class PuzzleSetData;

@interface PuzzleData : NSManagedObject

@property (nonatomic, retain) NSString * puzzle_id;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSDate * issuedAt;
@property (nonatomic, retain) NSNumber * base_score;
@property (nonatomic, retain) NSNumber * time_given;
@property (nonatomic, retain) NSNumber * height;
@property (nonatomic, retain) NSNumber * width;
@property (nonatomic, retain) NSSet *questions;
@property (nonatomic, retain) PuzzleSetData *puzzleSet;
@end

@interface PuzzleData (CoreDataGeneratedAccessors)

- (void)addQuestionsObject:(QuestionData *)value;
- (void)removeQuestionsObject:(QuestionData *)value;
- (void)addQuestions:(NSSet *)values;
- (void)removeQuestions:(NSSet *)values;
@end
