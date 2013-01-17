//
//  UserData.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/19/13.
//
//

#import <Foundation/Foundation.h>

@interface UserData : NSObject

-(id)initWithDictionary:(NSDictionary *)dict;

+(UserData *)userDataWithDictionary:(NSDictionary *)dict;

@property (nonatomic, strong) NSString * first_name;
@property (nonatomic, strong) NSString * last_name;
@property (nonatomic, strong) NSString * email;
@property (nonatomic, strong) NSString * provider;
@property (nonatomic, strong) NSString * provider_id;
@property (nonatomic, strong) NSString * city;
@property (nonatomic, strong) NSString * userpic_url;
@property (nonatomic, strong) NSDate * birthday;

@property () int position;
@property () int solved;
@property () int month_score;
@property () int high_score;
@property () int dynamics;
@property () int hints;


@end
