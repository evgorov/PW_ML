//
//  HintsQuery.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 9/17/13.
//
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface HintsQuery : NSManagedObject

@property (nonatomic, retain) NSNumber * done;
@property (nonatomic, retain) NSNumber * hints;
@property (nonatomic, retain) NSString * user;
@property (nonatomic, retain) NSString * key;

@end
