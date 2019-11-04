# neotv


- to build : ./gradlew build
- to run using script: 
```
java -jar script/build/libs/neotv.jar /
    --code=<neotv code> /
    --api=<tmdb key> /
    --group="French VOD" / 
    --format=<html|json> /
    --output=<file name> /
```
*Note : output is optional, if none then wil print in standart output*


- to run full app
```
java -Dapp.code=<neotv code> -jar server/build/libs/neotv-server-1.0.0.jar
```
or without building
```
./gradlew bootRun -Papp.code=<neotv code>
```
