//
//  UserData.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/19/13.
//
//

#import "UserData.h"
#import "NSData+Base64.h"
#import "GlobalData.h"

@implementation UserData

@synthesize user_id = _user_id;
@synthesize first_name = _first_name;
@synthesize last_name = _last_name;
@synthesize email = _email;
@synthesize city = _city;
@synthesize userpic = _userpic;
@synthesize userpic_url = _userpic_url;
@synthesize birthday = _birthday;
@synthesize createdAt = _createdAt;
@synthesize vkProvider = _vkProvider;
@synthesize fbProvider = _fbProvider;

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
        _first_name = @"";
        _last_name = @"";
        _email = @"";
        _user_id = @"";
        _city = @"";
        
        if ([dict objectForKey:@"name"] != nil && [dict objectForKey:@"name"] != [NSNull null])
            _first_name = [dict objectForKey:@"name"];
        if ([dict objectForKey:@"surname"] != nil && [dict objectForKey:@"surname"] != [NSNull null])
            _last_name = [dict objectForKey:@"surname"];
        if ([dict objectForKey:@"email"] != nil && [dict objectForKey:@"email"] != [NSNull null])
            _email = [dict objectForKey:@"email"];
        if ([dict objectForKey:@"id"] != nil && [dict objectForKey:@"id"] != [NSNull null])
            _user_id = [dict objectForKey:@"id"];
        if ([dict objectForKey:@"city"] != nil && [dict objectForKey:@"city"] != [NSNull null])
            _city = [dict objectForKey:@"city"];
        _userpic = nil;
        _userpic_url = nil;
        id userpicObject = [dict objectForKey:@"userpic"];
        if ([userpicObject isKindOfClass:[NSString class]])
        {
            NSString * userpicString = userpicObject;
            if (userpicString != nil && userpicString.length > 0)
            {
                if (userpicString.length > 10 && [userpicString rangeOfString:@"http" options:NSCaseInsensitiveSearch range:NSMakeRange(0, 10)].location != NSNotFound)
                {
                    _userpic_url = userpicString;
                }
                else
                {
                    _userpic = [UIImage imageWithData:[NSData dataFromBase64String:userpicString]];
                }
            }
        }
        NSString * dateString = [dict objectForKey:@"birthday"];
        NSDateFormatter * dateFormatter = [NSDateFormatter new];
        [dateFormatter setDateFormat:@"yyyy-MM-dd"];
        _birthday = nil;
        if (dateString != nil && dateString.length > 0)
        {
            _birthday = [dateFormatter dateFromString:dateString];
        }
        
        dateString = [dict objectForKey:@"created_at"];
        _createdAt = nil;
        if (dateString != nil && dateString.length > 0)
        {
            if ([dateString rangeOfString:@" "].location != NSNotFound)
            {
                _createdAt = [dateFormatter dateFromString:[dateString substringToIndex:[dateString rangeOfString:@" "].location]];
            }
            else
            {
                _createdAt = [dateFormatter dateFromString:dateString];
            }
        }

        _vkProvider = nil;
        _fbProvider = nil;
        NSArray * providers = [dict objectForKey:@"providers"];
        if (providers != nil)
        {
            for (NSDictionary * provider in providers)
            {
                NSString * provider_name = [provider objectForKey:@"provider_name"];
                if (provider_name != nil && [provider_name compare:@"vkontakte"] == NSOrderedSame)
                {
                    _vkProvider = provider;
                }
                else if (provider_name != nil && [provider_name compare:@"facebook"] == NSOrderedSame)
                {
                    _fbProvider = provider;
                }
            }
        }
        
        _position = [(NSNumber *)[dict objectForKey:@"position"] intValue];
        _solved = [(NSNumber *)[dict objectForKey:@"solved"] intValue];
        _month_score = [(NSNumber *)[dict objectForKey:@"month_score"] intValue];
        _high_score = [(NSNumber *)[dict objectForKey:@"high_score"] intValue];
        _dynamics = [(NSNumber *)[dict objectForKey:@"dynamics"] intValue];
        _hints = [(NSNumber *)[dict objectForKey:@"hints"] intValue];
        _invited = [(NSNumber *)[dict objectForKey:@"invite_sent"] boolValue];
        
        _count_fb_shared = [(NSNumber *)[dict objectForKey:@"count_fb_shared"] intValue];
        _count_vk_shared = [(NSNumber *)[dict objectForKey:@"count_vk_shared"] intValue];
        _shared_free_score = [(NSNumber *)[dict objectForKey:@"shared_free_score"] intValue];
        _shared_gold_score = [(NSNumber *)[dict objectForKey:@"shared_gold_score"] intValue];
        _shared_brilliant_score = [(NSNumber *)[dict objectForKey:@"shared_brilliant_score"] intValue];
        _shared_silver1_score = [(NSNumber *)[dict objectForKey:@"shared_silver1_score"] intValue];
        _shared_silver2_score = [(NSNumber *)[dict objectForKey:@"shared_silver2_score"] intValue];
        _is_app_rated = [(NSNumber *)[dict objectForKey:@"is_app_rated"] boolValue];
        _is_app_rated_this_month = _is_app_rated;
        
        NSLocale *enUSPOSIXLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
        [dateFormatter setLocale:enUSPOSIXLocale];
        [dateFormatter setDateFormat:@"yyyy'-'MM'-'dd' 'HH':'mm':'ss' 'ZZZZZ"];
        [dateFormatter setTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
        
        NSString *lastDate = [dict objectForKey:@"last_notification_time"];
        _last_notification_time = [dateFormatter dateFromString:lastDate];
        NSString *rateDateString = [dict objectForKey:@"rate_date"];
        if (rateDateString != nil && _is_app_rated_this_month)
        {
            NSDate *rateDate = [dateFormatter dateFromString:rateDateString];
            if (rateDate != nil)
            {
                NSCalendar * calendar = [NSCalendar currentCalendar];
                NSDateComponents * components = [calendar components:NSYearCalendarUnit|NSMonthCalendarUnit|NSDayCalendarUnit fromDate:rateDate];
                if (components.month != [GlobalData globalData].currentMonth || components.year != [GlobalData globalData].currentYear)
                {
                    _is_app_rated_this_month = NO;
                }
            }
        }
    }
    return self;
}

+(UserData *)userDataWithDictionary:(NSDictionary *)dict
{
    if (dict == nil)
    {
        return nil;
    }
    return [[UserData alloc] initWithDictionary:dict];
}

-(NSDictionary *)dictionaryRepresentation
{
    NSMutableArray * providers = [NSMutableArray new];
    if (_vkProvider != nil)
    {
        [providers addObject:_vkProvider];
    }
    if (_fbProvider != nil)
    {
        [providers addObject:_fbProvider];
    }
    NSDateFormatter * dateFormatter = [NSDateFormatter new];
    [dateFormatter setDateFormat:@"yyyy-MM-dd"];
    
    NSMutableDictionary * dict = [NSMutableDictionary dictionaryWithObjectsAndKeys:
                                  [NSNumber numberWithInt:_position], @"position",
                                  [NSNumber numberWithInt:_solved], @"solved",
                                  [NSNumber numberWithInt:_month_score], @"month_score",
                                  [NSNumber numberWithInt:_high_score], @"high_score",
                                  [NSNumber numberWithInt:_dynamics], @"dynamics",
                                  [NSNumber numberWithInt:_hints], @"hints",
                                  [NSNumber numberWithBool:_invited], @"invite_sent",
                                  nil];
    if (_first_name != nil) {
        [dict setObject:_first_name forKey:@"name"];
    }
    if (_last_name != nil) {
        [dict setObject:_last_name forKey:@"surname"];
    }
    if (_email != nil) {
        [dict setObject:_email forKey:@"email"];
    }
    if (_user_id != nil) {
        [dict setObject:_user_id forKey:@"id"];
    }
    if (_city != nil) {
        [dict setObject:_city forKey:@"city"];
    }
    if (providers != nil) {
        [dict setObject:providers forKey:@"providers"];
    }
    if (_userpic != nil)
    {
        [dict setObject:[UIImageJPEGRepresentation(_userpic, 1) base64EncodedString] forKey:@"userpic"];
    }
    else if (_userpic_url != nil)
    {
        [dict setObject:_userpic_url forKey:@"userpic"];
    }
    NSString * birthdayString = [dateFormatter stringFromDate:_birthday];
    if (birthdayString != nil)
    {
        [dict setObject:birthdayString forKey:@"birthday"];
    }
    NSString * createdAtString = [dateFormatter stringFromDate:_createdAt];
    if (createdAtString != nil)
    {
        [dict setObject:createdAtString forKey:@"created_at"];
    }
    if (_last_notification_time != nil) {
        NSLocale *enUSPOSIXLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
        NSDateFormatter * dateFormatter = [NSDateFormatter new];
        [dateFormatter setLocale:enUSPOSIXLocale];
        [dateFormatter setDateFormat:@"yyyy'-'MM'-'dd' 'HH':'mm':'ss' 'ZZZZZ"];
        [dateFormatter setTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
        [dict setObject:[dateFormatter stringFromDate:_last_notification_time] forKey:@"last_notification_time"];
    }
        
    return dict;
}

@end
