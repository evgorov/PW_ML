//
//  DataLayer.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/5/13.
//
//

#import <Foundation/Foundation.h>

typedef void(^ArrayDataFetchCallback)(NSArray * data, NSError * error);

@interface DataManager : NSObject

+(DataManager *)sharedManager;

- (void)cancelAll;

- (void)fetchArchiveSetsForMonth:(int)month year:(int)year completion:(ArrayDataFetchCallback)callback;
- (void)fetchPuzzles:(NSArray *)ids completion:(ArrayDataFetchCallback)callback;

- (NSArray *)localGetArchiveSetsForMonth:(int)month year:(int)year;
- (NSArray *)localGetPuzzles:(NSArray *)ids;

@end
