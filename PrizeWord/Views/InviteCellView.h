//
//  InviteCellView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/31/13.
//
//

#import <UIKit/UIKit.h>
#import "ExternalImage.h"

@interface InviteCellView : UIView
@property (strong, nonatomic) IBOutlet ExternalImage *imgAvatar;
@property (strong, nonatomic) IBOutlet UIButton *btnAdd;
@property (strong, nonatomic) IBOutlet UILabel *lblSurname;
@property (strong, nonatomic) IBOutlet UILabel *lblName;

@end
