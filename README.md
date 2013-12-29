CampFahrplan
------------

CampFahrplan is a viewer for schedules of Chaos Computer Club e.V. events such as
Chaos Communication Camp or Chaos Communication Congress.
  
In addition to an daily overview of talks, the app lets you read the abstracts, 
highlight talks, add a talk to your calendar, share talks with others, and 
set reminders within the app.

HOWTO build
-----------

**prerequisites**

- Android SDK with support for API level 19
- JDK 7
- ant >= 1.8
- git >= 1.7

**add necessary ADK directories to the execution path**

``` bash
export PATH=${ADK_HOME}/tools:${ADK_HOME}/platform-tools:$PATH
```

**get the source**

``` bash
# your preferred git repository location
cd ${GIT_REPOS_HOME}
git clone https://github.com/tuxmobil/CampFahrplan.git
git clone https://github.com/JakeWharton/ActionBarSherlock.git
```

**update the projects to use the recommented API level**
``` bash
cd ${GIT_REPOS_HOME}/ActionBarSherlock/actionbarsherlock
android update project --target 16 --path .

cd ${GIT_REPOS_HOME}/CampFahrplan
android update project --target 16 --path . --library ../ActionBarSherlock/actionbarsherlock --name 30c3_fahrplan
```

**update the Android support library in ActionBarProject to the same version as in CampFahrplan**
``` bash
rm ${GIT_REPOS_HOME}/ActionBarSherlock/actionbarsherlock/libs/android-support-v4.jar
ln -s ${GIT_REPOS_HOME}/CampFahrplan/libs/android-support-v4.jar ${GIT_REPOS_HOME}/ActionBarSherlock/actionbarsherlock/libs/android-support-v4.jar
```

**generate a signing key for development**
``` bash
cd ${GIT_REPOS_HOME}/CampFahrplan
keytool -genkeypair -dname "cn=, ou=, o=, c=" -alias ForDevelopmentOnly -keyalg RSA -keysize 2048 -keystore debug.keystore -keypass 123456 -storepass 123456 -validity 42

cat << EOF >> local.properties
key.store=debug.keystore
key.alias=ForDevelopmentOnly
key.store.password=123456
key.alias.password=123456
EOF
```

**compile and create the APK**
``` bash
# this step needs to be repeated each time you want to build a new apk
cd ${GIT_REPOS_HOME}/CampFahrplan
ant release 2>&1 | tee build.log
```

**install the APK into emulator or physical device**
``` bash
# replace x.y.z with the value of "android:versionName" in the file AndroidManifest.xml
cd ${GIT_REPOS_HOME}/CampFahrplan/bin
adb install 30c3_fahrplan-x.yy.z.apk
```

Copyrights and Credits
----------------------

Portions Copyright 2008-2011 The K-9 Dog Walkers and 2006-2011 the Android Open Source Project.

LICENSE
-------

   Copyright 2011-2013 Daniel Dorau
   Copyright 2013 SubOptimal, entropynil

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
