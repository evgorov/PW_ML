//
//  News.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/7/13.
//
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface NewsData : NSManagedObject

@property (nonatomic, retain) NSString * news1;
@property (nonatomic, retain) NSString * news2;
@property (nonatomic, retain) NSString * news3;
@property (nonatomic, retain) NSString * etag;

@end
