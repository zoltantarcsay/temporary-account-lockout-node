# Temporary Account Lockout

This Auth Tree Plugin implements the temporary account lockout functionality for trees. It uses the same user attribute
and data model as the existing temporary lockout feature, so the two are compatible (i.e. you can use both trees and modules/chains).

## Components

Comes with 2 nodes:
* **TemporaryAccountLockoutIncrementer**: sets the temporary lockout data in the user's profile
* **TemporaryAccountLockoutDecision**: returns unlocked or locked based on what's in the lockout attribute 

## Example configuration

![ScreenShot](example.png)

## License
[CDDL](legal/CDDL-1.0.txt)

## Disclaimer
        
The sample code described herein is provided on an "as is" basis, without warranty of any kind, to the fullest extent 
permitted by law. ForgeRock does not warrant or guarantee the individual success developers may have in implementing the 
sample code on their development platforms or in production configurations.

ForgeRock does not warrant, guarantee or make any representations regarding the use, results of use, accuracy, 
timeliness or completeness of any data or information relating to the sample code. ForgeRock disclaims all warranties, 
expressed or implied, and in particular, disclaims all warranties of merchantability, and warranties related to the 
code, or any service or software related thereto.

ForgeRock shall not be liable for any direct, indirect or consequential damages or costs of any type arising out of any 
action taken by you or others related to the sample code.

[forgerock_platform]: https://www.forgerock.com/platform/  
