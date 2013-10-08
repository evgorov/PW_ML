//
//  DataContext.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/8/13.
//
//

#import <CoreData/CoreData.h>

@interface DataContext : NSManagedObjectContext

+ (DataContext *)currentContext;

@end
