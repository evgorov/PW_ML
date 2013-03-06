//
//  ExternalImage.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 2/10/13.
//
//

#import "ExternalImage.h"

@implementation ExternalImage

static NSMutableDictionary * cache = nil;

+(void)clearCache
{
    [cache removeAllObjects];
}

-(void)awakeFromNib
{
    connection = nil;
}

-(id)init
{
    self = [super init];
    if (self)
    {
        connection = nil;
    }
    return self;
}

-(void)loadImageFromURL:(NSURL *)url
{
    [self cancelLoading];
    
    imageURL = url;
    UIImage * cachedImage = nil;
    if (cache != nil)
    {
        cachedImage = [cache objectForKey:url.absoluteString];
    }
    if (cachedImage != nil)
    {
        self.image = cachedImage;
    }
    else
    {
        receivedData = [NSMutableData new];
        connection = [NSURLConnection connectionWithRequest:[NSURLRequest requestWithURL:url cachePolicy:NSURLCacheStorageAllowed timeoutInterval:10] delegate:self];
        [connection start];
    }
}

-(void)cancelLoading
{
    if (connection != nil)
    {
        [connection cancel];
        connection = nil;
    }
}

-(void)clear
{
    [self cancelLoading];
    self.image = nil;
}

#pragma mark NSURLConnectionDelegate
-(BOOL)connection:(NSURLConnection *)connection canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *)protectionSpace
{
    return YES;
}

-(void)connection:(NSURLConnection *)conn didFailWithError:(NSError *)error
{
    NSLog(@"didFailWithError: %@", error.description);
    [receivedData setLength:0];
    connection = nil;
}

-(BOOL)connectionShouldUseCredentialStorage:(NSURLConnection *)conn
{
    return YES;
}

#pragma mark NSURLConnectionDataDelegate
-(void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    [receivedData appendData:data];
}

-(void)connectionDidFinishLoading:(NSURLConnection *)conn
{
    UIImage * loadedImage = [UIImage imageWithData:receivedData];
    if (loadedImage != nil)
    {
        if (cache == nil)
        {
            cache = [NSMutableDictionary new];
        }
        [cache setObject:loadedImage forKey:imageURL.absoluteString];
    }
    self.image = loadedImage;
    connection = nil;
}

-(void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
    [receivedData setLength:0];
}

@end
