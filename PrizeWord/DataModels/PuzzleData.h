//
//  PuzzleData.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/28/12.
//
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class HintData;

@interface PuzzleData : NSManagedObject

@property (nonatomic, retain) NSString * puzzle_id;
@property (nonatomic, retain) NSString * set_id;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSDate * issuedAt;
@property (nonatomic, retain) NSNumber * base_score;
@property (nonatomic, retain) NSNumber * time_given;
@property (nonatomic, retain) NSNumber * height;
@property (nonatomic, retain) NSNumber * width;
@property (nonatomic, retain) NSSet *hints;
@end

@interface PuzzleData (CoreDataGeneratedAccessors)

- (void)addHintsObject:(HintData *)value;
- (void)removeHintsObject:(HintData *)value;
- (void)addHints:(NSSet *)values;
- (void)removeHints:(NSSet *)values;
@end
