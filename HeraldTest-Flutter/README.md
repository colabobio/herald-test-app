# HeraldTest-Flutter

Important things you need to do so you don't get ERRORS in Xcode when trying to build the project.

1. To run the project in Xcode go to [projectDirectory]/ios/Runner.xcworkspace. Always open the .xcworkspace file not the Runner.xcodeproj file or else you will run into issues.

2. Once project is opened head over to the first blue file (Runner) --> Under TARGETS Click Runner --> Signing & Capabilities. Select your team.

3. After selecting team, Head over to the "Build Settings" section above and scroll all the way down to "User-Defined", the first thing you should see is "FLUTTER_ROOT". Now update the path to point to where the flutter folder is located on your machine.

4. Close the project now. Open up terminal and head over to [projectDirectory]/ios. Run "pod install" to install herald.

5. Open the project back up in Xcode.