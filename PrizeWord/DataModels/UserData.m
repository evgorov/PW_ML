//
//  UserData.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/19/13.
//
//

#import "UserData.h"

@implementation UserData

@synthesize first_name = _first_name;
@synthesize last_name = _last_name;
@synthesize email = _email;
@synthesize provider = _provider;
@synthesize provider_id = _provider_id;
@synthesize city = _city;
@synthesize userpic_url = _userpic_url;
@synthesize birthday = _birthday;

@synthesize position = _position;
@synthesize solved = _solved;
@synthesize month_score = _month_score;
@synthesize high_score = _high_score;
@synthesize dynamics = _dynamics;
@synthesize hints = _hints;
@synthesize invited = _invited;

-(id)initWithDictionary:(NSDictionary *)dict
{
    self = [super init];
    if (self)
    {
        _first_name = [dict objectForKey:@"name"];
        _last_name = [dict objectForKey:@"surname"];
        _email = [dict objectForKey:@"email"];
        _provider = [dict objectForKey:@"provider"];
        _provider_id = [dict objectForKey:@"id"];
        _city = [dict objectForKey:@"city"];
        _userpic_url = [dict objectForKey:@"userpic_url"];
        NSString * dateString = [dict objectForKey:@"birthday"];
        _birthday = nil;
        if (dateString != nil)
        {
            NSDateFormatter * dateFormatter = [NSDateFormatter new];
            [dateFormatter setDateFormat:@"yyyy-MM-dd"];
            _birthday = [dateFormatter dateFromString:dateString];
        }
        
        _position = [(NSNumber *)[dict objectForKey:@"position"] intValue];
        _solved = [(NSNumber *)[dict objectForKey:@"solved"] intValue];
        _month_score = [(NSNumber *)[dict objectForKey:@"month_score"] intValue];
        _high_score = [(NSNumber *)[dict objectForKey:@"high_score"] intValue];
        _dynamics = [(NSNumber *)[dict objectForKey:@"dynamics"] intValue];
        _hints = [(NSNumber *)[dict objectForKey:@"hints"] intValue];
        _invited = [(NSNumber *)[dict objectForKey:@"invite_sent"] boolValue];
    }
    return self;
}

+(UserData *)userDataWithDictionary:(NSDictionary *)dict
{
    return [[UserData alloc] initWithDictionary:dict];
}


@end
