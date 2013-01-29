//
//  PuzzleSetData.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/3/12.
//
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class PuzzleData;

typedef enum PuzzleSetType
{
    PUZZLESET_BRILLIANT = 0,
    PUZZLESET_GOLD,
    PUZZLESET_SILVER,
    PUZZLESET_FREE,
    PUZZLESET_SILVER2,
}
PuzzleSetType;

@interface PuzzleSetData : NSManagedObject

@property (nonatomic, retain) NSString * set_id;
@property (nonatomic, retain) NSString * user_id;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSNumber * type;
@property (nonatomic, retain) NSNumber * bought;
@property (nonatomic, retain) NSSet *puzzles;

+(PuzzleSetData *)puzzleSetWithDictionary:(NSDictionary *)dict andUserId:(NSString *)userId;

@end

@interface PuzzleSetData (CoreDataGeneratedAccessors)

- (void)addPuzzlesObject:(PuzzleData *)value;
- (void)removePuzzlesObject:(PuzzleData *)value;
- (void)addPuzzles:(NSSet *)values;
- (void)removePuzzles:(NSSet *)values;
@end

@interface PuzzleSetData (DependedParameters)
+(int)solved:(PuzzleSetData *)puzzleSet;
+(int)total:(PuzzleSetData *)puzzleSet;
+(float)percent:(PuzzleSetData *)puzzleSet;
+(int)score:(PuzzleSetData *)puzzleSet;
+(int)minScore:(PuzzleSetData *)puzzleSet;
@end