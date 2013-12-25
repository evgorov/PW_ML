//
//  PuzzleSetPackData.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/8/13.
//
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class PuzzleSetData;

@interface PuzzleSetPackData : NSManagedObject

@property (nonatomic, retain) NSNumber * month;
@property (nonatomic, retain) NSNumber * year;
@property (nonatomic, retain) NSString * user_id;
@property (nonatomic, retain) NSString * etag;
@property (nonatomic, retain) NSSet *puzzleSets;

+ (PuzzleSetPackData *)puzzleSetPackWithYear:(int)year andMonth:(int)month andUserId:(NSString *)userId inMOC:(NSManagedObjectContext *)moc;

@end

@interface PuzzleSetPackData (CoreDataGeneratedAccessors)

- (void)addPuzzleSetsObject:(PuzzleSetData *)value;
- (void)removePuzzleSetsObject:(PuzzleSetData *)value;
- (void)addPuzzleSets:(NSSet *)values;
- (void)removePuzzleSets:(NSSet *)values;

@end
