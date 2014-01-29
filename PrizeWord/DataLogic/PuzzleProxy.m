//
//  PuzzleProxy.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/27/14.
//
//

#import "PuzzleProxy.h"
#import "QuestionProxy.h"
#import "PuzzleSetProxy.h"

@implementation PuzzleProxy

DATAPROXY_FORWARDING_GET(PuzzleData *, NSString *, puzzle_id)
DATAPROXY_FORWARDING_GET(PuzzleData *, NSString *, name)
DATAPROXY_FORWARDING_GET(PuzzleData *, NSString *, user_id)
DATAPROXY_FORWARDING_GET(PuzzleData *, NSString *, etag)
DATAPROXY_FORWARDING_GET(PuzzleData *, NSNumber *, time_given)
DATAPROXY_FORWARDING_GET(PuzzleData *, NSNumber *, time_left)
DATAPROXY_FORWARDING_GET(PuzzleData *, NSNumber *, height)
DATAPROXY_FORWARDING_GET(PuzzleData *, NSNumber *, width)
DATAPROXY_FORWARDING_GET(PuzzleData *, NSNumber *, score)
DATAPROXY_FORWARDING_GET(PuzzleData *, NSDate *, issuedAt)
DATAPROXY_FORWARDING_GET(PuzzleData *, int, solved)
DATAPROXY_FORWARDING_GET(PuzzleData *, float, progress)

DATAPROXY_FORWARDING_SET(PuzzleData *, NSString *, setEtag)
DATAPROXY_FORWARDING_SET(PuzzleData *, NSNumber *, setTime_left)
DATAPROXY_FORWARDING_SET(PuzzleData *, NSNumber *, setScore)

- (PuzzleSetProxy *)puzzleSet
{
    [self prepareManagedObject];
    __block NSManagedObjectID * objectID = nil;
    [DataContext performSyncInDataQueue:^{
        objectID = [(PuzzleData *)self.managedObject puzzleSet].objectID;
    }];
    return [[PuzzleSetProxy alloc] initWithObjectID:objectID];
}

- (NSSet *)questions
{
    [self prepareManagedObject];
    __block NSMutableSet * objectIDs = [NSMutableSet new];
    [DataContext performSyncInDataQueue:^{
        for (QuestionData * question in [(PuzzleData *)self.managedObject questions]) {
            [objectIDs addObject:question.objectID];
        }
    }];
    NSMutableSet * questions = [NSMutableSet setWithCapacity:objectIDs.count];
    for (NSManagedObjectID * objectID in objectIDs) {
        [questions addObject:[[QuestionProxy alloc] initWithObjectID:objectID]];
    }
    return questions;
}

- (void)synchronize
{
    [self prepareManagedObject];
    [(PuzzleData *)self.managedObject synchronize];
}

@end
