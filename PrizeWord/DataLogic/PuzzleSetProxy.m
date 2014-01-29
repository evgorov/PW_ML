//
//  PuzzleSetProxy.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/27/14.
//
//

#import "PuzzleSetProxy.h"
#import "PuzzleProxy.h"
#import "PuzzleSetPackProxy.h"

@implementation PuzzleSetProxy

DATAPROXY_FORWARDING_GET(PuzzleSetProxy *, NSString *, set_id);
DATAPROXY_FORWARDING_GET(PuzzleSetProxy *, NSString *, user_id);
DATAPROXY_FORWARDING_GET(PuzzleSetProxy *, NSNumber *, type);
DATAPROXY_FORWARDING_GET(PuzzleSetProxy *, NSNumber *, bought);
DATAPROXY_FORWARDING_GET(PuzzleSetProxy *, int, solved);
DATAPROXY_FORWARDING_GET(PuzzleSetProxy *, int, total);
DATAPROXY_FORWARDING_GET(PuzzleSetProxy *, float, percent);
DATAPROXY_FORWARDING_GET(PuzzleSetProxy *, int, score);
DATAPROXY_FORWARDING_GET(PuzzleSetProxy *, int, minScore);

- (NSArray *)orderedPuzzles
{
    [self prepareManagedObject];
    __block NSMutableArray * objectIDs = [NSMutableArray new];
    [DataContext performSyncInDataQueue:^{
        for (PuzzleData * puzzle in [(PuzzleSetData *)self.managedObject orderedPuzzles]) {
            [objectIDs addObject:puzzle.objectID];
        }
    }];
    NSMutableArray * puzzles = [NSMutableArray arrayWithCapacity:objectIDs.count];
    for (NSManagedObjectID * objectID in objectIDs) {
        [puzzles addObject:[[PuzzleProxy alloc] initWithObjectID:objectID]];
    }
    return puzzles;
    
}

- (PuzzleSetPackProxy *)puzzleSetPack
{
    [self prepareManagedObject];
    __block NSManagedObjectID * objectID = nil;
    [DataContext performSyncInDataQueue:^{
        objectID = [(PuzzleSetData *)self.managedObject puzzleSetPack].objectID;
    }];
    return [[PuzzleSetPackProxy alloc] initWithObjectID:objectID];
}

@end
