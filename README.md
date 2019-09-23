# neotv


- to build : ./gradlew jar
- to run : 
```
java -jar build/libs/neotv.jar /
    --code=<neotv code> /
    --api=<tmdb key> /
    --groups="French VOD" / 
    --format=<html|json> /
    --output=<file name> /
```
Note : output is optional, if none then wil print in standart output