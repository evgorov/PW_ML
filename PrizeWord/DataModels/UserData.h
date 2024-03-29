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

@property (nonatomic, strong) NSString * user_id;
@property (nonatomic, strong) NSString * first_name;
@property (nonatomic, strong) NSString * last_name;
@property (nonatomic, strong) NSString * email;
@property (nonatomic, strong) NSString * city;
@property (nonatomic, strong) NSString * userpic_url;
@property (nonatomic, strong) UIImage * userpic;
@property (nonatomic, strong) NSDate * birthday;
@property (nonatomic, strong) NSDate * createdAt;
@property (nonatomic, strong) NSDictionary * vkProvider;
@property (nonatomic, strong) NSDictionary * fbProvider;

@property () int count_fb_shared;
@property () int count_vk_shared;
@property () int shared_free_score;
@property () int shared_gold_score;
@property () int shared_brilliant_score;
@property () int shared_silver1_score;
@property () int shared_silver2_score;
@property () BOOL is_app_rated;
@property () BOOL is_app_rated_this_month;
@property (nonatomic, strong) NSDate *last_notification_time;

@property () int position;
@property () int solved;
@property () int month_score;
@property () int high_score;
@property () int dynamics;
@property () int hints;
@property () BOOL invited;

-(NSDictionary *)dictionaryRepresentation;

@end
