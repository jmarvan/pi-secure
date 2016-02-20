#Pi-Secure
Make your own home security system with Raspberry PI!

This project has been created with the hopes that it will provide resources to help anyone build their own dependable home security system.  The source code found here has been engineered to be easily extensible and configurable to cover as may use-cases as possible.

##Licensing
[GPL V3](http://www.gnu.org/licenses/) license was chosen for this project in order to encourage sharing improvements.

##Feature Roadmap

### Version 1.0 features
1. ~~Add Configurable PowerUp/Down Module.~~
2. ~~Build event browser UI.~~
3. ~~Add APC UPS battery operation detection Module + handle warnings.~~
4. ~~Implement delayed system arming.~~
5. ~~Implement delayed alarm.~~

### Version 1.1 features
1. Research RF communication and develop Modules to handle RF sensors.
2. Devise the ability for the UI to arm/disarm specific sensor loops.
3. Add Module control to the UI.
4. Add Multi-Language UI support.
5. Improve logging (perhaps using standard logging library).
6. Develop configurable siren shut-off, we don't want it to go on forever.
7. Add module white-listing (load only those modules that are wanted)

### Version 1.x features
1. Develop GSM Messaging Module
2. Research GSM Data on Raspberry PI. 
3. Develop other RF signaling modules (Smoke Detector, etc.)
4. Add other home-automation goodies.

##Hardware Architecture
As of version 1.0, hardware setup consists of a WIFI AP, home router, Raspberry PI 2 with wired PiR motion sensors and an APC UPS that provides backup power for all devices.
 

##Software Architecture
TODO