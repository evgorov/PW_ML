//
//  UserData.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/19/13.
//
//

#import "UserData.h"
#import "NSData+Base64.h"

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
        _first_name = [dict objectForKey:@"name"];
        _last_name = [dict objectForKey:@"surname"];
        _email = [dict objectForKey:@"email"];
        _user_id = [dict objectForKey:@"id"];
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
            NSLog(@"%@", [dateFormatter stringFromDate:_createdAt]);
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
                                  _first_name, @"name",
                                  _last_name, @"surname",
                                  _email, @"email",
                                  _user_id, @"id",
                                  _city, @"city",
                                  providers, @"providers",
                                  [NSNumber numberWithInt:_position], @"position",
                                  [NSNumber numberWithInt:_solved], @"solved",
                                  [NSNumber numberWithInt:_month_score], @"month_score",
                                  [NSNumber numberWithInt:_high_score], @"high_score",
                                  [NSNumber numberWithInt:_dynamics], @"dynamics",
                                  [NSNumber numberWithInt:_hints], @"hints",
                                  [NSNumber numberWithBool:_invited], @"invite_sent",
                                  nil];
    
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
        
    return dict;
}

@end
