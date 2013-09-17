//
//  ScoreQuery.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 9/17/13.
//
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface ScoreQuery : NSManagedObject

@property (nonatomic, retain) NSNumber * score;
@property (nonatomic, retain) NSString * key;
@property (nonatomic, retain) NSString * user;
@property (nonatomic, retain) NSNumber * done;

@end
