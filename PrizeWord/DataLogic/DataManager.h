//
//  DataLayer.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/5/13.
//
//

#import <Foundation/Foundation.h>

typedef void(^ArrayDataFetchCallback)(NSArray * data, NSError * error);
typedef void(^DictionaryDataFetchCallback)(NSDictionary * data, NSError * error);

@class PuzzleSetPackData;

@interface DataManager : NSObject

+(DataManager *)sharedManager;

- (void)cancelAll;

- (void)fetchCurrentMonthSetsWithCompletion:(ArrayDataFetchCallback)callback;
- (void)fetchArchiveSetsForMonth:(int)month year:(int)year completion:(ArrayDataFetchCallback)callback;
- (void)fetchPuzzles:(NSArray *)ids completion:(ArrayDataFetchCallback)callback;
- (void)fetchNewsWithCompletion:(ArrayDataFetchCallback)callback;
- (void)fetchPricesForProductIDs:(NSArray *)productIDs completion:(DictionaryDataFetchCallback)callback;

- (PuzzleSetPackData *)localGetSetsForMonth:(int)month year:(int)year;
- (NSArray *)localGetPuzzles:(NSArray *)ids;

@end
